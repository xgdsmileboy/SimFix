/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.match;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.Type;
import org.junit.Test;

import cofix.common.config.Constant;
import cofix.common.util.Pair;
import cofix.common.util.Subject;
import cofix.core.modify.Modification;
import cofix.core.parser.NodeUtils;
import cofix.core.parser.ProjectInfo;
import cofix.core.parser.node.CodeBlock;
import cofix.core.parser.search.BuggyCode;
import cofix.core.parser.search.SimpleFilter;

/**
 * @author Jiajun
 * @date May 31, 2017
 */
public class CodeBlockMatcherTest {
	
	@Test
	public void test_chart_1(){
		Constant.PROJECT_HOME = System.getProperty("user.dir") + "/testfile";
		Subject subject = new Subject("chart", 1, "/source", "/tests", "/build", "/build-tests");
		ProjectInfo.init(subject);
		String file = subject.getHome() + subject.getSsrc() +"/org/jfree/chart/renderer/category/AbstractCategoryItemRenderer.java";
		int buggyLine = 1797;
		
		searchAndPrint(file, buggyLine, subject.getHome() + subject.getSsrc());
	}
	
	@Test
	public void test_chart_7(){
		Constant.PROJECT_HOME = System.getProperty("user.dir") + "/testfile";
		Subject subject = new Subject("chart", 7, "/source", "/tests", "/build", "/build-tests");
		ProjectInfo.init(subject);
		String file = subject.getHome() + "/source/org/jfree/data/time/TimePeriodValues.java";
		int buggyLine = 299;
		
		searchAndPrint(file, buggyLine, subject.getHome() + subject.getSsrc());
	}
	
