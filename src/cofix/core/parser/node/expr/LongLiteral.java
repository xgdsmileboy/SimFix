/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.parser.node.expr;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Type;

import cofix.common.util.Pair;
import cofix.core.modify.Modification;
import cofix.core.modify.Revision;
import cofix.core.parser.NodeUtils;
import cofix.core.parser.node.Node;

/**
 * @author Jiajun
 * @date Jun 23, 2017
 */
public class LongLiteral extends NumLiteral {

	private long _value = 0l;
	
	private String _replace = null;
	
	private final int EXPRID = 0;
	
	public LongLiteral(int startLine, int endLine, ASTNode node) {
		super(startLine, endLine, node);
		_nodeType = TYPE.LLITERAL;
	}
	
	public void setValue(long value){
		_value = value;
	}
	
	public long getValue(){
		return _value;
	}
	
	@Override
	public boolean match(Node node, Map<String, String> varTrans, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		boolean match = false;
		if(node instanceof LongLiteral){
			match = true;
			LongLiteral other = (LongLiteral) node;
			if(_value != other._value){
				if(!NodeUtils.isBoundaryValue(this) || (NodeUtils.isBoundaryValue(this) && NodeUtils.isBoundaryValue(other))){
					Revision revision = new Revision(this, EXPRID, other.toSrcString().toString(), _nodeType);
					modifications.add(revision);
				}
			}
		} else if(node instanceof SName || node instanceof QName){
			Label label = (Label) node;
			if(label.getType().toString().equals("long")){
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
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public StringBuffer toSrcString() {
		if(_replace != null){
			return new StringBuffer(String.valueOf(_replace));
		}
		return new StringBuffer(String.valueOf(_value));
	}

}
