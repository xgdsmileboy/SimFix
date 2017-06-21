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
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Type;
import org.junit.runner.Result;

import cofix.common.astnode.CodeBlock;
import cofix.common.code.search.BuggyCode;
import cofix.common.code.search.SimpleFilter;
import cofix.common.inst.Instrument;
import cofix.common.inst.MethodInstrumentVisitor;
import cofix.common.junit.runner.JUnitEngine;
import cofix.common.junit.runner.JUnitRuntime;
import cofix.common.junit.runner.OutStream;
import cofix.common.localization.FLocalization;
import cofix.common.parser.NodeUtils;
import cofix.common.util.JavaFile;
import cofix.common.util.Pair;
import cofix.common.util.Status;
import cofix.common.util.Subject;
import cofix.core.adapt.Delta;
import cofix.core.match.CodeBlockMatcher;

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

	public Status fix(Timer timer){
		String src = _subject.getHome() + _subject.getSsrc();
		for(Pair<String, Integer> loc : _localization.getLocations()){
			if(timer.timeout()){
				return Status.TIMEOUT;
			}
			System.out.println(loc.getFirst() + "::" + loc.getSecond());
			
			String file = _subject.getHome() + _subject.getSsrc() + "/" + loc.getFirst().replace(".", "/") + ".java";
			CompilationUnit unit = JavaFile.genASTFromFile(file);
			// get buggy code block
			CodeBlock block = BuggyCode.getBuggyCodeBlock(unit, loc.getSecond());
			// get all variables can be used at buggy line
			Map<String, Type> usableVars = NodeUtils.getUsableVarTypes(file, loc.getSecond());
			// search candidate similar code block
			SimpleFilter simpleFilter = new SimpleFilter(block);
			List<Pair<CodeBlock, Float>> candidates = simpleFilter.filter(src, 0.4);
			for(Pair<CodeBlock, Float> similar : candidates){
				// compute transformation
				List<Delta> modifications = CodeBlockMatcher.match(block, similar.getFirst(), usableVars);
				// try each transformation
				for(Delta modification : modifications){
					modification.apply(usableVars);
					// TODO : validate patch
					
					modification.restore();
				}
			}
		}
		return Status.FAILED;
	}

}
