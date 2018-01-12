package sbfl.locator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import cofix.common.config.Constant;
import cofix.common.localization.AbstractFaultlocalization;
import cofix.common.localization.OchiaiResult;
import cofix.common.run.CmdFactory;
import cofix.common.run.Executor;
import cofix.common.util.Pair;
import cofix.common.util.Subject;

public class SBFLocator extends AbstractFaultlocalization {
	
	public SBFLocator(Subject subject) {
		super(subject);
		locateFault(0);
	}
	
	public void setFailedTest(List<String> failedTests){
		_failedTests = failedTests;
	}
	

	@Override
	protected void locateFault(double threshold) {
		try {
			Executor.execute(CmdFactory.createSbflCmd(_subject, Constant.SBFL_TIMEOUT));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public List<String> getFailedTestCases() {
		if(_failedTests == null || _failedTests.size() == 0){
			try {
				Executor.execute(CmdFactory.createTestSubjectCmd(_subject, 600));
				parseFailedTestFromFile(_subject.getFailedTestRecFile());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return _failedTests;
	}
	
	private void parseFailedTestFromFile(String fileName) throws IOException{
		File file = new File(fileName);
		if(!file.exists()){
			System.err.println("Failed test file not exist : " + fileName);
		}
		BufferedReader bReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
		String line = null;
		while((line = bReader.readLine()) != null){
			line = line.trim();
			if(line.startsWith("---")){
				line = line.substring(3).trim();
				_failedTests.add(line);
			}
		}
		bReader.close();
	}

	@Override
	public List<Pair<String, Integer>> getLocations(int topK) {
		List<Pair<String, Integer>> lines = new ArrayList<>();
		try {
			lines = getSortedSuspStmt(_subject.getBuggyLineSuspFile());
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		while(lines.size() > topK){
			lines.remove(lines.size() - 1);
		}
		return lines;
	}
	
	private List<Pair<String, Integer>> getSortedSuspStmt(String fileName) throws NumberFormatException, IOException{
		List<Pair<String, Double>> suspStmt = new ArrayList<>();
		//org.jfree.chart.renderer.category.LineAndShapeRenderer#201,0.1889822365046136
		File file = new File(fileName);
		if(!file.exists()){
			System.err.println("Cannot get suspicious statement for non-existing file : " + fileName);
			OchiaiResult ochiaiResult = new OchiaiResult(_subject);
			return ochiaiResult.getLocations(500);
//			return new ArrayList<>();
		}
		String line = null;
		BufferedReader bReader = null;
		bReader = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));
		line = bReader.readLine();
		while((line = bReader.readLine()) != null){
			String[] lineAndSusp = line.split(",");
			if(lineAndSusp.length != 2){
				System.err.println("Suspicious line format error : " + line);
				continue;
			}
			String stmt = lineAndSusp[0];
			double susp = Double.parseDouble(lineAndSusp[1]);
			suspStmt.add(new Pair<String, Double>(stmt, susp));
		}
		
		bReader.close();
		
		Collections.sort(suspStmt, new Comparator<Pair<String, Double>>() {

			@Override
			public int compare(Pair<String, Double> o1, Pair<String, Double> o2) {
				if(o1.getSecond() > o2.getSecond()){
					return -1;
				} else if(o1.getSecond() < o2.getSecond()){
					return 1;
				} else {
					return 0;
				}
			}
		});
		
		File realtimeLocFile = new File(Constant.PROJ_REALTIME_LOC_BASE + "/" + _subject.getName() + "/" + _subject.getId() + ".txt");
		if(!realtimeLocFile.exists()){
			realtimeLocFile.getParentFile().mkdirs();
			realtimeLocFile.createNewFile();
		}
		BufferedWriter bWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(realtimeLocFile, false), "UTF-8"));
		
		List<Pair<String, Integer>> buggyLines = new LinkedList<>();
		for(Pair<String, Double> pair : suspStmt){
			bWriter.write(pair.getFirst() + "," + pair.getSecond() + "\n");
			String[] clazzAndLine = pair.getFirst().split("#");
			if(clazzAndLine.length != 2){
				System.err.println("Suspicous statement format error : " + pair.getFirst());
				continue;
			}
			
			String clazz = clazzAndLine[0];
			int index = clazz.indexOf("$");
			if(index > 0){
				clazz = clazz.substring(0, index);
			}
			int lineNum = Integer.parseInt(clazzAndLine[1]);
			buggyLines.add(new Pair<String, Integer>(clazz, lineNum));
		}
		bWriter.close();
		return buggyLines;
	}
}
