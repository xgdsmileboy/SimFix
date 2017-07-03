/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.match;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.Type;

import cofix.common.util.Pair;
import cofix.core.metric.NewFVector;
import cofix.core.metric.Variable;
import cofix.core.metric.Variable.USE_TYPE;
import cofix.core.modify.Deletion;
import cofix.core.modify.Modification;
import cofix.core.parser.node.CodeBlock;
import cofix.core.parser.node.Node;
import cofix.core.parser.node.Node.TYPE;
import cofix.core.parser.node.expr.DoubleLiteral;

/**
 * @author Jiajun
 * @datae Jun 29, 2017
 */
public class CodeBlockMatcher {

	public static double getSimilarity(CodeBlock buggyCode, CodeBlock codeBlock){
		NewFVector buggy = buggyCode.getFeatureVector();
		NewFVector other = codeBlock.getFeatureVector();
		return buggy.computeSimilarity(other, NewFVector.ALGO.COSINE);
	}
	
	public static List<Modification> match(CodeBlock buggyBlock, CodeBlock similarBlock, Map<String, Type> allUsableVariables){
		List<Modification> modifications = new LinkedList<>();
		// match variables first
		Map<String, String> varTrans = matchVariables(buggyBlock.getVariables(), similarBlock.getVariables());
		
		for(Entry<String, String> entry : varTrans.entrySet()){
			System.out.println(entry.getKey() + " : " + entry.getValue());
		}
		
		
		List<Node> bNodes = buggyBlock.getParsedNode();
		List<Node> sNodes = similarBlock.getParsedNode();
		
		Map<Integer, Integer> match = new HashMap<>();
		Map<Integer, Integer> reverseMatch = new HashMap<>();
		for(int i = 0; i < bNodes.size(); i++){
			Node buggyNode = bNodes.get(i);
			for(int j = 0; j < sNodes.size(); j++){
				if(reverseMatch.containsKey(j)){
					continue;
				}
				Node simNode = sNodes.get(j);
				List<Modification> tmp = new LinkedList<>();
				if(buggyNode.match(simNode, varTrans, allUsableVariables, tmp)){
					match.put(i, j);
					reverseMatch.put(j, i);
					modifications.addAll(tmp);
				}
			}
		}
		
		// insert nodes at buggy code site
		for(int j = 0; j < sNodes.size(); j++){
			if(!reverseMatch.containsKey(j)){
				// TODO : insert node 
			}
		}
		
		// delete nodes at buggy code site
		for(int i = 0; i < bNodes.size(); i++){
			if(!match.containsKey(i)){
				modifications.add(new Deletion(null, i, null, TYPE.UNKNOWN));
			}
		}
		
		return modifications;
	}
	
	private static Map<String, String> matchVariables(List<Variable> bVars, List<Variable> sVars){
		Map<String, String> matchingMap = new HashMap<>();
		
		Map<Variable, List<USE_TYPE>> bMap = new HashMap<>();
		for(Variable variable : bVars){
			List<USE_TYPE> list = bMap.get(variable);
			if(list == null){
				list = new ArrayList<>();
			}
			list.add(variable.getUseType());
			bMap.put(variable, list);
		}
			
		Map<Variable, List<USE_TYPE>> sMap = new HashMap<>();
		for(Variable variable : sVars){
			List<USE_TYPE> list = sMap.get(variable);
			if(list == null){
				list = new ArrayList<>();
			}
			list.add(variable.getUseType());
			sMap.put(variable, list);
		}
		
		Map<String, Pair<String, Double>> matching = new HashMap<>();
		for(Entry<Variable, List<USE_TYPE>> sim : sMap.entrySet()){
			String simName = sim.getKey().getName();
			for(Entry<Variable, List<USE_TYPE>> buggy : bMap.entrySet()){
				String buggyName = buggy.getKey().getName();
				Double similarity = LCS(sim.getValue(), buggy.getValue());
				if(similarity > 0.5){
					Pair<String, Double> pair = matching.get(simName);
					if(pair == null || pair.getSecond() < similarity){
						pair = new Pair<String, Double>(buggyName, similarity);
						matching.put(simName, pair);
					}
				}
			}
		}
		
		for(Entry<String, Pair<String, Double>> entry : matching.entrySet()){
			matchingMap.put(entry.getKey(), entry.getValue().getFirst());
		}
		
		return matchingMap;
	}
	
	
	private static double LCS(List<USE_TYPE> first, List<USE_TYPE> snd){
		int firstLen = first.size();
		int sndLen = snd.size();
		if(firstLen == 0 || sndLen == 0){
			return 0;
		}
		int[][] score = new int[firstLen + 1][sndLen + 1];
		for(int i = 0; i < firstLen; i++){
			for(int j = 0; j < sndLen; j++){
				if(first.get(i).equals(snd.get(j))){
					score[i + 1][j + 1] = score[i][j] + 1; 
				} else {
					score[i + 1][j + 1] = Math.max(score[i + 1][j], score[i][j + 1]);
				}
			}
		}
		double s = (double)(score[firstLen][sndLen] * 2) / (double)(firstLen + sndLen);
		return s;
	}
	
}
