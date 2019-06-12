/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.run;

import cofix.common.config.Constant;
import cofix.common.util.Subject;

/**
 * @author Jiajun
 * @date Jul 11, 2017
 */
public class CmdFactory {
	
	public static String[] createSbflCmd(Subject subject, int timeout){
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(Constant.COMMAND_CD + Constant.LOCATOR_HOME + " && ");
		stringBuffer.append(Constant.COMMAND_TIMEOUT + timeout + " ");
		stringBuffer.append(Constant.COMMAND_LOCATOR + subject.getName());
		stringBuffer.append(" " + subject.getId());
		stringBuffer.append(" " + subject.getHome());
		String[] cmd = new String[] { "/bin/bash", "-c", stringBuffer.toString() };
		return cmd;
	}
	
	/**
	 * build execution command for compiling a subject
	 * 
	 * @param subject
	 *            : subject to be compiled
	 * @return commands need to be executed
	 */
	public static String[] createBuildSubjectCmd(Subject subject) {
		return createD4JCmd(subject, "compile", Constant.COMPILE_TIMEOUT);
	}
	
	public static String[] createTestSubjectCmd(Subject subject, int timeout) {
		return createD4JCmd(subject, "test", timeout);
	}
	
	public static String[] createTestSingleTestCaseCmd(Subject subject, int timeout, String clazzAndMethod){
		return createD4JCmd(subject, "test -t " + clazzAndMethod, timeout);
	}
	
	public static String[] createTestSingleTestCaseCmd(Subject subject, int timeout, String clazz, String method){
		return createD4JCmd(subject, "test -t " + clazz + "::" + method, timeout);
	}
	
	public static String[] createTestSingleTestCaseCmd(Subject subject, String clazz, String method){
		return createD4JCmd(subject, "test -t " + clazz + "::" + method, -1);
	}
	
	/**
	 * create d4j command based on the given argument {@code args}
	 * 
	 * @param subject
	 *            : subject to be focused
	 * @param args
	 *            : command to be executed, e.g., "test", "compile", etc.
	 * @return command need to be executed
	 */
	private static String[] createD4JCmd(Subject subject, String args, int timeout) {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(Constant.COMMAND_CD + subject.getHome() + " && ");
		if(timeout > 0){
			stringBuffer.append(Constant.COMMAND_TIMEOUT + timeout + " ");
		}
		stringBuffer.append(Constant.COMMAND_D4J + args);
		String[] cmd = new String[] { "/bin/bash", "-c", stringBuffer.toString() };
		return cmd;
	}
}