	private void searchAndPrint(String buggyFile, int buggyLine, String searchPath){
		CodeBlock codeBlock = BuggyCode.getBuggyCodeBlock(buggyFile, buggyLine);
		System.out.println(codeBlock.toSrcString());
		System.out.println(codeBlock.getFeatureVector());
		
		Map<String, Type> allUsableVariabes = NodeUtils.getUsableVarTypes(buggyFile, buggyLine);
		
		SimpleFilter simpleFilter = new SimpleFilter(codeBlock);
		List<Pair<CodeBlock, Double>> candidates = simpleFilter.filter(searchPath, 0.5);
		int i = 1;
		for(Pair<CodeBlock, Double> block : candidates){
			System.out.println("---------------- " + (i++) + " ----Similarity : " + block.getSecond() + "-------------------------------------");
			System.out.println(block.getFirst().getFeatureVector());
			System.out.println(block.getFirst().toSrcString());
			
			List<Modification> modifications = CodeBlockMatcher.match(codeBlock, block.getFirst(), allUsableVariabes);
			for(Modification modification : modifications){
				modification.apply(allUsableVariabes);
				System.out.println("====================changed=====================");
				System.out.println(codeBlock.toSrcString().toString());
				modification.restore();
				System.out.println("====================original=====================");
				System.out.println(codeBlock.toSrcString().toString());
			}
//			break;
		}
		System.out.println("-----------" + candidates.size() + "-------------");
	}
	
	
	
	
//	@Test
//	public void test_match(){
//		Constant.PROJECT_HOME = "testfile";
//		Subject subject = new Subject("chart", 1, "/source", "/tests", "/build", "/build-tests");
//		ProjectInfo.init(subject);
//		String file = subject.getHome() + subject.getSsrc() +"/org/jfree/chart/renderer/category/AbstractCategoryItemRenderer.java";
//		int buggyLine = 1797;
//		
//		CompilationUnit unit = (CompilationUnit)JavaFile.genASTFromSource(JavaFile.readFileToString(file), ASTParser.K_COMPILATION_UNIT);
//		CodeBlock codeBlock = BuggyCode.getBuggyCodeBlock(unit, buggyLine);
//		Utils.print(codeBlock);
//		
//		String file_2 = subject.getHome() + subject.getSsrc() +"/org/jfree/chart/plot/MultiplePiePlot.java";
//		int buggyLine_2 = 566;
//		CodeBlock similar = Utils.search(file_2, buggyLine_2, codeBlock.getCurrentLine());
//		Utils.print(similar);
//		
//		Utils.showSimilarity(codeBlock, similar);
//		
//		Map<String, Type> usableVars = NodeUtils.getUsableVarTypes(file, buggyLine);
//		
//		for(Entry<String, Type> entry : usableVars.entrySet()){
//			System.out.println(entry.getKey() + "  " + entry.getValue());
//		}
//		System.out.println("=====================================");
//		List<Modification> modifications = CodeBlockMatcher.match(codeBlock, similar, usableVars);
//		
//		for(Modification modification : modifications){
//			System.out.println(modification);
//			modification.apply(usableVars);
//			Utils.print(codeBlock);
//			modification.restore();
//		}
//		
//		System.out.println(modifications.size());
//		
//	}
//	
//	@Test
//	public void test_chart_1() {
//		Constant.PROJECT_HOME = "testfile";
//		Subject subject = new Subject("chart", 1, "/source", "/tests", "/build", "/build-tests");
//		ProjectInfo.init(subject);
//		String file = subject.getHome() + subject.getSsrc() +"/org/jfree/chart/renderer/category/AbstractCategoryItemRenderer.java";
//		int buggyLine = 1797;
//		
//		CompilationUnit unit = (CompilationUnit) JavaFile.genASTFromSource(JavaFile.readFileToString(file), ASTParser.K_COMPILATION_UNIT);
//		CodeBlock codeBlock = BuggyCode.getBuggyCodeBlock(unit, buggyLine);
//
//		Utils.print(codeBlock);
//		
//		String file_2 = subject.getHome() + subject.getSsrc() +"/org/jfree/chart/plot/MultiplePiePlot.java";
//		int buggyLine_2 = 566;
//		CodeBlock similar = Utils.search(file_2, buggyLine_2, codeBlock.getCurrentLine());
//		Utils.print(similar);
//		
//		Utils.showSimilarity(codeBlock, similar);
//	}
//	
//	@Test
//	public void test_chart_2() {
//		Constant.PROJECT_HOME = "testfile";
//		Subject subject = new Subject("chart", 2, "/source", "/tests", "/build", "/build-tests");
//		ProjectInfo.init(subject);
//		String file = subject.getHome() + subject.getSsrc() + "/org/jfree/data/general/DatasetUtilities.java";
//		int buggyLine = 752;
//
//		CompilationUnit unit = (CompilationUnit) JavaFile.genASTFromSource(JavaFile.readFileToString(file), ASTParser.K_COMPILATION_UNIT);
//		CodeBlock codeBlock = BuggyCode.getBuggyCodeBlock(unit, buggyLine);
//
//		Utils.print(codeBlock);
//		
//		String file_2 = file;
//		int buggyLine_2 = 982;
//		CodeBlock similar = Utils.search(file_2, buggyLine_2, codeBlock.getCurrentLine());
//		Utils.print(similar);
//		
//		Utils.showSimilarity(codeBlock, similar);
//	}
//	
//	@Test
//	public void test_chart_3() {
//		Constant.PROJECT_HOME = "testfile";
//		Subject subject = new Subject("chart", 3, "/source", "/tests", "/build", "/build-tests");
//		ProjectInfo.init(subject);
//		String file = subject.getHome() + subject.getSsrc() + "/org/jfree/data/time/TimeSeries.java";
//		int buggyLine = 1057;
//
//		CompilationUnit unit = (CompilationUnit) JavaFile.genASTFromSource(JavaFile.readFileToString(file), ASTParser.K_COMPILATION_UNIT);
//		CodeBlock codeBlock = BuggyCode.getBuggyCodeBlock(unit, buggyLine);
//
//		Utils.print(codeBlock);
//		
//		String file_2 = file;
//		int buggyLine_2 = 190;
//		CodeBlock similar = Utils.search(file_2, buggyLine_2, codeBlock.getCurrentLine());
//		Utils.print(similar);
//		
//		Utils.showSimilarity(codeBlock, similar);
//	}
//	
//	@Test
//	public void test_chart_7(){
//		Constant.PROJECT_HOME = "testfile";
//		Subject subject = new Subject("chart", 7, "/source", "/tests", "/build", "/build-tests");
//		ProjectInfo.init(subject);
//		String file = subject.getHome() + "/source/org/jfree/data/time/TimePeriodValues.java";
//		int buggyLine = 299;
//
//		CompilationUnit unit = (CompilationUnit) JavaFile.genASTFromSource(JavaFile.readFileToString(file), ASTParser.K_COMPILATION_UNIT);
//		CodeBlock codeBlock = BuggyCode.getBuggyCodeBlock(unit, buggyLine);
//
//		Utils.print(codeBlock);
//		
//		String file_2 = file;
//		int buggyLine_2 = 285;
//		CodeBlock similar = Utils.search(file_2, buggyLine_2, codeBlock.getCurrentLine());
//		Utils.print(similar);
//		
//		Utils.showSimilarity(codeBlock, similar);
//	}
//	
	@Test
	public void test_chart_11(){
		Constant.PROJECT_HOME = "testfile";
		Subject subject = new Subject("chart", 11, "/source", "/tests", "/build", "/build-tests");
		ProjectInfo.init(subject);
		String file = subject.getHome() + subject.getSsrc() + "/org/jfree/chart/util/ShapeUtilities.java";
		int buggyLine = 275;

		searchAndPrint(file, buggyLine, subject.getHome() + subject.getSsrc());
	}
//	
//	@Test
//	public void test_chart_12(){
//		Constant.PROJECT_HOME = "testfile";
//		Subject subject = new Subject("chart", 12, "/source", "/tests", "/build", "/build-tests");
//		ProjectInfo.init(subject);
//		String file = subject.getHome() + subject.getSsrc() + "/org/jfree/chart/plot/MultiplePiePlot.java";
//		int buggyLine = 145;
//
//		CompilationUnit unit = (CompilationUnit) JavaFile.genASTFromSource(JavaFile.readFileToString(file), ASTParser.K_COMPILATION_UNIT);
//		CodeBlock codeBlock = BuggyCode.getBuggyCodeBlock(unit, buggyLine);
//
//		Utils.print(codeBlock);
//
//		String file_2 = file;
//		int buggyLine_2 = 183;
//		CodeBlock similar = Utils.search(file_2, buggyLine_2, codeBlock.getCurrentLine());
//		Utils.print(similar);
//		
//		Utils.showSimilarity(codeBlock, similar);
//	}
//	
	@Test
	public void test_chart_20(){
		Constant.PROJECT_HOME = "testfile";
		Subject subject = new Subject("chart", 20, "/source", "/tests", "/build", "/build-tests");
		ProjectInfo.init(subject);
		String file = subject.getHome() + subject.getSsrc() + "/org/jfree/chart/plot/ValueMarker.java";
		int buggyLine = 95;

		searchAndPrint(file, buggyLine, subject.getHome() + subject.getSsrc());
	}
	
