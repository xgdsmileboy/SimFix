/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.WildcardType;

import cofix.common.util.JavaFile;
import cofix.common.util.Pair;
import cofix.core.metric.Variable;
import cofix.core.modify.Insertion;
import cofix.core.modify.Modification;
import cofix.core.modify.Revision;
import cofix.core.parser.node.Node;
import cofix.core.parser.node.Node.TYPE;
import cofix.core.parser.node.expr.Assign;
import cofix.core.parser.node.expr.ConditionalExpr;
import cofix.core.parser.node.expr.DoubleLiteral;
import cofix.core.parser.node.expr.Expr;
import cofix.core.parser.node.expr.FieldAcc;
import cofix.core.parser.node.expr.FloatLiteral;
import cofix.core.parser.node.expr.InfixExpr;
import cofix.core.parser.node.expr.IntLiteral;
import cofix.core.parser.node.expr.LongLiteral;
import cofix.core.parser.node.expr.MethodInv;
import cofix.core.parser.node.expr.NumLiteral;
import cofix.core.parser.node.expr.QName;
import cofix.core.parser.node.expr.SName;
import cofix.core.parser.node.stmt.BreakStmt;
import cofix.core.parser.node.stmt.ContinueStmt;
import cofix.core.parser.node.stmt.DoStmt;
import cofix.core.parser.node.stmt.ForStmt;
import cofix.core.parser.node.stmt.ReturnStmt;
import cofix.core.parser.node.stmt.ThrowStmt;
import cofix.core.parser.node.stmt.VarDeclarationStmt;
import cofix.core.parser.node.stmt.WhileStmt;

/**
 * @author Jiajun
 * @date Jun 2, 2017
 */
public class NodeUtils {

	public static double typeSimilarity(Type t1, Type t2){
		if(t1 == null || t2 == null){
			return 0.0;
		}
		if(t1.toString().equals(t2.toString()) || (t1.isPrimitiveType() && t2.isPrimitiveType() && isWidenType(t1, t2))){
			return 1.0;
		}
		return 0.0;
	}
	
	public static double nameSimilarity(String name1, String name2){
		// remove digital at the end
		name1 = name1.replace("_", "");
		int index = name1.length() - 1;
		while(index > 0 && Character.isDigit(name1.charAt(index))){
			index --;
		}
		name1 = name1.substring(0, index + 1);
		
		name2 = name2.replace("_", "");
		index = name2.length() - 1;
		while(index > 0 && Character.isDigit(name2.charAt(index))){
			index --;
		}
		name2 = name2.substring(0, index + 1);
		
		if(name1.equals(name2)){
			return 1.0;
		}
		
		Set<String> set1 = new HashSet<>();
		int lower = 0;
		for(int i = 0; i < name1.length(); i++){
			if(Character.isUpperCase(name1.charAt(i))){
				String subName = name1.substring(lower, i);
				lower = i;
				set1.add(subName);
			} else if(name1.charAt(i) == '_'){
				String subName = name1.substring(lower, i);
				lower = i + 1;
				set1.add(subName);
			}
		}
		set1.add(name1.substring(lower));
		
		Set<String> set2 = new HashSet<>();
		lower = 0;
		for(int i = 0; i < name2.length(); i++){
			if(Character.isUpperCase(name2.charAt(i))){
				String subName = name2.substring(lower, i);
				lower = i;
				set2.add(subName);
			} else if(name2.charAt(i) == '_'){
				String subName = name2.substring(lower, i);
				lower = i + 1;
				set2.add(subName);
			}
		}
		
		set2.add(name2.substring(lower));
		
		double count = 0;
		for(String string : set1){
			if(set2.contains(string)){
				count ++;
			}
		}
		
		return (count * 2.0) / (set1.size() + set2.size());
		
		
//		// longest common continuous sub-sequence
//		int[][]c = new int[name1.length()+1][name2.length()+1];
//        int maxlen = 0;
//        for(int i = 1; i <= name1.length(); i++){
//            for(int j = 1; j <= name2.length(); j++){
//                if(name1.charAt(i-1) == name2.charAt(j-1)){
//                    c[i][j] = c[i-1][j-1] + 1;
//                    if(c[i][j] > maxlen){
//                        maxlen = c[i][j];
//                    }
//                }
//            }
//        }
//        
//        double value = (maxlen * 2.0) / (name1.length() + name2.length());
//		return value;
	}
	
	public static void replaceVariable(Map<SName, Pair<String, String>> record){
		//replace all variable
		for(Entry<SName, Pair<String, String>> entry : record.entrySet()){
			entry.getKey().setName(entry.getValue().getSecond());
		}
	}
	
	public static void restoreVariables(Map<SName, Pair<String, String>> record){
		//restore all variable
		for(Entry<SName, Pair<String, String>> entry : record.entrySet()){
			entry.getKey().setName(entry.getValue().getFirst());
		}
	}
	
