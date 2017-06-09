/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.match;

import java.util.List;

import cofix.common.astnode.CodeBlock;
import cofix.common.astnode.Operator;

/**
 * @author Jiajun
 * @datae May 31, 2017
 */
public class OperatorMetric extends Metric {

	public OperatorMetric(float weight) {
		_weight = weight;
	}
	
	@Override
	public float getSimilarity(CodeBlock src, CodeBlock tar) {
		return _weight * getPureSimilarity(src, tar);
	}
	
	private float getPureSimilarity(CodeBlock src, CodeBlock tar){
		float similarity = LCS(src.getOperators(), tar.getOperators());
		int count = src.getOperators().size() + tar.getOperators().size();
		similarity *= 2.0f;
		if(count == 0){
			return 1.0f;
		}
		return similarity / count;
	}
	
	private int LCS(List<Operator> srcOp, List<Operator> tarOp){
		if(srcOp.size() == 0 || tarOp.size() == 0){
			return 0;
		}
		int srcLen = srcOp.size();
		int tarLen = tarOp.size();
		int[][] score = new int[srcLen + 1][tarLen + 1];
		for(int i = 0; i < srcLen; i++){
			for(int j = 0; j < tarLen; j++){
				if(srcOp.get(i).equals(tarOp.get(j))){
					score[i + 1][j + 1] = score[i][j] + 1; 
				} else {
					score[i + 1][j + 1] = Math.max(score[i + 1][j], score[i][j + 1]);
				}
			}
		}
		return score[srcLen][tarLen];
	}
}
