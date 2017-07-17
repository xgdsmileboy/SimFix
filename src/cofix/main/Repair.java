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
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.jws.WebParam.Mode;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Type;
import org.junit.internal.runners.statements.Fail;
import org.junit.runner.Result;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import com.sun.imageio.plugins.common.SubImageInputStream;
import com.sun.java.swing.plaf.windows.WindowsTreeUI.ExpandedIcon;
import com.sun.org.apache.xpath.internal.operations.Mod;
import com.sun.org.glassfish.external.statistics.annotations.Reset;

import cofix.common.inst.Instrument;
import cofix.common.inst.MethodInstrumentVisitor;
import cofix.common.junit.runner.JUnitEngine;
import cofix.common.junit.runner.JUnitRuntime;
import cofix.common.junit.runner.OutStream;
import cofix.common.localization.AbstractFaultlocalization;
import cofix.common.localization.ManualLocator;
import cofix.common.run.Runner;
import cofix.common.util.JavaFile;
import cofix.common.util.Pair;
import cofix.common.util.Status;
import cofix.common.util.Subject;
import cofix.core.match.CodeBlockMatcher;
import cofix.core.modify.Modification;
import cofix.core.parser.NodeUtils;
import cofix.core.parser.node.CodeBlock;
import cofix.core.parser.search.BuggyCode;
import cofix.core.parser.search.SimpleFilter;
import sun.tools.jar.resources.jar;

/**
 * @author Jiajun
 * @datae Jun 20, 2017
 */
public class Repair {

	private AbstractFaultlocalization _localization = null;
	private Subject _subject = null;
	private List<String> _failedTestCases = null;
	private Map<Integer, Set<Pair<String, String>>> _passedTestCases = null;

