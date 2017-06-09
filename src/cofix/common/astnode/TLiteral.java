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
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeLiteral;

import cofix.core.adapt.Modification;
import cofix.core.adapt.Revision;

/**
 * @author Jiajun
 * @datae May 31, 2017
 */
public class TLiteral extends Literal {

	private Type _type = null;
	
	public TLiteral(ASTNode node, Type type) {
		_srcNode = node;
		_type = type;
	}
	
	@Override
	public Type getValue() {
		return _type;
	}

	@Override
	public TypeLiteral genAST() {
		AST ast = AST.newAST(AST.JLS8);
		TypeLiteral typeLiteral = ast.newTypeLiteral();
		typeLiteral.setType(_type);
		return typeLiteral;
	}

	@Override
	public Type getType() {
		return _type;
	}
	
	@Override
	public int hashCode() {
		return _type.toString().hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null){
			return false;
		}
		if(! (obj instanceof TLiteral)){
			return false;
		}
		TLiteral other = (TLiteral) obj;
		return _type.toString().equals(other.getValue().toString());
	}
	
	@Override
	public String toString() {
		return _type.toString();
	}

	@Override
	public boolean matchType(Expr expr, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		if(expr instanceof TLiteral){
			TLiteral other = (TLiteral) expr;
			if(!_type.toString().equals(other.getValue().toString())){
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
		_backup = new TLiteral(_srcNode, _type);
	}

	@Override
	public void restore() {
		this._srcNode = _backup.getOriginalASTnode();
		this._type = ((TLiteral)_backup).getValue();
	}

}
