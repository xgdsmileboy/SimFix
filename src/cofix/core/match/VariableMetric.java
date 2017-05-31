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
import cofix.common.astnode.Variable;

/**
 * @author Jiajun
 * @datae May 31, 2017
 */
public class VariableMetric extends Metric {

	
	public VariableMetric(float weight) {
		_weight = weight;
	}
	
	@Override
	public float getSimilarity(CodeBlock src, CodeBlock tar) {
		return _weight * getPureSimilarity(src, tar);
	}
	
	private float getPureSimilarity(CodeBlock src, CodeBlock tar){
		float similarity = 0.0f;
		Map<Variable, Integer> srcVarMap = src.getVariables();
		Map<Variable, Integer> tarVarMap = tar.getVariables();
		float count = 0f;
		for(Entry<Variable, Integer> entry : srcVarMap.entrySet()){
			Variable srcVar = entry.getKey();
			count += entry.getValue();
			Integer tarCount = tarVarMap.get(srcVar);
			if(tarCount != null){
				similarity += entry.getValue() > tarCount ? tarCount : entry.getValue();
			}
			// TODO : if variable with same name and type does not exit, consider variables with same type ?
		}
		
		return similarity / count;
	}

}
