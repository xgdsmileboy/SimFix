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
 * @datae Jul 4, 2017
 */
public class ManualLocator extends AbstractFaultlocalization {

	
	private Map<String, Pair<String, Integer>> _faultLocMap = new HashMap<>();
	private Map<String, String> _failedTest = new HashMap<>();
	private void init(){
		_faultLocMap.put("chart_1", new Pair<String, Integer>("org.jfree.chart.renderer.category.AbstractCategoryItemRenderer", 1797));
		_faultLocMap.put("chart_2", new Pair<String, Integer>("org.jfree.data.general.DatasetUtilities", 752));
		_faultLocMap.put("chart_3", new Pair<String, Integer>("org.jfree.data.time.TimeSeries", 1057));
		_faultLocMap.put("chart_7", new Pair<String, Integer>("org.jfree.data.time.TimePeriodValues", 299));
		_faultLocMap.put("chart_11", new Pair<String, Integer>("org.jfree.chart.util.ShapeUtilities", 275));
		_faultLocMap.put("chart_12", new Pair<String, Integer>("org.jfree.chart.plot.MultiplePiePlot", 145));
		_faultLocMap.put("chart_20", new Pair<String, Integer>("org.jfree.chart.plot.ValueMarker", 95));
		_faultLocMap.put("closure_14", new Pair<String, Integer>("com.google.javascript.jscomp.ControlFlowAnalysis", 767));
		_faultLocMap.put("closure_57", new Pair<String, Integer>("com.google.javascript.jscomp.ClosureCodingConvention", 197));
		_faultLocMap.put("closure_73", new Pair<String, Integer>("com.google.javascript.jscomp.CodeGenerator", 1045));
		_faultLocMap.put("closure_77", new Pair<String, Integer>("com.google.javascript.jscomp.CodeGenerator", 966));
		_faultLocMap.put("lang_16", new Pair<String, Integer>("org.apache.commons.lang3.math.NumberUtils", 458));
		_faultLocMap.put("lang_33", new Pair<String, Integer>("org.apache.commons.lang3.ClassUtils", 909));
		
		// need to split test case
//		_faultLocMap.put("lang_35", new Pair<String, Integer>("org.apache.commons.lang3.ArrayUtils", 3292));
		_faultLocMap.put("lang_35", new Pair<String, Integer>("org.apache.commons.lang3.ArrayUtils", 3575));
		
		_faultLocMap.put("lang_39", new Pair<String, Integer>("org.apache.commons.lang3.StringUtils", 3675));
		_faultLocMap.put("lang_43", new Pair<String, Integer>("org.apache.commons.lang.text.ExtendedMessageFormat", 421));
		_faultLocMap.put("lang_58", new Pair<String, Integer>("org.apache.commons.lang.math.NumberUtils", 452));
		_faultLocMap.put("lang_59", new Pair<String, Integer>("org.apache.commons.lang.text.StrBuilder", 884));
		
		// need to split test case
//		_faultLocMap.put("lang_60", new Pair<String, Integer>("org.apache.commons.lang.text.StrBuilder", 1673));
		_faultLocMap.put("lang_60", new Pair<String, Integer>("org.apache.commons.lang.text.StrBuilder", 1730));
		
		_faultLocMap.put("math_5", new Pair<String, Integer>("org.apache.commons.math3.complex.Complex", 304));
		_faultLocMap.put("math_33", new Pair<String, Integer>("org.apache.commons.math3.optimization.linear.SimplexTableau", 338));
		
		// need to avoid other failed test cases (comment others)
		_faultLocMap.put("math_35", new Pair<String, Integer>("org.apache.commons.math3.genetics.ElitisticListPopulation", 51));
		_failedTest.put("math_35", "org.apache.commons.math3.genetics.ElitisticListPopulationTest#testChromosomeListConstructorTooLow");
//		_faultLocMap.put("math_35", new Pair<String, Integer>("org.apache.commons.math3.genetics.ElitisticListPopulation", 51));
//		_failedTest.put("math_35", "org.apache.commons.math3.genetics.ElitisticListPopulationTest#testChromosomeListConstructorTooLow");
		
		_faultLocMap.put("math_41", new Pair<String, Integer>("org.apache.commons.math.stat.descriptive.moment.Variance", 520));
		_faultLocMap.put("math_49", new Pair<String, Integer>("org.apache.commons.math.linear.OpenMapRealVector", 345));
		_faultLocMap.put("math_53", new Pair<String, Integer>("org.apache.commons.math.complex.Complex", 153));
		_faultLocMap.put("math_59", new Pair<String, Integer>("org.apache.commons.math.util.FastMath", 3482));
		_faultLocMap.put("math_63", new Pair<String, Integer>("org.apache.commons.math.util.MathUtils", 417));
		_faultLocMap.put("math_70", new Pair<String, Integer>("org.apache.commons.math.analysis.solvers.BisectionSolver", 72));
		_faultLocMap.put("math_71", new Pair<String, Integer>("org.apache.commons.math.ode.nonstiff.EmbeddedRungeKuttaIntegrator", 294));
		_faultLocMap.put("math_72", new Pair<String, Integer>("org.apache.commons.math.analysis.solvers.BrentSolver", 115));
		_faultLocMap.put("math_75", new Pair<String, Integer>("org.apache.commons.math.stat.Frequency", 303));
		_faultLocMap.put("math_79", new Pair<String, Integer>("org.apache.commons.math.util.MathUtils", 1624));
		_faultLocMap.put("math_98", new Pair<String, Integer>("org.apache.commons.math.linear.BigMatrixImpl", 991));
		
		/*=============================================*/
		_failedTest.put("chart_1", "org.jfree.chart.renderer.category.junit.AbstractCategoryItemRendererTests#test2947660");
		_failedTest.put("chart_2", "org.jfree.data.general.junit.DatasetUtilitiesTests#testBug2849731_2");
		_failedTest.put("chart_3", "org.jfree.data.time.junit.TimeSeriesTests#testCreateCopy3");
		_failedTest.put("chart_7", "org.jfree.data.time.junit.TimePeriodValuesTests#testGetMaxMiddleIndex");
		_failedTest.put("chart_11", "org.jfree.chart.util.junit.ShapeUtilitiesTests#testEqualGeneralPaths");
		_failedTest.put("chart_12", "org.jfree.chart.plot.junit.MultiplePiePlotTests#testConstructor");
		_failedTest.put("chart_20", "org.jfree.chart.plot.junit.ValueMarkerTests#test1808376");
		_failedTest.put("closure_14", "com.google.javascript.jscomp.CheckMissingReturnTest#testIssue779");
		_failedTest.put("closure_57", "com.google.javascript.jscomp.ClosureCodingConventionTest#testRequire");
		_failedTest.put("closure_73", "com.google.javascript.jscomp.CodePrinterTest#testUnicode");
		_failedTest.put("closure_77", "com.google.javascript.jscomp.CodePrinterTest#testZero");
		_failedTest.put("lang_16", "org.apache.commons.lang3.math.NumberUtilsTest#testCreateNumber");
		_failedTest.put("lang_33", "org.apache.commons.lang3.ClassUtilsTest#testToClass_object");
		_failedTest.put("lang_35", "org.apache.commons.lang3.ArrayUtilsAddTest#testLANG571");
		_failedTest.put("lang_39", "org.apache.commons.lang3.StringUtilsTest#testReplace_StringStringArrayStringArray");
		_failedTest.put("lang_43", "org.apache.commons.lang.text.ExtendedMessageFormatTest#testEscapedQuote_LANG_477");
		_failedTest.put("lang_58", "org.apache.commons.lang.math.NumberUtilsTest#testLang300");
		_failedTest.put("lang_59", "org.apache.commons.lang.text.StrBuilderAppendInsertTest#testLang299");
		_failedTest.put("lang_60", "org.apache.commons.lang.text.StrBuilderTest#testLang295");
		_failedTest.put("math_5", "org.apache.commons.math3.complex.ComplexTest#testReciprocalZero");
		_failedTest.put("math_33", "org.apache.commons.math3.optimization.linear.SimplexSolverTest#testMath781");
		
		_failedTest.put("math_41", "org.apache.commons.math.stat.descriptive.moment.VarianceTest#testEvaluateArraySegmentWeighted");
		_failedTest.put("math_49", "org.apache.commons.math.linear.SparseRealVectorTest#testConcurrentModification");
		_failedTest.put("math_53", "org.apache.commons.math.complex.ComplexTest#testAddNaN");
		_failedTest.put("math_59", "org.apache.commons.math.util.FastMathTest#testMinMaxFloat");
		_failedTest.put("math_63", "org.apache.commons.math.util.MathUtilsTest#testArrayEquals");
		_failedTest.put("math_70", "org.apache.commons.math.analysis.solvers.BisectionSolverTest#testMath369");
		_failedTest.put("math_71", "org.apache.commons.math.ode.nonstiff.ClassicalRungeKuttaIntegratorTest#testMissedEndEvent");
		_failedTest.put("math_72", "org.apache.commons.math.analysis.solvers.BrentSolverTest#testRootEndpoints");
		_failedTest.put("math_75", "org.apache.commons.math.stat.FrequencyTest#testPcts");
		_failedTest.put("math_79", "org.apache.commons.math.stat.clustering.KMeansPlusPlusClustererTest#testPerformClusterAnalysisDegenerate");
		_failedTest.put("math_98", "org.apache.commons.math.linear.BigMatrixImplTest#testMath209");
	}

	
	public ManualLocator() {
		super();
		init();
	}
	
	@Override
	public List<Pair<String, Integer>> getLocations(Subject subject) {
		List<Pair<String, Integer>> locs = new ArrayList<>();
		String obj = subject.getName() + "_" + subject.getId();
		if(_faultLocMap.containsKey(obj)){
			locs.add(_faultLocMap.get(obj));
		}
		return locs;
	}

	public String getFailedTest(Subject subject){
		return _failedTest.get(subject.getName() + "_" + subject.getId());
	}
	

	@Override
	public void locateFault(Subject subject, double threshold) {
		// TODO Auto-generated method stub
	}

}
