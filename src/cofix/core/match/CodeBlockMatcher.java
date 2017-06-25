/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.match;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.Type;

import cofix.common.parser.astnode.CodeBlock;
import cofix.common.parser.astnode.Expr;
import cofix.common.parser.astnode.structure.Structure;
import cofix.core.adapt.Deletion;
import cofix.core.adapt.Insertion;
import cofix.core.adapt.Modification;

/**
 * @author Jiajun
 * @datae May 31, 2017
 */
public class CodeBlockMatcher {
	
	List<Metric> _metrics = new ArrayList<>();
	
	public CodeBlockMatcher(List<Metric> metrics){
		_metrics = metrics;
	}
	
	public float getSimilirity(CodeBlock src, CodeBlock tar){
		float similarity = 0.0f;
		for(Metric metric : _metrics){
			similarity += metric.getSimilarity(src, tar);
		}
		return similarity;
	}
	
	public static void match2(CodeBlock src, CodeBlock tar){
		
	}
	public static List<Modification> match(CodeBlock src, CodeBlock tar, Map<String, Type> allUsableVariables){
		List<Modification> modifications = new ArrayList<>();
		// match code block based on code structure firstly, if no structure exists, match context
		if(src.getStructures().size() > 0){
			List<Structure> srcStructure = src.getStructures();
			List<Structure> tarStructure = tar.getStructures();
			int[] match = StructrueMetric.LCS_REC(srcStructure, tarStructure);
			int[] revers_match = StructrueMetric.LCS_REC(tarStructure, srcStructure);
			for(int i = 0; i < match.length; i++){
				if(match[i] >= 0){
					modifications.addAll(compareStructure(srcStructure.get(i), tarStructure.get(match[i]), allUsableVariables));
				} else {
					modifications.add(new Deletion(srcStructure.get(i)));
				}
			}
			for(int i = 0; i < revers_match.length; i++){
				if(revers_match[i] < 0){
					modifications.add(new Insertion(tarStructure.get(i)));
				}
			}
		} else {
			// no structure in buggy source code
			
		}
		return modifications;
	}
	
	private static List<Modification> compareStructure(Structure src, Structure tar,  Map<String, Type> allUsableVariables){
		List<Modification> modifications = new ArrayList<>();
		List<Expr> srcExprs = src.getExprs();
		List<Expr> tarExprs = tar.getExprs();
		
		int srcLen = srcExprs.size();
		int tarLen = tarExprs.size();
		int[][] origin = new int[srcLen + 1][tarLen + 1];
		int[][] score = new int[srcLen + 1][tarLen + 1];
		for(int i = 0; i < srcLen; i++){
			for(int j = 0; j < tarLen; j++){
				List<Modification> tmp = new ArrayList<>();
				if(srcExprs.get(i).matchType(tarExprs.get(j), allUsableVariables, tmp)){
					modifications.addAll(tmp);
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
		
		return modifications;
	}
	
	
}
