/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.astnode;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.NumberLiteral;

/**
 * @author Jiajun
 *
 */
public class LongLiteral extends Literal {
	
	private Long _value = 0l;
	
	public LongLiteral(long value) {
		_value = value;
	}
	
	@Override
	public Long getValue() {
		return _value;
	}
	
	@Override
	public Class getType() {
		return long.class;
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
}