	public static List<Modification> handleArguments(Node currNode, int srcID, TYPE nodeType, List<Expr> srcArg, List<Expr> tarArgs, Map<String, String> varTrans, Map<String, Type> allUsableVariables){
		List<Modification> modifications = new ArrayList<>();
		StringBuffer original = new StringBuffer();
		if(srcArg.size() > 0){
			original.append(srcArg.get(0).toSrcString());
		}
		for(int i = 1; i < srcArg.size(); i ++){
			original.append(",");
			original.append(srcArg.get(i).toSrcString());
		}
		String originalArgStr = original.toString();
		if(srcArg.size() == tarArgs.size()){
			Set<Integer> change = new HashSet<>();
			Map<Integer, Map<SName, Pair<String, String>>> changeMap = new HashMap<>();
			for(int i = 0; i < srcArg.size(); i++){
				Expr sExpr = srcArg.get(i);
				Expr tExpr = tarArgs.get(i);
				if(!canReplaceArg(sExpr) || !canReplaceArg(tExpr)){
					continue;
				}
				String sString = sExpr.toSrcString().toString();
				String tString = tExpr.toSrcString().toString();
				if(sString.equals(tString)){
					if(varTrans.containsKey(tString) && varTrans.get(tString).equals(sString)){
						continue;
					}
				}
				Map<SName, Pair<String, String>> tmMap = tryReplaceAllVariables(tExpr, varTrans, allUsableVariables);
				if(tmMap != null){
					changeMap.put(i, tmMap);
					change.add(i);
				} 
//				else {
//					change = new HashSet<>();
//					break;
//				}
			}
			if(change.size() == 0){
				return modifications;
			}
			int revisionCount = 0;
			// change one argument each time
			for(Integer index : change){
				Map<SName, Pair<String, String>> record = changeMap.get(index);
				if(record != null){
					NodeUtils.replaceVariable(record);
				}
				StringBuffer stringBuffer = new StringBuffer();
				if(index == 0){
					stringBuffer.append(tarArgs.get(0).toSrcString());
				} else {
					stringBuffer.append(srcArg.get(0).toSrcString());
				}
				for(int i = 1; i < srcArg.size(); i ++){
					stringBuffer.append(",");
					if(i == index){
						stringBuffer.append(tarArgs.get(i).toSrcString());
					} else {
						stringBuffer.append(srcArg.get(i).toSrcString());
					}
				}
				String target = stringBuffer.toString();
				if(!originalArgStr.equals(target)){
					revisionCount ++;
					Revision revision = new Revision(currNode, srcID, target, nodeType);
					modifications.add(revision);
				}
				if(record != null){
					NodeUtils.restoreVariables(record);
				}
			}
			
			//change all arguments one time
			if(revisionCount > 1){
				for(Entry<Integer, Map<SName, Pair<String, String>>> entry : changeMap.entrySet()){
					NodeUtils.replaceVariable(entry.getValue());
				}
				StringBuffer stringBuffer = new StringBuffer();
				if(change.contains(0)){
					stringBuffer.append(tarArgs.get(0).toSrcString());
				} else {
					stringBuffer.append(srcArg.get(0).toSrcString());
				}
				
				for(int i = 1; i < srcArg.size(); i++){
					if(change.contains(i)){
						stringBuffer.append(",");
						stringBuffer.append(tarArgs.get(i).toSrcString());
					} else {
						stringBuffer.append(",");
						stringBuffer.append(tarArgs.get(i).toSrcString());
					}
				}
				String target = stringBuffer.toString();
				if(!originalArgStr.equals(target)){
					Revision revision = new Revision(currNode, srcID, target, nodeType);
					modifications.add(revision);
				}
				for(Entry<Integer, Map<SName, Pair<String, String>>> entry : changeMap.entrySet()){
					NodeUtils.restoreVariables(entry.getValue());
				}
			}
		} else if(srcArg.size() > tarArgs.size() && srcArg.size() <= tarArgs.size() + 2){
			Set<Integer> matchRec = new HashSet<>();
			for(int i = 0; i < tarArgs.size(); i++){
				boolean findSame = false;
				Expr tExpr = tarArgs.get(i);
				if(!canReplaceArg(tExpr)){
					continue;
				}
				for(int j = 0; j < srcArg.size(); j++){
					Expr sExpr = srcArg.get(j);
					if(!canReplaceArg(sExpr) || matchRec.contains(j)){
						continue;
					}
					if(tExpr.toSrcString().toString().equals(sExpr.toSrcString().toString())){
						matchRec.add(j);
						findSame = true;
						break;
					}
				}
				if(!findSame){
					for(int j = 0; j < srcArg.size(); j++){
						Expr sExpr = srcArg.get(j);
						if(!canReplaceArg(sExpr) || matchRec.contains(j)){
							continue;
						}
						if(sExpr.getType().toString().equals(tExpr.getType().toString())){
							matchRec.add(j);
							findSame = true;
							break;
						}
					}
					if(!findSame){
						return modifications;
					}
				}
			}
			// up to now, each argument in tarArgs is matched with one in srcArg, 
			// but some one in srcArg matched nothing, should be delete 
			boolean first = true;
			StringBuffer stringBuffer = new StringBuffer();
			if(matchRec.size() > 0 && matchRec.size() > srcArg.size() - 2){
				for(int i = 0; i < srcArg.size(); i++){
					// matched argument
					if(matchRec.contains(i)){
						if(first){
							first = false;
							stringBuffer.append(srcArg.get(i).toSrcString());
						} else {
							stringBuffer.append(",");
							stringBuffer.append(srcArg.get(i).toSrcString());
						}
					}
				}
				Revision revision = new Revision(currNode, srcID, stringBuffer.toString(), nodeType);
				modifications.add(revision);
			}
		} else if (srcArg.size() < tarArgs.size() && srcArg.size() + 2 >= tarArgs.size()){
			int[] matchRec = new int[tarArgs.size()];
			for(int i = 0; i < tarArgs.size(); i++){
				matchRec[i] = -1;
			}
			for(int i = 0; i < srcArg.size(); i++){
				boolean findSame = false;
				Expr sExpr = srcArg.get(i);
				if(!canReplaceArg(sExpr)){
					continue;
				}
				for(int j = 0; j < tarArgs.size(); j++){
					Expr tExpr = tarArgs.get(j);
					if(!canReplaceArg(tExpr) || matchRec[j] != -1){
						continue;
					}
					if(sExpr.toSrcString().toString().equals(tExpr.toSrcString().toString())){
						matchRec[j] = i;
						findSame = true;
						break;
					}
				}
				if(!findSame){
					for(int j = 0; j < tarArgs.size(); j++){
						Expr tExpr = tarArgs.get(i);
						if(!canReplaceArg(tExpr) || matchRec[j] != -1){
							continue;
						}
						if(tExpr.getType().toString().equals(sExpr.getType().toString())){
							matchRec[j] = i;
							findSame = true;
							break;
						}
					}
					if(!findSame){
						return modifications;
					}
				}
			}
			// up to now, each argument in srcArg is matched to one in tarArgs
			// but some arguments in tarArgs are not matched, we should try to add them
			StringBuffer stringBuffer = new StringBuffer();
			if(matchRec[0] == -1){
				if(!allUsableVariables.containsKey(tarArgs.get(0).toSrcString().toString())){
					return modifications;
				}
				stringBuffer.append(tarArgs.get(0).toSrcString());
			} else {
				stringBuffer.append(srcArg.get(matchRec[0]).toSrcString());
			}
			for(int i = 1; i < tarArgs.size(); i++){
				stringBuffer.append(",");
				if(matchRec[i] == -1){
					if(!allUsableVariables.containsKey(tarArgs.get(0).toSrcString())){
						return modifications;
					}
					stringBuffer.append(tarArgs.get(i).toSrcString());
				} else {
					stringBuffer.append(srcArg.get(matchRec[i]).toSrcString());
				}
			}
			Revision insertion = new Revision(currNode, srcID, stringBuffer.toString(), nodeType);
			modifications.add(insertion);
		}
		
		return modifications;
	}
	
