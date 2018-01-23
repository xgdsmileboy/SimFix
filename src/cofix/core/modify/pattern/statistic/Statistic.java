package cofix.core.modify.pattern.statistic;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Type;

import cofix.common.config.Constant;
import cofix.common.util.JavaFile;
import cofix.common.util.LevelLogger;
import cofix.common.util.Pair;
import cofix.core.match.CodeBlockMatcher;
import cofix.core.modify.Modification;
import cofix.core.modify.diff.Diff;
import cofix.core.modify.diff.TextDiff;
import cofix.core.modify.diff.line.Line;
import cofix.core.modify.pattern.Pattern;
import cofix.core.modify.pattern.match.Matcher;
import cofix.core.parser.node.CodeBlock;

public class Statistic {
	
	private static enum TESTOP {
		EXPORT_ALL_COMMIT,
		EXTRACT_DIFF_TEXT,
		EXTRACT_DIFF_FILES,
		EXTRACT_OP_PATTERN
	}

	public static void main(String[] args) {
		// configure me
		TESTOP op = TESTOP.EXTRACT_OP_PATTERN;

		Map<String, Map<Pair<String, String>, String>> allcommits = new HashMap<>();
		switch (op) {
		case EXPORT_ALL_COMMIT: {
			/***********
			 * extract commit pairs based on keywords, and write to file
			 ************/
			String[] projects = new String[] { "ant", "groovy", "guava", "hadoop", "j2objc", "lucene-solr" };
			try {
				for (String string : projects) {
					String path = Constant.DIR_ABSO_REPO + Constant.SEP_CH + string + "/log.txt";
					Map<Pair<String, String>, String> commits = Commit.parseCommit(path);
					allcommits.put(string, commits);
				}
			} catch (IOException exception) {
				exception.printStackTrace();
			}
			JavaFile.writeCommitToFile(allcommits, Constant.FILE_COMMIT_XML);
			break;
		}
		case EXTRACT_DIFF_TEXT: {
			/***********
			 * read commit pairs from file and extract diff files
			 ************/
			allcommits = JavaFile.readCommitWithMessageFromFile(Constant.FILE_COMMIT_XML);
			for (Entry<String, Map<Pair<String, String>, String>> entry : allcommits.entrySet()) {
				String name = entry.getKey();
				Commit.extractDiff(entry.getValue(), Constant.DIR_ABSO_REPO + Constant.SEP_CH + name,
						Constant.DIR_ABSO_DIFF + Constant.SEP_CH + name);
			}
			break;
		}
		case EXTRACT_DIFF_FILES: {
			/***********
			 * read commit from diff files and extract source files before and
			 * after revision.
			 * commits in the diff files are those after manually filtering
			 ************/
			Map<String, Map<String, Pair<String, String>>> commits = Commit.readCommitFromFile(Constant.DIR_ABSO_DIFF);
			for(Entry<String, Map<String, Pair<String, String>>> entry : commits.entrySet()) {
				System.out.println("------------" + entry.getKey() + "--------------");
				Commit.extractDiffFile(entry.getValue(), Constant.DIR_ABSO_REPO + Constant.SEP_CH + entry.getKey(), Constant.DIR_ABSO_DISTIL + Constant.SEP_CH + entry.getKey());
			}
			break;
		}
		case EXTRACT_OP_PATTERN: {
			Map<String, Map<String, Pair<String, String>>> commits = Commit
					.readCommitFromFile(Constant.DIR_ABSO_DIFF);
			Set<Pattern> patterns = getAllPatterns(commits);
			break;
		}
		default:
			System.err.println("Do Nothing!");
		}
	}
	
