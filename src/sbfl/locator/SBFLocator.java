package sbfl.locator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import cofix.common.config.Configure;
import cofix.common.localization.AbstractFaultlocalization;
import cofix.common.run.CmdFactory;
import cofix.common.run.Executor;
import cofix.common.util.Pair;
import cofix.common.util.Subject;

public class SBFLocator extends AbstractFaultlocalization {

	public static void main(String[] args) {
		Subject subject = Configure.getSubject("chart", 1);
		for(String string : CmdFactory.createSbflCmd(subject, 20)){
			System.out.println(string);
		}
		try {
			Executor.executeCommand(CmdFactory.createSbflCmd(subject, 300));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		List<Pair<String, Integer>> lines = null;
		try {
			lines = getSortedSuspStmt(subject.getBuggyLineSuspFile());
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		int i = 0;
		for(Pair<String, Integer> pair : lines){
			if(++ i >= 100){
				break;
			}
			System.out.println(pair.getFirst() + "#" + pair.getSecond());
		}
		
	}
	
	public SBFLocator(Subject subject) {
		super(subject);
	}

	@Override
	protected void locateFault(double threshold) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<Pair<String, Integer>> getLocations(int topK) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private static List<Pair<String, Integer>> getSortedSuspStmt(String fileName) throws NumberFormatException, IOException{
		List<Pair<String, Double>> suspStmt = new ArrayList<>();
		//org.jfree.chart.renderer.category.LineAndShapeRenderer#201,0.1889822365046136
		File file = new File(fileName);
		if(!file.exists()){
			System.err.println("Cannot get suspicious statement for non-existing file : " + fileName);
			return new ArrayList<>();
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
		
		List<Pair<String, Integer>> buggyLines = new LinkedList<>();
		for(Pair<String, Double> pair : suspStmt){
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
		return buggyLines;
	}
}
