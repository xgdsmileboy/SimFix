/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.match;

import java.util.Set;

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
		Set<MethodCall> sMethods = src.getMethodCalls();
		Set<MethodCall> tMethods = tar.getMethodCalls();
		for(MethodCall methodCall : sMethods){
			if(tMethods.contains(methodCall)){
				similarity ++;
			}
		}
		return similarity / sMethods.size();
	}

}
