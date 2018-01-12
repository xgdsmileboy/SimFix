/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.run;

import java.util.List;

import cofix.common.config.Constant;
import cofix.common.util.LevelLogger;
import cofix.common.util.Subject;

/**
 * @author Jiajun
 * @date Jul 11, 2017
 */
public class Runner {
	
	private final static String __name__ = "@Runner ";
	private final static String SUCCESSTEST = "Failing tests: 0";
	
	
	public static boolean testSingleTest(Subject subject, String clazzAndMethod){
		List<String> message = null;
		try {
			System.out.println("TESTING : " + clazzAndMethod);
			message = Executor.execute(CmdFactory.createTestSingleTestCaseCmd(subject, 30, clazzAndMethod));
		} catch (Exception e) {
			LevelLogger.fatal(__name__ + "#buildSubject run test single test case failed !", e);
		}
		
		boolean success = false;
		for(int i = message.size() - 1; i >= 0; i--){
//			System.out.println(message.get(i));
			if (message.get(i).contains(SUCCESSTEST)) {
				success = true;
				break;
			}
		}
		
		return success;
	}
	
	public static boolean testSingleTest(Subject subject, String clazz, String method){
		return testSingleTest(subject, clazz + "::" + method);
	}
	
	public static boolean runTestSuite(Subject subject){
		List<String> message = null;
		try {
			System.out.println("TESTING : " + subject.getName() + "_" + subject.getId());
			message = Executor.execute(CmdFactory.createTestSubjectCmd(subject, 10*60));
		} catch (Exception e) {
			LevelLogger.fatal(__name__ + "#buildSubject run test single test case failed !", e);
		}
		
		boolean success = false;
		for(int i = message.size() - 1; i >= 0; i--){
//			System.out.println(message.get(i));
			if (message.get(i).contains(SUCCESSTEST)) {
				success = true;
				break;
			}
		}
		
		return success;
	}
	
	public static boolean compileSubject(Subject subject) {
		List<String> message = null;
		try {
			message = Executor.execute(CmdFactory.createBuildSubjectCmd(subject));
		} catch (Exception e) {
			LevelLogger.fatal(__name__ + "#buildSubject run build subject failed !", e);
		}
		
		boolean success = true;
		for(int i = message.size() - 1; i >= 0; i--){
			if (message.get(i).contains(Constant.ANT_BUILD_FAILED)) {
				success = false;
				break;
			}
		}
		
		return success;
	}
}
