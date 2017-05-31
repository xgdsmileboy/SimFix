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
import cofix.common.astnode.Structure;

/**
 * @author Jiajun
 * @datae May 31, 2017
 */
public class StructrueMetric extends Metric {

	public StructrueMetric(float weight) {
		_weight = weight;
	}
	
	@Override
	public float getSimilarity(CodeBlock src, CodeBlock tar) {
		return _weight * getPureSimilarity(src, tar);
	}
	
	private float getPureSimilarity(CodeBlock src, CodeBlock tar){
		float similarity = LCS(src.getStructures(), tar.getStructures());
		return similarity / src.getStructures().size();
	}
	
	private int LCS(List<Structure> srcStruct, List<Structure> tarStruct){
		if(srcStruct.size() == 0 || tarStruct.size() == 0){
			return 0;
		}
		int srcLen = srcStruct.size();
		int tarLen = tarStruct.size();
		int[][] score = new int[srcLen + 1][tarLen + 1];
		for(int i = 0; i < srcLen; i++){
			for(int j = 0; j < tarLen; j++){
				if(srcStruct.get(i).equals(tarStruct.get(j))){
					score[i + 1][j + 1] = score[i][j] + 1; 
				} else {
					score[i + 1][j + 1] = Math.max(score[i + 1][j], score[i][j + 1]);
				}
			}
		}
		return score[srcLen][tarLen];
	}

}
