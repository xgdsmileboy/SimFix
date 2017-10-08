/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.parser.search;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NodeFinder;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import cofix.core.parser.NodeUtils;

/**
 * @author Jiajun
 * @date Jun 29, 2017
 */
public class CodeSearch {

	private CompilationUnit _unit = null;
	private int _extendedLine = 0;
	private Statement _extendedStatement = null;
	private int _lineRange = 0;
	private List<ASTNode> _nodes = new ArrayList<>(); 
	private int _currentLines = 0;
	private int MAX_LESS_THRESHOLD = 0;
	private int MAX_MORE_THRESHOLD = 5;
	

	public CodeSearch(CompilationUnit unit, int extendedLine, int lineRange) {
		this(unit, extendedLine, lineRange, null);
	}
	
	public CodeSearch(CompilationUnit unit, int extendedLine, int lineRange, Statement extendedStatement){
		this(unit, extendedLine, lineRange, extendedStatement, 0);
	}
	
	public CodeSearch(CompilationUnit unit, int extendedLine, int lineRange, Statement extendedStatement,
			int max_less_threshold) {
		_unit = unit;
		_extendedLine = extendedLine;
		_lineRange = lineRange;
		_extendedStatement = extendedStatement;
		MAX_LESS_THRESHOLD = max_less_threshold;
		search();
	}
	
	public List<ASTNode> getASTNodes(){
		return _nodes;
	}
	
	private void search(){
		// if the extended line is not given
		if(_extendedStatement == null){
			int position = _unit.getPosition(_extendedLine, 0);
			NodeFinder finder = new NodeFinder(_unit, position, 20);
			ASTNode prefind = finder.getCoveringNode();
			while (prefind != null && !(prefind instanceof Statement)) {
				prefind = prefind.getParent();
			}
			if(prefind != null){
				prefind.accept(new FindExactLineVisitor());
			} else {
				_unit.accept(new Traverse());
			}
		}
		// extend the statement to meet the requirement
		if(_extendedStatement != null){
			List<ASTNode> list = simpleExtend(_extendedStatement);
			if(list.size() > 0){
				_nodes.addAll(list);
			} else {
				_currentLines = NodeUtils.getValidLineNumber(_extendedStatement);
				if(_lineRange - _currentLines > MAX_LESS_THRESHOLD){
					_currentLines = 0;
					_nodes = extend(_extendedStatement);
				} else {
					if(_extendedStatement instanceof Block){
						ASTNode node = _extendedStatement.getParent();
						if (node instanceof IfStatement || node instanceof SwitchCase || node instanceof ForStatement
								|| node instanceof EnhancedForStatement || node instanceof WhileStatement) {
							_extendedStatement = (Statement) node;
						}
					}
					_nodes.add(_extendedStatement);
				}
			}
		}
	}
	
	private List<ASTNode> simpleExtend(ASTNode node){
		List<ASTNode> rslt = new ArrayList<>();
		ASTNode parent = node;
		while (parent != null) {
			if (parent instanceof IfStatement || parent instanceof ForStatement
					|| parent instanceof EnhancedForStatement || parent instanceof DoStatement
					|| parent instanceof WhileStatement) {
				int line = NodeUtils.getValidLineNumber(parent); 
				if(line - _lineRange < MAX_MORE_THRESHOLD){
					rslt.add(parent);
					_currentLines = line;
				}
				break;
			} else if(parent instanceof MethodDeclaration){
				MethodDeclaration mdDeclaration = (MethodDeclaration) parent;
				Block block = mdDeclaration.getBody();
				int line = 0;
				for(Object object : block.statements()){
					line += NodeUtils.getValidLineNumber((ASTNode) object);
				}
				if(line - _lineRange < MAX_MORE_THRESHOLD){
					for(Object object : block.statements()){
						rslt.add((ASTNode) object);
					}
					break;
				}
			}
			parent = parent.getParent();
		}
		return rslt;
	}
	
