/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.node.expr;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Type;

import cofix.common.node.Node;

/**
 * @author Jiajun
 * @datae Jun 23, 2017
 */
public abstract class Expr extends Node {
	
	protected Type _exprType = null;

	protected Expr(int startLine, int endLine, ASTNode node) {
		super(startLine, endLine, node, null);
		AST ast = AST.newAST(AST.JLS8);
		_exprType = ast.newWildcardType();
	}
	
	public void setType(Type exprType){
		if(exprType != null){
			_exprType = exprType;
		}
	}
	
	public Type getType(){
		return _exprType;
	}
}
