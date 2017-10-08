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

import cofix.common.localization.alg.Ochiai;
import cofix.common.util.Pair;
import cofix.common.util.Subject;


/**
 * @author Jiajun
 * @date Jun 15, 2017
 */
public class FLocalization extends AbstractFaultlocalization{
	
	private List<Statement> _candidates = null;
	
	public FLocalization(Subject subject) {
		this(subject, 0);
	}
	
	public FLocalization(Subject subject, double threshold) {
		super(subject);
		_candidates = new ArrayList<>();
		locateFault(threshold);
	}
	
	public List<Statement> getSuspiciousStatement(){
		return _candidates;
	}
	
	public List<Pair<String, Integer>> getLocations(int topK){
		List<Pair<String, Integer>> locations = new ArrayList<>();
		int count = 0;
		for(Statement statement : _candidates){
			String stmt = statement.getMethod().getParent().getLabel();
			Integer line = statement.getLineNumber();
			Pair<String, Integer> pair = new Pair<String, Integer>(stmt, line);
			locations.add(pair);
			count ++;
			if(count == topK){
				break;
			}
		}
		return locations;
	}
	
	protected void locateFault(double threshold) {
		GZoltar gz = null;
		try {
			gz = new Locator(_subject.getHome(), new Ochiai());
		} catch (Throwable t) {
			System.err.println(t);
			t.printStackTrace();
		}
		if (gz == null) {
			return;
		}
		ArrayList<String> classpaths = new ArrayList<>();
		classpaths.add(_subject.getHome() + _subject.getSbin());
		classpaths.add(_subject.getHome() + _subject.getTbin());
		gz.setClassPaths(classpaths);

		for (String p : _subject.getInstrumentPackage()) {
			gz.addPackageToInstrument(p);
		}
		
		for (String test : _subject.getTestClasses()) {
			gz.addTestToExecute(test);
			gz.addClassNotToInstrument(test);
		}

		gz.run();

		List<TestResult> test_rslts = gz.getTestResults();
		_total = test_rslts.size();
		for (TestResult tr : test_rslts) {
			if(!tr.wasSuccessful()){
				_failedTests.add(tr.getName());
			} else {
				_passedTests.add(tr.getName());
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