	private static boolean canReplaceArg(Expr expr){
		if(expr instanceof SName || expr instanceof QName || expr instanceof FieldAcc){
			return true;
		}
		return false;
	}
	
	public static boolean isSameNodeType(Node src, Node tar){
		if(isConstant(src)){
			if(isConstant(tar)){
				return true;
			}else {
				return false;
			}
		} else {
			if(isConstant(tar)){
				return false;
			} else {
				return true;
			}
		}
	}
	
	private static boolean isConstant(Node tar){
		if(tar instanceof NumLiteral){
			return true;
		}
		if(tar instanceof QName){
			QName qName = (QName) tar;
			if(Character.isUpperCase(qName.getLabel().charAt(0)) && Character.isUpperCase(qName.getIdentifier().charAt(0))){
				return true;
			}
		} else if(tar instanceof SName){
			SName sName = (SName)tar;
			if(sName.getName().toUpperCase().equals(sName.getName()) || sName.getName().equals("NaN")){
				return true;
			}
		}
		return false;
	}
	
	public static boolean conditionalMatch(Expr node, int id, ConditionalExpr conditionalExpr, Map<String, String> varTrans, Map<String, Type> allUsableVariables, List<Modification> modifications){
		boolean match = false;
		Expr first = conditionalExpr.getfirst();
		Expr second = conditionalExpr.getSecond();
		List<Modification> tmp = new ArrayList<>();
		if(node.match(first, varTrans, allUsableVariables, tmp)){
			match = true;
			modifications.addAll(tmp);
			Expr condition = conditionalExpr.getCondition();
			Map<SName, Pair<String, String>> record = NodeUtils.tryReplaceAllVariables(condition, varTrans, allUsableVariables);
			if(record != null){
				NodeUtils.replaceVariable(record);
				String conditionString = condition.toSrcString().toString();
				NodeUtils.restoreVariables(record);
				record = NodeUtils.tryReplaceAllVariables(second, varTrans, allUsableVariables);
				String otherSide = "";
				if(record != null && second.getType().toString().equals(node.getType().toString())){
					NodeUtils.replaceVariable(record);
					otherSide = second.toSrcString().toString();
					NodeUtils.restoreVariables(record);
				} else {
					otherSide = NodeUtils.getDefaultValue(node.getType());
				}
				String newStr = conditionString + "?" + node.toSrcString().toString() + ":" + otherSide;
				Revision revision = new Revision(node, id, newStr, node.getNodeType());
				modifications.add(revision);
			}
		} else {
			tmp = new ArrayList<>();
			if(node.match(second, varTrans, allUsableVariables, tmp)){
				match = true;
				modifications.addAll(tmp);
				Expr condition = conditionalExpr.getCondition();
				Map<SName, Pair<String, String>> record = NodeUtils.tryReplaceAllVariables(condition, varTrans, allUsableVariables);
				if(record != null){
					NodeUtils.replaceVariable(record);
					String conditionString = condition.toSrcString().toString();
					NodeUtils.restoreVariables(record);
					record = NodeUtils.tryReplaceAllVariables(first, varTrans, allUsableVariables);
					String otherSide = "";
					if(record != null && first.getType().toString().equals(node.getType().toString())){
						NodeUtils.replaceVariable(record);
						otherSide = first.toSrcString().toString();
						NodeUtils.restoreVariables(record);
					} else {
						otherSide = NodeUtils.getDefaultValue(node.getType());
					}
					String newStr = conditionString + "?" + otherSide + ":" + node.toSrcString().toString();
					Revision revision = new Revision(node, id, newStr, node.getNodeType());
					modifications.add(revision);
				}
			}
		}
		return match;
	}

	public static boolean nodeMatchList(Node node, List<? extends Node> tarList,
			Map<String, String> varTrans, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		boolean match = false;
		for (Node child : tarList) {
			List<Modification> tmp = new ArrayList<>();
			if (node.match(child, varTrans, allUsableVariables, tmp)) {
				match = true;
				modifications.addAll(tmp);
			}
		}
		return match;
	}

