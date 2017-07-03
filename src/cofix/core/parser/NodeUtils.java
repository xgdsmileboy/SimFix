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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.sound.midi.Soundbank;

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

import cofix.common.util.JavaFile;
import cofix.common.util.Pair;
import cofix.core.metric.Variable;
import cofix.core.modify.Deletion;
import cofix.core.modify.Insertion;
import cofix.core.modify.Modification;
import cofix.core.modify.Revision;
import cofix.core.parser.node.Node;
import cofix.core.parser.node.Node.TYPE;
import cofix.core.parser.node.expr.Expr;
import cofix.core.parser.node.expr.SName;

/**
 * @author Jiajun
 * @datae Jun 2, 2017
 */
public class NodeUtils {

	public static boolean maybeSameName(String name1, String name2){
		boolean same = false;
		// remove digital at the end
		int index = name1.length() - 1;
		name1 = name1.replace("_", "");
		while(index > 0 && Character.isDigit(name1.charAt(index))){
			index --;
		}
		name1 = name1.substring(0, index);
		
		index = name2.length() - 1;
		name2 = name2.replace("_", "");
		while(index > 0 && Character.isDigit(name2.charAt(index))){
			index --;
		}
		name2 = name2.substring(0, index);
		
		if(name1.equals(name2)){
			return true;
		}
		
		// longest common continuous sub-sequence
		int[][]c = new int[name1.length()+1][name2.length()+1];
        int maxlen = 0;
        for(int i = 1; i <= name1.length(); i++){
            for(int j = 1; j <= name2.length(); j++){
                if(name1.charAt(i-1) == name2.charAt(j-1)){
                    c[i][j] = c[i-1][j-1] + 1;
                    if(c[i][j] > maxlen){
                        maxlen = c[i][j];
                    }
                }
            }
        }
        
        double value = (maxlen * 2.0) / (name1.length() + name2.length());
        same = value > 0.5 ? true : false;
		return same;
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
	
	public static List<Modification> handleArguments(Node currNode, int srcID, TYPE nodeType, List<Expr> srcArg, List<Expr> tarArgs, Map<String, Type> allUsableVariables){
		List<Modification> modifications = new ArrayList<>();
		if(srcArg.size() == tarArgs.size()){
			Set<Integer> change = new HashSet<>();
			for(int i = 0; i < srcArg.size(); i++){
				Expr sExpr = srcArg.get(i);
				Expr tExpr = tarArgs.get(i);
				if(!sExpr.toSrcString().equals(tExpr.toSrcString())){
					List<Variable> variables = tExpr.getVariables();
					boolean canReplaceDirectly = true;
					for(Variable var : variables){
						if(!allUsableVariables.containsKey(var.getName())){
							canReplaceDirectly = false;
						}
					}
					if(canReplaceDirectly){
						change.add(i);
					}
				}
			}
			if(change.size() == 0){
				return modifications;
			}
			
			// change one argument each time
			for(Integer index : change){
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
				Revision revision = new Revision(currNode, srcID, stringBuffer.toString(), nodeType);
				modifications.add(revision);
			}
			//change all arguments one time
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
			Revision revision = new Revision(currNode, srcID, stringBuffer.toString(), nodeType);
			modifications.add(revision);
		} else if(srcArg.size() > tarArgs.size()){
			Set<Integer> matchRec = new HashSet<>();
			for(int i = 0; i < tarArgs.size(); i++){
				boolean findSame = false;
				for(int j = 0; j < srcArg.size(); j++){
					if(matchRec.contains(j)){
						continue;
					}
					if(tarArgs.get(i).toSrcString().equals(srcArg.get(j).toSrcString())){
						matchRec.add(j);
						findSame = true;
						break;
					}
				}
				if(!findSame){
					for(int j = 0; j < srcArg.size(); j++){
						if(matchRec.contains(j)){
							continue;
						}
						if(srcArg.get(j).getType().toString().equals(tarArgs.get(i).getType().toString())){
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
			Deletion deletion = new Deletion(currNode, srcID, stringBuffer.toString(), nodeType);
			modifications.add(deletion);
		} else {
			int[] matchRec = new int[tarArgs.size()];
			for(int i = 0; i < tarArgs.size(); i++){
				matchRec[i] = -1;
			}
			for(int i = 0; i < srcArg.size(); i++){
				boolean findSame = false;
				for(int j = 0; j < tarArgs.size(); j++){
					if(matchRec[j] != -1){
						continue;
					}
					if(srcArg.get(i).toSrcString().equals(tarArgs.get(j).toSrcString())){
						matchRec[j] = i;
						findSame = true;
						break;
					}
				}
				if(!findSame){
					for(int j = 0; j < tarArgs.size(); j++){
						if(matchRec[j] != -1){
							continue;
						}
						if(tarArgs.get(j).getType().toString().equals(srcArg.get(i).getType().toString())){
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
				if(!allUsableVariables.containsKey(tarArgs.get(0).toSrcString())){
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
			Insertion insertion = new Insertion(currNode, srcID, stringBuffer.toString(), nodeType);
			modifications.add(insertion);
		}
		
		return modifications;
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
		for (int i = 0; i < srcNodeList.size(); i++) {
			boolean findMatching = false;
			for(int j = 0; j < otherLen; j++){
				if(record[j] != -1){
					continue;
				}
				List<Modification> tmp = new ArrayList<>();
				if(srcNodeList.get(i).match(tarNodeList.get(j), varTrans, allUsableVariables, tmp)){
					record[j] = i;
					findMatching = true;
					modifications.addAll(tmp);
					break;
				}
			}
			if(!findMatching){
				Deletion deletion = new Deletion(currNode, i, "", nodeType);
				modifications.add(deletion);
			}
		}
		StringBuffer stringBuffer = new StringBuffer();
		int shouldIns = 1000;
		for(int i = 0; i < otherLen; i++){
			if(record[i] == -1){
				int index = -1;
				for(int j = i + 1; j < otherLen; j ++){
					if (record[j] != -1) {
						index = record[j];
						break;
					}
				}
				if(index != -1){
					Node inset = tarNodeList.get(i);
					Map<SName, Pair<String, String>> revision = NodeUtils.tryReplaceAllVariables(inset, varTrans, allUsableVariables);
					if(revision != null){
						NodeUtils.replaceVariable(revision);
						String target = inset.toSrcString().toString();
						stringBuffer.append(target + "\n");
						shouldIns = shouldIns > index ? index : shouldIns;
						Insertion insertion = new Insertion(currNode, index, target, nodeType);
						modifications.add(insertion);
						NodeUtils.restoreVariables(revision);
					}
				}
			}
		}
		if(stringBuffer.toString().length() > 0){
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
				Type type = sName.getType();
				String replace = varTrans.get(name);
				boolean matched = false;
				// try to replace variable using matched one
				if(replace != null){
					Type replaceType = allUsableVariables.get(replace) ;
					String typeStr = replaceType == null ? "?" : replaceType.toString();
					if(typeStr.equals(type.toString())){
						matched = true;
						record.put(sName, new Pair<String, String>(name, replace));
					}
				}
				// failed to replace variable
				if(!matched){
					if(!allUsableVariables.containsKey(name)){
						boolean findRepalcement = false;
						for(Entry<String, Type> entry : allUsableVariables.entrySet()){
							Type type2 = entry.getValue();
							String typeStr = type2 == null ? "?" : type2.toString();
							if(type.toString().equals(typeStr)){
								record.put(sName, new Pair<String, String>(name, entry.getKey()));
								findRepalcement = true;
								break;
							}
						}
						if(!findRepalcement){
							return null;
						}
					}
				}
			}
		}
		return record;
	}
	
	public static boolean replaceExpr(int srcID, Expr srcExpr, Expr tarExpr, Map<String, String> varTrans, Map<String, Type> allUsableVariables, List<Modification> modifications){
		if(srcExpr.getType().toString().equals(tarExpr.getType().toString())){
			Map<SName, Pair<String, String>> record = tryReplaceAllVariables(tarExpr, varTrans, allUsableVariables);
			if(record != null){
				//replace all variable
				replaceVariable(record);
				Revision revision = new Revision(srcExpr.getParent(), srcID, tarExpr.toSrcString().toString(), srcExpr.getNodeType());
				modifications.add(revision);
				//restore all variable
				restoreVariables(record);
			}
			return true;
		} else {
			return false;
		}
	}
	
	public static boolean canReplace(String op1, String op2){
		if(op1 == null || op2 == null || op1.equals(op2)){
			return false;
		}
		switch(op1){
		case "*":
		case "/":
		case "+":
		case "-":
		case "%":
			switch(op2){
			case "*":
			case "/":
			case "+":
			case "-":
			case "%":
				return true;
			default :
				return false;
			}
		case "<<":
		case ">>":
		case ">>>":
		case "^":
		case "&":
		case "|":
			switch(op2){
			case "<<":
			case ">>":
			case ">>>":
			case "^":
			case "&":
			case "|":
				return true;
			default :
				return false;
			}
		case "<":
		case ">":
		case "<=":
		case ">=":
		case "==":
		case "!=":
			switch(op2){
			case "<":
			case ">":
			case "<=":
			case ">=":
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
		case "++":
		case "--":
			switch(op2){
			case "++":
			case "--":
				return true;
			default :
				return false;
			}
		default :
			return false;
		}
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
				if(range.getFirst() < _line && _line <= range.getSecond()){
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