	public Repair(Subject subject, AbstractFaultlocalization fLocalization) {
		_localization = fLocalization;
		_subject = subject;
//		_failedTestCases = new ArrayList<>(_localization.getFailedTestInfo().keySet());
		_failedTestCases = new ArrayList<>();
		_failedTestCases.add(((ManualLocator)fLocalization).getFailedTest(_subject));
		
		_passedTestCases = new HashMap<>();
//		try {
//			computeMethodCoverage();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	}
	
	private void computeMethodCoverage() throws IOException{
		JUnitRuntime runtime = new JUnitRuntime(_subject);
		String src = _subject.getHome() + _subject.getSsrc();
		MethodInstrumentVisitor methodInstrumentVisitor = new MethodInstrumentVisitor();
		Instrument.execute(src, methodInstrumentVisitor);
		
		if(!Runner.compileSubject(_subject)){
			System.err.println("Build project failed!");
			System.exit(0);
		}
		
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
		for(Pair<String, Integer> loc : _localization.getLocations(_subject)){
			System.out.println(loc.getFirst() + "::" + loc.getSecond());
			String file = _subject.getHome() + _subject.getSsrc() + "/" + loc.getFirst().replace(".", "/") + ".java";
			String binFile = _subject.getHome() + _subject.getSbin() + "/" + loc.getFirst().replace(".", "/") + ".class";
			CompilationUnit unit = JavaFile.genASTFromFile(file);
			// get buggy code block
			CodeBlock buggyblock = BuggyCode.getBuggyCodeBlock(file, loc.getSecond());
			if(buggyblock.getWrapMethodID() == null){
				System.out.println("Find no block!");
				continue;
			}
//			Utils.print(buggyblock);
			Set<String> haveTry = new HashSet<>();
			// get all variables can be used at buggy line
			Map<String, Type> usableVars = NodeUtils.getUsableVarTypes(file, loc.getSecond());
			// search candidate similar code block
			SimpleFilter simpleFilter = new SimpleFilter(buggyblock);
			
			List<Pair<CodeBlock, Double>> candidates = simpleFilter.filter(src, 0.3);
			List<String> source = null;
			try {
				source = JavaFile.readFileToList(file);
			} catch (IOException e1) {
				System.err.println("Failed to read file to list : " + file);
				continue;
			}
			int i = 1;
			for(Pair<CodeBlock, Double> similar : candidates){
				System.out.println("=====================" + (i++) +"==============================");
				System.out.println(similar.getFirst().toSrcString().toString());
				// compute transformation
				List<Modification> modifications = CodeBlockMatcher.match(buggyblock, similar.getFirst(), usableVars);
				
				// try each transformation first
				List<Set<Modification>> list = new ArrayList<>();
				for(Modification modification : modifications){
					Set<Modification> set = new HashSet<>();
					set.add(modification);
					list.add(set);
				}
				
				List<Modification> legalModifications = new ArrayList<>();
				while(true){
					for(Set<Modification> modifySet : list){
						if(timer.timeout()){
							return Status.TIMEOUT;
						}
						for(Modification modification : modifySet){
							modification.apply(usableVars);
						}
						// validate correctness of patch
						Pair<Integer, Integer> range = buggyblock.getLineRangeInSource();
						String replace = buggyblock.toSrcString().toString();
						if(haveTry.contains(replace)){
							System.out.println("already try ...");
							for(Modification modification : modifySet){
								modification.restore();
							}
							continue;
						}
						
						System.out.println("========");
						System.out.println(replace);
						System.out.println("========");
						
						haveTry.add(replace);
						try {
							JavaFile.sourceReplace(file, source, range.getFirst(), range.getSecond(), replace);
						} catch (IOException e) {
							System.err.println("Failed to replace source code.");
							continue;
						}
						try {
							FileUtils.forceDelete(new File(binFile));
						} catch (IOException e) {
						}
						switch (validate(buggyblock)) {
						case COMPILE_FAILED:
							break;
						case SUCCESS:
							StringBuffer stringBuffer = new StringBuffer();
							stringBuffer.append("\n----------------------------------------\n");
							stringBuffer.append("Find a patch :\n");
							stringBuffer.append(buggyblock.toSrcString().toString());
							stringBuffer.append("\n----------------------------------------\n");
							stringBuffer.append("\nSuccessfully find a patch!\n");
							System.out.println(stringBuffer.toString());
							JavaFile.writeStringToFile("result.log", stringBuffer.toString(), true);
//							return Status.SUCCESS;
							System.out.print("Continue search ? (Y/N) ");
							Scanner scanner = new Scanner(System.in);
							String value = scanner.next();
							if(value.equals("N")){
								return Status.SUCCESS;
							}
						case TEST_FAILED:
							if(legalModifications != null){
								for(Modification modification : modifySet){
									legalModifications.add(modification);
								}
							}
						}
						for(Modification modification : modifySet){
							modification.restore();
						}
					}
					if(legalModifications == null){
						break;
					}
					list = combineModification(legalModifications);
					legalModifications = null;
				}
			}
		}
		return Status.FAILED;
	}
	
	private List<Set<Modification>> combineModification(List<Modification> modifications){
		List<Set<Modification>> list = new ArrayList<>();
		int length = modifications.size();
		if(length == 0){
			return list;
		}
		int[][] incompatibleMap = new int[length][length];
		for(int i = 0; i < length; i++){
			for(int j = i; j < length; j++){
				if(i == j){
					incompatibleMap[i][j] = 1;
				} else if(modifications.get(i).compatible(modifications.get(j))){
					incompatibleMap[i][j] = 0;
					incompatibleMap[j][i] = 0;
				} else {
					incompatibleMap[i][j] = 1;
					incompatibleMap[i][j] = 1;
				}
			}
		}
		List<Set<Integer>> baseSet = new ArrayList<>();
		for(int i = 0; i < modifications.size(); i++){
			Set<Integer> set = new HashSet<>();
			set.add(i);
			baseSet.add(set);
		}
		
		List<Set<Integer>> expanded = expand(incompatibleMap, baseSet, 2, incompatibleMap.length);
		for(Set<Integer> set : expanded){
			Set<Modification> combinedModification = new HashSet<>();
			for(Integer integer : set){
				combinedModification.add(modifications.get(integer));
			}
			list.add(combinedModification);
		}
		
		return list;
	}
	
	private List<Set<Integer>> expand(int[][] incompatibleTabe, List<Set<Integer>> baseSet, int currentSize, int upperbound){
		List<Set<Integer>> rslt = new LinkedList<>();
		if(currentSize > upperbound){
			return rslt;
		}
		int length = incompatibleTabe.length;
		for(Set<Integer> base : baseSet){
			int minIndex = 0;
			for(Integer integer : base){
				if(integer > minIndex){
					minIndex = integer;
				}
			}
			
			for(minIndex ++; minIndex < length; minIndex ++){
				boolean canExd = true;
				for(Integer integer : base){
					if(incompatibleTabe[minIndex][integer] == 1){
						canExd = false;
						break;
					}
				}
				if(canExd){
					Set<Integer> expanded = new HashSet<>(base);
					expanded.add(minIndex);
					rslt.add(expanded);
				}
			}
		}
		
		if(rslt.size() > 0){
			rslt.addAll(expand(incompatibleTabe, rslt, currentSize + 1, upperbound));
		}
		
		return rslt;
	}
	
	private ValidateStatus validate(CodeBlock buggyBlock){
		
		// TODO : need to build project first
		if(!Runner.compileSubject(_subject)){
			System.err.println("Build failed !");
			return ValidateStatus.COMPILE_FAILED;
		}
		
//		JUnitRuntime runtime = new JUnitRuntime(_subject);
		// validate patch using failed test cases
		for(String testcase : _failedTestCases){
			String[] testinfo = testcase.split("#");
			if(!Runner.testSingleTest(_subject, testinfo[0], testinfo[1])){
				return ValidateStatus.TEST_FAILED;
			}
//			Result result = JUnitEngine.getInstance(runtime).test(testinfo[0], testinfo[1], null);
//			if(result == null || result.getFailureCount() > 0){
//				return false;
//			}
		}
		
		if(!Runner.runTestSuite(_subject)){
			return ValidateStatus.TEST_FAILED;
		}
		
//		Integer revisedMethod = buggyBlock.getWrapMethodID();
//		Set<Pair<String, String>> coveredPassedTest = _passedTestCases.get(revisedMethod);
//		if(coveredPassedTest != null){
//			// validate patch using passed test cases
//			for(Pair<String, String> pair : coveredPassedTest){
//				Result result = JUnitEngine.getInstance(runtime).test(pair.getFirst(), pair.getSecond(), null);
//				if(result.getFailureCount() > 0){
//					return false;
//				}
//			}
//		}
		
		return ValidateStatus.SUCCESS;
	}
	
	private enum ValidateStatus{
		COMPILE_FAILED,
		TEST_FAILED,
		SUCCESS
	}

}