	@Test
	public void test_closure_14(){
		Constant.PROJECT_HOME = "testfile";
		Subject subject = new Subject("closure", 14, "/src", "/test", "/build/classes", "/build/test");
		ProjectInfo.init(subject);
		String file = subject.getHome() + subject.getSsrc() + "/com/google/javascript/jscomp/ControlFlowAnalysis.java";
		int buggyLine = 767;

		searchAndPrint(file, buggyLine, subject.getHome() + subject.getSsrc());
	}
	
	@Test
	public void test_closure_57(){
		Constant.PROJECT_HOME = "testfile";
		Subject subject = new Subject("closure", 57, "/src", "/test", "/build/classes", "/build/test");
		ProjectInfo.init(subject);
		String file = subject.getHome() + subject.getSsrc() + "/com/google/javascript/jscomp/ClosureCodingConvention.java";
		int buggyLine = 197;

		searchAndPrint(file, buggyLine, subject.getHome() + subject.getSsrc());
	}
	
	@Test
	public void test_closure_73(){
		Constant.PROJECT_HOME = "testfile";
		Subject subject = new Subject("closure", 73, "/src", "/test", "/build/classes", "/build/test");
		ProjectInfo.init(subject);
		String file = subject.getHome() + subject.getSsrc() + "/com/google/javascript/jscomp/CodeGenerator.java";
		int buggyLine = 1045;

		searchAndPrint(file, buggyLine, subject.getHome() + subject.getSsrc());
	}
//	
//	@Test
//	public void test_closure_77(){
//		// TODO : switch-case statement, lack of case "case '\0': sb.append("\\0"); break;"
//		Constant.PROJECT_HOME = "testfile";
//		Subject subject = new Subject("closure", 77, "/src", "/test", "/build/classes", "/build/test");
//		ProjectInfo.init(subject);
//		String file = subject.getHome() + subject.getSsrc() + "/com/google/javascript/jscomp/CodeGenerator.java";
//		int buggyLine = 966;
//
//		CompilationUnit unit = (CompilationUnit) JavaFile.genASTFromSource(JavaFile.readFileToString(file), ASTParser.K_COMPILATION_UNIT);
//		CodeBlock codeBlock = BuggyCode.getBuggyCodeBlock(unit, buggyLine);
//
//		Utils.print(codeBlock);
//	}
//	
//	@Test
//	public void test_lang_16(){
//		// TODO : compare with it self, similar to closure-77
//		Constant.PROJECT_HOME = "testfile";
//		Subject subject = new Subject("lang", 16, "/src/main/java", "/src/test/java", "", "");
//		ProjectInfo.init(subject);
//		String file = subject.getHome() + subject.getSsrc() + "/org/apache/commons/lang3/math/NumberUtils.java";
//		int buggyLine = 458;
//
//		CompilationUnit unit = (CompilationUnit) JavaFile.genASTFromSource(JavaFile.readFileToString(file), ASTParser.K_COMPILATION_UNIT);
//		CodeBlock codeBlock = BuggyCode.getBuggyCodeBlock(unit, buggyLine);
//
//		Utils.print(codeBlock);
//	}
//
//	@Test
//	public void test_lang_33(){
//		Constant.PROJECT_HOME = "testfile";
//		Subject subject = new Subject("lang", 33, "/src/main/java", "/src/test/java", "", "");
//		ProjectInfo.init(subject);
//		String file = subject.getHome() + subject.getSsrc() + "/org/apache/commons/lang3/ClassUtils.java";
//		int buggyLine = 909;
//
//		CompilationUnit unit = (CompilationUnit) JavaFile.genASTFromSource(JavaFile.readFileToString(file), ASTParser.K_COMPILATION_UNIT);
//		CodeBlock codeBlock = BuggyCode.getBuggyCodeBlock(unit, buggyLine);
//
//		Utils.print(codeBlock);
//
//		String file_2 = subject.getHome() + subject.getSsrc() + "/org/apache/commons/lang3/ArrayUtils.java";
//		int buggyLine_2 = 2728;
//		CodeBlock similar = Utils.search(file_2, buggyLine_2, codeBlock.getCurrentLine());
//		Utils.print(similar);
//		
//		Utils.showSimilarity(codeBlock, similar);
//	}
//
	@Test
	public void test_lang_35(){
		// TODO : low similarity
		Constant.PROJECT_HOME = "testfile";
		Subject subject = new Subject("lang", 35, "/src/main/java", "/src/test/java", "", "");
		ProjectInfo.init(subject);
		String file = subject.getHome() + subject.getSsrc() + "/org/apache/commons/lang3/ArrayUtils.java";
		int buggyLine = 3292;

		searchAndPrint(file, buggyLine, subject.getHome() + subject.getSsrc());
	}
//
//	@Test
//	public void test_lang_39(){
//		Constant.PROJECT_HOME = "testfile";
//		Subject subject = new Subject("lang", 39, "/src/java", "", "", "");
//		ProjectInfo.init(subject);
//		String file = subject.getHome() + subject.getSsrc() + "/org/apache/commons/lang3/StringUtils.java";
//		int buggyLine = 3675;
//
//		CompilationUnit unit = (CompilationUnit) JavaFile.genASTFromSource(JavaFile.readFileToString(file), ASTParser.K_COMPILATION_UNIT);
//		CodeBlock codeBlock = BuggyCode.getBuggyCodeBlock(unit, buggyLine);
//
//		Utils.print(codeBlock);
//
//		String file_2 = file;
//		int buggyLine_2 = 3644;
//		CodeBlock similar = Utils.search(file_2, buggyLine_2, codeBlock.getCurrentLine());
//		Utils.print(similar);
//		
//		Utils.showSimilarity(codeBlock, similar);
//	}
//
	@Test
	public void test_lang_43(){
		Constant.PROJECT_HOME = "testfile";
		Subject subject = new Subject("lang", 43, "/src/java", "", "", "");
		ProjectInfo.init(subject);
		String file = subject.getHome() + subject.getSsrc() + "/org/apache/commons/lang/text/ExtendedMessageFormat.java";
		int buggyLine = 421;

		searchAndPrint(file, buggyLine, subject.getHome() + subject.getSsrc());
	}

