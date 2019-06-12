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

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Type;

import cofix.core.metric.Literal;
import cofix.core.metric.Variable;
import cofix.core.modify.Modification;
import cofix.core.modify.Revision;
import cofix.core.parser.NodeUtils;
import cofix.core.parser.node.Node;

/**
 * @author Jiajun
 * @date Jun 23, 2017
 */
public class FloatLiteral extends NumLiteral {

	private float _value = .0f;
	
	private String _replace = null;
	
	private final int EXPRID = 0;
	
	public FloatLiteral(int startLine, int endLine, ASTNode node) {
		super(startLine, endLine, node);
		_nodeType = TYPE.FLITERAL;
	}
	
	public void setValue(float value){
		_value = value;
	}
	
	public float getValue(){
		return _value;
	}

	@Override
	public boolean match(Node node, Map<String, String> varTrans, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		boolean match = false;
		if(node instanceof FloatLiteral){
			match = true;
			FloatLiteral other = (FloatLiteral) node;
			if(_value != other._value){
				if(!NodeUtils.isBoundaryValue(this) || (NodeUtils.isBoundaryValue(this) && NodeUtils.isBoundaryValue(other))){
					Revision revision = new Revision(this, EXPRID, other.toSrcString().toString(), _nodeType);
					modifications.add(revision);
				}
			}
		} else if(node instanceof SName || node instanceof QName){
			Label label = (Label) node;
			if(label.getType().toString().equals("float")){
				match = true;
				String target = node.simplify(varTrans, allUsableVariables);
				if(target != null){
					Revision revision = new Revision(this, EXPRID, target, _nodeType);
					modifications.add(revision);
				}
			}
		} else {
			List<Modification> tmp = new ArrayList<>();
			if(node instanceof ConditionalExpr){
				ConditionalExpr conditionalExpr = (ConditionalExpr) node;
				if(NodeUtils.conditionalMatch(this, EXPRID, conditionalExpr, varTrans, allUsableVariables, tmp)){
					match = true;
					modifications.addAll(tmp);
				}
			} else {
				List<Node> children = node.getChildren();
				if(NodeUtils.nodeMatchList(this, children, varTrans, allUsableVariables, tmp)){
					match = true;
					modifications.addAll(tmp);
				}
			}
		}
		return match;
	}

	@Override
	public boolean adapt(Modification modification) {
		if(modification.getSourceID() == EXPRID){
			_replace = modification.getTargetString();
			return true;
		}
		return false;
	}

	@Override
	public boolean restore(Modification modification) {
		if(modification.getSourceID() == EXPRID){
			_replace = null;
			return true;
		}
		return false;
	}

	@Override
	public boolean backup(Modification modification) {
		return true;
	}
	
	@Override
	public StringBuffer toSrcString() {
		if(_replace != null){
			return new StringBuffer(_replace);
		}
		return new StringBuffer(String.valueOf(_value));
	}

	@Override
	public List<Literal> getLiterals() {
		List<Literal> list = new LinkedList<>();
		Literal literal = new Literal(this);
		list.add(literal);
		return list;
	}

	@Override
	public List<Variable> getVariables() {
		return new LinkedList<>();
	}
	
}
