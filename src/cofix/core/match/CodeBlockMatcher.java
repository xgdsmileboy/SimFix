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
import org.eclipse.jdt.core.util.ISignatureAttribute;

import com.gzoltar.core.components.Method;
import com.sun.org.apache.xalan.internal.xsltc.compiler.NodeTest;
import com.sun.org.apache.xpath.internal.SourceTreeManager;
import com.sun.org.apache.xpath.internal.operations.Mod;

import cofix.common.util.Pair;
import cofix.core.metric.MethodCall;
import cofix.core.metric.NewFVector;
import cofix.core.metric.Variable;
import cofix.core.metric.Variable.USE_TYPE;
import cofix.core.modify.Deletion;
import cofix.core.modify.Insertion;
import cofix.core.modify.Modification;
import cofix.core.modify.Revision;
import cofix.core.parser.NodeUtils;
import cofix.core.parser.node.CodeBlock;
import cofix.core.parser.node.Node;
import cofix.core.parser.node.Node.TYPE;
import cofix.core.parser.node.expr.SName;
import cofix.core.parser.node.stmt.BreakStmt;
import cofix.core.parser.node.stmt.ContinueStmt;
import cofix.core.parser.node.stmt.DoStmt;
import cofix.core.parser.node.stmt.ForStmt;
import cofix.core.parser.node.stmt.IfStmt;
import cofix.core.parser.node.stmt.ReturnStmt;
import cofix.core.parser.node.stmt.ThrowStmt;
import cofix.core.parser.node.stmt.VarDeclarationStmt;
import cofix.core.parser.node.stmt.WhileStmt;
import cofix.core.parser.search.BuggyCode;
import sun.security.provider.MD2;
import sun.security.x509.UniqueIdentity;

/**
 * @author Jiajun
 * @date Jun 29, 2017
 */
public class CodeBlockMatcher {

	public static double getSimilarity(CodeBlock buggyCode, CodeBlock codeBlock){
		NewFVector buggy = buggyCode.getFeatureVector();
		NewFVector other = codeBlock.getFeatureVector();
		return buggy.computeSimilarity(other, NewFVector.ALGO.COSINE);
	}
	
	public static double getRewardSimilarity(CodeBlock buggyCode, CodeBlock codeBlock){
		List<Variable> buggyVars = buggyCode.getVariables();
		List<Variable> simVars = codeBlock.getVariables();
		int total = buggyVars.size() + simVars.size();
		double varReward = 0;
		if(total != 0){
			double varSame = 0;
			for(Variable variable : buggyVars){
				if(simVars.contains(variable)){
					varSame += 1.0;
				}
			}
			varReward = varSame * 2.0 / (double)total;;
		}
		
		
		
		List<MethodCall> buggyMethodCalls = buggyCode.getMethodCalls();
		List<MethodCall> simMethodCalls = codeBlock.getMethodCalls();
		int allMethods = buggyMethodCalls.size() + simMethodCalls.size();
		double methodReward = 0;
		if(allMethods != 0){
			double methodSame = 0;
			for(MethodCall methodCall : simMethodCalls){
				for(MethodCall buggy : buggyMethodCalls){
					if(methodCall.getName().equals(buggy.getName())){
						methodSame += 1.0;
					}
				}
			}
			methodReward = methodSame * 2.0 / (double)allMethods;
		}
		
		return varReward + methodReward;
	}
	
	public static List<Modification> match(CodeBlock buggyBlock, CodeBlock similarBlock, Map<String, Type> allUsableVariables){
		List<Modification> modifications = new LinkedList<>();
		
		// match variables first
		Map<String, String> varTrans = matchVariables(buggyBlock, similarBlock);
		
//		for(Entry<String, String> entry : varTrans.entrySet()){
//			System.out.println(entry.getKey() + " : " + entry.getValue());
//		}
		
		
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
					break;
				}
			}
		}
		
		// insert nodes at buggy code site only some node has been matched
		if(match.size() > 0){
			for(int j = 0; j < sNodes.size(); j++){
				if(!reverseMatch.containsKey(j)){
					Node tarNode = sNodes.get(j);
					if (tarNode instanceof ReturnStmt || tarNode instanceof ThrowStmt || tarNode instanceof BreakStmt
							|| tarNode instanceof ContinueStmt || tarNode instanceof WhileStmt
							|| tarNode instanceof ForStmt || tarNode instanceof DoStmt || tarNode instanceof VarDeclarationStmt) {
						continue;
					}
					Map<SName, Pair<String, String>> record = NodeUtils.tryReplaceAllVariables(tarNode, varTrans, allUsableVariables);
					if(record == null){
						continue;
					}
					int nextMatchIndex = -1;
					for(int index = j; index < sNodes.size(); index++){
						if(reverseMatch.containsKey(index)){
							nextMatchIndex = reverseMatch.get(index);
							break;
						}
					}
					if(nextMatchIndex == -1){
						int last = nextMatchIndex;
						for(; last >= 0; last --){
							Node node = bNodes.get(last);
							if(!(node instanceof ReturnStmt) && !(node instanceof ThrowStmt) && !(node instanceof BreakStmt) && !(node instanceof ContinueStmt)){
								List<Variable> bVariables = node.getVariables();
								List<Variable> sVariables = tarNode.getVariables();
								boolean dependency = false;
								for(Variable variable : sVariables){
									if(bVariables.contains(variable)){
										dependency = true;
										break;
									}
								}
								if(!dependency){
									break;
								}
							}
						}
						nextMatchIndex = last >= 0 ? last : 0;
					}
					
					NodeUtils.replaceVariable(record);
					String target = tarNode.toSrcString().toString();
					NodeUtils.restoreVariables(record);
					Insertion insertion = new Insertion(buggyBlock, nextMatchIndex, target, TYPE.UNKNOWN);
					modifications.add(insertion);
					
				}
			}
		}
		