	public static List<Modification> listNodeMatching(Node currNode, Node.TYPE nodeType,
			List<? extends Node> srcNodeList, List<? extends Node> tarNodeList, Map<String, String> varTrans,
			Map<String, Type> allUsableVariables) {
		List<Modification> modifications = new ArrayList<>();
		if (srcNodeList == null || tarNodeList == null) {
			return modifications;
		}
		int otherLen = tarNodeList.size();
		int[] record = new int[otherLen];
		for (int i = 0; i < otherLen; i++) {
			record[i] = -1;
		}
		boolean existMatchNode = false;
		for (int i = 0; i < srcNodeList.size(); i++) {
			boolean findMatching = false;
			for(int j = 0; j < otherLen; j++){
				if(record[j] != -1){
					continue;
				}
				List<Modification> tmp = new ArrayList<>();
				if(srcNodeList.get(i).match(tarNodeList.get(j), varTrans, allUsableVariables, tmp)){
					existMatchNode = true;
					record[j] = i;
					findMatching = true;
					modifications.addAll(tmp);
					break;
				}
			}
//			if(!findMatching){
//				Deletion deletion = new Deletion(currNode, i, "", nodeType);
//				modifications.add(deletion);
//			}
		}
		StringBuffer stringBuffer = new StringBuffer();
		int shouldIns = 1000;
		int insertCount = 0;
		if(existMatchNode){
			for(int i = 0; i < otherLen; i++){
				if(record[i] == -1){
					int index = 0;
					for(int j = i + 1; j < otherLen; j ++){
						if (record[j] != -1) {
							index = record[j];
							break;
						}
					}
					if(index != -1){
						Node insert = tarNodeList.get(i);
						if(insert instanceof ReturnStmt || insert instanceof ThrowStmt || insert instanceof BreakStmt || insert instanceof ContinueStmt){
							if(index != srcNodeList.size() - 1){
								continue;
							}
						}
						if(insert instanceof WhileStmt || insert instanceof ForStmt || insert instanceof DoStmt || insert instanceof VarDeclarationStmt){
							continue;
						}
						int last = index;
						for(; last >= 0; last --){
							Node node = srcNodeList.get(last);
							if(!(node instanceof ReturnStmt) && !(node instanceof ThrowStmt) && !(node instanceof BreakStmt) && !(node instanceof ContinueStmt)){
								List<Variable> bVariables = node.getVariables();
								List<Variable> sVariables = insert.getVariables();
								boolean dependency = false;
								for(Variable variable : sVariables){
									if(bVariables.contains(variable)){
										dependency = true;
										break;
									}
								}
								if(dependency){
									index = last - 1;
								}
							} else {
								index = last - 1;
							}
						}
						index = index >= 0 ? index : 0;
							
						String tarString = insert.simplify(varTrans, allUsableVariables);
						if(tarString != null){
							insertCount ++;
							stringBuffer.append(tarString + "\n");
							shouldIns = shouldIns > index ? index : shouldIns;
							Insertion insertion = new Insertion(currNode, index, tarString, nodeType);
							modifications.add(insertion);
						}
					}
				}
			}
		}
		if(insertCount > 1){
			Insertion insertion = new Insertion(currNode, shouldIns, stringBuffer.toString(), nodeType);
			modifications.add(insertion);
		}
		return modifications;
	}
	
	public static Map<SName, Pair<String, String>> tryReplaceAllVariables(Node replaceNode, Map<String, String> varTrans, Map<String, Type> allUsableVariables){
		Map<SName, Pair<String, String>> record = new HashMap<>();
		Set<Variable> variables = new HashSet<>(replaceNode.getVariables());
		// find replacement for each variable
		for(Variable variable : variables){
			if(variable.getNode() instanceof SName){
				SName sName = (SName) variable.getNode();
				String name = sName.getName();
				Node parent = sName.getParent();
				if(parent instanceof QName){
					QName qName = (QName) parent;
					if(name.equals(qName.getIdentifier()) && !"this".equals(qName.getLabel())){
						continue;
					}
				}
				Type type = sName.getType();
				String replace = varTrans.get(name);
				boolean matched = false;
				// try to replace variable using matched one
				if(replace != null){
					Type replaceType = allUsableVariables.get(replace) ;
					String typeStr = replaceType == null ? "?" : replaceType.toString();
					if(typeStr.equals(type.toString()) || type.toString().equals("?") || typeStr.equals("?")){
						matched = true;
						if(!name.equals(replace)){
							record.put(sName, new Pair<String, String>(name, replace));
						}
					}
				}
				// failed to replace variable
				if(!matched){
					if(!allUsableVariables.containsKey(name)){
						Expr expr = sName.getDirectDependency();
						Node sNameParent = sName.getParent();
						boolean isDependencyExpr = false;
						if(sNameParent instanceof Assign){
							if(sName == ((Assign)sNameParent).getLhs()){
								isDependencyExpr = true;
							}
						}
						boolean canUse = false;
						if(expr != null && !(expr instanceof NumLiteral) && !isDependencyExpr){
							sName.setDirectDependency(null);
							Map<SName, Pair<String, String>> tryReplace = tryReplaceAllVariables(expr, varTrans, allUsableVariables);
							sName.setDirectDependency(expr);
							if(tryReplace != null){
								boolean duplicate = false;
								if (expr instanceof SName) {
									for (Entry<SName, Pair<String, String>> entry : tryReplace.entrySet()) {
										for (Entry<SName, Pair<String, String>> exist : record.entrySet()) {
											if (duplicate || (!exist.getKey().getName().equals(name) && exist.getValue()
													.getSecond().equals(entry.getValue().getSecond()))) {
												duplicate = true;
												break;
											}
										}
									}
								}
								if(!duplicate){
									canUse = true;
									NodeUtils.replaceVariable(tryReplace);
									String replaceName = expr.toSrcString().toString();
									record.put(sName, new Pair<String, String>(name, replaceName));
									NodeUtils.restoreVariables(tryReplace);
								}
							}
						}
						if(!canUse){
							Set<String> candidates = new HashSet<>();
							for(Entry<String, Type> entry : allUsableVariables.entrySet()){
								Type type2 = entry.getValue();
								String typeStr = type2 == null ? "?" : type2.toString();
								if(type.toString().equals(typeStr)){
									if(!varTrans.values().contains(entry.getKey())){
//										record.put(sName, new Pair<String, String>(name, entry.getKey()));
										candidates.add(entry.getKey());
									}
								}
							}
							if(candidates.size() == 0){
								return null;
							} else {
								String replaceName = "";
								double similarity = -1;
								for(String candidate : candidates){
									double value = nameSimilarity(candidate, name); 
									if(value > similarity){
										similarity = value;
										replaceName = candidate;
									}
								}
								record.put(sName, new Pair<String, String>(name, replaceName));
							}
						}
					}
				}
			}
		}
		return record;
	}
	
