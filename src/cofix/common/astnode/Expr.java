/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */

package cofix.common.astnode;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Type;

public abstract class Expr {
	
	protected ASTNode _srcNode = null;
	
	public abstract Type getType();
	
	public ASTNode getOriginalASTnode(){
		return _srcNode;
	}

}
