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
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.Type;

public class DoubleLiteral extends Literal {

	
	private final double threshold = 1e-12;
	private double _value = 0.0;
	
	public DoubleLiteral(ASTNode node, double value) {
		_srcNode = node;
		_value = value;
	}
	
	@Override
	public Double getValue() {
		return _value;
	}

	@Override
	public Type getType() {
		AST ast = AST.newAST(AST.JLS8);
		return ast.newPrimitiveType(PrimitiveType.DOUBLE);
	}
	
	public NumberLiteral genAST(){
		AST ast = AST.newAST(AST.JLS8);
		return ast.newNumberLiteral(String.valueOf(_value));
	}
	
	@Override
	public int hashCode() {
		return Double.valueOf(_value).hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null){
			return false;
		}
		if(!(obj instanceof DoubleLiteral)){
			return false;
		}
		DoubleLiteral other = (DoubleLiteral) obj;
		return Math.abs(_value - other.getValue()) < threshold;
	}
	
	@Override
	public String toString() {
		return String.valueOf(_value);
	}

}
