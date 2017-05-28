/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.astnode;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CharacterLiteral;

/**
 * @author Jiajun
 *
 */
public class CharLiteral extends Literal {

	private char _value;
	
	/**
	 * 
	 */
	public CharLiteral(char value) {
		_value = value;
	}

	@Override
	public Character getValue() {
		return _value;
	}

	@Override
	public Class getType() {
		return char.class;
	}

	@Override
	public CharacterLiteral genAST() {
		AST ast = AST.newAST(AST.JLS8);
		CharacterLiteral literal = ast.newCharacterLiteral();
		literal.setCharValue(_value);
		return literal;
	}
	
	@Override
	public int hashCode() {
		return Character.valueOf(_value).hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null){
			return false;
		}
		if(!(obj instanceof CharLiteral)){
			return false;
		}
		CharLiteral other = (CharLiteral) obj;
		return _value == other.getValue().charValue();
	}
	
	@Override
	public String toString() {
		return String.valueOf(_value);
	}

}