	private List<ASTNode> extend(ASTNode node){
		List<ASTNode> result = new ArrayList<>();
	    List<ASTNode> list = NodeUtils.getAllSiblingNodes(node);
	    int selfIndex = -1;
	    for(int i = 0; i < list.size(); i++){
	    	if(list.get(i) == node){
	    		selfIndex = i;
	    		break;
	    	}
	    }
	    // find self position
	    if(selfIndex != -1){
	    	int left = selfIndex - 1;
	    	int right = selfIndex + 1;
	    	boolean leftExt = true;
	    	boolean rightExt = true;
	    	while(_lineRange - _currentLines > MAX_LESS_THRESHOLD){
	    		boolean extended = false;
	    		int leftLine = Integer.MAX_VALUE;
	    		int rightLine = Integer.MAX_VALUE;
	    		if(left >= 0 && leftExt){
	    			leftLine = NodeUtils.getValidLineNumber(list.get(left));
	    			if(list.get(left) instanceof SwitchCase){
	    				leftExt = false;
	    			}
	    			if((_currentLines + leftLine - _lineRange) < MAX_MORE_THRESHOLD ){
	    				_currentLines += leftLine;
	    				left --;
	    				extended = true;
	    			}
	    		}
	    		if(right < list.size() && rightExt){
	    			rightLine = NodeUtils.getValidLineNumber(list.get(right));
	    			if(list.get(right) instanceof SwitchCase){
	    				rightExt = false;
	    			}
	    			if((_currentLines + rightLine - _lineRange) < MAX_MORE_THRESHOLD){
	    				_currentLines += rightLine;
	    				right ++;
	    				extended = true;
	    			}
	    		}
	    		if(!extended){
	    			if(leftLine != Integer.MAX_VALUE || rightLine != Integer.MAX_VALUE){
	    				if(leftLine < rightLine){
	    					_currentLines += leftLine;
	    					left --;
	    				}
	    			}
	    			break;
	    		}
	    	}
			if ((_currentLines - _lineRange) < MAX_MORE_THRESHOLD && _lineRange - _currentLines > MAX_LESS_THRESHOLD
					&& !(node.getParent() instanceof MethodDeclaration) && !(node.getParent() instanceof SwitchStatement)) {
				_currentLines = 0;
				result.addAll(extend(node.getParent()));
			} else {
				boolean first = true;
		    	for(int i = left + 1; i < right; i ++){
		    		if(first && left >= 0 && list.get(left) instanceof SwitchCase){
		    			result.add(list.get(left));
		    		}
		    		first = false;
		    		result.add(list.get(i));
		    	}
	    	}
	    } else {
	    	ASTNode parent = node.getParent();
	    	int line = NodeUtils.getValidLineNumber(parent);
	    	if(line < _lineRange){
	    		if(parent instanceof MethodDeclaration){
	    			result.add(node);
	    		} else {
	    			result.addAll(extend(parent));
	    		}
	    	} else {
	    		if(line - _lineRange > MAX_MORE_THRESHOLD || parent instanceof MethodDeclaration){
	    			result.add(node);
	    		} else {
	    			result.add(parent);
	    		}
	    	}
	    }
	    return result;
	}
	

	class Traverse extends ASTVisitor {

		public boolean visit(MethodDeclaration node){
			
			int start = _unit.getLineNumber(node.getStartPosition());
			int end = _unit.getLineNumber(node.getStartPosition() + node.getLength());
			if(start <= _extendedLine && _extendedLine <= end){
				FindExactLineVisitor visitor = new FindExactLineVisitor();
				node.accept(visitor);
				return false;
			}
			return true;
		}
		
	}
	
	/**
	 * find statement of exact line number
	 * @author Jiajun
	 * @date Jun 14, 2017
	 */
	class FindExactLineVisitor extends ASTVisitor{
		
		public boolean visit(AssertStatement node) {
			int start = _unit.getLineNumber(node.getStartPosition());
			if(start == _extendedLine){
				_extendedStatement = node;
				return false;
			}
			return true;
		}
		
		public boolean visit(BreakStatement node) {
			int start = _unit.getLineNumber(node.getStartPosition());
			if(start == _extendedLine){
				_extendedStatement = node;
				return false;
			}
			return true;
		}
		
