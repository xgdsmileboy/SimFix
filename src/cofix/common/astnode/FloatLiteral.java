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
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.Type;

import com.sun.corba.se.spi.activation._ActivatorStub;

import cofix.core.adapt.Modification;
import cofix.core.adapt.Revision;

public class FloatLiteral extends Literal{

	private final double threshold = 1e-6;
	private float _value = 0.0f;
	
	public FloatLiteral(ASTNode node, float value) {
		_srcNode = node;
		_value = value;
	}
	
	@Override
	public Float getValue() {
		return _value;
	}

	@Override
	public Type getType() {
		AST ast = AST.newAST(AST.JLS8);
		return ast.newPrimitiveType(PrimitiveType.FLOAT);
	}
	
	@Override
	public int hashCode() {
		return Float.valueOf(_value).hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null){
			return false;
		}
		if(!(obj instanceof FloatLiteral)){
			return false;
		}
		FloatLiteral other = (FloatLiteral) obj;
		return Math.abs((double)_value - other.getValue()) < threshold;
	}

	@Override
	public NumberLiteral genAST() {
		AST ast = AST.newAST(AST.JLS8);
		return ast.newNumberLiteral(String.valueOf(_value));
	}
	
	@Override
	public String toString() {
		return String.valueOf(_value);
	}

	@Override
	public boolean matchType(Expr expr, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		// exactly match
		if(expr instanceof FloatLiteral){
			FloatLiteral other = (FloatLiteral) expr;
			if(Math.abs(_value - other.getValue()) > threshold){
				Revision revision = new Revision(this);
				revision.setTar(expr);
				revision.setModificationComplexity(1);
				modifications.add(revision);
			}
			return true;
		} else if(expr != null){
			// match type
			Type type = expr.getType();
			if(type != null){
				String typeStr = type.toString();
				if(typeStr.equals("double") || typeStr.equals("float") || typeStr.equals("int")){
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
		_backup = new FloatLiteral(_srcNode, _value);
	}

	@Override
	public void restore() {
		this._value = ((FloatLiteral)_backup).getValue();
		this._srcNode = _backup.getOriginalASTnode();
	}

}
