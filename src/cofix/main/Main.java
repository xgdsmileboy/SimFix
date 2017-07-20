/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.main;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import cofix.common.config.Configure;
import cofix.common.config.Constant;
import cofix.common.localization.AbstractFaultlocalization;
import cofix.common.localization.ManualLocator;
import cofix.common.util.JavaFile;
import cofix.common.util.Status;
import cofix.common.util.Subject;
import cofix.core.parser.ProjectInfo;

/**
 * @author Jiajun
 * @datae Jun 19, 2017
 */
public class Main {
	
	private static void tryFix(Subject subject) throws IOException{
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("=================================================\n");
		stringBuffer.append("Project : " + subject.getName() + "_" + subject.getId() + "\t");
		SimpleDateFormat myFmt1=new SimpleDateFormat("yy/MM/dd HH:mm"); 
		stringBuffer.append("start : " + myFmt1.format(new Date()) + "\n");
		System.out.println(stringBuffer.toString());
		JavaFile.writeStringToFile(Constant.HOME + "/result.log", stringBuffer.toString(), true);
		subject.backup();
		ProjectInfo.init(subject);
		AbstractFaultlocalization fLocalization = new ManualLocator(subject);
//		AbstractFaultlocalization fLocalization = new OchiaiResult(subject);
		Repair repair = new Repair(subject, fLocalization);
		Timer timer = new Timer(5, 0);
		timer.start();
		Status status = repair.fix(timer);
		switch (status) {
		case TIMEOUT:
			System.out.println(status);
			JavaFile.writeStringToFile("result.log", "Timeout time : " + myFmt1.format(new Date()) + "\n", true);
			break;
		case SUCCESS:
			System.out.println(status);
			JavaFile.writeStringToFile("result.log", "Success time : " + myFmt1.format(new Date()) + "\n", true);
			break;
		case FAILED:
			System.out.println(status);
			JavaFile.writeStringToFile("result.log", "Failed time : " + myFmt1.format(new Date()) + "\n", true);
		default:
			break;
		}
		subject.restore();
	}
	

	public static void main(String[] args) throws IOException {
		Constant.PROJECT_HOME = System.getProperty("user.dir") + "/testfile";
		System.out.println(Constant.PROJECT_HOME);
//		List<Subject> subjects = Configure.getSubjectFromXML("project.xml");
		
		Map<String, Set<Integer>> subjects = getSubject();
		
		for(Entry<String, Set<Integer>> entry : subjects.entrySet()){
			String name = entry.getKey();
			for(Integer id : entry.getValue()){
				Subject subject = Configure.getSubject(name, id);
				tryFix(subject);
			}
		}
	}
	
	
	private static Map<String, Set<Integer>> getSubject(){
		Map<String, Set<Integer>> subjects = new HashMap<>();
		Set<Integer> chartID = new HashSet<>();
		chartID.add(1);
		chartID.add(7);
		chartID.add(20);
		subjects.put("chart", chartID);
//		Set<Integer> closureID = new HashSet<>();
//		closureID.add(14);
//		closureID.add(57);
//		closureID.add(73);
//		subjects.put("closure", closureID);
//		Set<Integer> langID = new HashSet<>();
//		langID.add(33);
////		langID.add(35); // need split
//		langID.add(39);
//		langID.add(43);
//		langID.add(58);
//		langID.add(60);
//		subjects.put("lang", langID);
//		Set<Integer> mathID = new HashSet<>();
//		mathID.add(5);
//		mathID.add(33);
//		mathID.add(35);
//		mathID.add(41);
////		mathID.add(49); // need split
//		mathID.add(53);
//		mathID.add(59);
//		mathID.add(63);
//		mathID.add(70);
////		mathID.add(71); // need split
////		mathID.add(72); // need split
//		mathID.add(75);
//		mathID.add(79);
////		mathID.add(98); // need split
//		subjects.put("math", mathID);
		
		return subjects;
	}

}
