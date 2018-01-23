/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */

package cofix.common.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import cofix.common.config.Constant;


/**
 * This class contains some auxiliary methods that provide convenience
 * 
 * @author Jiajun
 *
 */

public class JavaFile {

	private final static String __name__ = "@JavaFile ";

	public static CompilationUnit genASTFromSource(String icu, String[] classPath, String[] sourcePath){
		ASTParser parser = ASTParser.newParser(AST.JLS8);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		Map<?, ?> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
		parser.setCompilerOptions(options);
 
//		String[] sources = { "C:\\Users\\pc\\workspace\\asttester\\src" }; 
//		String[] classpath = {"C:\\Program Files\\Java\\jre1.8.0_25\\lib\\rt.jar"};
 
		parser.setEnvironment(classPath, sourcePath, new String[] { "UTF-8"}, true);
		parser.setSource(icu.toCharArray());
		return (CompilationUnit) parser.createAST(null);
	}
	
	/**
	 * generate {@code CompilationUnit} from {@code ICompilationUnit}
	 * 
	 * @param icu
	 * @return
	 */
	public static CompilationUnit genASTFromICU(ICompilationUnit icu) {
		ASTParser astParser = ASTParser.newParser(AST.JLS8);
		Map<?, ?> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
		astParser.setCompilerOptions(options);
		astParser.setSource(icu);
		astParser.setKind(ASTParser.K_COMPILATION_UNIT);
		astParser.setResolveBindings(true);
		return (CompilationUnit) astParser.createAST(null);
	}

	/**
	 * generate {@code CompilationUnit} from source code based on the specific
	 * type (e.g., {@code ASTParser.K_COMPILATION_UNIT})
	 * 
	 * @param icu
	 * @param type
	 * @return
	 */
	public static ASTNode genASTFromSource(String icu, int type) {
		ASTParser astParser = ASTParser.newParser(AST.JLS8);
		Map<?, ?> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
		astParser.setCompilerOptions(options);
		astParser.setSource(icu.toCharArray());
		astParser.setKind(type);
		astParser.setResolveBindings(true);
		astParser.setBindingsRecovery(true);
		return astParser.createAST(null);
	}
	
	public static CompilationUnit genASTFromFile(String fileName){
		return (CompilationUnit)genASTFromSource(readFileToString(fileName), ASTParser.K_COMPILATION_UNIT);
	}
	
	public static CompilationUnit genASTFromFile(File file){
		return (CompilationUnit)genASTFromSource(readFileToString(file), ASTParser.K_COMPILATION_UNIT);
	}

	/**
	 * write {@code string} into file with mode as "not append"
	 * 
	 * @param filePath
	 *            : path of file
	 * @param string
	 *            : message
	 * @return
	 */
	public static boolean writeStringToFile(String filePath, String string) {
		return writeStringToFile(filePath, string, false);
	}

	/**
	 * write {@code string} to file with mode as "not append"
	 * 
	 * @param file
	 *            : file of type {@code File}
	 * @param string
	 *            : message
	 * @return
	 */
	public static boolean writeStringToFile(File file, String string) {
		return writeStringToFile(file, string, false);
	}

	/**
	 * write {@code string} into file with specific mode
	 * 
	 * @param filePath
	 *            : file path
	 * @param string
	 *            : message
	 * @param append
	 *            : writing mode
	 * @return
	 */
	public static boolean writeStringToFile(String filePath, String string, boolean append) {
		if (filePath == null) {
			LevelLogger.error(__name__ + "#writeStringToFile Illegal file path : null.");
			return false;
		}
		File file = new File(filePath);
		return writeStringToFile(file, string, append);
	}

