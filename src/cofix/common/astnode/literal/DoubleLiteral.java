/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */

package cofix.common.astnode.literal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.Type;

import cofix.common.astnode.Expr;
import cofix.common.astnode.expr.Variable;
import cofix.core.adapt.Delta;
import cofix.core.adapt.Revision;

public class DoubleLiteral extends Literal {

	private final double threshold = 1e-12;
	private double _value = 0.0;
	
	public DoubleLiteral(ASTNode node, double value) {
		_srcNode = node;
		_value = value;
	}
	
	@Override
	public Double getValue() {
		return _value;
	}

	@Override
	public Type getType() {
		AST ast = AST.newAST(AST.JLS8);
		return ast.newPrimitiveType(PrimitiveType.DOUBLE);
	}
	
	public NumberLiteral genAST(){
		AST ast = AST.newAST(AST.JLS8);
		return ast.newNumberLiteral(String.valueOf(_value));
	}
	
	@Override
	public int hashCode() {
		return Double.valueOf(_value).hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null){
			return false;
		}
		if(!(obj instanceof DoubleLiteral)){
			return false;
		}
		DoubleLiteral other = (DoubleLiteral) obj;
		return Math.abs(_value - other.getValue()) < threshold;
	}
	
	@Override
	public String toString() {
		return String.valueOf(_value);
	}

	@Override
	public boolean matchType(Expr expr, Map<String, Type> allUsableVariables, List<Delta> modifications) {
		//exactly match
		if(expr instanceof DoubleLiteral){
			DoubleLiteral other = (DoubleLiteral) expr;
			if(Math.abs(_value - other.getValue()) > threshold){
				Revision revision = new Revision(this);
				revision.setTar(expr);
				revision.setModificationComplexity(1);
				modifications.add(revision);
			}
			return true;
		} else if(expr != null){
			// type match
			Type type = expr.getType();
			if(type != null){
				String typeStr = type.toString();
				if(typeStr.equals("double") || type.equals("float") || type.equals("int")){
					Revision revision = new Revision(this);
					revision.setTar(expr);
					revision.setModificationComplexity(1);
					modifications.add(revision);
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public Expr adapt(Expr tar, Map<String, Type> allUsableVarMap) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Variable> getVariables() {
		return new ArrayList<>();
	}

	@Override
	public void backup() {
		_backup = new DoubleLiteral(_srcNode, _value);
	}

	@Override
	public void restore() {
		this._value = ((DoubleLiteral)_backup).getValue();
		this._srcNode = _backup.getOriginalASTnode();
	}

}
