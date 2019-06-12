/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.parser.node.expr;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.print.attribute.standard.MediaSize.Other;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Type;

import cofix.common.util.Pair;
import cofix.core.metric.Literal;
import cofix.core.metric.NewFVector;
import cofix.core.metric.Variable;
import cofix.core.modify.Modification;
import cofix.core.modify.Revision;
import cofix.core.parser.NodeUtils;
import cofix.core.parser.node.Node;

/**
 * @author Jiajun
 * @date Jun 23, 2017
 */
public class QName extends Label {

	private Label _name = null;
	private SName _sname = null;
	
	private String _replace = null;
	private int WHOLE = 0;
	
	/**
	 * QualifiedName:
     *	Name . SimpleName
	 */
	public QName(int startLine, int endLine, ASTNode node) {
		super(startLine, endLine, node);
		_nodeType = TYPE.QNAME;
	}
	
	public void setName(Label namee, SName sname){
		_name = namee;
		_sname = sname;
	}
	
	public String getIdentifier(){
		return _sname.getName();
	}
	
	public String getLabel(){
		return _name.toSrcString().toString();
	}
	
	@Override
	public boolean adapt(Modification modification) {
		if(modification.getSourceID() == WHOLE){
			_replace = modification.getTargetString();
			return true;
		}
		return false;
	}

	@Override
	public boolean restore(Modification modification) {
		if(modification.getSourceID() == WHOLE){
			_replace = null;
			return true;
		}
		return false;
	}

	@Override
	public boolean backup(Modification modification) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean match(Node node, Map<String, String> varTrans, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		boolean match = false;
		if(node instanceof QName){
			QName qName = (QName) node;
			if(_exprType.toString().equals(qName._exprType.toString()) || NodeUtils.isWidenType(_exprType, qName._exprType)){
				match = true;
				String target = node.simplify(varTrans, allUsableVariables);
				if(target != null && !target.equals(toSrcString().toString())){
					Revision revision = new Revision(this, WHOLE, target, _nodeType);
					modifications.add(revision);
				}
			}
		} else if(node instanceof SName){
			SName sName = (SName) node;
			String srcType = _exprType.toString();
			String tarType = sName._exprType.toString();
			if(srcType.equals("?") || tarType.equals("?") || srcType.equals(tarType) || NodeUtils.isWidenType(_exprType, sName._exprType)){
				match = true;
				if(allUsableVariables.containsKey(sName.getName())){
					Revision revision = new Revision(this, WHOLE, sName.getName(), _nodeType);
					modifications.add(revision);
				}
			} else {
				String mapvar = varTrans.get(sName.getName());
				if(mapvar != null){
					String typeStr = allUsableVariables.get(mapvar) == null ? "?" : allUsableVariables.get(mapvar).toString();
					if(_exprType.toString().equals(typeStr) || NodeUtils.isWidenType(_exprType, allUsableVariables.get(mapvar))){
						match = true;
						Revision revision = new Revision(this, WHOLE, mapvar, _nodeType);
						modifications.add(revision);
					}
				}
			}
		} else {
			List<Node> children = node.getChildren();
			List<Modification> tmp = new ArrayList<>();
			if(NodeUtils.nodeMatchList(this, children, varTrans, allUsableVariables, tmp)){
				match = true;
				modifications.addAll(tmp);
			}
		}
		return match;
	}
	
	@Override
	public StringBuffer toSrcString() {
		StringBuffer stringBuffer = new StringBuffer();
		if(_replace != null){
			stringBuffer.append(_replace);
		} else {
			stringBuffer.append(_name.toSrcString());
			stringBuffer.append(".");
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
		List<Variable> list = new LinkedList<>();
		if(!Character.isUpperCase(_sname.getName().charAt(0))){
			list.addAll(_sname.getVariables());
		}
		return list;
	}
	
	@Override
	public void computeFeatureVector() {
		_fVector = new NewFVector();
		String name = _name.toString();
		String sname = _sname.toString();
		if(_name instanceof SName && Character.isUpperCase(name.charAt(0)) && sname.toUpperCase().equals(sname)){
			_fVector.inc(NewFVector.INDEX_LITERAL);
		} else {
			_fVector.combineFeature(_name.getFeatureVector());
			_fVector.combineFeature(_sname.getFeatureVector());
		}
	}
	
	@Override
	public List<Node> getChildren() {
		List<Node> list = new ArrayList<>();
		list.add(_name);
		list.add(_sname);
		return list;
	}

	@Override
	public String simplify(Map<String, String> varTrans, Map<String, Type> allUsableVariables) {
		Map<SName, Pair<String, String>> record = NodeUtils.tryReplaceAllVariables(this, varTrans, allUsableVariables);
		if(record == null){
			return null;
		}
		NodeUtils.replaceVariable(record);
		String string = toSrcString().toString();
		NodeUtils.restoreVariables(record);
		return string;
	}
}
