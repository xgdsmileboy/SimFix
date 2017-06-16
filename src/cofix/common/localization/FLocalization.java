/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.localization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.gzoltar.core.GZoltar;
import com.gzoltar.core.components.Statement;
import com.gzoltar.core.instr.testing.TestResult;

import cofix.common.util.Pair;
import cofix.common.util.Subject;


/**
 * @author Jiajun
 * @datae Jun 15, 2017
 */
public class FLocalization {
	
	private int _totalTest = 0;
	private int _failedTest = 0;
	private List<Pair<String, String>> _failedTrace = null;
	private List<Statement> _candidates = null;
	
	public FLocalization() {
		_failedTrace = new ArrayList<>();
		_candidates = new ArrayList<>();
	}
	
	public int getTotalTestCases(){
		return _totalTest;
	}
	
	public int getFailedTestCases(){
		return _failedTest;
	}
	
	public List<Pair<String, String>> getFailedTestInfo(){
		return _failedTrace;
	}
	
	public List<Statement> getSuspiciousStatement(){
		return _candidates;
	}
	
	public void locateFault(Subject subject, double threshold) {
		GZoltar gz = null;
		try {
			gz = new GZoltar(subject.getHome());
		} catch (Throwable t) {
			System.err.println(t);
			t.printStackTrace();
		}
		if (gz == null) {
			return;
		}
		ArrayList<String> classpaths = new ArrayList<>();
		classpaths.add(subject.getHome() + subject.getSbin());
		classpaths.add(subject.getHome() + subject.getTbin());
		gz.setClassPaths(classpaths);

		for (String p : subject.getInstrumentPackage()) {
			gz.addPackageToInstrument(p);
		}
		
		for (String test : subject.getTestClasses()) {
			gz.addTestToExecute(test);
			gz.addClassNotToInstrument(test);
		}

		gz.run();

		List<TestResult> test_rslts = gz.getTestResults();
		for (TestResult tr : test_rslts) {
			_totalTest++;
			if(!tr.wasSuccessful()){
				_failedTest ++;
				_failedTrace.add(new Pair<String, String>(tr.getName(), tr.getTrace()));
			}
		}
		
		for (Statement s : gz.getSuspiciousStatements()) {
			double susp = s.getSuspiciousness();
			if (susp > threshold) {
				_candidates.add(s);
			}
		}

		Collections.sort(_candidates, new Comparator<Statement>() {

			@Override
			public int compare(Statement o1, Statement o2) {
				if (o1 == null || o2 == null) {
					return 0;
				}
				return Double.compare(o2.getSuspiciousness(), o1.getSuspiciousness());
			}
		});
	}
}