		public boolean visit(Block node) {
			return true;
		}
		
		public boolean visit(ConstructorInvocation node) {
			int start = _unit.getLineNumber(node.getStartPosition());
			if(start == _extendedLine){
				_extendedStatement = node;
				return false;
			}
			return true;
		}
		
		public boolean visit(ContinueStatement node) {
			int start = _unit.getLineNumber(node.getStartPosition());
			if(start == _extendedLine){
				_extendedStatement = node;
				return false;
			}
			return true;
		}
		
		public boolean visit(DoStatement node) {
			int start = _unit.getLineNumber(node.getExpression().getStartPosition());
			if(start == _extendedLine){
				_extendedStatement = node;
				return false;
			}
			return true;
		}
		
		public boolean visit(EmptyStatement node) {
			return true;
		}
		
		public boolean visit(EnhancedForStatement node) {
			int start = _unit.getLineNumber(node.getExpression().getStartPosition());
			if(start == _extendedLine){
				_extendedStatement = node;
				return false;
			}
			return true;
		}
		
		public boolean visit(ExpressionStatement node) {
			int start = _unit.getLineNumber(node.getStartPosition());
			if(start == _extendedLine){
				_extendedStatement = node;
				return false;
			}
			return true;
		}
		
		public boolean visit(ForStatement node) {
			int position = 0;
			if(node.getExpression() != null){
				position = node.getExpression().getStartPosition();
			} else if(node.initializers() != null && node.initializers().size() > 0){
				position = ((ASTNode)node.initializers().get(0)).getStartPosition();
			} else if(node.updaters() != null && node.updaters().size() > 0){
				position = ((ASTNode)node.updaters().get(0)).getStartPosition();
			}
			int start = _unit.getLineNumber(position);
			if(start == _extendedLine){
				_extendedStatement = node;
				return false;
			}
			return true;
		}
		
		public boolean visit(IfStatement node) {
			int start = _unit.getLineNumber(node.getExpression().getStartPosition());
			if(start == _extendedLine){
				_extendedStatement = node;
				return false;
			}
			return true;
		}
		
		public boolean visit(LabeledStatement node) {
			int start = _unit.getLineNumber(node.getStartPosition());
			if(start == _extendedLine){
				_extendedStatement = node;
				return false;
			}
			return true;
		}
		
		public boolean visit(ReturnStatement node) {
			int start = _unit.getLineNumber(node.getStartPosition());
			if(start == _extendedLine){
				_extendedStatement = node;
				return false;
			}
			return true;
		}
		
		public boolean visit(SuperConstructorInvocation node) {
			int start = _unit.getLineNumber(node.getStartPosition());
			if(start == _extendedLine){
				_extendedStatement = node;
				return false;
			}
			return true;
		}
		
		public boolean visit(SwitchCase node) {
			int start = _unit.getLineNumber(node.getStartPosition());
			if(start == _extendedLine){
				_extendedStatement = node;
				return false;
			}
			return true;
		}
		
		public boolean visit(SwitchStatement node) {
			return true;
		}
		
		public boolean visit(SynchronizedStatement node) {
			return true;
		}
		
		public boolean visit(ThrowStatement node) {
			int start = _unit.getLineNumber(node.getStartPosition());
			if(start == _extendedLine){
				_extendedStatement = node;
				return false;
			}
			return true;
		}
		
		public boolean visit(TryStatement node) {
			return true;
		}
		
		public boolean visit(TypeDeclarationStatement node){
			return true;
		}
		
		public boolean visit(VariableDeclarationStatement node){
			int start = _unit.getLineNumber(node.getStartPosition());
			if(start == _extendedLine){
				_extendedStatement = node;
				return false;
			}
			return true;
		}
		
		public boolean visit(WhileStatement node) {
			int start = _unit.getLineNumber(node.getExpression().getStartPosition());
			if(start == _extendedLine){
				_extendedStatement = node;
				return false;
			}
			return true;
		}
	}
	
}
