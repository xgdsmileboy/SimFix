/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package fl.visitor;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

/**
 * @author Jiajun
 * @date Jul 18, 2017
 */
public class FindMethodVisitor extends ASTVisitor{
	
	private int _line = 0; 
	private String _method = null;
	private CompilationUnit _unit = null;
	private String _fileName = null;
	
	public FindMethodVisitor(int line, String fileName) {
		_line = line;
		_fileName = fileName;
	}
	
	public String getWrapMethod(){
		return _method;
	}
	
	public boolean visit(CompilationUnit unit){
		_unit = unit;
		return true;
	}
	
	public boolean visit(MethodDeclaration node){
		int start = _unit.getLineNumber(node.getStartPosition());
		int end = _unit.getLineNumber(node.getStartPosition() + node.getLength());
		if(start <= _line && _line <= end){
			_method = buildMethodInfoString(node);
			return false;
		}
		return true;
	}
	
	private String getFullClazzName(MethodDeclaration node) {
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
		if(!clazz.equals(_fileName)){
			clazz = _fileName + "$" + clazz;
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
	
	private  String buildMethodInfoString(MethodDeclaration node) {
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
}
