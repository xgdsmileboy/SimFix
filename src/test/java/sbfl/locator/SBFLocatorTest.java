package sbfl.locator;

import java.util.List;

import org.junit.Test;

import cofix.common.config.Configure;
import cofix.common.util.Pair;
import cofix.common.util.Subject;

public class SBFLocatorTest {

	@Test
	public void test(){
		Configure.configEnvironment();
		Subject subject = Configure.getSubject("chart", 1);
		SBFLocator sbfLocator = new SBFLocator(subject);
		List<Pair<String, Integer>> lines = sbfLocator.getLocations(100);
		
		int i = 0;
		for(Pair<String, Integer> pair : lines){
			if(++ i >= 100){
				break;
			}
			System.out.println(pair.getFirst() + "#" + pair.getSecond());
		}
		System.out.println("===============Failed Test==================");
		System.out.println(sbfLocator.getFailedTestCases());
	}
	
}
