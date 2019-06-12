/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.localization;

import java.util.ArrayList;
import java.util.List;

import cofix.common.util.Pair;
import cofix.common.util.Subject;

/**
 * @author Jiajun
 * @date Jul 4, 2017
 */
public abstract class AbstractFaultlocalization {
	
	protected Subject _subject = null;
	protected int _total = 0;
	protected List<String> _failedTests = null;
	protected List<String> _passedTests = null;
	
	
	public AbstractFaultlocalization(Subject subject) {
		_subject = subject;
		_failedTests = new ArrayList<>();
		_passedTests = new ArrayList<>();
	}
	
	public int getTotalTestCases(){
		return _total;
	}
	
	public List<String> getPassedTestCases(){
		return _passedTests;
	}
	
	public List<String> getFailedTestCases(){
		return _failedTests;
	}
	
	protected abstract void locateFault(double threshold);
	
	public abstract List<Pair<String, Integer>> getLocations(int topK);
	
}
