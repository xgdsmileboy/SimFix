/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */

package cofix.common.astnode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;

import cofix.core.adapt.Modification;
import cofix.core.adapt.Revision;

public class StrLiteral extends Literal {

	private String _value = null;
	
	public StrLiteral(ASTNode node, String value) {
		_srcNode = node;
		_value = value;
	}
	
	@Override
	public String getValue() {
		return _value;
	}

	@Override
	public Type getType() {
		AST ast = AST.newAST(AST.JLS8);
		return ast.newSimpleType(ast.newSimpleName("String"));
	}
	
	@Override
	public StringLiteral genAST() {
		AST ast = AST.newAST(AST.JLS8);
		StringLiteral stringLiteral = ast.newStringLiteral();
		stringLiteral.setLiteralValue(_value);
		return stringLiteral;
	}
	
	@Override
	public int hashCode() {
		return _value.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null){
			return false;
		}
		if(! (obj instanceof StrLiteral)){
			return false;
		}
		StrLiteral other = (StrLiteral) obj;
		return this._value.equals(other.getValue());
	}
	
	@Override
	public String toString() {
		return "\"" + _value + "\"";
	}

	@Override
	public boolean matchType(Expr expr, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		if(expr instanceof StrLiteral){
			StrLiteral other = (StrLiteral) expr;
			if(!_value.equals(other.getValue())){
				Revision revision = new Revision(this);
				revision.setTar(expr);
				revision.setModificationComplexity(1);
				modifications.add(revision);
			}
			return true;
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
		_backup = new StrLiteral(_srcNode, _value);
	}

	@Override
	public void restore() {
		StrLiteral literal = (StrLiteral)_backup;
		this._srcNode = literal.getOriginalASTnode();
		this._value = literal.getValue();
	}
	
}
