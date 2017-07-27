package sbfl.locator;

import java.io.IOException;
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
			Executor.executeCommand(CmdFactory.createSbflCmd(subject, 120));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	
//	public static Lis
}