	public static boolean replaceExpr(int srcID, String avoid, Expr srcExpr, Expr tarExpr, Map<String, String> varTrans, Map<String, Type> allUsableVariables, List<Modification> modifications){
		if(srcExpr.toSrcString().toString().equals(tarExpr.toSrcString().toString())){
			return true;
		}
		if(srcExpr.toSrcString().toString().length() < 2){
			return false;
		}
		if(srcExpr instanceof MethodInv){
			if(tarExpr instanceof MethodInv){
				MethodInv srcMethod = (MethodInv) srcExpr;
				MethodInv tarMethod = (MethodInv) tarExpr;
				List<Modification> tmp = new LinkedList<>();
				if (srcMethod.getExpression() != null && tarMethod.getExpression() != null) {
					Expr src = srcMethod.getExpression();
					Expr tar = tarMethod.getExpression();
					if ((src instanceof SName || tar instanceof SName)
							&& ((NodeUtils.isClass(src.toSrcString().toString())
									&& !NodeUtils.isClass(tar.toSrcString().toString()))
									|| (!NodeUtils.isClass(src.toSrcString().toString())
											&& NodeUtils.isClass(tar.toSrcString().toString())))) {
						return false;
					} else if(!srcMethod.getExpression().match(tarMethod.getExpression(), varTrans, allUsableVariables,tmp)){
						return false;
					}
				}
			} else {
				return false;
			}
		} else if(tarExpr instanceof MethodInv){
			return false;
		}
		if(srcExpr instanceof InfixExpr){
			if(!(tarExpr instanceof InfixExpr)){
				return false;
			} else if(!canReplaceOperator(((InfixExpr)srcExpr).getOperator(), ((InfixExpr)tarExpr).getOperator())){
				return false;
			}
		} else if(tarExpr instanceof InfixExpr){
			InfixExpr infixExpr = (InfixExpr) tarExpr;
			if(infixExpr.getLhs() instanceof NumLiteral && NodeUtils.isBoundaryValue((NumLiteral) infixExpr.getLhs())){
				return false;
			} else if(infixExpr.getRhs() instanceof NumLiteral && NodeUtils.isBoundaryValue((NumLiteral) infixExpr.getRhs())){
				return false;
			}
		}
		Type srcType = srcExpr.getType();
		Type tarType = tarExpr.getType();
		if (srcType.toString().equals(tarType.toString()) || srcType instanceof WildcardType
				|| tarType instanceof WildcardType) {
			Map<SName, Pair<String, String>> record = tryReplaceAllVariables(tarExpr, varTrans, allUsableVariables);
			if (record != null) {
				// replace all variable
				replaceVariable(record);
				String target = tarExpr.toSrcString().toString();
				if (!srcExpr.toSrcString().toString().equals(target) && !avoid.equals(target)) {
					Revision revision = new Revision(srcExpr.getParent(), srcID, target, srcExpr.getNodeType());
					modifications.add(revision);
				}
				// restore all variable
				restoreVariables(record);
			}
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean isBoundaryValue(NumLiteral literal){
		boolean isBoundary = false;
		double epsilon = 1e-5;
		if(literal instanceof DoubleLiteral){
			double value = ((DoubleLiteral)literal).getValue();
			if(Math.abs(value - 1.0) < epsilon || Math.abs(value - 0) < epsilon || Math.abs(value + 1) < epsilon){
				isBoundary = true;
			}
		} else if(literal instanceof FloatLiteral){
			float value = ((FloatLiteral)literal).getValue();
			if(Math.abs(value - 1.0) < epsilon || Math.abs(value - 0) < epsilon || Math.abs(value + 1) < epsilon){
				isBoundary = true;
			}
		} else if(literal instanceof LongLiteral){
			long value = ((LongLiteral)literal).getValue();
			if(value == 0l || value == 1l || value == -1l || value == Long.MAX_VALUE || value == Long.MIN_VALUE){
				isBoundary = true;
			}
		} else if(literal instanceof IntLiteral){
			int value = ((IntLiteral)literal).getValue();
			if(value == 0l || value == 1l || value == -1l || value == Integer.MAX_VALUE || value == Integer.MIN_VALUE){
				isBoundary = true;
			}
		}
		
		return isBoundary;
	}
	
	public static String getDefaultValue(Type type){
		if(type == null){
			return "null";
		}
		if(isPrimitiveType(type)){
			switch(type.toString()){
			case "Boolean":
			case "boolean": return "false";
			case "Integer":
			case "int": return "0";
			case "Float":
			case "float": return "0f";
			case "Double":
			case "double": return "0d";
			case "Short":
			case "short": return "0";
			case "Long":
			case "long": return "0l";
			case "Character":
			case "char": return "' '";
			default : return null;
			}
		} else {
			return "null";
		}
	}
	
	private static boolean isPrimitiveType(Type type){
		if(type == null){
			return false;
		}
		if(type.isPrimitiveType()){
			return true;
		}
		switch(type.toString()){
		case "Integer":
		case "Long":
		case "Float":
		case "Short":
		case "Double":
			return true;
		}
		return false;
	}
	
	public static boolean skipMethodCall(String expression, String name){
		if(expression == null || name == null){
			return false;
		}
		boolean skip = false;
		switch(expression){
		case "Double":
			switch(name){
			case "isNaN":
			case "isInfinite":
				// TODO : some others
				skip = true;
				break;
			}
			break;
		case "Math":
			switch(name){
			case "abs":
			case "max":
			case "min":
			case "sqrt":
				// TODO : some others
				skip = true;
				break;
			}
			break;
		}
		return skip;
	}
	
	public static boolean compatibleType(Type type1, Type type2){
		String type2Str = type2.toString();
		if(type2Str.equals("?")){
			return true;
		}
		if(isPrimitiveType(type1)){
			if(isPrimitiveType(type2)){
				return type1.toString().equals(type2Str) || isWidenType(type1, type2) || isWidenType(type2, type1);
			}
			if(type2Str.equals("Serializable") || type2Str.startsWith("Comparable<")){
				return true;
			}
		} else {
			if(isPrimitiveType(type2)){
				return false;
			}
			// parse child-parent type relation in source code
			return ProjectInfo.isParentType(type1.toString(), type2Str);
		}
		return false;
	}
	
	public static boolean canReplaceOperator(String op1, String op2){
		if(op1 == null || op2 == null){
			return false;
		}
		switch(op1){
//		case "*":
//		case "/":
//		case "+":
//		case "-":
//		case "%":
//			switch(op2){
//			case "*":
//			case "/":
//			case "+":
//			case "-":
//			case "%":
//				return true;
//			default :
//				return false;
//			}
//		case "<<":
//		case ">>":
//		case ">>>":
//		case "^":
//		case "&":
//		case "|":
//			switch(op2){
//			case "<<":
//			case ">>":
//			case ">>>":
//			case "^":
//			case "&":
//			case "|":
//				return true;
//			default :
//				return false;
//			}
		case "<":
		case "<=":
			switch(op2){
			case "<":
			case "<=":
				return true;
			default :
				return false;
			}
		case ">":
		case ">=":
			switch(op2){
			case ">":
			case ">=":
				return true;
			default :
				return false;
			}
		case "==":
		case "!=":
			switch(op2){
			case "==":
			case "!=":
				return true;
			default :
				return false;
			}
		case "&&":
		case "||":
			switch(op2){
			case "&&":
			case "||":
				return true;
			default :
				return false;
			}
//		case "++":
//		case "--":
//			switch(op2){
//			case "++":
//			case "--":
//				return true;
//			default :
//				return false;
//			}
		default :
			return false;
		}
	}
	
	public static boolean isOperator(String operator){
		if(operator == null){
			return false;
		}
		switch(operator){
		case "<":
		case ">":
		case "<=":
		case ">=":
		case "==":
		case "!=":
		case "&&":
		case "||":
			return true;
		default :
			return false;
		}
	}
	
	public static boolean isClass(String name){
		if(name == null) return false;
		return Character.isUpperCase(name.charAt(0)) && !name.toUpperCase().equals(name);
	}
	
	public static Type parseExprType(Expr left, String operator, Expr right){
		if(left == null){
			return parsePreExprType(right, operator);
		}
		
		if(right == null){
			return parsePostExprType(left, operator);
		}
		
		AST ast = AST.newAST(AST.JLS8);
		switch(operator){
		case "*":
		case "/":
		case "+":
		case "-":
			Type type = union(left.getType(), right.getType());
			if(type == null){
				type = ast.newPrimitiveType(PrimitiveType.DOUBLE);
			}
			return type;
		case "%":
		case "<<":
		case ">>":
		case ">>>":
		case "^":
		case "&":
		case "|":
			return ast.newPrimitiveType(PrimitiveType.INT);
		case "<":
		case ">":
		case "<=":
		case ">=":
		case "==":
		case "!=":
		case "&&":
		case "||":
			return ast.newPrimitiveType(PrimitiveType.BOOLEAN);
		default :
			return null;
		}
		
	}
	
	public static boolean isWidenType(Type ty1, Type ty2){
		if(ty1 == null || ty2 == null){
			return false;
		}
		if(ty1.toString().equals(ty2.toString())){
			return false;
		}
		Type union = union(ty1, ty2);
		if(union != null && union.toString().equals(ty2.toString())){
			return true;
		}
		return false;
	}
	
	private static Type union(Type ty1, Type ty2){
		if(ty1 == null){
			return ty2;
		} else if(ty2 == null){
			return ty1;
		}
		
		if(!ty1.isPrimitiveType() || !ty2.isPrimitiveType()){
			return null;
		}
		
		String ty1String = ty1.toString().toLowerCase().replace("integer", "int");
		String ty2String = ty2.toString().toLowerCase().replace("integer", "int");
		
		AST ast = AST.newAST(AST.JLS8);
		if(ty1String.equals("double") || ty2String.equals("double")){
			
			return ast.newPrimitiveType(PrimitiveType.DOUBLE);
			
		} else if(ty1String.equals("float") || ty2String.equals("float")){
			
			return ast.newPrimitiveType(PrimitiveType.FLOAT);
			
		} else if(ty1String.equals("long") || ty2String.equals("long")){
			
			return ast.newPrimitiveType(PrimitiveType.LONG);
			
		} else if(ty1String.equals("int") || ty2String.equals("int")){
			
			return ast.newPrimitiveType(PrimitiveType.INT);
			
		} else if(ty1String.equals("short") || ty2String.equals("short")){
			
			return ast.newPrimitiveType(PrimitiveType.SHORT);
			
		} else {
			
			return ast.newPrimitiveType(PrimitiveType.BYTE);
			
		}
		
	}
	
	private static Type parsePostExprType(Expr expr, String operator){
		// ++/--
		AST ast = AST.newAST(AST.JLS8);
		return ast.newPrimitiveType(PrimitiveType.INT);
	}
	
	private static Type parsePreExprType(Expr expr, String operator){
		AST ast = AST.newAST(AST.JLS8);
		switch(operator){
		case "++":
		case "--":
			return ast.newPrimitiveType(PrimitiveType.INT);
		case "+":
		case "-":
			return expr.getType();
		case "~":
		case "!":
			return ast.newPrimitiveType(PrimitiveType.BOOLEAN);
		default :
			return null;
		}
	}
	
	public static ASTNode replace(ASTNode node, ASTNode replace){
		ASTNode replacement = ASTNode.copySubtree(node.getAST(), replace);
		ASTNode parent = node.getParent();
		if(parent instanceof InfixExpression){
			InfixExpression infixExpression = (InfixExpression) parent;
			if(node.equals(infixExpression.getLeftOperand())){
				infixExpression.setLeftOperand((Expression) replacement);
			} else {
				infixExpression.setRightOperand((Expression) replacement);
			}
		} else if(parent instanceof IfStatement){
			IfStatement ifStatement = (IfStatement) parent;
			if(ifStatement.getExpression().equals(node)){
				ifStatement.setExpression((Expression) replacement);
			} else {
				System.out.println("match if body statement");
			}
		}
		return replacement;
	}
	
	private static String getFullClazzName(MethodDeclaration node) {
		String clazz = "";
		// filter those methods that defined in anonymous classes
		ASTNode parent = node.getParent();
		while (parent != null) {
			if (parent instanceof ClassInstanceCreation) {
				return null;
			} else if(parent instanceof TypeDeclaration){
				clazz = ((TypeDeclaration) parent).getName().getFullyQualifiedName();
				break;
			} else if(parent instanceof EnumDeclaration){
				clazz = ((EnumDeclaration) parent).getName().getFullyQualifiedName();
				break;
			}
			parent = parent.getParent();
		}
		if(parent != null){
			while(parent != null){
				if(parent instanceof CompilationUnit){
					String packageName = ((CompilationUnit) parent).getPackage().getName().getFullyQualifiedName();
					clazz = packageName + "." + clazz;
					return clazz;
				}
				parent = parent.getParent();
			}
		}
		return null;
	}
	
	public static  String buildMethodInfoString(MethodDeclaration node) {
		String currentClassName = getFullClazzName(node);
		if (currentClassName == null) {
			return null;
		}
		StringBuffer buffer = new StringBuffer(currentClassName + "#");

		String retType = "?";
		if (node.getReturnType2() != null) {
			retType = node.getReturnType2().toString();
		}
		StringBuffer param = new StringBuffer("?");
		for (Object object : node.parameters()) {
			if (!(object instanceof SingleVariableDeclaration)) {
				param.append(",?");
			} else {
				SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) object;
				param.append("," + singleVariableDeclaration.getType().toString());
			}
		}
		// add method return type
		buffer.append(retType + "#");
		// add method name
		buffer.append(node.getName().getFullyQualifiedName() + "#");
		// add method params, NOTE: the first parameter starts at index 1.
		buffer.append(param);
		return buffer.toString();
	}
	
	public static Pair<String, String> getTypeDecAndMethodDec(ASTNode node) {
		ASTNode parent = node.getParent();
		String methodName = null;
		String className = null;
		while(parent != null){
			if(parent instanceof MethodDeclaration){
				MethodDeclaration methodDeclaration = (MethodDeclaration) parent; 
				methodName = methodDeclaration.getName().getFullyQualifiedName();
				String params = "";
				for(Object obj : methodDeclaration.parameters()){
					SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) obj;
					params += ","+singleVariableDeclaration.getType().toString();
				}
				methodName += params;
			} else if(parent instanceof TypeDeclaration){
				TypeDeclaration typeDeclaration = (TypeDeclaration) parent;
				if(Modifier.isPublic(typeDeclaration.getModifiers()) && className != null){
					className = typeDeclaration.getName().getFullyQualifiedName() + "$" + className;
				} else {
					if(className == null) {
						className = ((TypeDeclaration)parent).getName().getFullyQualifiedName();
					}
				}
			} else if(parent instanceof EnumDeclaration){
				className = ((EnumDeclaration)parent).getName().getFullyQualifiedName();
			}
			parent = parent.getParent();
		}
		return new Pair<String, String>(className, methodName);
	}
	
