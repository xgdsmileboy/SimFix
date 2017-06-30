/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.main;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Type;
import org.junit.runner.Result;

import cofix.common.inst.Instrument;
import cofix.common.inst.MethodInstrumentVisitor;
import cofix.common.junit.runner.JUnitEngine;
import cofix.common.junit.runner.JUnitRuntime;
import cofix.common.junit.runner.OutStream;
import cofix.common.localization.FLocalization;
import cofix.common.util.JavaFile;
import cofix.common.util.Pair;
import cofix.common.util.Status;
import cofix.common.util.Subject;
import cofix.core.modify.Modification;
import cofix.core.parser.NodeUtils;
import cofix.core.parser.node.CodeBlock;
import cofix.core.parser.search.BuggyCode;
import cofix.core.parser.search.SimpleFilter;

/**
 * @author Jiajun
 * @datae Jun 20, 2017
 */
public class Repair {

	private FLocalization _localization = null;
	private Subject _subject = null;
	private List<String> _failedTestCases = null;
	private Map<Integer, Set<Pair<String, String>>> _passedTestCases = null;

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
		MethodInstrumentVisitor methodInstrumentVisitor = new MethodInstrumentVisitor();
		Instrument.execute(src, methodInstrumentVisitor);
		
		System.out.println("Passed test classes : " + _localization.getPassedTestCases().size());
		for(String test : _localization.getPassedTestCases()){
			String[] testStr = test.split("#");
			String clazz = testStr[0];
			String methodName = testStr[1];
			OutStream outStream = new OutStream();
			Result result = JUnitEngine.getInstance(runtime).test(clazz, methodName, new PrintStream(outStream));
			if(result.getFailureCount() > 0){
				System.out.println("Error : Passed test cases running failed ! => " + clazz);
				System.exit(0);
			}
			for(Integer method : outStream.getOut()){
				Set<Pair<String, String>> tcases = _passedTestCases.get(method);
				if(tcases == null){
					tcases = new HashSet<>();
				}
				tcases.add(new Pair<String, String>(clazz, methodName));
				_passedTestCases.put(method, tcases);
			}
		}
		// restore source file
		_subject.restore();
	}

	public Status fix(Timer timer){
		String src = _subject.getHome() + _subject.getSsrc();
		for(Pair<String, Integer> loc : _localization.getLocations()){
			System.out.println(loc.getFirst() + "::" + loc.getSecond());
			String file = _subject.getHome() + _subject.getSsrc() + "/" + loc.getFirst().replace(".", "/") + ".java";
			CompilationUnit unit = JavaFile.genASTFromFile(file);
			// get buggy code block
			CodeBlock buggyblock = BuggyCode.getBuggyCodeBlock(unit, loc.getSecond());
			if(buggyblock.getWrapMethodID() == null){
				System.out.println("Find no block!");
				continue;
			}
//			Utils.print(buggyblock);
			
			// get all variables can be used at buggy line
			Map<String, Type> usableVars = NodeUtils.getUsableVarTypes(file, loc.getSecond());
			// search candidate similar code block
			SimpleFilter simpleFilter = new SimpleFilter(buggyblock);
			
			List<Pair<CodeBlock, Double>> candidates = simpleFilter.filter(src, 0.4);
			List<String> source = null;
			try {
				source = JavaFile.readFileToList(file);
			} catch (IOException e1) {
				System.err.println("Failed to read file to list : " + file);
				continue;
			}
			for(Pair<CodeBlock, Double> similar : candidates){
//				Utils.print(similar.getFirst());
				// compute transformation
//				List<Modification> modifications = CodeBlockMatcher.match(buggyblock, similar.getFirst(), usableVars);
				List<Modification> modifications = null;
				// try each transformation
				for(Modification modification : modifications){
					if(timer.timeout()){
						return Status.TIMEOUT;
					}
					modification.apply(usableVars);
					// validate correctness of patch
					Pair<Integer, Integer> range = buggyblock.getLineRangeInSource();
					try {
						JavaFile.sourceReplace(file, source, range.getFirst(), range.getSecond(), buggyblock.getNodes());
					} catch (IOException e) {
						System.err.println("Failed to replace source code.");
						continue;
					}
					if(validate(buggyblock)){
						System.out.println("\n----------------------------------------\n");
						System.out.println("Find a patch :");
						System.out.println(modification);
						System.out.println("\n----------------------------------------\n");
					}
					modification.restore();
				}
			}
		}
		return Status.FAILED;
	}
	
	private boolean validate(CodeBlock buggyBlock){
		JUnitRuntime runtime = new JUnitRuntime(_subject);
		// validate patch using failed test cases
		for(String testcase : _failedTestCases){
			String[] testinfo = testcase.split("#");
			Result result = JUnitEngine.getInstance(runtime).test(testinfo[0], testinfo[1], null);
			if(result.getFailureCount() > 0){
				return false;
			}
		}
		
		Integer revisedMethod = buggyBlock.getWrapMethodID();
		Set<Pair<String, String>> coveredPassedTest = _passedTestCases.get(revisedMethod);
		// validate patch using passed test cases
		for(Pair<String, String> pair : coveredPassedTest){
			Result result = JUnitEngine.getInstance(runtime).test(pair.getFirst(), pair.getSecond(), null);
			if(result.getFailureCount() > 0){
				return false;
			}
		}
		
		return true;
	}

}
