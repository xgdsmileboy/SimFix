/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */

package cofix.common.astnode;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.Type;

public class NilLiteral extends Literal {

	@Override
	public Object getValue() {
		return null;
	}

	@Override
	public Type getType() {
		AST ast = AST.newAST(AST.JLS8);
		return ast.newWildcardType();
	}
	
	@Override
	public NullLiteral genAST() {
		AST ast = AST.newAST(AST.JLS8);
		return ast.newNullLiteral();
	}
	
	@Override
	public int hashCode() {
		return 0;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null){
			return true;
		}
		if(!(obj instanceof NilLiteral)){
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return String.valueOf("null");
	}

}
