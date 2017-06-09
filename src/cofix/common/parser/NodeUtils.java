/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
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
				className = ((TypeDeclaration)parent).getName().getFullyQualifiedName();
				break;
			}
			parent = parent.getParent();
		}
		return new Pair<String, String>(className, methodName);
	}
	
	public static Map<String, Type> getUsableVarTypes(String file, int line){
		String content = JavaFile.readFileToString(file);
		CompilationUnit unit = (CompilationUnit) JavaFile.genASTFromSource(content, ASTParser.K_COMPILATION_UNIT);
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
				if(range.first() < _line && _line <= range.second()){
					Pair<String, Type> variable = entry.getKey();
					_vars.put(variable.first(), variable.second());
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
