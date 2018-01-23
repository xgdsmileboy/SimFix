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
		TESTOP op = TESTOP.EXPORT_ALL_COMMIT;
		
		Map<String, Map<Pair<String, String>, String>> allcommits = new HashMap<>();
		switch(op) {
		case EXPORT_ALL_COMMIT:
		{
			/***********extract commit pairs based on keywords, and write to file************/
			String[] projects = new String[]{"ant","groovy","guava","hadoop","j2objc","lucene-solr"};
			try {
				for(String string : projects) {
					String path = Constant.DIR_BASE_STATISTIC + Constant.SEP_CH + string + "/log.txt";
					Map<Pair<String, String>, String> commits = Commit.parseCommit(path);
					allcommits.put(string, commits);
				}
			} catch (IOException exception) {
				exception.printStackTrace();
			}
			JavaFile.writeCommitToFile(allcommits, Constant.FILE_COMMIT_XML);
			break;
		}
		case EXTRACT_DIFF_TEXT:
		{
			/***********read commit pairs from file and extract diff files************/
			allcommits = JavaFile.readCommitWithMessageFromFile(Constant.FILE_COMMIT_XML);
			for(Entry<String, Map<Pair<String, String>, String>> entry : allcommits.entrySet()) {
				String name = entry.getKey();
				if (name.equals("ant")) {
					Commit.extractDiff(entry.getValue(), Constant.DIR_BASE_STATISTIC + Constant.SEP_CH + name,
							Constant.DIR_ABSO_DIFF + Constant.SEP_CH + name);
				}
			}
		}
		case EXTRACT_DIFF_FILES: {
			/***********
			 * read commit from diff files and extract source files before and
			 * after revision.
			 ************/
			/***********
			 * commits in the diff files are those after manually filtering
			 ************/
			allcommits = JavaFile.readCommitWithMessageFromFile(Constant.FILE_COMMIT_XML);
			for (Entry<String, Map<Pair<String, String>, String>> entry : allcommits.entrySet()) {
				System.out.println("------------" + entry.getKey() + "--------------");
				Commit.extractDiffFile(entry.getValue().keySet(),
						Constant.DIR_BASE_STATISTIC + Constant.SEP_CH + entry.getKey(),
						Constant.DIR_ABSO_DISTIL + Constant.SEP_CH + entry.getKey());
			}
		}
		case EXTRACT_OP_PATTERN:
		{
			Map<String, Map<String, Pair<String, String>>> commits = Commit.readCommitFromFile(Constant.FILE_COMMIT_XML);
			Set<Pattern> patterns = getAllPatternNodes(commits);
			
		}
		default:
			
		}
		
		
		
	}
	
	protected static Set<Pattern> getAllPatternNodes(Map<String, Map<String, Pair<String, String>>> commitPairs) {
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
						
						Set<String> modifies = buildPatternNode(srcFile, tarFile);
						if(modifies == null) {
							Set<String> set = manually.get(entry.getKey());
							if(set == null) {
								set = new HashSet<>();
							}
							set.add(innerEntry.getKey());
							manually.put(entry.getKey(), set);
							types = null;
							break;
						} else {
							types.addAll(modifies);
						}
					}
					if(types != null) {
						for(String modify : types) {
							Integer integer = modificationCount.get(modify);
							if(integer == null) {
								integer = new Integer(0);
							}
							integer ++;
							modificationCount.put(modify, integer);
						}
					}
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
		
