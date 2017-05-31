/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.astnode;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.Type;

/**
 * @author Jiajun
 *
 */
public class BoolLiteral extends Literal {

	private boolean _value = false;
	
	public BoolLiteral(boolean value) {
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
		return String.valueOf(_value);
	}


}
