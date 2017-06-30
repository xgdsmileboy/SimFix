/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.parser.node;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.Test;

import cofix.common.config.Constant;
import cofix.common.util.JavaFile;
import cofix.common.util.Subject;
import cofix.core.parser.ProjectInfo;
import cofix.core.parser.search.BuggyCode;

/**
 * @author Jiajun
 * @datae Jun 30, 2017
 */
public class CodeBlockTest {
	
	@Test
	public void test_codeBlock(){
		Constant.PROJECT_HOME = "testfile";
		Subject subject = new Subject("chart", 7, "/source", "/tests", "/build", "build-tests");
		ProjectInfo.init(subject);
		
		String file = subject.getHome() + "/source/org/jfree/data/time/TimePeriodValues.java";
		int buggyLine = 299;
		CompilationUnit unit = JavaFile.genASTFromFile(file);
		
		CodeBlock codeBlock = BuggyCode.getBuggyCodeBlock(unit, buggyLine);
		
		System.out.println(codeBlock.toSrcString());
	}
	
}