//		Map<String, Pair<Set<String>, Integer>> update = new HashMap<>();
//		Map<String, Integer> insert = new HashMap<>();
//		Map<String, Integer> delete = new HashMap<>();
//		
//		for(Entry<String, Integer> entry : modificationCount.entrySet()) {
//			String string = entry.getKey();
//			if(string.startsWith("[INS]")) {
//				string = string.substring("[INS]".length());
//				Integer integer = insert.get(string);
//				if(integer == null) {
//					integer = 0;
//				}
//				insert.put(string, integer + entry.getValue());
//			} else if(string.startsWith("[UPD]")) {
//				string = string.substring("[UPD]".length());
//				String[] change = string.split("->");
//				Pair<Set<String>, Integer> record = update.get(change[0]);
//				if(record == null) {
//					record = new Pair<Set<String>, Integer>(new HashSet<String>(), 0);
//				}
//				record.getFirst().add(change[1]);
//				record.setSecond(record.getSecond() + entry.getValue());
//				update.put(change[0], record);
//			} else if(string.startsWith("[DEL]")) {
//				string = string.substring("[DEL]".length());
//				Integer integer = delete.get(string);
//				if(integer == null) {
//					integer = 0;
//				}
//				delete.put(string, integer + entry.getValue());
//			}
//		}
//		
//		List<Entry<String, Pair<Set<String>, Integer>>> updatePair = new ArrayList<>(update.entrySet());
//		List<Entry<String, Integer>> insertPair = new ArrayList<>(insert.entrySet());
//		List<Entry<String, Integer>> deletePair = new ArrayList<>(delete.entrySet());
//		
//		Collections.sort(updatePair, new Comparator<Entry<String, Pair<Set<String>, Integer>>>() {
//			@Override
//			public int compare(Entry<String, Pair<Set<String>, Integer>> o1,
//					Entry<String, Pair<Set<String>, Integer>> o2) {
//				return o2.getValue().getSecond().compareTo(o1.getValue().getSecond());
//			}
//		});
//		
//		Collections.sort(insertPair, new Comparator<Entry<String, Integer>>() {
//			@Override
//			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
//				return o2.getValue().compareTo(o1.getValue());
//			}
//		});
//		
//		Collections.sort(deletePair, new Comparator<Entry<String, Integer>>() {
//			@Override
//			public int compare(Entry<String, Integer> o1, Entry<String, Integer> o2) {
//				return o2.getValue().compareTo(o1.getValue());
//			}
//		});
//		
//		
//		
//		int total = 0;
//		for(Entry<String, Pair<Set<String>, Integer>> entry : updatePair) {
//			System.out.print("[UPD]" + entry.getKey() + "->");
//			for(String string : entry.getValue().getFirst()) {
//				System.out.print(string + ",");
//			}
//			System.out.println(entry.getValue().getSecond());
//			total += entry.getValue().getSecond();
//		}
//		
//		for(Entry<String, Integer> entry : insertPair) {
//			System.out.println("[INS]" + entry.getKey() + "," + entry.getValue());
//			total += entry.getValue();
//		}
//		
//		for(Entry<String, Integer> entry : deletePair) {
//			System.out.println("[DEL]" + entry.getKey() + "," + entry.getValue());
//			total += entry.getValue();
//		}
		
		
//		for(Pair<String, Integer> pair : allModifications) {
//			System.out.println(pair );
//			total += pair.getSecond();
//		}
		
//		System.out.println("TOTAL : " + total);
		
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
	
	private static Set<String> buildPatternNode(String srcFile, String tarFile) {
		Set<String> patterns = new HashSet<>();
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
//			Diff<Line> textdiff = new TextDiff(srcNode, tarNode);
//			if(textdiff.getMiniDiff().size() > 10) {
//				continue;
//			}
			
//			// output patterns into file
//			String teString = "--------------------------------------\n" + textdiff.toString();
//			JavaFile.writeStringToFile("/Users/Jiajun/Desktop/patterns.txt", teString, true);
			
			List<Modification> modifications = CodeBlockMatcher.match(srcNode, tarNode, new HashMap<String, Type>());
			// filter those log information
//			for(Modification modification : modifications) {
//				if(modification instanceof Insertion) {
//					Insertion insertion = (Insertion) modification;
////					if( insertion.getTarget().getNodeType() == TYPE.MINVOCATION) {
////						System.out.println(insertion + "=>\n" + insertion.getTarget().getParentStmt());
////						System.out.println("-----------------------");
////					}
////					if(insertion.getTarget() instanceof ExpressionStmt && insertion.getTarget().toSrcString().toString().startsWith("LOG.")) {
////						continue;
////					}
//				} else if(modification instanceof Revision) {
//					Revision revision = (Revision) modification;
//					if(revision.getSrcNode() instanceof Stmt || revision.getTarNode() instanceof Stmt) {
//						return null;
//					}
////					if(update.getSrcNode().getNodeType()==TYPE.MINVOCATION && update.getTarNode() != null && update.getTarNode().getNodeType() == TYPE.PARENTHESISZED) {
////						System.out.println(update);
////						System.out.println("-----------------------");
////					}
////					if(update.getSrcNode() instanceof Stmt || update.getSrcNode() instanceof Stmt) {
////						continue;
////					}
//					if(revision.getSrcNode() instanceof ExpressionStmt && revision.getSrcNode().toSrcString().toString().startsWith("LOG.")) {
//						continue;
//					} else if(revision.getTarNode() instanceof ExpressionStmt && revision.getTarNode().toSrcString().toString().startsWith("LOG.")) {
//						continue;
//					}
//				} else if(modification instanceof Deletion) {
//					Deletion deletion = (Deletion) modification;
//					if(deletion.getSrcNode() instanceof ExpressionStmt && deletion.getSrcNode().toSrcString().toString().startsWith("LOG.")) {
//						continue;
//					}
//				}
//				patterns.add(modification.getExplain());
//			}
			
		}
		return patterns;
	}

}