	public static int getValidLineNumber(ASTNode statement){
		if(statement == null){
			return 0;
		}
		String[] contents = statement.toString().split("\n");
		int line = 0;
		boolean comment_start_flag = false;
		for(String string : contents){
			string = string.trim();
			// empty line
			if(string.length() == 0){
				continue;
			}
			// comment for single line
			if(string.startsWith("//")){
				continue;
			}
			// comment start for multi-lines
			if(string.startsWith("/*")){
				comment_start_flag = true;
				continue;
			}
			// comment end for multi-lines
			if(string.contains("*/")){
				comment_start_flag = false;
				continue;
			}
			// comment in multi-lines
			if(comment_start_flag){
				continue;
			}
			// meaningless lines
			if(string.equals("{") || string.equals("}")){
				continue;
			}
			line ++;
		}
		return line;
	}
	
	public static List<ASTNode> getAllSiblingNodes(ASTNode node){
		List<ASTNode> siblings = new ArrayList<>();
		StructuralPropertyDescriptor structuralPropertyDescriptor = node.getLocationInParent();
		if (structuralPropertyDescriptor == null) {
			return siblings;
		} else if(structuralPropertyDescriptor.isChildListProperty()){
			List list = (List) node.getParent().getStructuralProperty(structuralPropertyDescriptor);
			for(Object object : list){
				if(object instanceof ASTNode){
					siblings.add((ASTNode) object);
				}
			}
		} 
//		else if(structuralPropertyDescriptor.isChildProperty()){
//			ASTNode child = (ASTNode) node.getParent().getStructuralProperty(structuralPropertyDescriptor);
//			siblings.add(child);
//		}
		return siblings;
 	}
	