	@Test
	public void test_lang_58(){
		// TODO : switch case , should be completed
		Constant.PROJECT_HOME = "testfile";
		Subject subject = new Subject("lang", 58, "/src/java", "", "", "");
		ProjectInfo.init(subject);
		String file = subject.getHome() + subject.getSsrc() + "/org/apache/commons/lang/math/NumberUtils.java";
		int buggyLine = 452;

		searchAndPrint(file, buggyLine, subject.getHome() + subject.getSsrc());
	}
	

	@Test
	public void test_lang_59(){
		Constant.PROJECT_HOME = "testfile";
		Subject subject = new Subject("lang", 59, "/src/java", "", "", "");
		ProjectInfo.init(subject);
		String file = subject.getHome() + subject.getSsrc() + "/org/apache/commons/lang/text/StrBuilder.java";
		int buggyLine = 884;

		searchAndPrint(file, buggyLine, subject.getHome() + subject.getSsrc());
	}
//
//	@Test
//	public void test_lang_60(){
//		Constant.PROJECT_HOME = "testfile";
//		Subject subject = new Subject("lang", 60, "/src/java", "", "", "");
//		ProjectInfo.init(subject);
//		String file = subject.getHome() + subject.getSsrc() + "/org/apache/commons/lang/text/StrBuilder.java";
//		int buggyLine = 1673;
//
//		CompilationUnit unit = (CompilationUnit) JavaFile.genASTFromSource(JavaFile.readFileToString(file), ASTParser.K_COMPILATION_UNIT);
//		CodeBlock codeBlock = BuggyCode.getBuggyCodeBlock(unit, buggyLine);
//
//		Utils.print(codeBlock);
//
//		String file_2 = file;
//		int buggyLine_2 = 1167;
//		CodeBlock similar = Utils.search(file_2, buggyLine_2, codeBlock.getCurrentLine());
//		Utils.print(similar);
//		
//		Utils.showSimilarity(codeBlock, similar);
//	}
//
	@Test
	public void test_math_5(){
		Constant.PROJECT_HOME = "testfile";
		Subject subject = new Subject("math", 5, "/src/main/java", "", "", "");
		ProjectInfo.init(subject);
		String file = subject.getHome() + subject.getSsrc() + "/org/apache/commons/math3/complex/Complex.java";
		int buggyLine = 304;

		searchAndPrint(file, buggyLine, subject.getHome() + subject.getSsrc());
	}

