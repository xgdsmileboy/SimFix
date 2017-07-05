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

/**
 * @author Jiajun
 * @datae Jul 4, 2017
 */
public abstract class AbstractFaultlocalization {
	
	protected int _totalTest = 0;
	protected int _failedTest = 0;
	protected Map<String, String> _failedTrace = null;
	protected List<String> _passedTests = null;
	
	
	public AbstractFaultlocalization() {
		_failedTrace = new HashMap<String, String>();
		_passedTests = new ArrayList<>();
	}
	
	public int getTotalTestCases(){
		return _totalTest;
	}
	
	public int getFailedTestCases(){
		return _failedTest;
	}
	
	public List<String> getPassedTestCases(){
		return _passedTests;
	}
	
	public Map<String, String> getFailedTestInfo(){
		return _failedTrace;
	}
	
	public abstract List<Pair<String, Integer>> getLocations();
	
}
