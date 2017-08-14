/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.main;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import cofix.common.config.Configure;
import cofix.common.config.Constant;
import cofix.common.localization.AbstractFaultlocalization;
import cofix.common.localization.OchiaiResult;
import cofix.common.run.Runner;
import cofix.common.util.JavaFile;
import cofix.common.util.Pair;
import cofix.common.util.Status;
import cofix.common.util.Subject;
import cofix.core.parser.ProjectInfo;
import cofix.test.purification.CommentTestCase;
import cofix.test.purification.Purification;
import sbfl.locator.SBFLocator;

/**
 * @author Jiajun
 * @datae Jun 19, 2017
 */
public class Main {
	
	private static void trySingleFix(Subject subject) throws IOException{
		String logFile = Constant.PROJ_LOG_BASE_PATH + "/" + subject.getName() + "/" + subject.getId() + ".log";
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
		Status status = repair.fix(timer, logFile, 1);
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
	
	private static void trySplitFix(Subject subject, boolean purify) throws IOException{
		
		String logFile = Constant.PROJ_LOG_BASE_PATH + "/" + subject.getName() + "/" + subject.getId() + ".log";
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("=================================================\n");
		stringBuffer.append("Project : " + subject.getName() + "_" + subject.getId() + "\t");
		SimpleDateFormat simpleFormat=new SimpleDateFormat("yy/MM/dd HH:mm"); 
		stringBuffer.append("start : " + simpleFormat.format(new Date()) + "\n");
		System.out.println(stringBuffer.toString());
		JavaFile.writeStringToFile(logFile, stringBuffer.toString(), true);
		
		ProjectInfo.init(subject);
		subject.backup(subject.getHome() + subject.getSsrc());
		subject.backup(subject.getHome() + subject.getTsrc());
		FileUtils.deleteDirectory(new File(subject.getHome() + subject.getTbin()));
		FileUtils.deleteDirectory(new File(subject.getHome() + subject.getSbin()));
		Purification purification = new Purification(subject);
		List<String> purifiedFailedTestCases = null;
		if(purify){
			purifiedFailedTestCases = purification.purify();
		}
		if(purifiedFailedTestCases == null || purifiedFailedTestCases.size() == 0){
			purifiedFailedTestCases = purification.getFailedTest();
		}
		File purifiedTest = new File(subject.getHome() + subject.getTsrc());
		File purifyBackup = new File(subject.getHome() + subject.getTsrc() + "_purify");
		FileUtils.copyDirectory(purifiedTest, purifyBackup);
		Set<String> alreadyFix = new HashSet<>();
		boolean lastRslt = false;
		for(int currentTry = 0; currentTry < purifiedFailedTestCases.size(); currentTry ++){
			String teString = purifiedFailedTestCases.get(currentTry);
			JavaFile.writeStringToFile(logFile, "Current failed test : " + teString + " | " + simpleFormat.format(new Date()) + "\n", true);
			FileUtils.copyDirectory(purifyBackup, purifiedTest);
			FileUtils.deleteDirectory(new File(subject.getHome() + subject.getTbin()));
			if(lastRslt){
				for(int i = currentTry; i < purifiedFailedTestCases.size(); i++){
					if(Runner.testSingleTest(subject, purifiedFailedTestCases.get(i))){
						alreadyFix.add(purifiedFailedTestCases.get(i));
					}
				}
			}
			lastRslt = false;
			if(alreadyFix.contains(teString)){
				JavaFile.writeStringToFile(logFile, "Already fixed : " + teString + "\n", true);
				continue;
			}
			// can only find one patch now, should be optimized after fixing one test
			subject.restore(subject.getHome() + subject.getSsrc());
			FileUtils.deleteDirectory(new File(subject.getHome() + subject.getSbin()));
			FileUtils.deleteDirectory(new File(subject.getHome() + subject.getTbin()));
			CommentTestCase.comment(subject.getHome() + subject.getTsrc(), purifiedFailedTestCases, teString);
			SBFLocator sbfLocator = new SBFLocator(subject);
//			MFLocalization sbfLocator = new MFLocalization(subject);
			List<String> currentFailedTests = new ArrayList<>();
			currentFailedTests.add(teString);
			sbfLocator.setFailedTest(currentFailedTests);
			
			Repair repair = new Repair(subject, sbfLocator);
			Timer timer = new Timer(5, 0);
			timer.start();
			Status status = repair.fix(timer, logFile, currentTry);
			switch (status) {
			case TIMEOUT:
				System.out.println(status);
				JavaFile.writeStringToFile(logFile, "Timeout time : " + simpleFormat.format(new Date()) + "\n", true);
				break;
			case SUCCESS:
				lastRslt = true;
				System.out.println(status);
				JavaFile.writeStringToFile(logFile, "Success time : " + simpleFormat.format(new Date()) + "\n", true);
				break;
			case FAILED:
				System.out.println(status);
				JavaFile.writeStringToFile(logFile, "Failed time : " + simpleFormat.format(new Date()) + "\n", true);
			default:
				break;
			}
		}
		
		FileUtils.deleteDirectory(purifyBackup);
		subject.restore(subject.getHome() + subject.getSsrc());
		subject.restore(subject.getHome() + subject.getTsrc());
	}
	

	public static void main(String[] args) throws IOException {
		Constant.PATCH_NUM = 1;
		String projName = null;
		Set<Integer> idSet = new HashSet<>();
		if(args.length < 3){
			printUsage();
			System.exit(0);
		}
		Map<String, Pair<Integer, Set<Integer>>> projInfo = Configure.getProjectInfoFromJSon();
		for(int i = 0; i < args.length; i++){
			if(args[i].startsWith("--proj_home=")){
				Constant.PROJECT_HOME = args[i].substring("--proj_home=".length());
			} else if(args[i].startsWith("--proj_name=")){
				projName = args[i].substring("--proj_name=".length());
			} else if(args[i].startsWith("--bug_id=")){
				String idseq = args[i].substring("--bug_id=".length());
				if(idseq.equalsIgnoreCase("single")){
					idSet.addAll(projInfo.get(projName).getSecond());
				} else if(idseq.equalsIgnoreCase("multi")){
					for(int id = 1; id <= projInfo.get(projName).getFirst(); id++){
						if(projInfo.get(projName).getSecond().contains(id)){
							continue;
						}
						idSet.add(id);
					}
				} else if(idseq.equalsIgnoreCase("all")){
					for(int id = 1; id <= projInfo.get(projName).getFirst(); id++){
						idSet.add(id);
					}
				} else if(idseq.contains("-")){
					int start = Integer.parseInt(idseq.substring(0, idseq.indexOf("-")));
					int end = Integer.parseInt(idseq.substring(idseq.indexOf("-") + 1, idseq.length()));
					for(int id = start; id <= end; id++){
						idSet.add(id);
					}
				} else {
					String[] split = idseq.split(",");
					for(String string : split){
						int id = Integer.parseInt(string);
						idSet.add(id);
					}
				}
			}
		}
		
		if(Constant.PROJECT_HOME == null || projName == null || idSet.size() == 0){
			printUsage();
			System.exit(0);
		}
		
		Configure.configEnvironment();
		System.out.println(Constant.PROJECT_HOME);
		
//		runSmallDataset();
//		runAllProjectSingle("math");
		flexibelConfigure(projName, idSet, projInfo);
		
	}
	
	private static void printUsage(){
		// --proj_home=/home/jiajun/d4j/projects --proj_name=chart --bug_id=3-5/all/1
		System.err.println("Usage : --proj_home=\"project home\" --proj_name=\"project name\" --bug_id=\"3-5/all/1/1,2,5/single/multi\"");
	}
	
	private static void flexibelConfigure(String projName, Set<Integer> ids, Map<String, Pair<Integer, Set<Integer>>> projInfo) throws IOException{
		Map<String, Set<Integer>> subjects = getSubject();
		
		Pair<Integer, Set<Integer>> bugIDs = projInfo.get(projName);
		
		for(Integer id : ids){
			Subject subject = Configure.getSubject(projName, id);
			trySplitFix(subject, !bugIDs.getSecond().contains(id));
//			trySingleFix(subject);
		}
	}
	
	private static void runSmallDataset() throws IOException{
		Map<String, Pair<Integer, Set<Integer>>> projInfo = Configure.getProjectInfoFromJSon();
		Map<String, Set<Integer>> subjects = getSubject();
		for(Entry<String, Set<Integer>> entry : subjects.entrySet()){
			String name = entry.getKey();
			Pair<Integer, Set<Integer>> bugIDs = projInfo.get(name);
			for(Integer id : entry.getValue()){
				Subject subject = Configure.getSubject(name, id);
				if(bugIDs.getSecond().contains(id)){
					trySingleFix(subject);
				} else {
					trySplitFix(subject, true);
				}
			}
		}
	}
	
	private static void runAllProjectSingle(String projName) throws IOException{
		Map<String, Pair<Integer, Set<Integer>>> projInfo = Configure.getProjectInfoFromJSon();
		Map<String, Set<Integer>> subjects = getSubject();
		
		Pair<Integer, Set<Integer>> bugIDs = projInfo.get(projName);
		Set<Integer> already = subjects.get(projName);
		
		for(int id = 1; id < bugIDs.getFirst(); id++){
			if(already.contains(id)){
				continue;
			}
			Subject subject = Configure.getSubject(projName, id);
			if(bugIDs.getSecond().contains(id)){
				trySingleFix(subject);
			} else {
				trySplitFix(subject, true);
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
		Set<Integer> timeID = new HashSet<>();
		subjects.put("time", timeID);
		
		return subjects;
	}

}
