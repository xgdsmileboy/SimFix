/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */

package cofix.common.parser.astnode.structure;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Type;

import cofix.common.parser.astnode.Expr;
import cofix.common.parser.astnode.expr.Variable;
import cofix.core.adapt.Modification;

public class Structure extends Expr{
	
	
	public static final String IF = "if";
	public static final String ELSE = "else";
	public static final String FOR = "for";
	public static final String WHILE = "while";
	public static final String BREAK = "break";
	public static final String CONTINUE = "continue";
	public static final String RETURN = "return";
	public static final String THRWO = "throw";
	
	
	private String _name = null;
	private ASTNode _srcNode = null; 
	private List<Expr> _exprs = null;
	
	public Structure(ASTNode node, String name){
		_srcNode = node;
		_name = name;
		_exprs = new ArrayList<>();
	}
	
	public ASTNode getOriginalASTNode(){
		return _srcNode;
	}
	
	public List<Expr> getExprs(){
		return _exprs;
	}
	
	public void addExpr(Expr expr){
		_exprs.add(expr);
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
		if(!(obj instanceof Structure)){
			return false;
		}
		Structure other = (Structure) obj;
		return toString().equals(other.toString());
	}
	
	@Override
	public String toString() {
		return _name;
	}

	@Override
	public Type getType() {
		return null;
	}

	@Override
	public boolean matchType(Expr expr, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		if(equals(expr)){
			Structure other = (Structure) expr;
			// TODO : how to match?
		}
		return false;
	}

	@Override
	public Expr adapt(Expr tar, Modification modify, Map<String, Type> allUsableVarMap) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Variable> getVariables() {
		List<Variable> variables = new ArrayList<>();
		for(Expr expr : _exprs){
			if(expr != null){
				variables.addAll(expr.getVariables());
			}
		}
		return variables;
	}

	@Override
	public void backup() {
		_backup = new Structure(_srcNode, _name);
		((Structure)_backup)._exprs = _exprs;
	}

	@Override
	public void restore() {
		Structure structure = (Structure)_backup;
		this._srcNode = structure.getOriginalASTnode();
		this._name = structure._name;
	}
	
}
