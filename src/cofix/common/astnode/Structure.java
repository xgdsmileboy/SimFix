/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */

package cofix.common.astnode;

import org.eclipse.jdt.core.dom.ASTNode;

public class Structure{
	
	
	public static final String IF = "if";
	public static final String ELSE = "else";
	public static final String FOR = "for";
	public static final String WHILE = "while";
	public static final String BREAK = "break";
	public static final String CONTINUE = "continue";
	public static final String RETURN = "return";
	public static final String THRWO = "throw";
	
	
	private String _name = null;
	private ASTNode _srcNode = null; 
	
	public Structure(ASTNode node, String name){
		_srcNode = node;
		_name = name;
	}
	
	public ASTNode getOriginalASTNode(){
		return _srcNode;
	}
	
	@Override
	public String toString() {
		return _name;
	}
	
}
