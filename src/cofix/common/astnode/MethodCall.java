/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */

package cofix.common.astnode;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Type;

public class MethodCall extends Expr{
	
	protected Expr _expr = null;
	protected String _name = null;
	protected Type _retType = null;
	protected List<Expr> _parameters = null;
	
	public MethodCall(ASTNode node, Type retType, Expr expr, String name) {
		this(node, retType, expr, name, new ArrayList<>());
	}
	
	public MethodCall(ASTNode node, Type retType, Expr expr, String name, List<Expr> parameters){
		if(retType == null){
			AST ast = AST.newAST(AST.JLS8);
			retType = ast.newWildcardType();
		}
		_srcNode = node;
		_retType = retType;
		_expr = expr;
		_name = name;
		_parameters = parameters;
	}
	
	@Override
	public Type getType() {
		return _retType;
	}
	
	public Type getExprType(){
		if(_expr == null){
			AST ast = AST.newAST(AST.JLS8);
			return ast.newWildcardType();
		}
		return _expr.getType();
	}
	
	public Expr getExpr(){
		return _expr;
	}
	
	public String getName(){
		return _name;
	}
	
	public List<Expr> getParameters(){
		return _parameters;
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
		if(!(obj instanceof MethodCall)){
			return false;
		}
		MethodCall other = (MethodCall) obj;
		// same method name
		if(!_name.equals(other.getName())){
			return false;
		}
		// method in same clazz
		if(!getExprType().toString().equals(other.getExprType().toString())){
			return false;
		}
		// return type should be same
		if(!getType().toString().equals(other.getType().toString())){
			return false;
		}
		// TODO : parameters should be equal ?
		
		return true;
	}
	
	@Override
	public String toString() {
		StringBuffer stringBuffer = new StringBuffer();
		if(_expr != null){
//			Type type = _expr.getType();
			stringBuffer.append(_expr);
//			stringBuffer.append("(");
//			if(type == null){
//				stringBuffer.append("null");
//			} else {
//				stringBuffer.append(type);
//			}
//			stringBuffer.append(")");
			stringBuffer.append(".");
		} else {
		}
		stringBuffer.append(_name);
		stringBuffer.append("(");
		if(_parameters.size() > 0){
			stringBuffer.append(_parameters.get(0));
		}
		for(int i = 1; i < _parameters.size(); i++){
			stringBuffer.append(",");
			stringBuffer.append(_parameters.get(i));
		}
		stringBuffer.append(")");
		return stringBuffer.toString();
	}

}
