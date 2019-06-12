/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package fl.util;

import cofix.common.util.JavaFile;
import cofix.common.util.Pair;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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
		return JavaFile.genAST(fileName);
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
