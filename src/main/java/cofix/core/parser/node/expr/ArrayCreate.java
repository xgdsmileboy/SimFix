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
import java.util.concurrent.locks.Condition;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.util.ISignatureAttribute;

import com.sun.corba.se.spi.ior.TaggedProfileTemplate;

import cofix.common.util.Pair;
import cofix.core.metric.Literal;
import cofix.core.metric.MethodCall;
import cofix.core.metric.NewFVector;
import cofix.core.metric.Operator;
import cofix.core.metric.Variable;
import cofix.core.metric.Variable.USE_TYPE;
import cofix.core.modify.Modification;
import cofix.core.parser.NodeUtils;
import cofix.core.parser.node.Node;
import cofix.core.parser.node.stmt.IfStmt;

/**
 * @author Jiajun
 * @date Jun 23, 2017
 */
public class ArrayCreate extends Expr {

	private Type _type = null;
	private List<Expr> _dimension = null;
	private ArrayInitial _initializer = null;
	
	/**
	 * ArrayCreation:
     *	new PrimitiveType [ Expression ] { [ Expression ] } { [ ] }
     *	new TypeName [ < Type { , Type } > ]
     *    [ Expression ] { [ Expression ] } { [ ] }
     *	new PrimitiveType [ ] { [ ] } ArrayInitializer
     *	new TypeName [ < Type { , Type } > ]
     *    [ ] { [ ] } ArrayInitializer
	 */
	public ArrayCreate(int startLine, int endLine, ASTNode node) {
		super(startLine, endLine, node);
		_nodeType = TYPE.ARRCREAT;
	}
	
	public void setArrayType(Type type){
		_type = type;
	}
	
	public void setDimension(List<Expr> dimension){
		_dimension = dimension;
	}
	
	public void setInitializer(ArrayInitial initializer){
		_initializer = initializer;
	}

	@Override
	public boolean match(Node node, Map<String, String> varTrans, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		boolean match = false;
		if(node instanceof ArrayCreate){
			match = true;
			ArrayCreate other = (ArrayCreate) node;
			if(_dimension != null && other._dimension != null){
				for(Expr expr : _dimension){
					for(Expr otherExpr : other._dimension){
						List<Modification> tmp = new ArrayList<>();
						if(expr.match(otherExpr, varTrans, allUsableVariables, tmp)){
							modifications.addAll(tmp);
						}
					}
				}
				modifications.addAll(NodeUtils.listNodeMatching(this, _nodeType, _dimension, other._dimension, varTrans, allUsableVariables));
			}
		} else {
			
			List<Modification> tmp = new ArrayList<>();
			List<Node> children = node.getChildren();
			if(NodeUtils.nodeMatchList(this, children, varTrans, allUsableVariables, tmp)){
				match = true;
				modifications.addAll(tmp);
			}
			
		}
		return match;
	}

	@Override
	public boolean adapt(Modification modification) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean restore(Modification modification) {
		return true;
	}

	@Override
	public boolean backup(Modification modification) {
		return true;
	}

	@Override
	public StringBuffer toSrcString() {
		StringBuffer stringBuffer = new StringBuffer();
		stringBuffer.append("new ");
		if(_dimension != null && _dimension.size() > 0){
			// new a[4][];
			if(_type instanceof ArrayType){
				ArrayType arrayType = (ArrayType) _type;
				stringBuffer.append(arrayType.getElementType());
				for(int i = 0; i < arrayType.getDimensions(); i++){
					stringBuffer.append("[");
					if(_dimension.size() > i){
						stringBuffer.append(_dimension.get(i).toSrcString());
					}
					stringBuffer.append("]");
				}
				if(_initializer != null){
					stringBuffer.append(_initializer.toSrcString());
				}
			} else {
				stringBuffer = new StringBuffer();
				stringBuffer.append(_originalNode.toString());
			}
		} else {
			// new a[][]{1,2;3,4};
			stringBuffer.append(_type);
			if(_initializer != null){
				stringBuffer.append(_initializer.toSrcString());
			}
		}
		return stringBuffer;
	}
	
	@Override
	public List<Literal> getLiterals() {
		List<Literal> list = new LinkedList<>();
		for(Expr expr : _dimension){
			list.addAll(expr.getLiterals());
		}
		if(_initializer != null){
			list.addAll(_initializer.getLiterals());
		}
		return list;
	}

	@Override
	public List<Variable> getVariables() {
		List<Variable> list = new LinkedList<>();
		for(Expr expr : _dimension){
			list.addAll(expr.getVariables());
		}
		if(_initializer != null){
			list.addAll(_initializer.getVariables());
		}
		return list;
	}

	@Override
	public List<MethodCall> getMethodCalls() {
		List<MethodCall> list = new LinkedList<>();
//		MethodCall methodCall = new MethodCall(this, _type.toString());
//		list.add(methodCall);
		return list;
	}
	
	@Override
	public List<Operator> getOperators() {
		return new LinkedList<>();
	}

	@Override
	public void computeFeatureVector() {
		_fVector = new NewFVector();
		_fVector.inc(NewFVector.INDEX_MCALL);
		if(_dimension != null){
			for(Expr expr : _dimension){
				_fVector.combineFeature(expr.getFeatureVector());
			}
		}
		if(_initializer != null){
			_fVector.combineFeature(_initializer.getFeatureVector());
		}
	}

	@Override
	public USE_TYPE getUseType(Node child) {
		return USE_TYPE.USE_METHOD_PARAM;
	}
	
	@Override
	public List<Node> getChildren() {
		return new ArrayList<>();
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
