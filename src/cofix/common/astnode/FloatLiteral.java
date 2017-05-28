/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */

package cofix.common.astnode;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.NumberLiteral;

public class FloatLiteral extends Literal{

	private final double threshold = 1e-6;
	private float _value = 0.0f;
	
	public FloatLiteral(float value) {
		_value = value;
	}
	
	@Override
	public Float getValue() {
		return _value;
	}

	@Override
	public Class getType() {
		return float.class;
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

}
