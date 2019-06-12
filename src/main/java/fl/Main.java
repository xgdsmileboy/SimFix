/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package fl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.CompilationUnit;

import cofix.common.util.JavaFile;
import cofix.common.util.Pair;
import fl.util.Utils;
import fl.visitor.FindMethodVisitor;

/**
 * @author Jiajun
 * @date Jul 18, 2017
 */
public class Main {

	public static List<Pair<String, List<Integer>>> converter(String projectSourcePath, String locateFile){
		
		List<Pair<String, Integer>> locations = Utils.readFile2List(locateFile);
		Map<String, Integer> indexMap = new HashMap<>();
		List<Pair<String, List<Integer>>> converted = new ArrayList<>();
		Map<String, CompilationUnit> unitMap = new HashMap<>();
		for(Pair<String, Integer> pair : locations){
			
//			System.out.println("PARSE : " + pair.getFirst() + "," + pair.getSecond());
			
			String packageName = pair.getFirst();
			int indexOfDollar = packageName.indexOf("$");
			if(indexOfDollar > 0){
				packageName = packageName.substring(0, indexOfDollar);
			}
			
			String name = packageName.substring(packageName.lastIndexOf(".") + 1);
			
			String fileName = projectSourcePath + "/" + packageName.replace(".", "/") + ".java";
			
			CompilationUnit unit = unitMap.get(fileName);
			if(unit == null){
				unit = Utils.genASTFromFile(fileName);
				unitMap.put(pair.getFirst(), unit);
			}
			
			FindMethodVisitor visitor = new FindMethodVisitor(pair.getSecond(), name);
			unit.accept(visitor);
			String method = visitor.getWrapMethod();
			if(method == null){
				System.err.println("NO method found!");
				JavaFile.writeStringToFile("tmp.txt", fileName + "," + pair.getSecond() + "\n", true);
				continue;
			}
			Integer index = indexMap.get(method);
			if(index != null){
				converted.get(index).getSecond().add(pair.getSecond());
			} else {
				List<Integer> liIntegers = new ArrayList<>();
				liIntegers.add(pair.getSecond());
				converted.add(new Pair<String, List<Integer>>(method, liIntegers));
				indexMap.put(method, converted.size() - 1);
			}
		}
		
		return converted;
	}
	
	
//	public static void main(String[] args) {
//		Map<String, Integer> proj = new HashMap<>();
//		proj.put("chart", 26);
//		proj.put("closure", 133);
//		proj.put("lang", 65);
//		proj.put("math", 106);
//		proj.put("time", 27);
//		
//		for(Entry<String, Integer> entry : proj.entrySet()){
//			String name = entry.getKey();
//			int guard = entry.getValue();
//			for(int i = 1; i <= guard; i++){
//				System.out.println("PARSING : " + name + " " + i);
//				Subject subject = Configure.getSubject(name, i);
//				String locateFile = Constant.ORI_FAULTLOC + "/" + name + "/" + i + ".txt";
//				List<Pair<String, List<Integer>>> locations = converter(subject.getHome() + subject.getSsrc(), locateFile);
//				String target = Constant.CONVER_FAULTLOC + "/" + name + "/" + i + ".txt";
//				Utils.dump2File(target, locations);
//				System.out.println("FROM : " + locateFile);
//				System.out.println("TO   : " + target + "\n");
//			}
//		}
//		
//	}
	
}
