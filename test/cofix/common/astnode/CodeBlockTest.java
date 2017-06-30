/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.astnode;

/**
 * @author Jiajun
 *
 */
public class CodeBlockTest {
	
//	@Test
//	public void test_codeBlock(){
//		Constant.PROJECT_HOME = "testfile";
//		Subject subject = new Subject("chart", 7, "/source", "/tests", "/build", "build-tests");
//		ProjectInfo.init(subject);
//		
//		String file = subject.getHome() + "/source/org/jfree/data/time/TimePeriodValues.java";
//		int buggyLine = 299;
//		CompilationUnit unit = (CompilationUnit) JavaFile.genASTFromSource(JavaFile.readFileToString(file), ASTParser.K_COMPILATION_UNIT);
//		
//		CodeBlock codeBlock = BuggyCode.getBuggyCodeBlock(unit, buggyLine);
//		
//		print(codeBlock);
//		
//		CodeSearch codeSearch2 = new CodeSearch(unit, 285, codeBlock.getCurrentLine());
//		List<ASTNode> nodes2 = codeSearch2.getASTNodes();
//		CodeBlock codeBlock2 = new CodeBlock(unit, nodes2);
//		
//		print(codeBlock2);
//		
//		
//		VariableMetric variableMetric = new VariableMetric(.2f);
//		LiteralMetric literalMetric = new LiteralMetric(.2f);
//		StructrueMetric structrueMetric = new StructrueMetric(.2f);
//		OperatorMetric operatorMetric = new OperatorMetric(.2f);
//		MethodMetric methodMetric = new MethodMetric(.2f);
//		List<Metric> metrics = new ArrayList<>();
//		metrics.add(variableMetric);
//		metrics.add(literalMetric);
//		metrics.add(structrueMetric);
//		metrics.add(operatorMetric);
//		metrics.add(methodMetric);
//		CodeBlockMatcher codeBlockMatcher = new CodeBlockMatcher(metrics);
//		System.out.println(codeBlockMatcher.getSimilirity(codeBlock, codeBlock2));
//	}
//	
//	@Test
//	public void test_lineCount(){
//		Constant.PROJECT_HOME = "testfile";
//		Subject subject = new Subject("chart", 7, "/source", "/tests", "/build", "build-tests");
//		ProjectInfo.init(subject);
//		
//		String file = subject.getHome() + "/source/org/jfree/data/time/TimePeriodValues.java";
//		int buggyLine = 299;
//		CompilationUnit unit = (CompilationUnit) JavaFile.genASTFromSource(JavaFile.readFileToString(file), ASTParser.K_COMPILATION_UNIT);
//		
//		CodeSearch codeSearch = new CodeSearch(unit, buggyLine, 10);
//		List<ASTNode> nodes = codeSearch.getASTNodes();
//		
//		CodeBlock codeBlock = new CodeBlock(unit, nodes);
//		
//		System.out.println(codeBlock.getCurrentLine());
//	}
//	
//	private void print(CodeBlock codeBlock){
//		System.out.println("----------------- Constant -----------------");
//		for(Entry<Literal, Integer> entry : codeBlock.getConstants().entrySet()){
//			System.out.println(entry.getKey() + " : " + entry.getValue());
//		}
//		System.out.println("----------------- Variable -----------------");
//		for(Entry<Variable, Integer> entry : codeBlock.getVariables().entrySet()){
//			System.out.println(entry.getKey() + " : " + entry.getValue());
//		}
//		System.out.println("----------------- Structure -----------------");
//		for(Structure structure : codeBlock.getStructures()){
//			System.out.println(structure);
//		}
//		
//		System.out.println("----------------- Operator -----------------");
//		for(Operator operator : codeBlock.getOperators()){
//			System.out.println(operator.toString());
//		}
//		
//		System.out.println("----------------- MethodCall -----------------");
//		for(Entry<MethodCall, Integer> entry : codeBlock.getMethodCalls().entrySet()){
//			System.out.println(entry.getKey() + " : " + entry.getValue());
//		}
//	}
	
}
