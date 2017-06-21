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

/**
 * @author Jiajun
 *
 */
public class LongLiteral extends Literal {
	
	private Long _value = 0l;
	
	public LongLiteral(ASTNode node, long value) {
		_srcNode = node;
		_value = value;
	}
	
	@Override
	public Long getValue() {
		return _value;
	}
	
	@Override
	public Type getType() {
		AST ast = AST.newAST(AST.JLS8);
		return ast.newPrimitiveType(PrimitiveType.LONG);
	}
	
	@Override
	public NumberLiteral genAST() {
		AST ast = AST.newAST(AST.JLS8);
		return ast.newNumberLiteral(String.valueOf(_value));
	}
	
	@Override
	public int hashCode() {
		return Long.valueOf(_value).hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null){
			return false;
		}
		if(!(obj instanceof LongLiteral)){
			return false;
		}
		LongLiteral other = (LongLiteral)obj;
		return this._value == other.getValue();
	}
	
	@Override
	public String toString() {
		return String.valueOf(_value);
	}

	@Override
	public boolean matchType(Expr expr, Map<String, Type> allUsableVariables, List<Delta> modifications) {
		// exactly match
		if(expr instanceof LongLiteral){
			LongLiteral other = (LongLiteral) expr;
			if(_value != other.getValue()){
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
				if(typeStr.equals("long") || typeStr.equals("int")){
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
		_backup = new LongLiteral(_srcNode, _value);
	}

	@Override
	public void restore() {
		this._value = ((LongLiteral)_backup).getValue();
		this._srcNode = _backup.getOriginalASTnode();
	}
}
