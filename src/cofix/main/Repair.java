/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.main;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.junit.runner.Result;

import cofix.common.inst.Instrument;
import cofix.common.inst.MethodInstrumentVisitor;
import cofix.common.junit.runner.JUnitEngine;
import cofix.common.junit.runner.JUnitRuntime;
import cofix.common.junit.runner.OutStream;
import cofix.common.localization.FLocalization;
import cofix.common.util.Subject;

/**
 * @author Jiajun
 * @datae Jun 20, 2017
 */
public class Repair {

	private FLocalization _localization = null;
	private Subject _subject = null;
	private List<String> _failedTestCases = null;
	private Map<String, Set<String>> _passedTestCases = null;

	public Repair(Subject subject, FLocalization fLocalization) {
		_localization = fLocalization;
		_subject = subject;
		_failedTestCases = new ArrayList<>(_localization.getFailedTestInfo().keySet());
		try {
			computeMethodCoverage();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void computeMethodCoverage() throws IOException{
		JUnitRuntime runtime = new JUnitRuntime(_subject);
		String src = _subject.getHome() + _subject.getSsrc();
		// backup source file
		FileUtils.copyDirectory(new File(src), new File(src + "_ori"));
		MethodInstrumentVisitor methodInstrumentVisitor = new MethodInstrumentVisitor();
		Instrument.execute(src, methodInstrumentVisitor);
		for(String test : _localization.getPassedTestCases()){
			String[] testStr = test.split("#");
			String clazz = testStr[0];
			OutStream outStream = new OutStream();
			Result result = JUnitEngine.getInstance(runtime).test(clazz, new PrintStream(outStream));
			if(result.getFailureCount() > 0){
				System.out.println("Error : Passed test cases running failed ! => " + test);
				System.exit(0);
			}
			for(String method : outStream.getOut()){
				Set<String> tcases = _passedTestCases.get(method);
				if(tcases == null){
					tcases = new HashSet<>();
				}
				tcases.add(clazz);
				_passedTestCases.put(method, tcases);
			}
		}
		// restore source file
		FileUtils.copyDirectory(new File(src + "_ori/"), new File(src));
	}
	
	public static void main(String[] args) {
		String src = "/Users/Jiajun/Code/Java/fault-fix/SimilarFix/testfile/chart/chart_1_buggy/source";
		try {
			FileUtils.copyDirectory(new File(src), new File(src + "_ori"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public boolean repair(){
		
		//TODO :
		
		return false;
	}

}
