/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */

package cofix.common.astnode;

import org.eclipse.jdt.core.dom.ASTNode;

public abstract class Literal extends Identifier{
	
	public abstract Object getValue();
	
	public abstract Class getType();
	
	public abstract ASTNode genAST();
	
}
