/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.astnode;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeLiteral;

/**
 * @author Jiajun
 * @datae May 31, 2017
 */
public class TLiteral extends Literal {

	private Type _type = null;
	
	public TLiteral(Type type) {
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

}