	@Test
	public void test_math_33(){
		Constant.PROJECT_HOME = "testfile";
		Subject subject = new Subject("math", 33, "/src/main/java", "", "", "");
		ProjectInfo.init(subject);
		String file = subject.getHome() + subject.getSsrc() + "/org/apache/commons/math3/optimization/linear/SimplexTableau.java";
		int buggyLine = 338;

		searchAndPrint(file, buggyLine, subject.getHome() + subject.getSsrc());
	}
//
//	@Test
//	public void test_math_35(){
//		Constant.PROJECT_HOME = "testfile";
//		Subject subject = new Subject("math", 35, "/src/main/java", "", "", "");
//		ProjectInfo.init(subject);
//		String file = subject.getHome() + subject.getSsrc() + "/org/apache/commons/math3/genetics/ElitisticListPopulation.java";
//		int buggyLine = 51;
//
//		CompilationUnit unit = (CompilationUnit) JavaFile.genASTFromSource(JavaFile.readFileToString(file), ASTParser.K_COMPILATION_UNIT);
//		CodeBlock codeBlock = BuggyCode.getBuggyCodeBlock(unit, buggyLine);
//
//		Utils.print(codeBlock);
//		
//
//		String file_2 = file;
//		int buggyLine_2 = 101;
//		CodeBlock similar = Utils.search(file_2, buggyLine_2, codeBlock.getCurrentLine());
//		Utils.print(similar);
//		
//		Utils.showSimilarity(codeBlock, similar);
//	}
//
//	@Test
//	public void test_math_41(){
//		Constant.PROJECT_HOME = "testfile";
//		Subject subject = new Subject("math", 41, "/src/main/java", "", "", "");
//		ProjectInfo.init(subject);
//		String file = subject.getHome() + subject.getSsrc() + "/org/apache/commons/math/stat/descriptive/moment/Variance.java";
//		int buggyLine = 520;
//
//		CompilationUnit unit = (CompilationUnit) JavaFile.genASTFromSource(JavaFile.readFileToString(file), ASTParser.K_COMPILATION_UNIT);
//		CodeBlock codeBlock = BuggyCode.getBuggyCodeBlock(unit, buggyLine);
//
//		Utils.print(codeBlock);
//
//		String file_2 = file;
//		int buggyLine_2 = 513;
//		CodeBlock similar = Utils.search(file_2, buggyLine_2, codeBlock.getCurrentLine());
//		Utils.print(similar);
//		
//		Utils.showSimilarity(codeBlock, similar);
//	}
//
//	@Test
//	public void test_math_49(){
//		Constant.PROJECT_HOME = "testfile";
//		Subject subject = new Subject("math", 49, "/src/main/java", "", "", "");
//		ProjectInfo.init(subject);
//		String file = subject.getHome() + subject.getSsrc() + "/org/apache/commons/math/linear/OpenMapRealVector.java";
//		int buggyLine = 345;
//
//		CompilationUnit unit = (CompilationUnit) JavaFile.genASTFromSource(JavaFile.readFileToString(file), ASTParser.K_COMPILATION_UNIT);
//		CodeBlock codeBlock = BuggyCode.getBuggyCodeBlock(unit, buggyLine);
//
//		Utils.print(codeBlock);
//		
//		String file_2 = file;
//		int buggyLine_2 = 397;
//		CodeBlock similar = Utils.search(file_2, buggyLine_2, codeBlock.getCurrentLine());
//		Utils.print(similar);
//		
//		Utils.showSimilarity(codeBlock, similar);
//	}
//
//	@Test
//	public void test_math_53(){
//		Constant.PROJECT_HOME = "testfile";
//		Subject subject = new Subject("math", 53, "/src/main/java", "", "", "");
//		ProjectInfo.init(subject);
//		String file = subject.getHome() + subject.getSsrc() + "/org/apache/commons/math/complex/Complex.java";
//		int buggyLine = 153;
//
//		CompilationUnit unit = (CompilationUnit) JavaFile.genASTFromSource(JavaFile.readFileToString(file), ASTParser.K_COMPILATION_UNIT);
//		CodeBlock codeBlock = BuggyCode.getBuggyCodeBlock(unit, buggyLine);
//
//		Utils.print(codeBlock);
//
//		String file_2 = file;
//		int buggyLine_2 = 456;
//		CodeBlock similar = Utils.search(file_2, buggyLine_2, codeBlock.getCurrentLine());
//		Utils.print(similar);
//		
//		Utils.showSimilarity(codeBlock, similar);
//	}
//
	@Test
	public void test_math_59(){
		Constant.PROJECT_HOME = "testfile";
		Subject subject = new Subject("math", 59, "/src/main/java", "", "", "");
		ProjectInfo.init(subject);
		String file = subject.getHome() + subject.getSsrc() + "/org/apache/commons/math/util/FastMath.java";
		int buggyLine = 3482;

		searchAndPrint(file, buggyLine, subject.getHome() + subject.getSsrc());
	}
//
//	@Test
//	public void test_math_63(){
//		Constant.PROJECT_HOME = "testfile";
//		Subject subject = new Subject("math", 63, "/src/main/java", "", "", "");
//		ProjectInfo.init(subject);
//		String file = subject.getHome() + subject.getSsrc() + "/org/apache/commons/math/util/MathUtils.java";
//		int buggyLine = 417;
//
//		CompilationUnit unit = (CompilationUnit) JavaFile.genASTFromSource(JavaFile.readFileToString(file), ASTParser.K_COMPILATION_UNIT);
//		CodeBlock codeBlock = BuggyCode.getBuggyCodeBlock(unit, buggyLine);
//
//		Utils.print(codeBlock);
//
//		String file_2 = file;
//		int buggyLine_2 = 442;
//		CodeBlock similar = Utils.search(file_2, buggyLine_2, codeBlock.getCurrentLine());
//		Utils.print(similar);
//		
//		Utils.showSimilarity(codeBlock, similar);
//	}

