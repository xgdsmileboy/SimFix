/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */

package cofix.core.match;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.WhileStatement;

import cofix.common.util.LevelLogger;

public class VariableStgy implements Strategy {

	private float similarity_threshold = 0.5f;
	
	@Override
	public float match(Statement src, Statement tar) {
		return 0;
	}

	@Override
	public float match(Expression src, Expression tar) {
		return 0;
	}

	@Override
	public List<Statement> findCondition(Expression src, CompilationUnit tar) {
		// TODO Auto-generated method stub
		return null;
	}
	
	class SearchSimilarCode extends ASTVisitor {

		private ASTNode source = null;
		private List<ASTNode> matches = new ArrayList<>();
		private Map<String, Integer> variables = null;
		

		public SearchSimilarCode(ASTNode src, CompilationUnit unit) {
			source = src;
			variables = new CollectIdentifier(source).getIdentifiers();
			unit.accept(this);
		}
		
		public SearchSimilarCode(ASTNode src, CompilationUnit unit, float threshold) {
			source = src;
			similarity_threshold = threshold;
			variables = new CollectIdentifier(source).getIdentifiers();
			unit.accept(this);
		}
		
		public List<ASTNode> getMatches(){
			return this.matches;
		}
		
		private float similarity(final Map<String, Integer> comp){
			float similarity = 0;
			for(Entry<String, Integer> entry : variables.entrySet()){
				Integer value = comp.get(entry.getKey());
				if(value != null && (value == entry.getValue() || (value > 1 && entry.getValue() > 1) )){
					if(LevelLogger.logON){
						LevelLogger.info("@SearchSimilarCode #similarity Find similar variables : " + entry.getKey() + " : " + entry.getValue() + "," + value);
					}
					similarity ++;
				}
			}
			return similarity / variables.size();
		}

		public boolean visit(IfStatement node) {
			if (source instanceof IfStatement) {
				Map<String, Integer> vars = new CollectIdentifier(node).getIdentifiers();
				float similar = similarity(vars);
				if(similar >= similarity_threshold){
					if(LevelLogger.logON){
						LevelLogger.info("@SearchSimilarCode #visit Find IfStatement : " + source + "\n" + node);
					}
					System.out.println(similar);
					System.out.println(node);
					matches.add(node);
				} else {
					if(LevelLogger.logON){
						LevelLogger.info("@SearchSimilarCode #visit Not enough similarity IfStatement : " + node);
					}
				}
			}
			return true;
		}

		public boolean visit(ForStatement node) {
			if (source instanceof ForStatement) {
				Map<String, Integer> vars = new CollectIdentifier(node).getIdentifiers();
				float similar = similarity(vars);
				if(similar >= similarity_threshold){
					if(LevelLogger.logON){
						LevelLogger.info("@SearchSimilarCode #visit Find ForStatement : " + source + "\n" + node);
					}
					System.out.println(similar);
					System.out.println(node);
					matches.add(node);
				} else {
					if(LevelLogger.logON){
						LevelLogger.info("@SearchSimilarCode #visit Not enough similarity ForStatement : " + node);
					}
				}
			} else if(source instanceof EnhancedForStatement){
				
			} else if(source instanceof WhileStatement){
				
			} else if(source instanceof DoStatement){
				
			}
			return true;
		}

		public boolean visit(EnhancedForStatement node) {
			if (source instanceof ForStatement) {

			} else if(source instanceof EnhancedForStatement){
				
			} else if(source instanceof WhileStatement){
				
			} else if(source instanceof DoStatement){
				
			}
			return true;
		}

	}

}
