/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.node.expr;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Type;

import com.sun.org.apache.xpath.internal.Expression;

import cofix.common.node.Node;
import cofix.common.node.metric.Literal;
import cofix.common.node.metric.Variable;
import cofix.common.node.modify.Modification;

/**
 * @author Jiajun
 * @datae Jun 23, 2017
 */
public class QName extends Label {

	private Label _name = null;
	private SName _sname = null;
	
	private Expr _name_replace = null;
	private SName _sname_replace = null;
	
	/**
	 * QualifiedName:
     *	Name . SimpleName
	 */
	public QName(int startLine, int endLine, ASTNode node) {
		super(startLine, endLine, node);
	}
	
	public void setName(Label namee, SName sname){
		_name = namee;
		_sname = sname;
	}
	
	@Override
	public boolean adapt(Modification modification) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean restore(Modification modification) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean backup(Modification modification) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean match(Node node, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public StringBuffer toSrcString() {
		StringBuffer stringBuffer = new StringBuffer();
		if(_name_replace != null){
			stringBuffer.append(_name_replace.toSrcString());
		} else {
			stringBuffer.append(_name.toSrcString());
		}
		stringBuffer.append(".");
		if(_sname_replace != null){
			stringBuffer.append(_sname_replace.toSrcString());
		} else {
			stringBuffer.append(_sname.toSrcString());
		}
		return stringBuffer;
	}

	@Override
	public List<Literal> getLiterals() {
		return new LinkedList<>();
	}

	@Override
	public List<Variable> getVariables() {
		List<Variable> list = _sname.getVariables();
		if(_name != null){
			list.addAll(_name.getVariables());
		}
		return list;
	}
}
