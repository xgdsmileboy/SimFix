/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */

package cofix.common.astnode;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.StringLiteral;

public class StrLiteral extends Literal {

	private String _value = null;
	
	@Override
	public String getValue() {
		return _value;
	}

	@Override
	public Class getType() {
		return String.class;
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
		return _value;
	}

}
