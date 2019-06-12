/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import cofix.common.util.LevelLogger;
import cofix.common.util.Pair;
import cofix.common.util.Subject;

/**
 * @author Jiajun
 * @date Jun 19, 2017
 */
public class Configure {

	private final static String __name__ = "@Configure ";
	
	public static void configEnvironment(){
		String d4jhome = System.getenv("DEFECTS4J_HOME");
		if(d4jhome == null){
			System.err.println("Please set defects4j classpath first!");
			System.exit(0);
		}
		Constant.COMMAND_D4J = d4jhome + "/framework/bin/defects4j "; 
	}
	
	public static List<String> getFailedTests(Subject subject){
		String path = Constant.HOME + "/d4j-info/failed_tests/" + subject.getName() + "/" + subject.getId() + ".txt"; 
		File file = new File(path);
		if(!file.exists()){
			System.err.println("Failed test file does not exist : " + path);
			System.exit(0);
		}
		List<String> failedTest = new ArrayList<>();
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		String line = null;
		try {
			while((line = bufferedReader.readLine()) != null){
				if(line.length() > 0){
					failedTest.add(line);
				}
			}
			bufferedReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return failedTest;
	}
	
	public static Map<String, Pair<Integer, Set<Integer>>> getProjectInfoFromJSon() {
		Map<String, Pair<Integer, Set<Integer>>> projectInfo = new HashMap<>();
		try {
			// read the json file
			FileReader reader = new FileReader(Constant.PROJ_JSON_FILE);
			JSONParser jsonParser = new JSONParser();
			JSONArray jsonArray = (JSONArray) jsonParser.parse(reader);

			for(int i = 0; i < jsonArray.size(); i++){
				JSONObject project = (JSONObject)jsonArray.get(i);
				String name = (String) project.get("name");
				JSONObject info = (JSONObject) project.get("info");
				Long number = (Long)info.get("number");
				String idString = (String) info.get("single");
				
				Set<Integer> bugId = new HashSet<>();
				String[] ids = idString.split(",");
				for(int j = 0; j < ids.length; j++){
					int dash = ids[j].indexOf("-");
					if(dash == -1){
						bugId.add(Integer.parseInt(ids[j]));
					} else {
						int start = Integer.parseInt(ids[j].substring(0, dash));
						int end = Integer.parseInt(ids[j].substring(dash + 1));
						for(; start <= end; start ++){
							bugId.add(start);
						}
					}
				}
				projectInfo.put(name, new Pair<Integer, Set<Integer>>(number.intValue(), bugId));
			}
			reader.close();
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ParseException ex) {
			ex.printStackTrace();
		} catch (NullPointerException ex) {
			ex.printStackTrace();
		}
		return projectInfo;
	}
	
//	public static void main(String[] args) {
//		Map<String, Pair<Integer, Set<Integer>>> info = getProjectInfoFromJSon();
//		for(Entry<String, Pair<Integer, Set<Integer>>> entry : info.entrySet()){
//			System.out.println(entry.getKey() + " " + entry.getValue().getFirst());
//			System.out.println(entry.getValue().getSecond());
//		}
//	}
	
	public static Subject getSubject(String name, int id){
		String fileName = Constant.PROJ_INFO + "/" + name + "/" + id + ".txt";
		File file = new File(fileName);
		if(!file.exists()){
			System.out.println("File : " + fileName + " does not exist!");
			return null;
		}
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		String line = null;
		List<String> source = new ArrayList<>();
		try {
			while((line = br.readLine()) != null){
				source.add(line);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(source.size() < 4){
			System.err.println("PROJEC INFO CONFIGURE ERROR !");
			System.exit(0);
		}
		
		String ssrc = source.get(0);
		String sbin = source.get(1);
		String tsrc = source.get(2);
		String tbin = source.get(3);
		
		Subject subject = new Subject(name, id, ssrc, tsrc, sbin, tbin);
		return subject;
	}
	
	public static List<Subject> getSubjectFromXML(String fileName) throws NumberFormatException {
		List<Subject> list = new ArrayList<>();

		File inputXml = new File(fileName);
		SAXReader saxReader = new SAXReader();
		try {
			Document document = saxReader.read(inputXml);
			Element root = document.getRootElement();
			
			for (Iterator iterator = root.elementIterator(); iterator.hasNext();) {
				Element element = (Element) iterator.next();
				String name = element.attributeValue("name");
				int id = 0;
				try {
					id = Integer.parseInt(element.attributeValue("id"));
				} catch (NumberFormatException e) {
					throw new NumberFormatException("Parse id failed!");
				}
				String ssrc = element.elementText("ssrc");
				String tsrc = element.elementText("tsrc");
				String sbin = element.elementText("sbin");
				String tbin = element.elementText("tbin");
				Subject subject = new Subject(name, id, ssrc, tsrc, sbin, tbin);
				
				Element pathElem = element.element("classpath");
				
				List<String> classpath = new ArrayList<>();
				if(pathElem != null){
					for(Iterator iterInner = pathElem.elementIterator(); iterInner.hasNext();){
						Element path = (Element) iterInner.next();
						String clp = path.getText();
						if(clp != null){
							for(String jar : getJarFile(new File(subject.getHome() + clp))){
								classpath.add(jar);
							}
						}
					}
				}
				subject.setDependency(classpath);
				list.add(subject);
			}
		} catch (DocumentException e) {
			LevelLogger.fatal(__name__ + "#getSubjectFromXML parse xml file failed !", e);
		}
		return list;
	}
	
	private static List<String> getJarFile(File path){
		List<String> jars = new ArrayList<>();
		if(path.isFile()){
			String file = path.getAbsolutePath();
			if(file.endsWith(".jar")){
				jars.add(file);
			}
		} else if(path.isDirectory()){
			File[] files = path.listFiles();
			for(File f : files){
				jars.addAll(getJarFile(f));
			}
		}
		return jars;
	}
	
}
