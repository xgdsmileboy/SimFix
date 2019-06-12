/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.parser.node.expr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.util.ISignatureAttribute;

import cofix.common.util.Pair;
import cofix.core.metric.Literal;
import cofix.core.metric.MethodCall;
import cofix.core.metric.NewFVector;
import cofix.core.metric.Operator;
import cofix.core.metric.Variable;
import cofix.core.metric.Variable.USE_TYPE;
import cofix.core.modify.Modification;
import cofix.core.modify.Revision;
import cofix.core.parser.NodeUtils;
import cofix.core.parser.node.Node;

/**
 * @author Jiajun
 * @date Jun 23, 2017
 */
public class ArrayAcc extends Expr {

	private Expr _index = null;
	private Expr _array = null;
	
	private String _index_replace = null;
	private String _array_replace = null;
	
	private final int INDEXID = 0;
	private final int ARRAYID = 1;
	
	/**
	 * ArrayAccess:
     *	Expression [ Expression ]
	 */
	public ArrayAcc(int startLine, int endLine, ASTNode node) {
		super(startLine, endLine, node);
		_nodeType = TYPE.ARRACC;
	}
	
	public void setArray(Expr array){
		_array = array;
	}
	
	public void setIndex(Expr index){
		_index = index;
	}

	@Override
	public boolean match(Node node, Map<String, String> varTrans, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		boolean match = false;
		if(node instanceof ArrayAcc){
			ArrayAcc other = (ArrayAcc) node;
			List<Modification> tmp = new ArrayList<>();
			if(!_index.toSrcString().toString().equals(other._index.toSrcString().toString())){
				boolean canReplace = false;
				if(_index instanceof SName && other._index instanceof SName){
					canReplace = true;
					String srcName = _index.toSrcString().toString();
					String tarName = other._index.toSrcString().toString();
					if(srcName.toUpperCase().equals(srcName)){
						if(!tarName.toUpperCase().equals(tarName)){
							canReplace = false;
						}
					} else if(tarName.toUpperCase().equals(tarName)){
						canReplace = false;
					}
				}
				if(canReplace){
					if(NodeUtils.replaceExpr(INDEXID, _index.toSrcString().toString(), _index, other._index, varTrans, allUsableVariables, tmp)){
						modifications.addAll(tmp);
						match = true;
					}
				}
			} else {
				match = true;
			}
		} else if(node instanceof Expr && node.getNodeType().toString().equals("int")){
			Map<SName, Pair<String, String>> record = NodeUtils.tryReplaceAllVariables((Expr)node, varTrans, allUsableVariables);
			if(record != null){
				NodeUtils.replaceVariable(record);
				String target = node.toSrcString().toString();
				if(!_index.toSrcString().toString().equals(target)){
					Revision revision = new Revision(this, INDEXID, target, _nodeType);
					modifications.add(revision);
				}
				NodeUtils.restoreVariables(record);
				match = true;
				
			}
		}
		return match;
	}

	@Override
	public boolean adapt(Modification modification) {
		int id = modification.getSourceID();
		switch(id){
		case INDEXID:
			_index_replace = modification.getTargetString();
			break;
		case ARRAYID:
			_array_replace = modification.getTargetString();
		}
		return true;
	}

	@Override
	public boolean restore(Modification modification) {
		int id = modification.getSourceID();
		switch(id){
		case INDEXID:
			_index_replace = null;
			break;
		case ARRAYID:
			_array_replace = null;
		}
		return true;
	}

	@Override
	public boolean backup(Modification modification) {
		return true;
	}
	
	@Override
	public StringBuffer toSrcString() {
		StringBuffer stringBuffer = new StringBuffer();
		if(_array_replace != null){
			stringBuffer.append(_array_replace);
		} else {
			stringBuffer.append(_array.toSrcString());
		}
		stringBuffer.append("[");
		if(_index_replace != null){
			stringBuffer.append(_index_replace);
		} else {
			stringBuffer.append(_index.toSrcString());
		}
		stringBuffer.append("]");
		return stringBuffer;
	}

	@Override
	public List<Literal> getLiterals() {
		List<Literal> list = new LinkedList<>();
		list.addAll(_array.getLiterals());
		list.addAll(_index.getLiterals());
		return list;
	}

	@Override
	public List<Variable> getVariables() {
		List<Variable> list = new LinkedList<>();
		list.addAll(_array.getVariables());
		list.addAll(_index.getVariables());
		return list;
	}

	@Override
	public List<MethodCall> getMethodCalls() {
		List<MethodCall> list = new LinkedList<>();
		list.addAll(_array.getMethodCalls());
		list.addAll(_index.getMethodCalls());
		return list;
	}

	@Override
	public List<Operator> getOperators() {
		List<Operator> list = new LinkedList<>();
		Operator operator = new Operator(this, Operator.KIND.ACC);
		list.add(operator);
		list.addAll(_array.getOperators());
		list.addAll(_index.getOperators());
		return list;
	}

	@Override
	public void computeFeatureVector() {
		_fVector = new NewFVector();
		_fVector.combineFeature(_array.getFeatureVector());
		_fVector.combineFeature(_index.getFeatureVector());
		_fVector.inc(NewFVector.INDEX_OP_ACC);
	}

	@Override
	public USE_TYPE getUseType(Node child) {
		return USE_TYPE.USE_ARR_ACC;
	}
	
	@Override
	public List<Node> getChildren() {
		return new ArrayList<>();
	}

	@Override
	public String simplify(Map<String, String> varTrans, Map<String, Type> allUsableVariables) {
		Map<SName, Pair<String, String>> record = new HashMap<>();
		if(!allUsableVariables.containsKey(_array) && allUsableVariables.containsKey(_index)){
			record = NodeUtils.tryReplaceAllVariables(this, varTrans, allUsableVariables);
		}
		if(record == null){
			return null;
		}
		NodeUtils.replaceVariable(record);
		String string = toSrcString().toString();
		NodeUtils.restoreVariables(record);
		return string;
	}

}
