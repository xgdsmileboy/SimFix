/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.parser.node.stmt;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Type;

import com.sun.org.apache.xpath.internal.operations.Mod;

import cofix.core.metric.NewFVector;
import cofix.core.modify.Modification;
import cofix.core.modify.Revision;
import cofix.core.parser.NodeUtils;
import cofix.core.parser.node.Node;

/**
 * @author Jiajun
 * @date Jun 23, 2017
 */
public class AssertStmt extends Stmt{

	private String _replace = null;
	
	/**
	 * AssertStatement:
     *	assert Expression [ : Expression ] ;
	 */
	public AssertStmt(int startLine, int endLine, ASTNode node) {
		this(startLine, endLine, node, null);
		_nodeType = TYPE.ASSERT;
	}
	
	public AssertStmt(int startLine, int endLine, ASTNode node, Node parent) {
		super(startLine, endLine, node, parent);
	}

	@Override
	public boolean match(Node node, Map<String, String> varTrans, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		boolean match = false;
		if(node instanceof AssertStmt) {
			match = true;
			AssertStmt other = (AssertStmt) node;
			String target = other.toSrcString().toString();
			if(!target.equals(toSrcString().toString())) {
				Revision revision = new Revision(this, 0, target, _nodeType);
				modifications.add(revision);
			}
		} else {
			List<Node> children = node.getChildren();
			List<Modification> tmp = new LinkedList<>();
			if(NodeUtils.nodeMatchList(this, children, varTrans, allUsableVariables, tmp)) {
				modifications.addAll(tmp);
				match = true;
			}
		}
		return match;
	}

	@Override
	public boolean adapt(Modification modification) {
		if(modification instanceof Revision) {
			_replace = modification.getTargetString();
			return true;
		}
		return false;
	}

	@Override
	public boolean restore(Modification modification) {
		if(modification instanceof Revision) {
			_replace = null;
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
		if(_replace != null) {
			return new StringBuffer(_replace);
		}
		return new StringBuffer(_originalNode.toString());
	}
	
	@Override
	public void computeFeatureVector() {
		_fVector = new NewFVector();
	}
	
	@Override
	public List<Node> getChildren() {
		return new ArrayList<>();
	}

	@Override
	public String simplify(Map<String, String> varTrans, Map<String, Type> allUsableVariables) {
		return toSrcString().toString();
	}
	
}