//		// delete nodes at buggy code site
//		if(buggyBlock.getParsedNode().size() > 2){
//			for(int i = 0; i < bNodes.size(); i++){
//				if(!match.containsKey(i)){
//					modifications.add(new Deletion(buggyBlock, i, null, TYPE.UNKNOWN));
//				}
//			}
//		}
		
//		//remove duplicate modifications
//		List<Modification> unique = new LinkedList<>();
//		for (Modification modification : modifications) {
//			boolean exist = false;
//			for (Modification u : unique) {
//				if (u.getRevisionTypeID() == modification.getRevisionTypeID()
//						&& u.getSourceID() == modification.getSourceID()
//						&& u.getTargetString().equals(modification.getTargetString())
//						&& u.getSrcNode() == modification.getSrcNode()) {
//					exist = true;
//					break;
//				}
//			}
//			if(!exist){
//				unique.add(modification);
//			}
//		}
//		modifications = unique;
		
		// revision first
		List<Modification> revisions = new LinkedList<>();
		List<Modification> insertions = new LinkedList<>();
		List<Modification> deletions = new LinkedList<>();
		List<Modification> finalModifications = new ArrayList<>(modifications.size());
		for(Modification modification : modifications){
			if(modification instanceof Revision){
				revisions.add(modification);
			} else if(modification instanceof Insertion){
//				if(modification.getTargetString().startsWith("if(")){
//					finalModifications.add(modification);
//				} else {
					insertions.add(modification);
//				}
			} else {
				deletions.add(modification);
			}
		}
		
		finalModifications.addAll(revisions);
		finalModifications.addAll(insertions);
		finalModifications.addAll(deletions);
		
		return finalModifications;
	}
	
	private static Map<String, String> matchVariables(CodeBlock buggyBlock, CodeBlock similarBlock){
		
		List<Variable> bVars = buggyBlock.getVariables();
		List<Variable> sVars = similarBlock.getVariables();
		
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
		
		Map<String, Integer> buggyNameMap = new HashMap<>();
		Map<Integer, String> reverseBuggyNameMap = new HashMap<>(); 
		int i = 0;
		for(Variable variable : bMap.keySet()){
			buggyNameMap.put(variable.getName(), i);
			reverseBuggyNameMap.put(i, variable.getName());
			i++;
		}
		Map<String, Integer> simNameMap = new HashMap<>();
		Map<Integer, String> reverseSimNameMap = new HashMap<>();
		int j = 0;
		for(Variable variable : sMap.keySet()){
			simNameMap.put(variable.getName(), j);
			reverseSimNameMap.put(j, variable.getName());
			j++;
		}
		if(i < 1 || j < 1){
			return new HashMap<>();
		}
		double[][] similarityTable = new double[j][i];
		
		for(Entry<Variable, List<USE_TYPE>> sim : sMap.entrySet()){
			String simName = sim.getKey().getName();
			for(Entry<Variable, List<USE_TYPE>> buggy : bMap.entrySet()){
				String buggyName = buggy.getKey().getName();
				Double similarity = 0.7 * LCS(sim.getValue(), buggy.getValue()) + 0.1 * NodeUtils.nameSimilarity(simName, buggyName) + 0.2 * NodeUtils.typeSimilarity(sim.getKey().getType(), buggy.getKey().getType());
				if(similarity > 0.5){
					similarityTable[simNameMap.get(simName)][buggyNameMap.get(buggyName)] = similarity;
				}
			}
		}
		
		
		List<MethodCall> buggyCalls = buggyBlock.getMethodCalls();
		List<MethodCall> simCalls = similarBlock.getMethodCalls();
		
		for(MethodCall methodCall : buggyCalls){
			for(MethodCall other : simCalls){
				if(methodCall.equals(other)){
					Map<String, String> map = methodCall.matchArgs(other);
					for(Entry<String, String> entry : map.entrySet()){
						Integer srcIndex = buggyNameMap.get(entry.getKey());
						Integer tarIndex = simNameMap.get(entry.getValue());
						if(srcIndex != null && tarIndex != null){
							similarityTable[tarIndex][srcIndex] += 0.2;
						}
					}
				}
			}
		}
		
		Map<String, String> varMap = tryMatch(similarityTable, reverseSimNameMap, reverseBuggyNameMap);
		return varMap;
	}
	private static Map<String, String> tryMatch(double[][] similarityTable, Map<Integer, String> reverseSimNameMap, Map<Integer, String> reverseBuggyNameMap){
		Map<String, String> matchTable = new HashMap<>();
		int rowGuard = similarityTable.length;
		int colGuard = similarityTable[0].length;
		while(true){
			double currentBiggest = 0.1;
			int row = 0;
			int colum = 0;
			for(int i = 0; i < rowGuard; i++){
				for(int j = 0; j < colGuard; j++){
					if(similarityTable[i][j] > currentBiggest){
						currentBiggest = similarityTable[i][j];
						row = i;
						colum = j;
					}
				}
			}
			if(currentBiggest > 0.1){
				matchTable.put(reverseSimNameMap.get(row), reverseBuggyNameMap.get(colum));
				for(int j = 0; j < colGuard; j++){
					similarityTable[row][j] = 0;
				}
				for(int i = 0; i < rowGuard; i++){
					similarityTable[i][colum] = 0;
				}
			} else {
				break;
			}
		}
		return matchTable;
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
