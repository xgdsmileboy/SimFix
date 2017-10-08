/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.localization;

import org.junit.Test;

import com.gzoltar.core.components.Statement;

import cofix.common.config.Constant;
import cofix.common.util.Subject;

/**
 * @author Jiajun
 * @date Jun 16, 2017
 */
public class FLocalizationTest {
	@Test
	public void test_fl2(){

		Constant.PROJECT_HOME = "/Users/Jiajun/Code/Java/fault-fix/SimilarFix/testfile";
		Subject subject = new Subject("chart", 7, "/source", "/tests", "/build", "/build-tests");
		
		FLocalization fLocalization = new FLocalization(subject);
		fLocalization.locateFault(0);
		
		for(Statement stmt : fLocalization.getSuspiciousStatement()){
			System.out.println(stmt.getMethod().getParent().getLabel() + "," + stmt.getLineNumber() + "," + stmt.getSuspiciousness());
		}
		
//		System.out.println("\n-----------------------------------------\n");
//		for(String string : fLocalization.getPassedTestCases()){
//			System.out.println(string);
//		}
//		
	}
}
