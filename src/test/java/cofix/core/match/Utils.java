/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.match;

/**
 * @author Jiajun
 * @date May 31, 2017
 */
public class Utils {

//	public static CodeBlock search(String file, int buggyLine, int lineRange){
//		CompilationUnit unit = (CompilationUnit) JavaFile.genASTFromSource(JavaFile.readFileToString(file), ASTParser.K_COMPILATION_UNIT);
//		CodeSearch codeSearch = new CodeSearch(unit, buggyLine, lineRange);
//		List<ASTNode> nodes = codeSearch.getASTNodes();
//		CodeBlock codeBlock = new CodeBlock(unit, nodes);
//		return codeBlock;
//	}
//	
//	public static void print(CodeBlock codeBlock){
//		for(ASTNode node : codeBlock.getNodes()){
//			System.out.println(node.toString());
//		}
////		System.out.println("====================================================================");
////		System.out.println("----------------- Constant -----------------");
////		for(Entry<Literal, Integer> entry : codeBlock.getConstants().entrySet()){
////			System.out.println(entry.getKey() + " : " + entry.getValue());
////		}
////		System.out.println("----------------- Variable -----------------");
////		for(Entry<Variable, Integer> entry : codeBlock.getVariables().entrySet()){
////			System.out.println(entry.getKey() + " : " + entry.getValue());
////		}
////		System.out.println("----------------- Structure -----------------");
////		for(Structure structure : codeBlock.getStructures()){
////			System.out.println(structure);
////		}
////		
////		System.out.println("----------------- Operator -----------------");
////		for(Operator operator : codeBlock.getOperators()){
////			System.out.println(operator.toString());
////		}
////		
////		System.out.println("----------------- MethodCall -----------------");
////		for(Entry<MethodCall, Integer> entry : codeBlock.getMethodCalls().entrySet()){
////			System.out.println(entry.getKey() + " : " + entry.getValue());
////		}
////		System.out.println("====================================================================");
//	}
//	
//	public static float computeSimilarity(CodeBlock source, CodeBlock similar){
//		VariableMetric variableMetric = new VariableMetric(.2f);
//		LiteralMetric literalMetric = new LiteralMetric(.1f);
//		StructrueMetric structrueMetric = new StructrueMetric(.3f);
//		OperatorMetric operatorMetric = new OperatorMetric(.2f);
//		MethodMetric methodMetric = new MethodMetric(.2f);
//		List<Metric> metrics = new ArrayList<>();
//		metrics.add(variableMetric);
//		metrics.add(literalMetric);
//		metrics.add(structrueMetric);
//		metrics.add(operatorMetric);
//		metrics.add(methodMetric);
//		CodeBlockMatcher codeBlockMatcher = new CodeBlockMatcher(metrics);
//		return codeBlockMatcher.getSimilirity(source, similar);
//	}
//	
//	public static void showSimilarity(CodeBlock source, CodeBlock similar){
//		System.out.println("\n****************** " + computeSimilarity(source, similar) + " ******************\n");
//		System.out.println("------------------------------------------------------------------------------------------");
//	}
	
}
