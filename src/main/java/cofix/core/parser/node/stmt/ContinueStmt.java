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

import cofix.core.metric.NewFVector;
import cofix.core.metric.OtherStruct;
import cofix.core.modify.Modification;
import cofix.core.modify.Revision;
import cofix.core.parser.NodeUtils;
import cofix.core.parser.node.Node;

/**
 * @author Jiajun
 * @date Jun 23, 2017
 */
public class ContinueStmt extends Stmt {

	private String _identifier = null;
	
	private String _replace = null;
	
	private int STMTID = 0;
	
	/**
	 * ContinueStatement:
     *	continue [ Identifier ] ;
	 */
	public ContinueStmt(int startLine, int endLine, ASTNode node) {
		this(startLine, endLine, node, null);
		_nodeType = TYPE.CONTINUE;
	}

	public ContinueStmt(int startLine, int endLine, ASTNode node, Node parent) {
		super(startLine, endLine, node, parent);
	}
	
	public void setIdentifier(String identifier){
		_identifier = identifier;
	}
	
	@Override
	public boolean match(Node node, Map<String, String> varTrans, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		boolean match = false;
		if(node instanceof ContinueStmt){
			match = true;
		} else if(node instanceof BreakStmt){
			match = true;
//			Revision revision = new Revision(this, STMTID, node.toSrcString().toString(), _nodeType);
//			modifications.add(revision);
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
	public boolean adapt(Modification modification) {
		_replace = modification.getTargetString();
		return true;
	}

	@Override
	public boolean restore(Modification modification) {
		_replace = null;
		return true;
	}

	@Override
	public boolean backup(Modification modification) {
		// TODO Auto-generated method stub
		return false;
	}
	
	@Override
	public StringBuffer toSrcString() {
		if(_replace != null){
			return new StringBuffer(_replace);
		}
		StringBuffer stringBuffer = new StringBuffer("continue");
		if(_identifier != null){
			stringBuffer.append(" ");
			stringBuffer.append(_identifier);
		}
		stringBuffer.append(";");
		return stringBuffer;
	}
	
	@Override
	public List<OtherStruct> getOtherStruct() {
		List<OtherStruct> list = new LinkedList<>();
		OtherStruct otherStruct = new OtherStruct(this, OtherStruct.KIND.CONTINUE);
		list.add(otherStruct);
		return list;
	}
	
	@Override
	public void computeFeatureVector() {
		_fVector = new NewFVector();
		_fVector.inc(NewFVector.INDEX_STRUCT_OTHER);
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
