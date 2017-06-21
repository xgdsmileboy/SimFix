/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.junit.runner;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;

import cofix.common.util.LevelLogger;

/**
 * @author Jiajun
 * @datae Jun 19, 2017
 */
public class JUnitEngine {


	private final String __name__ = "@JUnitRunner ";
	private JUnitRuntime _runtime = null;

	private JUnitEngine(JUnitRuntime runtime) {
		_runtime = runtime;
	}

	private static JUnitEngine instance = null;

	public static JUnitEngine getInstance(JUnitRuntime runtime) {
		if (instance == null || runtime.equals(instance.getRuntime())) {
			instance = new JUnitEngine(runtime);
		}
		return instance;
	}

	private JUnitRuntime getRuntime() {
		return _runtime;
	}

	public Result test(List<String> testClazzes, PrintStream printStream) {
		if (testClazzes == null) {
			LevelLogger.error(__name__ + "#runTest Illegal input for running multiple test class.");
			return null;
		}
		
		ClassLoader currentThreadClassLoader
		 = Thread.currentThread().getContextClassLoader();

		// Replace the thread classloader - assumes
		// you have permissions to do so
		Thread.currentThread().setContextClassLoader(_runtime.getClassLoader());

		// This should work now!
		Thread.currentThread().getContextClassLoader().getResourceAsStream("context.xml");

		Class[] clazzes = new Class[testClazzes.size()];
		for (int i = 0; i < testClazzes.size(); i++) {
			try {
				Class javaClass = Class.forName(testClazzes.get(i), true, _runtime.getClassLoader());
				clazzes[i] = javaClass;
			} catch (ClassNotFoundException e) {
				LevelLogger.error(__name__ + "#test Load class failed : " + e.getMessage());
			}
		}
		
		PrintStream old = System.out;
		if(printStream != null){
			System.setOut(printStream);
		}
		Result result = new JUnitCore().run(clazzes);
		System.setOut(old);
		return result;

	}

	public Result test(String clazz, PrintStream printStream) {
		if (clazz == null) {
			LevelLogger.error(__name__ + "#runTest Illegal input for running single test class.");
			return null;
		}
		List<String> classes = new ArrayList<>();
		classes.add(clazz);
		return test(classes, printStream);

	}
	
	public Result test(String clazz, String testMethod, PrintStream printStream) {
		if (clazz == null || testMethod == null) {
			LevelLogger.error(__name__ + "#test Illegal input for running single test case.");
			return null;
		}
		
		System.out.println("TESTING : " + clazz + "::" + testMethod);
		
		ClassLoader currentThreadClassLoader = Thread.currentThread().getContextClassLoader();

		// Replace the thread classloader - assumes
		// you have permissions to do so
		Thread.currentThread().setContextClassLoader(_runtime.getClassLoader());

		// This should work now!
		Thread.currentThread().getContextClassLoader().getResourceAsStream("context.xml");
		
		Class<?> junitTest = null;
		try {
			junitTest = Class.forName(clazz, true, _runtime.getClassLoader());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		PrintStream old = System.out;
		if(printStream != null){
			System.setOut(printStream);
		}
		Request request = Request.method(junitTest, testMethod);
		Result result = new JUnitCore().run(request);
		System.setOut(old);
		return result;
	}
	
}
