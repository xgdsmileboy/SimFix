/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.config;

import java.util.List;

import org.junit.Test;

import cofix.common.util.Subject;

/**
 * @author Jiajun
 * @date Jun 20, 2017
 */
public class ConfigureTest {

	@Test 
	public void test_getSubjectFromXML(){
		Constant.PROJECT_HOME = System.getProperty("user.dir") + "/testfile";
		List<Subject> subjects = Configure.getSubjectFromXML("project.xml");
		for(Subject subject : subjects){
			System.out.println(subject);
		}
	}
}
