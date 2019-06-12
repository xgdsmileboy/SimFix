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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;

import cofix.common.config.Configure;
import cofix.common.config.Constant;
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
 * @date Jun 19, 2017
 */
public class Main {
	
	public static void main(String[] args) throws IOException {
		Constant.PATCH_NUM = 1;
		
		Map<String, Pair<Integer, Set<Integer>>> projInfo = Configure.getProjectInfoFromJSon();
		Pair<String, Set<Integer>> options = parseCommandLine(args, projInfo);
		if(Constant.PROJECT_HOME == null || options.getFirst() == null || options.getSecond().size() == 0){
			printUsage();
			System.exit(0);
		}
		
		Configure.configEnvironment();
		System.out.println(Constant.PROJECT_HOME);
		
		flexibelConfigure(options.getFirst(), options.getSecond(), projInfo);
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
		List<String> purifiedFailedTestCases = purification.purify(purify);
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
			List<String> currentFailedTests = new ArrayList<>();
			int timeout = 300;
			if(purify){
				timeout /= purifiedFailedTestCases.size();
				currentFailedTests.add(teString);
				CommentTestCase.comment(subject.getHome() + subject.getTsrc(), purifiedFailedTestCases, teString);
			} else {
				currentFailedTests.addAll(purifiedFailedTestCases);
			}
			SBFLocator sbfLocator = new SBFLocator(subject);
//			MFLocalization sbfLocator = new MFLocalization(subject);
			sbfLocator.setFailedTest(currentFailedTests);
			
			Repair repair = new Repair(subject, sbfLocator);
			Timer timer = new Timer(0, timeout);
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
			if(!purify){
				break;
			}
		}
		
		FileUtils.deleteDirectory(purifyBackup);
		subject.restore(subject.getHome() + subject.getSsrc());
		subject.restore(subject.getHome() + subject.getTsrc());
	}
	
	private static Pair<String, Set<Integer>> parseCommandLine(String[] args, Map<String, Pair<Integer, Set<Integer>>> projInfo) {
		Pair<String, Set<Integer>> options = new Pair<String, Set<Integer>>();
		if(args.length < 3){
			return options;
		}
		String projName = null;
		Set<Integer> idSet = new HashSet<>();
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
		options.setFirst(projName);
		options.setSecond(idSet);
		return options;
	}
	
	private static void printUsage(){
		// --proj_home=/home/user/d4j/projects --proj_name=chart --bug_id=3-5/all/1
		System.err.println("Usage : --proj_home=\"project home\" --proj_name=\"project name\" --bug_id=\"3-5/all/1/1,2,5/single/multi\"");
	}
	
	private static void flexibelConfigure(String projName, Set<Integer> ids, Map<String, Pair<Integer, Set<Integer>>> projInfo) throws IOException{
		Pair<Integer, Set<Integer>> bugIDs = projInfo.get(projName);
		for(Integer id : ids){
			Subject subject = Configure.getSubject(projName, id);
			trySplitFix(subject, !bugIDs.getSecond().contains(id));
		}
	}
	
}
