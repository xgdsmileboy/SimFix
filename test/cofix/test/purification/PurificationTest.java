/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.test.purification;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Test;

import cofix.common.config.Configure;
import cofix.common.config.Constant;
import cofix.common.util.Subject;

/**
 * @author Jiajun
 * @date Jul 26, 2017
 */
public class PurificationTest {
	
//	@Test
//	public void testPurify_assert(){
//		Constant.PROJECT_HOME = Constant.HOME + "/testfile";
//		Subject subject = Configure.getSubject("lang", 60);
//		String failedTest = "org.apache.commons.lang.text.StrBuilderTest::testLang295";
//		Purification purification = new Purification(subject);
//		Map<String, List<String>> tests = purification.purify();
//		for(Entry<String, List<String>> entry : tests.entrySet()){
//			System.out.println(entry.getKey());
//			System.out.println(entry.getValue());
//		}
//	}
//	
	@Test
	public void testPurify_fail(){
//		Constant.PROJECT_HOME = Constant.HOME + "/testfile";
		Configure.configEnvironment();
		Subject subject = Configure.getSubject("math", 72);
		try {
			subject.backup(subject.getHome() + subject.getSsrc());
			subject.backup(subject.getHome() + subject.getTsrc());
		} catch (IOException e) {
			e.printStackTrace();
		}
		Purification purification = new Purification(subject);
		List<String> purifiedFailedTestCases = purification.purify(true);
		for(String teString : purifiedFailedTestCases){
			System.out.println(teString);
		}
		
	}
	
}
