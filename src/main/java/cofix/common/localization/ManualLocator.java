/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.localization;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cofix.common.util.Pair;
import cofix.common.util.Subject;

/**
 * @author Jiajun
 * @date Jul 4, 2017
 */
public class ManualLocator extends AbstractFaultlocalization {

	
	private Map<String, Pair<String, Integer>> _faultLocMap = new HashMap<>();
	private Map<String, String> _failedTestMap = new HashMap<>();
	private void init(){
		/**================================================================================================================*/
		/*--------------------------------CHART----------------------------------------*/
		_faultLocMap.put("chart_1", new Pair<String, Integer>("org.jfree.chart.renderer.category.AbstractCategoryItemRenderer", 1797));
		_failedTestMap.put("chart_1", "org.jfree.chart.renderer.category.junit.AbstractCategoryItemRendererTests::test2947660");
		
		_faultLocMap.put("chart_2", new Pair<String, Integer>("org.jfree.data.general.DatasetUtilities", 752));
		_failedTestMap.put("chart_2", "org.jfree.data.general.junit.DatasetUtilitiesTests::testBug2849731_2");
		
		_faultLocMap.put("chart_3", new Pair<String, Integer>("org.jfree.data.time.TimeSeries", 1057));
		_failedTestMap.put("chart_3", "org.jfree.data.time.junit.TimeSeriesTests::testCreateCopy3");
		
		_faultLocMap.put("chart_7", new Pair<String, Integer>("org.jfree.data.time.TimePeriodValues", 299));
		_failedTestMap.put("chart_7", "org.jfree.data.time.junit.TimePeriodValuesTests::testGetMaxMiddleIndex");
		
		_faultLocMap.put("chart_11", new Pair<String, Integer>("org.jfree.chart.util.ShapeUtilities", 275));
		_failedTestMap.put("chart_11", "org.jfree.chart.util.junit.ShapeUtilitiesTests::testEqualGeneralPaths");
		
		_faultLocMap.put("chart_12", new Pair<String, Integer>("org.jfree.chart.plot.MultiplePiePlot", 145));
		_failedTestMap.put("chart_12", "org.jfree.chart.plot.junit.MultiplePiePlotTests::testConstructor");
		
		_faultLocMap.put("chart_20", new Pair<String, Integer>("org.jfree.chart.plot.ValueMarker", 95));
		_failedTestMap.put("chart_20", "org.jfree.chart.plot.junit.ValueMarkerTests::test1808376");
		
		/**================================================================================================================*/
		/*--------------------------------CLOSURE----------------------------------------*/
		_faultLocMap.put("closure_14", new Pair<String, Integer>("com.google.javascript.jscomp.ControlFlowAnalysis", 767));
		_failedTestMap.put("closure_14", "com.google.javascript.jscomp.CheckMissingReturnTest::testIssue779");
		
		_faultLocMap.put("closure_57", new Pair<String, Integer>("com.google.javascript.jscomp.ClosureCodingConvention", 197));
		_failedTestMap.put("closure_57", "com.google.javascript.jscomp.ClosureCodingConventionTest::testRequire");
		
		_faultLocMap.put("closure_73", new Pair<String, Integer>("com.google.javascript.jscomp.CodeGenerator", 1045));
		_failedTestMap.put("closure_73", "com.google.javascript.jscomp.CodePrinterTest::testUnicode");
		
		_faultLocMap.put("closure_77", new Pair<String, Integer>("com.google.javascript.jscomp.CodeGenerator", 966));
		_failedTestMap.put("closure_77", "com.google.javascript.jscomp.CodePrinterTest::testZero");
		
		/**================================================================================================================*/
		/*--------------------------------LANG----------------------------------------*/
		_faultLocMap.put("lang_16", new Pair<String, Integer>("org.apache.commons.lang3.math.NumberUtils", 458));
		_failedTestMap.put("lang_16", "org.apache.commons.lang3.math.NumberUtilsTest::testCreateNumber");
		
		_faultLocMap.put("lang_33", new Pair<String, Integer>("org.apache.commons.lang3.ClassUtils", 909));
		_failedTestMap.put("lang_33", "org.apache.commons.lang3.ClassUtilsTest::testToClass_object");
		
		// need to split test case
		// first one : triggered by
		// String[] sa = ArrayUtils.add(stringArray, aString);
		_faultLocMap.put("lang_35", new Pair<String, Integer>("org.apache.commons.lang3.ArrayUtils", 3292));
		// second one : triggered by
		// String[] sa = ArrayUtils.add(stringArray, 0, aString);
//		_faultLocMap.put("lang_35", new Pair<String, Integer>("org.apache.commons.lang3.ArrayUtils", 3575));
		_failedTestMap.put("lang_35", "org.apache.commons.lang3.ArrayUtilsAddTest::testLANG571");
		
		_faultLocMap.put("lang_39", new Pair<String, Integer>("org.apache.commons.lang3.StringUtils", 3675));
		_failedTestMap.put("lang_39", "org.apache.commons.lang3.StringUtilsTest::testReplace_StringStringArrayStringArray");
		
		_faultLocMap.put("lang_43", new Pair<String, Integer>("org.apache.commons.lang.text.ExtendedMessageFormat", 421));
		_failedTestMap.put("lang_43", "org.apache.commons.lang.text.ExtendedMessageFormatTest::testEscapedQuote_LANG_477");
		
		_faultLocMap.put("lang_58", new Pair<String, Integer>("org.apache.commons.lang.math.NumberUtils", 452));
		_failedTestMap.put("lang_58", "org.apache.commons.lang.math.NumberUtilsTest::testLang300");
		
		_faultLocMap.put("lang_59", new Pair<String, Integer>("org.apache.commons.lang.text.StrBuilder", 884));
		_failedTestMap.put("lang_59", "org.apache.commons.lang.text.StrBuilderAppendInsertTest::testLang299");
		
		// need to split test case
		// first one: triggered by
		// assertFalse( "The contains(char) method is looking beyond the end of the string", sb.contains('h'));
		_faultLocMap.put("lang_60", new Pair<String, Integer>("org.apache.commons.lang.text.StrBuilder", 1673));
		// second one: triggered by 
		// assertEquals( "The indexOf(char) method is looking beyond the end of the string", -1, sb.indexOf('h'));
//		_faultLocMap.put("lang_60", new Pair<String, Integer>("org.apache.commons.lang.text.StrBuilder", 1730));
		_failedTestMap.put("lang_60", "org.apache.commons.lang.text.StrBuilderTest::testLang295");
		
		/**================================================================================================================*/
		/*--------------------------------MATH----------------------------------------*/
		_faultLocMap.put("math_5", new Pair<String, Integer>("org.apache.commons.math3.complex.Complex", 304));
		_failedTestMap.put("math_5", "org.apache.commons.math3.complex.ComplexTest::testReciprocalZero");
		
		_faultLocMap.put("math_33", new Pair<String, Integer>("org.apache.commons.math3.optimization.linear.SimplexTableau", 338));
		_failedTestMap.put("math_33", "org.apache.commons.math3.optimization.linear.SimplexSolverTest::testMath781");
		
		// need to avoid other failed test cases (comment others)
		// triggered by test cases: 
		// org.apache.commons.math3.genetics.ElitisticListPopulationTest::testChromosomeListConstructorTooLow &
		// org.apache.commons.math3.genetics.ElitisticListPopulationTest::testChromosomeListConstructorTooHigh
		_faultLocMap.put("math_35", new Pair<String, Integer>("org.apache.commons.math3.genetics.ElitisticListPopulation", 51));
		_failedTestMap.put("math_35", "org.apache.commons.math3.genetics.ElitisticListPopulationTest::testChromosomeListConstructorTooLow");
		// triggered by test cases:
		// org.apache.commons.math3.genetics.ElitisticListPopulationTest::testConstructorTooLow &
		// org.apache.commons.math3.genetics.ElitisticListPopulationTest::testConstructorTooHigh
//		_faultLocMap.put("math_35", new Pair<String, Integer>("org.apache.commons.math3.genetics.ElitisticListPopulation", 65));
//		_failedTest.put("math_35", "org.apache.commons.math3.genetics.ElitisticListPopulationTest::testConstructorTooHigh");
		
		_faultLocMap.put("math_41", new Pair<String, Integer>("org.apache.commons.math.stat.descriptive.moment.Variance", 520));
		_failedTestMap.put("math_41", "org.apache.commons.math.stat.descriptive.moment.VarianceTest::testEvaluateArraySegmentWeighted");
		
		// need to spit test case
		// first one: for [org.apache.commons.math.linear.SparseRealVectorTest::testConcurrentModification && w = u.ebeDivide(v1);]
		_faultLocMap.put("math_49", new Pair<String, Integer>("org.apache.commons.math.linear.OpenMapRealVector", 345));
//		// second one: for [org.apache.commons.math.linear.SparseRealVectorTest::testConcurrentModification && w = u.ebeDivide(v2);]
//		_faultLocMap.put("math_49", new Pair<String, Integer>("org.apache.commons.math.linear.OpenMapRealVector", 358));
//		// third one: for [org.apache.commons.math.linear.SparseRealVectorTest::testConcurrentModification && w = u.ebeMultiply(v1);]
//		_faultLocMap.put("math_49", new Pair<String, Integer>("org.apache.commons.math.linear.OpenMapRealVector", 370));
//		// forth one: for [org.apache.commons.math.linear.SparseRealVectorTest::testConcurrentModification && w = u.ebeMultiply(v2);]
//		_faultLocMap.put("math_49", new Pair<String, Integer>("org.apache.commons.math.linear.OpenMapRealVector", 383));
		_failedTestMap.put("math_49", "org.apache.commons.math.linear.SparseRealVectorTest::testConcurrentModification");
		
		_faultLocMap.put("math_53", new Pair<String, Integer>("org.apache.commons.math.complex.Complex", 153));
		_failedTestMap.put("math_53", "org.apache.commons.math.complex.ComplexTest::testAddNaN");
		
		_faultLocMap.put("math_59", new Pair<String, Integer>("org.apache.commons.math.util.FastMath", 3482));
		_failedTestMap.put("math_59", "org.apache.commons.math.util.FastMathTest::testMinMaxFloat");
		
		_faultLocMap.put("math_63", new Pair<String, Integer>("org.apache.commons.math.util.MathUtils", 417));
		_failedTestMap.put("math_63", "org.apache.commons.math.util.MathUtilsTest::testArrayEquals");
		
		_faultLocMap.put("math_70", new Pair<String, Integer>("org.apache.commons.math.analysis.solvers.BisectionSolver", 72));
		_failedTestMap.put("math_70", "org.apache.commons.math.analysis.solvers.BisectionSolverTest::testMath369");
		
		// need to avoid other failed test cases (comment others)
		// triggered by test case:
		// org.apache.commons.math.ode.nonstiff.ClassicalRungeKuttaIntegratorTest::testMissedEndEvent
//		_faultLocMap.put("math_71", new Pair<String, Integer>("org.apache.commons.math.ode.nonstiff.RungeKuttaIntegrator", 174));
//		_failedTest.put("math_71", "org.apache.commons.math.ode.nonstiff.ClassicalRungeKuttaIntegratorTest::testMissedEndEvent");
		// triggered by test case:
		// org.apache.commons.math.ode.nonstiff.DormandPrince853IntegratorTest::testMissedEndEvent
		_faultLocMap.put("math_71", new Pair<String, Integer>("org.apache.commons.math.ode.nonstiff.EmbeddedRungeKuttaIntegrator", 294));
		_failedTestMap.put("math_71", "org.apache.commons.math.ode.nonstiff.DormandPrince853IntegratorTest::testMissedEndEvent");
		
		// triggered by test case:
		// result = solver.solve(f, Math.PI, 4, 3.5);
//		_faultLocMap.put("math_72", new Pair<String, Integer>("org.apache.commons.math.analysis.solvers.BrentSolver", 115));
		// triggered by Test case:
		// result = solver.solve(f, 3, Math.PI, 3.07);
		_faultLocMap.put("math_72", new Pair<String, Integer>("org.apache.commons.math.analysis.solvers.BrentSolver", 127));
		_failedTestMap.put("math_72", "org.apache.commons.math.analysis.solvers.BrentSolverTest::testRootEndpoints");
		
		_faultLocMap.put("math_75", new Pair<String, Integer>("org.apache.commons.math.stat.Frequency", 303));
		_failedTestMap.put("math_75", "org.apache.commons.math.stat.FrequencyTest::testPcts");
		
		_faultLocMap.put("math_79", new Pair<String, Integer>("org.apache.commons.math.util.MathUtils", 1624));
		_failedTestMap.put("math_79", "org.apache.commons.math.stat.clustering.KMeansPlusPlusClustererTest::testPerformClusterAnalysisDegenerate");
		
		// two failed test cases
		// first one, triggered by :
		// org.apache.commons.math.linear.BigMatrixImplTest::testMath209
		_faultLocMap.put("math_98", new Pair<String, Integer>("org.apache.commons.math.linear.BigMatrixImpl", 991));
		_failedTestMap.put("math_98", "org.apache.commons.math.linear.BigMatrixImplTest::testMath209");
		// second one, triggered by :
		// org.apache.commons.math.linear.RealMatrixImplTest::testMath209
//		_faultLocMap.put("math_98", new Pair<String, Integer>("org.apache.commons.math.linear.RealMatrixImpl", 779));
//		_failedTest.put("math_98", "org.apache.commons.math.linear.RealMatrixImplTest::testMath209");
	}

	
	public ManualLocator(Subject subject) {
		super(subject);
		init();
		locateFault(0);
	}
	
	@Override
	public List<Pair<String, Integer>> getLocations(int topK) {
		List<Pair<String, Integer>> locs = new ArrayList<>();
		String obj = _subject.getName() + "_" + _subject.getId();
		if(_faultLocMap.containsKey(obj)){
			locs.add(_faultLocMap.get(obj));
		}
		return locs;
	}


	@Override
	protected void locateFault(double threshold) {
		_failedTests.add(_failedTestMap.get(_subject.getName() + "_" + _subject.getId()));
	}

}
