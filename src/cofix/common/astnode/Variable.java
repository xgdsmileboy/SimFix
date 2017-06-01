/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */

package cofix.common.astnode;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Type;

public class Variable extends Expr {
	
	private Type _type = null; 
	private String _name = null;
	
	public Variable(ASTNode node, Type type, String name) {
		if(type == null){
			AST ast = AST.newAST(AST.JLS8);
			type = ast.newWildcardType();
		}
		_srcNode = node;
		_type = type;
		_name = name;
	}
	
	public Type getType(){
		return _type;
	}
	
	public String getName(){
		return _name;
	}
	
	@Override
	public int hashCode() {
		return _name.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null){
			return false;
		}
		if(!(obj instanceof Variable)){
			return false;
		}
		Variable other = (Variable) obj;
		
		if(!_name.equals(other.getName())){
			return false;
		}
		if(_type == null){
			return other.getType() == null;
		} else {
			if(other.getType() == null){
				return false;
			}
			return _type.toString().equals(other.getType().toString());
		}
	}
	
	@Override
	public String toString() {
		return _name + "(" + _type + ")";
	}
	
}
