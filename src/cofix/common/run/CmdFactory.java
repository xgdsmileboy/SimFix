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
 * @datae Jul 11, 2017
 */
public class CmdFactory {
	/**
	 * build execution command for compiling a subject
	 * 
	 * @param subject
	 *            : subject to be compiled
	 * @return commands need to be executed
	 */
	public static String[] createBuildSubjectCmd(Subject subject) {
		return createD4JCmd(subject, "compile");
	}
	
	public static String[] createTestSubjectCmd(Subject subject, int timeout) {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(Constant.COMMAND_CD + subject.getHome() + " && ");
		stringBuffer.append("gtimeout " + timeout + " ");
		stringBuffer.append(Constant.COMMAND_D4J + "test");
		String[] cmd = new String[] { "/bin/bash", "-c", stringBuffer.toString() };
		return cmd;
	}
	
	public static String[] createTestSingleTestCaseCmd(Subject subject, int timeout, String clazz, String method){
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(Constant.COMMAND_CD + subject.getHome() + " && ");
		stringBuffer.append("gtimeout " + timeout + " ");
		stringBuffer.append(Constant.COMMAND_D4J + "test -t " + clazz + "::" + method);
		String[] cmd = new String[] { "/bin/bash", "-c", stringBuffer.toString() };
		return cmd;
	}
	
	public static String[] createTestSingleTestCaseCmd(Subject subject, String clazz, String method){
		return createD4JCmd(subject, "test -t " + clazz + "::" + method);
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
	private static String[] createD4JCmd(Subject subject, String args) {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append(Constant.COMMAND_CD + subject.getHome() + " && ");
		stringBuffer.append(Constant.COMMAND_D4J + args);
		String[] cmd = new String[] { "/bin/bash", "-c", stringBuffer.toString() };
		return cmd;
	}
}
