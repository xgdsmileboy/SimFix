/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.adapt;

import java.util.Map;

import org.eclipse.jdt.core.dom.Type;

import cofix.common.astnode.Expr;

/**
 * @author Jiajun
 * @datae Jun 7, 2017
 */
public class Revision extends Modification {

	private Expr _tar = null;
	
	
	public Revision(Expr expr) {
		super(expr);
	}
	
	public void setTar(Expr tar){
		_tar = tar;
	}
	
	public Expr getSrcExpr() {
		return _expr;
	}
	
	public Expr getTarExpr() {
		return _tar;
	}
	
	@Override
	public String toString() {
		String source = _expr.toString().trim();
		String target = _tar.toString().trim();
		source = source.replace("\n", "\n -");
		target = target.replace("\n", "\n +");
		return "-" + source + "\n+" + target;
	}

	@Override
	public boolean apply(Map<String, Type> allUsableVarMap) {
		_expr.backup();
		_expr.adapt(_tar, allUsableVarMap);
		return true;
	}
	
	@Override
	public boolean restore() {
		_expr.restore();
		return true;
	}

}
