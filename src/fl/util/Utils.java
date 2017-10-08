/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package fl.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import cofix.common.util.Pair;

/**
 * @author Jiajun
 * @date Jul 18, 2017
 */
public class Utils {
	
	public static void dump2File(String fileName, List<Pair<String, List<Integer>>> locations){
		File file = new File(fileName);
		if(!file.exists()){
			file.getParentFile().mkdirs();
			try {
				file.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		BufferedWriter bWriter = null;
		try {
			bWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, false),  "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		try {
			for(Pair<String, List<Integer>> pair : locations){
				List<Integer> lines = pair.getSecond();
				String methodName = pair.getFirst();
				String fullClazzName = methodName.substring(0, methodName.indexOf("#"));
				for(int i = 0; i < lines.size(); i++){
					bWriter.write(fullClazzName);
					bWriter.write(",");
					bWriter.write(String.valueOf(lines.get(i)));
					bWriter.write("\n");
				}
			}
			bWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{
			if(bWriter != null){
				try {
					bWriter.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	public static CompilationUnit genASTFromFile(String fileName){
		return (CompilationUnit)genASTFromSource(readFileToString(fileName), ASTParser.K_COMPILATION_UNIT);
	}
	
	public static String readFileToString(String filePath) {
		if (filePath == null) {
			return new String();
		}
		File file = new File(filePath);
		if (!file.exists() || !file.isFile()) {
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
	
	public static List<Pair<String, Integer>> readFile2List(String fileName){
		List<Pair<String, Integer>> locations = new ArrayList<>();
		if (fileName == null) {
			return locations;
		}
		File file = new File(fileName);
		if (!file.exists() || !file.isFile()) {
			return locations;
		}
		BufferedReader bReader = null;
		try {
			bReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
			String line = null;
			while ((line = bReader.readLine()) != null) {
				String[] split = line.split("#");
				if(split.length != 2){
					System.err.println("Error : " + line);
					System.exit(0);
				}
				String[] number = split[1].split(",");
				if(number.length < 2){
					System.err.println("Error : " + line);
					System.exit(0);
				}
				Integer integer = Integer.parseInt(number[0]);
				locations.add(new Pair<String, Integer>(split[0], integer));
			}
			bReader.close();
		} catch (Exception e) {
			if (bReader != null) {
				try {
					bReader.close();
				} catch (IOException e1) {
					return locations;
				}
			}
		}
		return locations;
	}
}
