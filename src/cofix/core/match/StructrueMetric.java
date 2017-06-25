/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.match;

import java.util.ArrayList;
import java.util.List;

import javax.swing.text.AbstractDocument.BranchElement;

import com.sun.org.apache.xpath.internal.operations.String;

import cofix.common.parser.astnode.CodeBlock;
import cofix.common.parser.astnode.structure.Structure;

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
		int count = src.getStructures().size() + tar.getStructures().size();
		similarity *= 2.0f;
		if(count == 0){
			return 1.0f;
		}
		return similarity / count;
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
	
	public static int[] LCS_REC(List<Structure> srcStruct, List<Structure> tarStruct){
		if(srcStruct.size() == 0 || tarStruct.size() == 0){
			return null;
		}
		
		int srcLen = srcStruct.size();
		int tarLen = tarStruct.size();
		// score[i + 1][j + 1] comes from :
		// 1 : score[i][j]
		// 2 : score[i][j+1]
		// 3 : score[i+1][j]
		int[][] origin = new int[srcLen + 1][tarLen + 1];
		int[][] score = new int[srcLen + 1][tarLen + 1];
		for(int i = 0; i < srcLen; i++){
			for(int j = 0; j < tarLen; j++){
				if(srcStruct.get(i).equals(tarStruct.get(j))){
					score[i + 1][j + 1] = score[i][j] + 1; 
					origin[i + 1][j + 1] = 1;
				} else {
					if(score[i + 1][j] > score[i][j + 1]){
						score[i + 1][j + 1] = score[i + 1][j];
						origin[i + 1][j + 1] = 3;
					} else {
						score[i + 1][j + 1] = score[i][j + 1];
						origin[i + 1][j + 1] = 2;
					}
				}
			}
		}
		
		int[] match = new int[srcLen];
		int column = tarLen;
		for(int i = srcLen; i > 0;){
			switch(origin[i][column]){
			case 1: match[i - 1] = column - 1; i--; column--; break;
			case 2: match[i - 1] = -1; i--; break;
			case 3: match[i - 1] = -1; column --; break;
			default :
				match[i - 1] = -1;
				i--;
			}
		}
		
		return match;
	}

}
