/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Modifier;
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

/**
 * @author Jiajun
 * @datae Jun 2, 2017
 */
public class NodeUtils {

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
		if(parent == null){
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
					className = ((TypeDeclaration)parent).getName().getFullyQualifiedName();
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
			if(string.startsWith("\\*")){
				comment_start_flag = true;
				continue;
			}
			// comment end for multi-lines
			if(string.endsWith("*/")){
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
