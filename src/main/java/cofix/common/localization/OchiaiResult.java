/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.localization;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import cofix.common.config.Constant;
import cofix.common.util.Pair;
import cofix.common.util.Subject;

/**
 * @author Jiajun
 * @date Jul 19, 2017
 */
public class OchiaiResult extends AbstractFaultlocalization {

	private final String _locRsltPath = Constant.HOME + "/d4j-info/location/ochiai";
	private final String _allTestsPath = Constant.HOME + "/d4j-info/all_tests";
	private final String _failedTestsPath = Constant.HOME + "/d4j-info/failed_tests";
	
	public OchiaiResult(Subject subject) {
		super(subject);
		locateFault(0);
	}
	
	@Override
	protected void locateFault(double threshold) {
		readFailedTests(_failedTestsPath + "/" + _subject.getName() + "/" + _subject.getId() + ".txt");
		readPassedTest(_allTestsPath + "/" + _subject.getName()+ "/" + _subject.getId() + ".txt");
	}
	
	private void readFailedTests(String path){
		File file = new File(path);
		if(!file.exists()){
			System.err.println("Failed test file does not exist : " + path);
			System.exit(0);
		}
		
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		String line = null;
		try {
			while((line = bufferedReader.readLine()) != null){
				if(line.length() > 0){
					_failedTests.add(line);
				}
			}
			bufferedReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void readPassedTest(String path){
		File file = new File(path);
		if(!file.exists()){
			System.err.println("All test file does not exist : " + path);
			System.exit(0);
		}
		
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		String line = null;
		try {
			while((line = bufferedReader.readLine()) != null){
				if(line.length() > 0){
					int left = line.indexOf("(");
					int right = line.indexOf(")");
					if(left < 0 || right < 0){
						System.err.println("Test format error : " + line);
						System.exit(0);
					}
					String method = line.substring(0, left);
					String clazz = line.substring(left + 1, right);
					String test = clazz + "::" + method;
					if(!_failedTests.contains(test)){
						_passedTests.add(test);
					}
				}
			}
			bufferedReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<Pair<String, Integer>> getLocations(int topK) {
		String path = _locRsltPath + "/" + _subject.getName() + "/" + _subject.getId() + ".txt";
		File file = new File(path);
		if(!file.exists()){
			System.err.println("All test file does not exist : " + path);
			System.exit(0);
		}
		
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		List<Pair<String, Integer>> locations = new ArrayList<>();
		String line = null;
		try {
			int count = 0;
			while((line = bufferedReader.readLine()) != null){
				if(line.length() > 0){
					String[] info = line.split("#");
					if(info.length < 2){
						System.err.println("Location format error : " + line);
						System.exit(0);
					}
					String[] linesInfo = info[1].split(",");
					Integer lineNumber = Integer.parseInt(linesInfo[0]);
					String stmt = info[0];
					int index = stmt.indexOf("$");
					if(index > 0){
						stmt = stmt.substring(0, index);
					}
					locations.add(new Pair<String, Integer>(stmt, lineNumber));
					count ++;
					if(count == topK){
						break;
					}
				}
			}
			bufferedReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return locations;
	}

}
