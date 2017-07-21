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

	
	// useful file path 
	public static String ORI_FAULTLOC = HOME + "/d4j-info/location/ochiai";
	public static String CONVER_FAULTLOC = HOME + "/d4j-info/location/conver";
	public static String PROJINFOR = HOME + "/d4j-info/src_path";
	
	// command configuration
	public final static String COMMAND_CD = "cd ";
	
	/*
	 * for mac
	 */
//	public final static String COMMAND_TIMEOUT = "/usr/local/bin/gtimeout ";
//	public final static String COMMAND_D4J = "/Users/Jiajun/Code/Defects4J/defects4j/framework/bin/defects4j ";
//	public static String PPROJECT_HOME = HOME + "/testfile";
	
	/*
	 * for linux
	 */
	public final static String COMMAND_TIMEOUT = "/usr/bin/timeout ";
	public final static String COMMAND_D4J = "/home/similar-fix/d4j/defects4j/framework/bin/defects4j ";
	public static String PROJECT_HOME = "/home/similar-fix/d4j/projects";

}
