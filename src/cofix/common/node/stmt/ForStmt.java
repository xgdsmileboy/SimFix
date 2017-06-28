/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.node.stmt;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.print.attribute.standard.MediaSize.Other;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Type;

import cofix.common.node.Node;
import cofix.common.node.expr.Expr;
import cofix.common.node.metric.CondStruct;
import cofix.common.node.metric.Literal;
import cofix.common.node.metric.MethodCall;
import cofix.common.node.metric.Operator;
import cofix.common.node.metric.OtherStruct;
import cofix.common.node.metric.LoopStruct;
import cofix.common.node.metric.Variable;
import cofix.common.node.modify.Modification;

/**
 * @author Jiajun
 * @datae Jun 23, 2017
 */
public class ForStmt extends Stmt {

	private List<Expr> _initializers = null;
	private List<Expr> _updaters = null;
	private Expr _condition = null;
	private Stmt _body = null;
	
	/**
	 * for (
     *           [ ForInit ];
     *           [ Expression ] ;
     *           [ ForUpdate ] )
     *           Statement
     * ForInit:
     *           Expression { , Expression }
     * ForUpdate:
     *           Expression { , Expression }
	 */
	public ForStmt(int startLine, int endLine, ASTNode node) {
		this(startLine, endLine, node, null);
	}
	
	public ForStmt(int startLine, int endLine, ASTNode node, Node parent) {
		super(startLine, endLine, node, parent);
	}
	
	public void setCondition(Expr condition){
		_condition = condition;
	}
	
	public void setInitializer(List<Expr> initializers){
		_initializers = initializers;
	}
	
	public void setUpdaters(List<Expr> updaters){
		_updaters = updaters;
	}
	
	public void setBody(Stmt body){
		_body = body;
	}

	@Override
	public boolean match(Node node, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		// TODO Auto-generated method stub
		return false;
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
	public List<Literal> getLiterals() {
		List<Literal> list = new LinkedList<>();
		if(_initializers != null){
			for(Expr expr : _initializers){
				list.addAll(expr.getLiterals());
			}
		}
		if(_condition != null){
			list.addAll(_condition.getLiterals());
		}
		if(_updaters != null){
			for(Expr expr : _updaters){
				list.addAll(expr.getLiterals());
			}
		}
		if(_body != null){
			list.addAll(_body.getLiterals());
		}
		return list;
	}

	@Override
	public List<Variable> getVariables() {
		List<Variable> list = new LinkedList<>();
		if(_initializers != null){
			for(Expr expr : _initializers){
				list.addAll(expr.getVariables());
			}
		}
		if(_condition != null){
			list.addAll(_condition.getVariables());
		}
		if(_updaters != null){
			for(Expr expr : _updaters){
				list.addAll(expr.getVariables());
			}
		}
		if(_body != null){
			list.addAll(_body.getVariables());
		}
		return list;
	}

	@Override
	public List<LoopStruct> getLoopStruct() {
		List<LoopStruct> list = new LinkedList<>();
		LoopStruct loopStruct = new LoopStruct(this, LoopStruct.KIND.FOR);
		list.add(loopStruct);
		if(_body != null){
			list.addAll(_body.getLoopStruct());
		}
		return list;
	}
	
	@Override
	public List<CondStruct> getCondStruct() {
		List<CondStruct> list = new LinkedList<>();
		if(_initializers != null){
			for(Expr expr : _initializers){
				list.addAll(expr.getCondStruct());
			}
		}
		if(_condition != null){
			list.addAll(_condition.getCondStruct());
		}
		if(_updaters != null){
			for(Expr expr : _updaters){
				list.addAll(expr.getCondStruct());
			}
		}
		if(_body != null){
			list.addAll(_body.getCondStruct());
		}
		return list;
	}

	@Override
	public List<MethodCall> getMethodCalls() {
		List<MethodCall> list = new LinkedList<>();
		if(_initializers != null){
			for(Expr expr : _initializers){
				list.addAll(expr.getMethodCalls());
			}
		}
		if(_condition != null){
			list.addAll(_condition.getMethodCalls());
		}
		if(_updaters != null){
			for(Expr expr : _updaters){
				list.addAll(expr.getMethodCalls());
			}
		}
		if(_body != null){
			list.addAll(_body.getMethodCalls());
		}
		return list;
	}

	@Override
	public List<Operator> getOperators() {
		List<Operator> list = new LinkedList<>();
		if(_initializers != null){
			for(Expr expr : _initializers){
				list.addAll(expr.getOperators());
			}
		}
		if(_condition != null){
			list.addAll(_condition.getOperators());
		}
		if(_updaters != null){
			for(Expr expr : _updaters){
				list.addAll(expr.getOperators());
			}
		}
		if(_body != null){
			list.addAll(_body.getOperators());
		}
		return list;
	}
	
	@Override
	public List<OtherStruct> getOtherStruct() {
		List<OtherStruct> list = new LinkedList<>();
		if(_body != null){
			list.addAll(_body.getOtherStruct());
		}
		return list;
	}
}
