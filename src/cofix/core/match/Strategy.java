/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */

package cofix.core.match;


import java.util.List;

import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.Statement;

public interface Strategy {
	
	public float match(Statement src, Statement tar);
	
	public float match(Expression src, Expression tar);
	
	public List<Statement> findCondition(Expression src, CompilationUnit tar);
}
