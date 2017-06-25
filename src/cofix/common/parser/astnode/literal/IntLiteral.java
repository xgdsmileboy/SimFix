/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */

package cofix.common.parser.astnode.literal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.Type;

import cofix.common.parser.astnode.Expr;
import cofix.common.parser.astnode.expr.Variable;
import cofix.core.adapt.Modification;
import cofix.core.adapt.Revision;

public class IntLiteral extends Literal{

	private Integer _value = 0;
	
	public IntLiteral(ASTNode node, int value) {
		_srcNode = node;
		_value = value;
	}
	
	@Override
	public Integer getValue() {
		return _value;
	}
	
	@Override
	public Type getType() {
		AST ast = AST.newAST(AST.JLS8);
		return ast.newPrimitiveType(PrimitiveType.INT);
	}
	
	@Override
	public NumberLiteral genAST() {
		AST ast = AST.newAST(AST.JLS8);
		return ast.newNumberLiteral(String.valueOf(_value));
	}
	
	@Override
	public int hashCode() {
		return Integer.valueOf(_value).hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null){
			return false;
		}
		if(!(obj instanceof IntLiteral)){
			return false;
		}
		IntLiteral other = (IntLiteral)obj;
		return this._value == other.getValue();
	}
	
	@Override
	public String toString() {
		return String.valueOf(_value);
	}

	@Override
	public boolean matchType(Expr expr, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		// exactly match
		if(expr instanceof IntLiteral){
			IntLiteral other = (IntLiteral) expr;
			if(_value != other.getValue()){
				Revision revision = new Revision(this);
				AST ast = AST.newAST(AST.JLS8);
				ASTNode node = ASTNode.copySubtree(ast, expr.getOriginalASTnode());
				revision.setTar(expr, node);
				revision.setModificationComplexity(1);
				modifications.add(revision);
			}
			return true;
		} else if(expr != null){
			// type match
			Type type = expr.getType();
			if(type != null){
				String typeStr = type.toString();
				if(typeStr.equals("double") || typeStr.equals("float") || typeStr.equals("int")){
					Revision revision = new Revision(this);
					AST ast = AST.newAST(AST.JLS8);
					ASTNode node = ASTNode.copySubtree(ast, expr.getOriginalASTnode());
					revision.setTar(expr, node);
					revision.setModificationComplexity(1);
					modifications.add(revision);
					return true;
				}
			}
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
		return new ArrayList<>();
	}

	@Override
	public void backup() {
		_backup = new IntLiteral(_srcNode, _value);
	}

	@Override
	public void restore() {
		this._value = ((IntLiteral)_backup).getValue();
		this._srcNode = _backup.getOriginalASTnode();
	}
	
}