	public static Map<String, Type> getUsableVarTypes(String file, int line){
		CompilationUnit unit = JavaFile.genASTFromFile(file);
		VariableVisitor variableVisitor = new VariableVisitor(line, unit);
		unit.accept(variableVisitor);
		return variableVisitor.getVars();
	}
	
}

class VariableVisitor extends ASTVisitor {
	private Map<String, Type> _vars = new HashMap<>();
	private int _line = 0;
	private CompilationUnit _unit;
	
	public VariableVisitor(int line, CompilationUnit unit) {
		_line = line;
		_unit = unit;
	}
	
	public boolean visit(FieldDeclaration node) {
		Type type = node.getType();
		for(Object object: node.fragments()){
			VariableDeclarationFragment vdf = (VariableDeclarationFragment) object;
			_vars.put(vdf.getName().toString(), type);
		}
		return true;
	}
	
	public Map<String, Type> getVars(){
		return _vars;
	}
	
	@Override
	public boolean visit(MethodDeclaration node) {
		int start = _unit.getLineNumber(node.getStartPosition());
		int end = _unit.getLineNumber(node.getStartPosition() + node.getLength());
		if(start <= _line && _line <= end){
			for(Object object : node.parameters()){
				SingleVariableDeclaration svd = (SingleVariableDeclaration) object;
				_vars.put(svd.getName().toString(), svd.getType());
			}
			
			if(node.getBody() != null){
				MethodVisitor methodVisitor = new MethodVisitor();
				node.getBody().accept(methodVisitor);
				methodVisitor.dumpVarMap();
			}
			return false;
		}
		return true;
	}
	