	protected static Set<Pattern> getAllPatterns(Map<String, Map<String, Pair<String, String>>> commitPairs) {
		Set<Pattern> patterns = new HashSet<>();
		Map<String, Integer> modificationCount = new HashMap<>();
		Map<String, Set<String>> manually = new HashMap<>();
		for(Entry<String, Map<String, Pair<String, String>>> entry : commitPairs.entrySet()) {
			String base = Constant.DIR_ABSO_DISTIL + Constant.SEP_CH + entry.getKey();
			for(Entry<String, Pair<String, String>> innerEntry : entry.getValue().entrySet()) {
				Pair<String, String> pair = innerEntry.getValue();
				String srcPath = base + Constant.SEP_CH + Commit.buildPath(pair) + Constant.SEP_CH + pair.getFirst();
				String tarPath = base + Constant.SEP_CH + Commit.buildPath(pair) + Constant.SEP_CH + pair.getSecond();
				Set<String> fileNames = verify(srcPath, tarPath);
				Set<String> types = new HashSet<>();
				if(fileNames == null) {
					LevelLogger.warn("different files before and after commit : " + entry.getKey() + " : " + pair);
				} else {
					for(String fName : fileNames) {
						String srcFile = srcPath + Constant.SEP_CH + fName;
						String tarFile = tarPath + Constant.SEP_CH + fName;
						
						Set<Pattern> modifies = buildPatterns(srcFile, tarFile);
						for(Pattern pattern : modifies) {
							System.out.println(pattern);
						}
//						if(modifies == null) {
//							Set<String> set = manually.get(entry.getKey());
//							if(set == null) {
//								set = new HashSet<>();
//							}
//							set.add(innerEntry.getKey());
//							manually.put(entry.getKey(), set);
//							types = null;
//							break;
//						} else {
//							types.addAll(modifies);
//						}
					}
//					if(types != null) {
//						for(String modify : types) {
//							Integer integer = modificationCount.get(modify);
//							if(integer == null) {
//								integer = new Integer(0);
//							}
//							integer ++;
//							modificationCount.put(modify, integer);
//						}
//					}
				}
			}
		}
		
		// post-process
		
		List<Entry<String, Integer>> list = new ArrayList<>(modificationCount.entrySet());
		Collections.sort(list, new Comparator<Entry<String, Integer>>() {
			@Override
			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
				return o2.getValue() - o1.getValue();
			}
		});
		
		for(Entry<String, Integer> entry : list) {
			System.out.println(entry.getKey() + "," + entry.getValue());
		}
		
		for(Entry<String, Set<String>> entry : manually.entrySet()) {
			System.out.println(entry.getKey() + entry.getValue().size());
			for(String string : entry.getValue()) {
				System.out.print(string + ",");
			}
			System.out.println("\n---------------------");
		}
		
		return patterns;
	}
	
	private static Set<String> verify(String srcPath, String tarPath) {
		Set<File> srcFiles = new HashSet<>(JavaFile.ergodic(new File(srcPath), new LinkedList<File>()));
		List<File> tarFiles = JavaFile.ergodic(new File(tarPath), new LinkedList<File>());
		Set<String> tarFileNames = new HashSet<>();
		for(File file : tarFiles) {
			tarFileNames.add(file.getName());
		}
		Set<String> files = new HashSet<>(tarFiles.size());
		for(File file : srcFiles) {
			// minimal different file sets
			if(tarFileNames.contains(file.getName())) {
				files.add(file.getName());
			} else {
				return null;
			}
		}
		return files;
	}
	
	private static Set<Pattern> buildPatterns(String srcFile, String tarFile) {
		Set<Pattern> patterns = new HashSet<>();
		CompilationUnit srcUnit = JavaFile.genASTFromFile(srcFile);
		CompilationUnit tarUnit = JavaFile.genASTFromFile(tarFile);
		List<Pair<MethodDeclaration, MethodDeclaration>> matchMap = Matcher.match(srcUnit, tarUnit);
		for(Pair<MethodDeclaration, MethodDeclaration> matchPair : matchMap) {
			if(matchPair.getFirst().getBody() == null) {
				continue;
			}
			CodeBlock srcNode = new CodeBlock(srcFile, srcUnit, Arrays.asList((ASTNode)matchPair.getFirst().getBody()), Integer.MAX_VALUE);
			CodeBlock tarNode = new CodeBlock(tarFile, tarUnit, Arrays.asList((ASTNode)matchPair.getSecond().getBody()), Integer.MAX_VALUE);
			String src = srcNode.toSrcString().toString();
			String tar = tarNode.toSrcString().toString();
			// not changed method
			if(src.equals(tar)) {
				continue;
			}
			//cannot change too much
			Diff<Line> textdiff = new TextDiff(srcNode, tarNode);
			if(textdiff.getMiniDiff().size() > 10) {
				continue;
			}
		}
		return patterns;
	}

}

