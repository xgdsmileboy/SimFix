/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.junit.runner;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

import javax.print.attribute.standard.PrinterLocation;

import org.junit.internal.TextListener;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

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

		Class javaClass = null;
		try {
			javaClass = Class.forName(clazz, true, _runtime.getClassLoader());
		} catch (ClassNotFoundException e) {
			LevelLogger.error(__name__ + "#test Load class failed : " + e.getMessage());
		}
		
		PrintStream old = System.out;
		if(printStream != null){
			System.setOut(printStream);
		}
		
		JUnitCore jUnitCore = new JUnitCore();
		Result result = jUnitCore.run(javaClass);
		System.setOut(old);
		
		return result;

	}
	
	public Result test(String clazz, String testMethod, PrintStream printStream) {
		if (clazz == null || testMethod == null) {
			LevelLogger.error(__name__ + "#test Illegal input for running single test case.");
			return null;
		}
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
