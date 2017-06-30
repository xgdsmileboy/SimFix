/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.parser.search;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.junit.Test;

import cofix.common.config.Constant;
import cofix.common.util.JavaFile;
import cofix.common.util.Pair;
import cofix.common.util.Subject;
import cofix.core.parser.ProjectInfo;
import cofix.core.parser.node.CodeBlock;

/**
 * @author Jiajun
 * @datae Jun 30, 2017
 */
public class SimpleFilterTest {
	
	private double guard = 0.5f;

	@Test
	public void test_filter(){
		Constant.PROJECT_HOME = "testfile";
		Subject subject = new Subject("chart", 1, "/source", "/tests", "/build", "/build-tests");
		ProjectInfo.init(subject);
		String file = subject.getHome() + subject.getSsrc() +"/org/jfree/chart/renderer/category/AbstractCategoryItemRenderer.java";
		int buggyLine = 1797;
		
		CompilationUnit unit = (CompilationUnit) JavaFile.genASTFromSource(JavaFile.readFileToString(file), ASTParser.K_COMPILATION_UNIT);
		
		CodeBlock codeBlock = BuggyCode.getBuggyCodeBlock(unit, buggyLine);
		
		System.out.println(codeBlock.toSrcString());
		
		SimpleFilter simpleFilter = new SimpleFilter(codeBlock);
		List<Pair<CodeBlock, Double>> candidates = simpleFilter.filter(subject.getHome() + subject.getSsrc(), guard);
		
		for(Pair<CodeBlock, Double> block : candidates){
			System.out.println("----------------Similarity : " + block.getSecond() + "-------------------------------------");
			System.out.println(block.getFirst().toSrcString());
		}
		
		System.out.println("-----------" + candidates.size() + "-------------");
	}
	
}
