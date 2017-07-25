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

import org.eclipse.jdt.internal.eval.CodeSnippetAllocationExpression;

import java.util.Set;

import cofix.common.config.Configure;
import cofix.common.config.Constant;
import cofix.common.localization.AbstractFaultlocalization;
import cofix.common.localization.OchiaiResult;
import cofix.common.util.JavaFile;
import cofix.common.util.Pair;
import cofix.common.util.Status;
import cofix.common.util.Subject;
import cofix.core.parser.ProjectInfo;

/**
 * @author Jiajun
 * @datae Jun 19, 2017
 */
public class Main {
	
	private static void tryFix(Subject subject) throws IOException{
		String logFile = Constant.PROJLOGBASEPATH = "/" + subject.getName() + "/" + subject.getId() + ".log";
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("=================================================\n");
		stringBuffer.append("Project : " + subject.getName() + "_" + subject.getId() + "\t");
		SimpleDateFormat simpleFormat=new SimpleDateFormat("yy/MM/dd HH:mm"); 
		stringBuffer.append("start : " + simpleFormat.format(new Date()) + "\n");
		System.out.println(stringBuffer.toString());
		JavaFile.writeStringToFile(logFile, stringBuffer.toString(), true);
		
		subject.backup();
		ProjectInfo.init(subject);
//		AbstractFaultlocalization fLocalization = new ManualLocator(subject);
		AbstractFaultlocalization fLocalization = new OchiaiResult(subject);
		Repair repair = new Repair(subject, fLocalization);
		Timer timer = new Timer(5, 0);
		timer.start();
		Status status = repair.fix(timer, logFile);
		switch (status) {
		case TIMEOUT:
			System.out.println(status);
			JavaFile.writeStringToFile(logFile, "Timeout time : " + simpleFormat.format(new Date()) + "\n", true);
			break;
		case SUCCESS:
			System.out.println(status);
			JavaFile.writeStringToFile(logFile, "Success time : " + simpleFormat.format(new Date()) + "\n", true);
			break;
		case FAILED:
			System.out.println(status);
			JavaFile.writeStringToFile(logFile, "Failed time : " + simpleFormat.format(new Date()) + "\n", true);
		default:
			break;
		}
		subject.restore();
	}
	

	public static void main(String[] args) throws IOException {
//		// for debug
		Constant.COMMAND_TIMEOUT = "/usr/local/bin/gtimeout ";
		Constant.PROJECT_HOME = Constant.HOME + "/testfile";
		
		Constant.PATCH_NUM = 3;
		Configure.configEnvironment();
		System.out.println(Constant.PROJECT_HOME);
		
//		runSmallDataset();
		runAllProjectSingle("chart");
		
	}
	
	private static void runSmallDataset() throws IOException{
		Map<String, Set<Integer>> subjects = getSubject();
		for(Entry<String, Set<Integer>> entry : subjects.entrySet()){
			String name = entry.getKey();
			for(Integer id : entry.getValue()){
				Subject subject = Configure.getSubject(name, id);
				tryFix(subject);
			}
		}
	}
	
	private static void runAllProjectSingle(String projName) throws IOException{
		Map<String, Pair<Integer, Set<Integer>>> projInfo = Configure.getProjectInfoFromJSon();
		Map<String, Set<Integer>> subjects = getSubject();
		
		Pair<Integer, Set<Integer>> bugIDs = projInfo.get(projName);
		Set<Integer> already = subjects.get(projName);
		
		for(Integer id : bugIDs.getSecond()){
			if(!already.contains(id)){
				Subject subject = Configure.getSubject(projName, id);
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
		Set<Integer> closureID = new HashSet<>();
		closureID.add(14);
		closureID.add(57);
		closureID.add(73);
		subjects.put("closure", closureID);
		Set<Integer> langID = new HashSet<>();
		langID.add(33);
		langID.add(35); // need split
		langID.add(39);
		langID.add(43);
		langID.add(58);
		langID.add(60); // need split
		subjects.put("lang", langID);
		Set<Integer> mathID = new HashSet<>();
		mathID.add(5); //OK
		mathID.add(33); //OK
		mathID.add(35); // need split
		mathID.add(41);
		mathID.add(49); // need split
		mathID.add(53); //OK
		mathID.add(59); //OK
		mathID.add(63); //OK
		mathID.add(70); //OK
		mathID.add(71); // need split
		mathID.add(72); // need split
		mathID.add(75); //OK
		mathID.add(79); //OK
		mathID.add(98); // need split
		subjects.put("math", mathID);
		
		return subjects;
	}

}
