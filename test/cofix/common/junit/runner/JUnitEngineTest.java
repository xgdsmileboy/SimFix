/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.junit.runner;

import java.io.PrintStream;
import java.util.List;

import org.junit.Test;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import cofix.common.config.Configure;
import cofix.common.config.Constant;
import cofix.common.util.Subject;

/**
 * @author Jiajun
 * @datae Jun 20, 2017
 */
public class JUnitEngineTest {
	
	@Test
	public void test_testClazz(){
		Constant.PROJECT_HOME = System.getProperty("user.dir") + "/testfile";
		System.out.println(Constant.PROJECT_HOME);
		List<Subject> subjects = Configure.getSubjectFromXML("project.xml");
		Subject subject = subjects.get(0);
		String test = "org.jfree.chart.renderer.category.junit.AbstractCategoryItemRendererTests";
		JUnitRuntime runtime = new JUnitRuntime(subject);
		OutStream out = new OutStream();
		Result result = JUnitEngine.getInstance(runtime).test(test, new PrintStream(out));
		System.out.println(result.getFailureCount());
		for(Failure failure : result.getFailures()){
			System.out.println(failure.getDescription());
			System.out.println(failure.getTrace());
		}
		
		System.out.println("out : " + out.getOut().size());
		for(String string : out.getOut()){
			System.out.println(string);
		}
	}
	
}
