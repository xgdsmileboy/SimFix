/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */

package cofix.common.parser.astnode.expr;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Type;

import cofix.common.parser.NodeUtils;
import cofix.common.parser.astnode.Expr;
import cofix.core.adapt.Modification;
import cofix.core.adapt.Revision;

public class Variable extends Expr {
	
	private Type _type = null; 
	private String _name = null;
	private Expr _replace = null;
	private ASTNode _newnode = null;
	
	public Variable(ASTNode node, Type type, String name) {
		if(type == null){
			type = node.getAST().newWildcardType();
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
		if(_replace != null){
			return _replace.toString();
		}
		return _name + "(" + _type + ")";
	}

	@Override
	public boolean matchType(Expr expr, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		if(expr != null && canReplace(expr, allUsableVariables)){
			if(!(expr instanceof Variable) || !((Variable)expr).getName().equals(_name)){
				Revision revision = new Revision(this);
				AST ast = AST.newAST(AST.JLS8);
				ASTNode node = ASTNode.copySubtree(ast, expr.getOriginalASTnode());
				revision.setTar(expr, node);
				revision.setModificationComplexity(1);
				modifications.add(revision);
				return true;
			}
		}
		return false;
	}

	@Override
	public Expr adapt(Expr tar, Modification modify, Map<String, Type> allUsableVarMap) {
		Expr newExpr = null;
		if(tar instanceof Variable){
			// replace variable with another one with same type
			Variable var = (Variable)tar;
			this._name = var.getName();
			newExpr = this;
		} else {
			// use another expression replace current variable
			List<Variable> variables = tar.getVariables();
			for(Variable variable : variables){
				String name = variable.getName(); 
				if(!name.equals("THIS")){
					Type type = allUsableVarMap.get(name);
					if(!type.toString().equals(variable.getType().toString())){
						return this;
					}
				}
			}
			newExpr = tar;
			_replace = tar;
			_srcNode = NodeUtils.replace(_srcNode, tar.getOriginalASTnode());
		}
		return newExpr;
	}
	
	@Override
	public List<Variable> getVariables() {
		List<Variable> variables = new ArrayList<>();
		variables.add(this);
		return variables;
	}

	@Override
	public void backup() {
		AST ast = AST.newAST(AST.JLS8);
		ASTNode node = ASTNode.copySubtree(ast, _srcNode);
		_backup = new Variable(node, _type, _name);
	}

	@Override
	public void restore() {
		NodeUtils.replace(_srcNode, _backup.getOriginalASTnode());
//		this._replace = null;
//		Variable var = (Variable)_backup;
//		this._srcNode = var.getOriginalASTnode();
//		this._type = var.getType();
//		this._name = var.getName();
	}
	
}
