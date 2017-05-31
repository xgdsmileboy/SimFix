/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.match;

import java.util.Map;
import java.util.Map.Entry;

import cofix.common.astnode.CodeBlock;
import cofix.common.astnode.MethodCall;

/**
 * @author Jiajun
 * @datae May 31, 2017
 */
public class MethodMetric extends Metric {

	public MethodMetric(float weight) {
		_weight = weight;
	}
	
	@Override
	public float getSimilarity(CodeBlock src, CodeBlock tar) {
		return _weight * getPureSimilarity(src, tar);
	}
	
	private float getPureSimilarity(CodeBlock src, CodeBlock tar){
		float similarity = 0.0f;
		Map<MethodCall, Integer> sMethods = src.getMethodCalls();
		Map<MethodCall, Integer> tMethods = tar.getMethodCalls();
		int count = 0;
		for(Entry<MethodCall, Integer> entry : sMethods.entrySet()){
			MethodCall methodCall = entry.getKey();
			count += entry.getValue();
			Integer tarCount = tMethods.get(methodCall);
			if(tarCount != null){
				similarity += entry.getValue() > tarCount ? tarCount : entry.getValue();
			}
		}
		if(count == 0){
			return 1.0f;
		}
		return similarity / count;
	}

}