	class MethodVisitor extends ASTVisitor {
		Map<Pair<String, Type>, Pair<Integer, Integer>> _tmpVars = new HashMap<>();

		public void dumpVarMap() {
			for(Entry<Pair<String, Type>, Pair<Integer, Integer>> entry : _tmpVars.entrySet()){
				Pair<Integer, Integer> range = entry.getValue();
				if(range.getFirst() <= _line && _line <= range.getSecond()){
					Pair<String, Type> variable = entry.getKey();
					_vars.put(variable.getFirst(), variable.getSecond());
				}
			}
		}

		public boolean visit(VariableDeclarationStatement node) {
			ASTNode parent = node.getParent();
			while(parent != null){
				if(parent instanceof Block){
					break;
				}
				parent = parent.getParent();
			}
			if(parent != null) {
				int start = _unit.getLineNumber(node.getStartPosition());
				int end = _unit.getLineNumber(parent.getStartPosition() + parent.getLength());
				for (Object o : node.fragments()) {
					VariableDeclarationFragment vdf = (VariableDeclarationFragment) o;
					Pair<String, Type> pair = new Pair<String, Type>(vdf.getName().getFullyQualifiedName(), node.getType());
					Pair<Integer, Integer> range = new Pair<Integer, Integer>(start, end);
					_tmpVars.put(pair, range);
				}
			}
			return true;
		}

		public boolean visit(VariableDeclarationExpression node) {
			ASTNode parent = node.getParent();
			while(parent != null){
				if(parent instanceof Block || parent instanceof ForStatement){
					break;
				}
				parent = parent.getParent();
			}
			if(parent != null) {
				int start = _unit.getLineNumber(node.getStartPosition());
				int end = _unit.getLineNumber(parent.getStartPosition() + parent.getLength());
				for (Object o : node.fragments()) {
					VariableDeclarationFragment vdf = (VariableDeclarationFragment) o;
					Pair<String, Type> pair = new Pair<String, Type>(vdf.getName().getFullyQualifiedName(), node.getType());
					Pair<Integer, Integer> range = new Pair<Integer, Integer>(start, end);
					_tmpVars.put(pair, range);
				}
			}
			return true;
		}
		
		public boolean visit(SingleVariableDeclaration node){
			ASTNode parent = node.getParent();
			while(parent != null){
				if(parent instanceof Block || parent instanceof ForStatement || parent instanceof IfStatement || parent instanceof EnhancedForStatement || parent instanceof WhileStatement){
					break;
				}
				parent = parent.getParent();
			}
			if(parent != null) {
				int start = _unit.getLineNumber(node.getStartPosition());
				int end = _unit.getLineNumber(parent.getStartPosition() + parent.getLength());
				Pair<String, Type> pair = new Pair<String, Type>(node.getName().getFullyQualifiedName(), node.getType());
				Pair<Integer, Integer> range = new Pair<Integer, Integer>(start, end);
				_tmpVars.put(pair, range);
			}
			return true;
		}
		
	}
	
}
