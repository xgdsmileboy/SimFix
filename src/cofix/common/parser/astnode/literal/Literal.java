/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */

package cofix.common.parser.astnode.literal;

import org.eclipse.jdt.core.dom.ASTNode;

import cofix.common.parser.astnode.Expr;

public abstract class Literal extends Expr{
	
	public abstract Object getValue();
	
	public abstract ASTNode genAST();
	
}
