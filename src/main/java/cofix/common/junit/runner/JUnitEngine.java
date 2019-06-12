/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.junit.runner;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Result;
import org.junit.runner.notification.RunNotifier;

import cofix.common.util.LevelLogger;

/**
 * @author Jiajun
 * @date Jun 19, 2017
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
		JUnitCore core = new JUnitCore();
		
		WatchDog watchDog = new WatchDog(core, Thread.currentThread());
		Thread thread = new Thread(watchDog);
		thread.start();
		
		System.err.println("begin running testing ...");
		
		Result result = null;
		try{
			result = core.run(request);
		} catch(Exception e){
			
		}
		
		watchDog.interrupt();
		
		System.setOut(old);
		return result;
	}
	
	class WatchDog implements Runnable{
		
		private JUnitCore jUnitCore = null;
		private long start = 0;
		private long time = 2 * 60 * 1000; // 5 minutes
		private Thread mainThread = null;
		
		public WatchDog(JUnitCore core, Thread thread) {
			jUnitCore = core;
			mainThread = thread;
		}
		
		@Override
		public void run() {
			System.out.println("run ...");
			start = System.currentTimeMillis();
			long current = start;
			while(current - start < time){
				System.err.println("Checking ...." + current + "==" + start + "==" + time);
				try {
//					mainThread.join();
					Thread.sleep(30000);
//					Thread.currentThread().join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				current = System.currentTimeMillis();
			}
			System.err.println("Try to stop ....");
			try {
				Field field = JUnitCore.class.getDeclaredField("notifier");
				field.setAccessible(true);
				RunNotifier runNotifier = (RunNotifier) field.get(jUnitCore);
				runNotifier.pleaseStop();
				jUnitCore = null;
			} catch (Exception e) {
				e.printStackTrace();
			} 
		}
		
		public void interrupt() {
			System.err.println("Try interrupt ...");
			time = 0;
		}
	}
	
}
