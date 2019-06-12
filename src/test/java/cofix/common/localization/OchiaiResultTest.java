/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.localization;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import cofix.common.config.Configure;
import cofix.common.util.Pair;
import cofix.common.util.Subject;

/**
 * @author Jiajun
 * @date Jul 19, 2017
 */
public class OchiaiResultTest {
	
	@Test
	public void test(){
		Subject subject = Configure.getSubject("chart", 1);
		OchiaiResult ochiaiResult = new OchiaiResult(subject);
		
		Assert.assertTrue(ochiaiResult.getFailedTestCases().size() == 1);
//		System.out.println("Failed Test :"); 
//		for(String test : ochiaiResult.getFailedTestCases()){
//			System.out.println(test);
//		}

		Assert.assertTrue(ochiaiResult.getPassedTestCases().size() == 2192);
//		System.out.println("Passed Test :"); 
//		for(String test : ochiaiResult.getPassedTestCases()){
//			System.out.println(test);
//		}
		
		List<Pair<String, Integer>> locations = ochiaiResult.getLocations(10000);
		Assert.assertTrue(locations.size() == 7057);
//		System.out.println("Faulty locations :");
//		for(Pair<String, Integer> line : locations){
//			System.out.println(line.getFirst() + "," + line.getSecond());
//		}
		
		locations = ochiaiResult.getLocations(100);
		Assert.assertTrue(locations.size() == 100);
//		System.out.println("Faulty locations :");
//		for(Pair<String, Integer> line : locations){
//			System.out.println(line.getFirst() + "," + line.getSecond());
//		}
		
	}
}