	/**
	 * write {@code string} into file with specific mode
	 * 
	 * @param file
	 *            : file of type {@code File}
	 * @param string
	 *            : message
	 * @param append
	 *            : writing mode
	 * @return
	 */
	public static boolean writeStringToFile(File file, String string, boolean append) {
		if (file == null || string == null) {
			LevelLogger.error(__name__ + "#writeStringToFile Illegal arguments : null.");
			return false;
		}
		if (!file.exists()) {
			try {
				file.getParentFile().mkdirs();
				file.createNewFile();
			} catch (IOException e) {
				LevelLogger.error(__name__ + "#writeStringToFile Create new file failed : " + file.getAbsolutePath());
				return false;
			}
		}
		BufferedWriter bufferedWriter = null;
		try {
			bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, append), "UTF-8"));
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			bufferedWriter.write(string);
			bufferedWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (bufferedWriter != null) {
				try {
					bufferedWriter.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}

	/**
	 * read string from file
	 * 
	 * @param filePath
	 *            : file path
	 * @return : string in the file
	 */
	public static String readFileToString(String filePath) {
		if (filePath == null) {
			LevelLogger.error(__name__ + "#readFileToString Illegal input file path : null.");
			return new String();
		}
		File file = new File(filePath);
		if (!file.exists() || !file.isFile()) {
			LevelLogger.error(__name__ + "#readFileToString Illegal input file path : " + filePath);
			return new String();
		}
		return readFileToString(file);
	}

	/**
	 * read string from file
	 * 
	 * @param file
	 *            : file of type {@code File}
	 * @return : string in the file
	 */
	public static String readFileToString(File file) {
		if (file == null) {
			LevelLogger.error(__name__ + "#readFileToString Illegal input file : null.");
			return new String();
		}
		StringBuffer stringBuffer = new StringBuffer();
		InputStream in = null;
		InputStreamReader inputStreamReader = null;
		try {
			in = new FileInputStream(file);
			inputStreamReader = new InputStreamReader(in, "UTF-8");
			char[] ch = new char[1024];
			int readCount = 0;
			while ((readCount = inputStreamReader.read(ch)) != -1) {
				stringBuffer.append(ch, 0, readCount);
			}
			inputStreamReader.close();
			in.close();

		} catch (Exception e) {
			if (inputStreamReader != null) {
				try {
					inputStreamReader.close();
				} catch (IOException e1) {
					return new String();
				}
			}
			if (in != null) {
				try {
					in.close();
				} catch (IOException e1) {
					return new String();
				}
			}
		}
		return stringBuffer.toString();
	}

	/**
	 * iteratively search files with the root as {@code file}
	 * 
	 * @param file
	 *            : root file of type {@code File}
	 * @param fileList
	 *            : list to save all the files
	 * @return : a list of all files
	 */
	public static List<File> ergodic(File file, List<File> fileList) {
		if (file == null) {
			LevelLogger.error(__name__ + "#ergodic Illegal input file : null.");
			return fileList;
		}
		File[] files = file.listFiles();
		if (files == null)
			return fileList;
		for (File f : files) {
			if (f.isDirectory()) {
				ergodic(f, fileList);
			} else if (f.getName().endsWith(".java"))
				fileList.add(f);
		}
		return fileList;
	}

	/**
	 * iteratively search the file in the given {@code directory}
	 * 
	 * @param directory
	 *            : root directory
	 * @param fileList
	 *            : list of file
	 * @return : a list of file
	 */
	public static List<String> ergodic(String directory, List<String> fileList) {
		if (directory == null) {
			LevelLogger.error(__name__ + "#ergodic Illegal input file : null.");
			return fileList;
		}
		File file = new File(directory);
		File[] files = file.listFiles();
		if (files == null)
			return fileList;
		for (File f : files) {
			if (f.isDirectory()) {
				ergodic(f.getAbsolutePath(), fileList);
			} else if (f.getName().endsWith(Constant.SOURCE_FILE_SUFFIX))
				fileList.add(f.getAbsolutePath());
		}
		return fileList;
	}
	
	public static List<String> readFileToList(String fileName) throws IOException{
		File file = new File(fileName);
		if(!file.exists()){
			System.out.println("File : " + fileName + " does not exist!");
			return null;
		}
		BufferedReader br = new BufferedReader(new FileReader(file));
		String line = null;
		List<String> source = new ArrayList<>();
		source.add("useless");
		while((line = br.readLine()) != null){
			source.add(line);
		}
		br.close();
		
		return source;
	}
	
	public static void sourceReplace(String fileName, List<String> source, int startLine, int endLine, String replace) throws IOException{
		File file = new File(fileName);
		if(!file.exists()){
			System.out.println("File : " + fileName + " does not exist!");
			return;
		}
		boolean flag = false;
		StringBuffer stringBuffer = new StringBuffer();
		BufferedWriter bw = new BufferedWriter(new FileWriter(file, false));
		for(int i = 1; i < source.size(); i++){
			if(i == startLine){
				String origin = source.get(i).replace(" ", "");
				if(origin.startsWith("}else")){
					bw.write("} else ");
				}
				bw.write("// start of generated patch\n");
				bw.write(replace);
				bw.write("// end of generated patch\n");
				stringBuffer.append("/* start of original code\n");
				stringBuffer.append(source.get(i) + "\n");
				flag = true;
			} else if(startLine < i && i <= endLine){
				stringBuffer.append(source.get(i) + "\n");
				continue;
			} else {
				if(flag){
					bw.write(stringBuffer.toString());
					bw.write(" end of original code*/\n");
					stringBuffer = null;
					flag = false;
				}
				bw.write(source.get(i) + "\n");
			}
		}
		bw.close();
	}
	
	/**
	 * 
	 * @param commits
	 *            commit information for each {@code RepoSubject}, which
	 *            contains the commit pair and corresponding commit message
	 * @return true if successfully write into file
	 *         {@code Constant.FILE_COMMIT_PAIR_XML}, false otherwise
	 */
	public static boolean writeCommitToFile(Map<String, Map<Pair<String, String>, String>> commits, String targetFile) {
		boolean writen = true;
		OutputStream outputStream = null;
		XMLWriter xmlWriter = null;
		Document document = null;
		try {
			document = DocumentHelper.createDocument();
			Element topRoot = DocumentHelper.createElement("projects");
			int id = 1;
			for (Entry<String, Map<Pair<String, String>, String>> entry : commits.entrySet()) {
				Element root = DocumentHelper.createElement("project");
				root.addAttribute("name", entry.getKey());
				Element pElement = DocumentHelper.createElement("pairs");
				root.add(pElement);
				for (Entry<Pair<String, String>, String> inEntry : entry.getValue().entrySet()) {
					Element element = DocumentHelper.createElement("pair");
					element.addAttribute("id", String.valueOf(id));
					id++;
					Element before = DocumentHelper.createElement("before");
					before.setText(inEntry.getKey().getFirst());
					element.add(before);
					Element after = DocumentHelper.createElement("after");
					after.setText(inEntry.getKey().getSecond());
					element.add(after);
					Element comment = DocumentHelper.createElement("message");
					comment.setText(inEntry.getValue());
					element.add(comment);
					pElement.add(element);
				}
				topRoot.add(root);
			}
			document.add(topRoot);
			OutputFormat outputFormat = new OutputFormat();
			outputFormat.setEncoding("UTF-8");
			outputFormat.setNewlines(true);
			outputFormat.setIndent(true);
			outputFormat.setIndent("    ");

			outputStream = new FileOutputStream(targetFile);
			xmlWriter = new XMLWriter(outputStream, outputFormat);

			xmlWriter.write(document);
			outputStream.close();
			xmlWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (outputStream != null) {
					outputStream.close();
				}
				if (xmlWriter != null) {
					xmlWriter.close();
				}
			} catch (Exception e) {
			}
		}

		return writen;
	}
	
	/**
	 * read commit information from xml file
	 * {@code Constant.FILE_COMMIT_XML}, which contains commit ids and
	 * commit message information. return a map that contains
	 * 
	 * - all commit pairs and - commit message
	 * 
	 * for each subject from file.
	 * 
	 * @return commit information for each subject with commit message for
	 *         each commit pair
	 */
	public static Map<String, Map<Pair<String, String>, String>> readCommitWithMessageFromFile(String xmlfile) {
		Map<String, Map<Pair<String, String>, String>> commits = new HashMap<>();
		File inputXml = new File(xmlfile);
		SAXReader saxReader = new SAXReader();
		try {
			Document document = saxReader.read(inputXml);
			Element root = document.getRootElement();
			for (Iterator<Element> iterator = root.elementIterator(); iterator.hasNext();) {
				Element element = iterator.next();
				String name = element.attributeValue("name");
				Map<Pair<String, String>, String> allPairs = new HashMap<>();
				Element pairs = element.element("pairs");
				if (pairs != null) {
					for (Iterator<Element> itor = pairs.elementIterator(); itor.hasNext();) {
						Element pair = itor.next();
						String before = pair.elementText("before");
						String after = pair.elementText("after");
						Pair<String, String> change = new Pair<String, String>(before, after);
						String message = pair.elementText("message");
						allPairs.put(change, message);
					}
				}
				commits.put(name, allPairs);
			}
		} catch (DocumentException e) {
			LevelLogger.fatal(__name__ + "#readCommitWithMessageFromFile parse xml file failed !", e);
		}
		return commits;
	}

}
