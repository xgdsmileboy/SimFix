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
	public static int PATCH_NUM = 1;
	
	// useful file path 
	public static String PROJECT_HOME = null;
	public static String ORI_FAULTLOC = HOME + "/d4j-info/location/ochiai";
	public static String CONVER_FAULTLOC = HOME + "/d4j-info/location/conver";
	public static String PROJ_INFO = HOME + "/d4j-info/src_path";
	public static String PROJ_JSON_FILE = HOME + "/d4j-info/project.json";
	public static String PROJ_LOG_BASE_PATH = HOME + "/log";
	public static String PROJ_REALTIME_LOC_BASE = HOME + "/d4j-info/realtime/location";
	
	
	// command configuration
	public final static String COMMAND_CD = "cd ";
	public final static int COMPILE_TIMEOUT = 120;
	public final static int SBFL_TIMEOUT = 3600;
	
	public final static String LOCATOR_HOME = HOME + "/sbfl";
	public final static String COMMAND_LOCATOR = LOCATOR_HOME + "/sbfl.sh ";
	public final static String LOCATOR_SUSP_FILE_BASE = LOCATOR_HOME + "/ochiai";
	public static String ENV_D4J = "DEFECTS4J_HOME";
	public static String COMMAND_TIMEOUT = "/usr/bin/timeout ";
	public static String COMMAND_D4J = null;
	

}