	@Test
	public void test_math_70(){
		Constant.PROJECT_HOME = "testfile";
		Subject subject = new Subject("math", 70, "/src/main/java", "", "", "");
		ProjectInfo.init(subject);
		String file = subject.getHome() + subject.getSsrc() + "/org/apache/commons/math/analysis/solvers/BisectionSolver.java";
		int buggyLine = 72;

		searchAndPrint(file, buggyLine, subject.getHome() + subject.getSsrc());
	}
//	
//	@Test
//	public void test_math_71(){
//		Constant.PROJECT_HOME = "testfile";
//		Subject subject = new Subject("math", 71, "/src/main/java", "", "", "");
//		ProjectInfo.init(subject);
//		String file = subject.getHome() + subject.getSsrc() + "/org/apache/commons/math/ode/nonstiff/EmbeddedRungeKuttaIntegrator.java";
//		int buggyLine = 294;
//
//		CompilationUnit unit = (CompilationUnit) JavaFile.genASTFromSource(JavaFile.readFileToString(file), ASTParser.K_COMPILATION_UNIT);
//		CodeBlock codeBlock = BuggyCode.getBuggyCodeBlock(unit, buggyLine);
//
//		Utils.print(codeBlock);
//
//		String file_2 = subject.getHome() + subject.getSsrc() + "/org/apache/commons/math/ode/nonstiff/AdamsMoultonIntegrator.java";
//		int buggyLine_2 = 291;
//		CodeBlock similar = Utils.search(file_2, buggyLine_2, codeBlock.getCurrentLine());
//		Utils.print(similar);
//		
//		Utils.showSimilarity(codeBlock, similar);
//	}
//
//	@Test
//	public void test_math_72(){
//		Constant.PROJECT_HOME = "testfile";
//		Subject subject = new Subject("math", 72, "/src/main/java", "", "", "");
//		ProjectInfo.init(subject);
//		String file = subject.getHome() + subject.getSsrc() + "/org/apache/commons/math/analysis/solvers/BrentSolver.java";
//		int buggyLine = 115;
//
//		CompilationUnit unit = (CompilationUnit) JavaFile.genASTFromSource(JavaFile.readFileToString(file), ASTParser.K_COMPILATION_UNIT);
//		CodeBlock codeBlock = BuggyCode.getBuggyCodeBlock(unit, buggyLine);
//
//		Utils.print(codeBlock);
//		
//		String file_2 = file;
//		int buggyLine_2 = 181;
//		CodeBlock similar = Utils.search(file_2, buggyLine_2, codeBlock.getCurrentLine());
//		Utils.print(similar);
//		
//		Utils.showSimilarity(codeBlock, similar);
//	}
//
//	@Test
//	public void test_math_75(){
//		Constant.PROJECT_HOME = "testfile";
//		Subject subject = new Subject("math", 75, "/src/main/java", "", "", "");
//		ProjectInfo.init(subject);
//		String file = subject.getHome() + subject.getSsrc() + "/org/apache/commons/math/stat/Frequency.java";
//		int buggyLine = 303;
//
//		CompilationUnit unit = (CompilationUnit) JavaFile.genASTFromSource(JavaFile.readFileToString(file), ASTParser.K_COMPILATION_UNIT);
//		CodeBlock codeBlock = BuggyCode.getBuggyCodeBlock(unit, buggyLine);
//
//		Utils.print(codeBlock);
//		
//		String file_2 = file;
//		int buggyLine_2 = 342;
//		CodeBlock similar = Utils.search(file_2, buggyLine_2, codeBlock.getCurrentLine());
//		Utils.print(similar);
//		
//		Utils.showSimilarity(codeBlock, similar);
//	}
//
//	@Test
//	public void test_math_79(){
//		Constant.PROJECT_HOME = "testfile";
//		Subject subject = new Subject("math", 79, "/src/main/java", "", "", "");
//		ProjectInfo.init(subject);
//		String file = subject.getHome() + subject.getSsrc() + "/org/apache/commons/math/util/MathUtils.java";
//		int buggyLine = 1626;
//
//		CompilationUnit unit = (CompilationUnit) JavaFile.genASTFromSource(JavaFile.readFileToString(file), ASTParser.K_COMPILATION_UNIT);
//		CodeBlock codeBlock = BuggyCode.getBuggyCodeBlock(unit, buggyLine);
//
//		Utils.print(codeBlock);
//		
//		String file_2 = file;
//		int buggyLine_2 = 1734;
//		CodeBlock similar = Utils.search(file_2, buggyLine_2, codeBlock.getCurrentLine());
//		Utils.print(similar);
//		
//		Utils.showSimilarity(codeBlock, similar);
//	}
//
//	@Test
//	public void test_math_98(){
//		Constant.PROJECT_HOME = "testfile";
//		Subject subject = new Subject("math", 98, "/src/java", "", "", "");
//		ProjectInfo.init(subject);
//		String file = subject.getHome() + subject.getSsrc() + "/org/apache/commons/math/linear/BigMatrixImpl.java";
//		int buggyLine = 991;
//
//		CompilationUnit unit = (CompilationUnit) JavaFile.genASTFromSource(JavaFile.readFileToString(file), ASTParser.K_COMPILATION_UNIT);
//		CodeBlock codeBlock = BuggyCode.getBuggyCodeBlock(unit, buggyLine);
//
//		Utils.print(codeBlock);
//		
//		String file_2 = subject.getHome() + subject.getSsrc() + "/org/apache/commons/math/linear/RealMatrixImpl.java";
//		int buggyLine_2 = 802;
//		CodeBlock similar = Utils.search(file_2, buggyLine_2, codeBlock.getCurrentLine());
//		Utils.print(similar);
//		
//		Utils.showSimilarity(codeBlock, similar);
//	}
	
}
