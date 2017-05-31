/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.common.code.search;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.WhileStatement;

import cofix.common.util.LevelLogger;

/**
 * @author Jiajun
 *
 */
public class CodeSearch {
	private CompilationUnit _unit = null;
	private int _buggyLine = 0;
	private int _lineRange = 0;
	private List<Statement> _nodes = new ArrayList<>(); 

	public CodeSearch(CompilationUnit unit, int buggyLine, int lineRange) {
		_unit = unit;
		_buggyLine = buggyLine;
		_lineRange = lineRange;
		_unit.accept(new Traverse());
	}
	
	public List<Statement> getASTNodes(){
		return _nodes;
	}

	class Traverse extends ASTVisitor {

		public boolean visit(MethodDeclaration node){
			
			int start = _unit.getLineNumber(node.getStartPosition());
			int end = _unit.getLineNumber(node.getStartPosition() + node.getLength());
			if(start <= _buggyLine && _buggyLine <= end){
				
				Statement statement = node.getBody();
				if(statement == null){
					LevelLogger.error("non body for code search !");
					return false;
				}
				
				process(statement);
				
				return false;
			}
			return true;
		}
		
		public boolean process(Statement statement) {

			int start = _unit.getLineNumber(statement.getStartPosition());
			int end = _unit.getLineNumber(statement.getStartPosition() + statement.getLength());

			if (start <= _buggyLine && _buggyLine <= end) {
				if (statement instanceof IfStatement || statement instanceof ForStatement
						|| statement instanceof WhileStatement || statement instanceof DoStatement
						|| statement instanceof EnhancedForStatement) {
					_nodes.add(statement);
					return false;
				} else if(statement instanceof Block){
					Block block = (Block) statement;
					for(Object object : block.statements()){
						process((Statement)object);
					}
				}
			}

			return true;
		}

	}

}
