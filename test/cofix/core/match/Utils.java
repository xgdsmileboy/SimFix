/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.match;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Statement;

import cofix.common.astnode.CodeBlock;
import cofix.common.astnode.Literal;
import cofix.common.astnode.MethodCall;
import cofix.common.astnode.Operator;
import cofix.common.astnode.Structure;
import cofix.common.astnode.Variable;
import cofix.common.code.search.CodeSearch;
import cofix.common.util.JavaFile;

/**
 * @author Jiajun
 * @datae May 31, 2017
 */
public class Utils {

	public static CodeBlock search(String file, int buggyLine, int lineRange){
		CompilationUnit unit = (CompilationUnit) JavaFile.genASTFromSource(JavaFile.readFileToString(file), ASTParser.K_COMPILATION_UNIT);
		CodeSearch codeSearch = new CodeSearch(unit, buggyLine, lineRange);
		List<Statement> nodes = codeSearch.getASTNodes();
		CodeBlock codeBlock = new CodeBlock(unit, nodes);
		return codeBlock;
	}
	
	public static void print(CodeBlock codeBlock){
		System.out.println("====================================================================");
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
		System.out.println("====================================================================");
	}
	
	public static void showSimilarity(CodeBlock source, CodeBlock similar){
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
		System.out.println(codeBlockMatcher.getSimilirity(source, similar));
	}
	
}
