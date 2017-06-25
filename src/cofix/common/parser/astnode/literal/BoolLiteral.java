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
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.Type;

import com.sun.org.apache.regexp.internal.recompile;

import cofix.common.parser.astnode.Expr;
import cofix.common.parser.astnode.expr.Variable;
import cofix.core.adapt.Modification;
import cofix.core.adapt.Revision;

/**
 * @author Jiajun
 *
 */
public class BoolLiteral extends Literal {

	private boolean _value = false;
	
	private Expr _replace = null;
	
	public BoolLiteral(ASTNode node, boolean value) {
		_srcNode = node;
		_value = value;
	}
	
	@Override
	public Boolean getValue() {
		return _value;
	}
	
	@Override
	public Type getType() {
		AST ast = AST.newAST(AST.JLS8);
		return ast.newPrimitiveType(PrimitiveType.BOOLEAN);
	}

	@Override
	public BooleanLiteral genAST() {
		AST ast = AST.newAST(AST.JLS8);
		BooleanLiteral literal = ast.newBooleanLiteral(_value);
		return literal;
	}
	
	@Override
	public int hashCode() {
		return Boolean.valueOf(_value).hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null){
			return false;
		}
		if(!(obj instanceof BoolLiteral)){
			return false;
		}
		BoolLiteral other = (BoolLiteral) obj;
		return _value == other.getValue().booleanValue();
	}
	
	@Override
	public String toString() {
		if(_replace != null){
			return _replace.toString();
		}
		return String.valueOf(_value);
	}

	@Override
	public boolean matchType(Expr expr, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		// exactly match
		if(expr instanceof BoolLiteral){
			BoolLiteral other = (BoolLiteral) expr;
			if(_value != other.getValue()){
				Revision revision = new Revision(this);
				AST ast = AST.newAST(AST.JLS8);
				ASTNode astNode = ASTNode.copySubtree(ast, other.getOriginalASTnode());
				revision.setTar(expr, astNode);
				revision.setModificationComplexity(1);
				modifications.add(revision);
			}
			return true;
		} else if(expr != null) {
			// type match
			Type type = expr.getType();
			if(type != null && type.toString().equals("boolean")){
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
		if(tar instanceof BoolLiteral){
			BoolLiteral other = (BoolLiteral) tar;
			this._value = other.getValue();
			newExpr = this;
		} else {
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
		}
		return newExpr;
	}
	

	@Override
	public List<Variable> getVariables() {
		return new ArrayList<>();
	}

	@Override
	public void backup() {
		_backup = new BoolLiteral(_srcNode, _value);
	}

	@Override
	public void restore() {
		_replace = null;
		this._value = ((BoolLiteral)_backup).getValue();
		this._srcNode = _backup.getOriginalASTnode();
	}


}
