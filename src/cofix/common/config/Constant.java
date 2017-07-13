/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */

package cofix.common.config;

/**
 * This class contains all constant variables
 * @author Jiajun
 *
 */
public class Constant {

	public final static String HOME = System.getProperty("user.dir");

	// common info
	public final static String SOURCE_FILE_SUFFIX = ".java";
	
	// build failed flag
	public final static String ANT_BUILD_FAILED = "BUILD FAILED";

	// code search configure
	public final static int MAX_BLOCK_LINE = 10;

	public static String PROJECT_HOME = null;
	
	public final static String COMMAND_CD = "cd ";
	public final static String COMMAND_D4J = "/Users/Jiajun/Code/Defects4J/defects4j/framework/bin/defects4j ";

}
