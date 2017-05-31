/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.astnode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Statement;
import org.junit.Test;

import cofix.common.code.search.CodeSearch;
import cofix.common.config.Constant;
import cofix.common.parser.ProjectInfo;
import cofix.common.util.JavaFile;
import cofix.common.util.Subject;
import cofix.core.match.CodeBlockMatcher;
import cofix.core.match.LiteralMetric;
import cofix.core.match.MethodMetric;
import cofix.core.match.Metric;
import cofix.core.match.OperatorMetric;
import cofix.core.match.StructrueMetric;
import cofix.core.match.VariableMetric;

/**
 * @author Jiajun
 *
 */
public class CodeBlockTest {
	
	@Test
	public void test_codeBlock(){
		Constant.PROJECT_HOME = "testfile";
		Subject subject = new Subject("chart", 7, "/source", "/tests", "/build", "build-tests");
		ProjectInfo.init(subject);
		
		String file = subject.getHome() + "/source/org/jfree/data/time/TimePeriodValues.java";
		int buggyLine = 299;
		CompilationUnit unit = (CompilationUnit) JavaFile.genASTFromSource(JavaFile.readFileToString(file), ASTParser.K_COMPILATION_UNIT);
		
		CodeSearch codeSearch = new CodeSearch(unit, buggyLine, 10);
		List<Statement> nodes = codeSearch.getASTNodes();
		
		CodeBlock codeBlock = new CodeBlock(unit, nodes);
		
		print(codeBlock);
		
		CodeSearch codeSearch2 = new CodeSearch(unit, 285, 10);
		List<Statement> nodes2 = codeSearch2.getASTNodes();
		CodeBlock codeBlock2 = new CodeBlock(unit, nodes2);
		
		print(codeBlock2);
		
		
		VariableMetric variableMetric = new VariableMetric(.2f);
		LiteralMetric literalMetric = new LiteralMetric(.2f);
		StructrueMetric structrueMetric = new StructrueMetric(.2f);
		OperatorMetric operatorMetric = new OperatorMetric(.2f);
		MethodMetric methodMetric = new MethodMetric(.2f);
		List<Metric> metrics = new ArrayList<>();
		metrics.add(variableMetric);
		metrics.add(literalMetric);
		metrics.add(structrueMetric);
		metrics.add(operatorMetric);
		metrics.add(methodMetric);
		CodeBlockMatcher codeBlockMatcher = new CodeBlockMatcher(metrics);
		System.out.println(codeBlockMatcher.getSimilirity(codeBlock, codeBlock2));
	}
	
	private void print(CodeBlock codeBlock){
		System.out.println("----------------- Constant -----------------");
		for(Entry<Literal, Integer> entry : codeBlock.getConstants().entrySet()){
			System.out.println(entry.getKey() + " : " + entry.getValue());
		}
		System.out.println("----------------- Variable -----------------");
		for(Entry<Variable, Integer> entry : codeBlock.getVariables().entrySet()){
			System.out.println(entry.getKey() + " : " + entry.getValue());
		}
		System.out.println("----------------- Structure -----------------");
		for(Structure structure : codeBlock.getStructures()){
			System.out.println(structure);
		}
		
		System.out.println("----------------- Operator -----------------");
		for(Operator operator : codeBlock.getOperators()){
			System.out.println(operator.toString());
		}
		
		System.out.println("----------------- MethodCall -----------------");
		for(Entry<MethodCall, Integer> entry : codeBlock.getMethodCalls().entrySet()){
			System.out.println(entry.getKey() + " : " + entry.getValue());
		}
	}
	
}
