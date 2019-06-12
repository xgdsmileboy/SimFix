/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.util;

import java.util.List;

import org.junit.Test;

import cofix.common.config.Constant;

/**
 * @author Jiajun
 * @date Jun 16, 2017
 */
public class SubjectTest {
	
	@Test
	public void test_getTestSuiteClasses(){
		Constant.PROJECT_HOME = "testfile";
		Subject subject = new Subject("chart", 7, "/source", "/tests", "/build", "build-tests");
		List<String> classes = subject.getTestClasses();
		for(String string : classes){
			System.out.println(string);
		}
		System.out.println(classes.size());
		
	}
}
