/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.test.purification;

import java.util.List;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.Test;

import cofix.common.config.Configure;
import cofix.common.config.Constant;
import cofix.common.util.Pair;
import cofix.common.util.Subject;

/**
 * @author Jiajun
 * @datae Jul 26, 2017
 */
public class PurificationTest {
	
	@Test
	public void testPurify_assert(){
		Constant.PROJECT_HOME = Constant.HOME + "/testfile";
		Subject subject = Configure.getSubject("lang", 60);
		String failedTest = "org.apache.commons.lang.text.StrBuilderTest::testLang295";
		Purification purification = new Purification(subject, failedTest);
		List<Pair<String, MethodDeclaration>> tests = purification.purify();
		for(Pair<String, MethodDeclaration> t : tests){
			System.out.println(t.getFirst());
			System.out.println(t.getSecond());
		}
	}
	
	@Test
	public void testPurify_fail(){
		Constant.PROJECT_HOME = Constant.HOME + "/testfile";
		Subject subject = Configure.getSubject("math", 72);
		String failedTest = "org.apache.commons.math.analysis.solvers.BrentSolverTest::testInitialGuess";
		Purification purification = new Purification(subject, failedTest);
		List<Pair<String, MethodDeclaration>> tests = purification.purify();
		for(Pair<String, MethodDeclaration> t : tests){
			System.out.println(t.getFirst());
			System.out.println(t.getSecond());
		}
		
	}
	
}
