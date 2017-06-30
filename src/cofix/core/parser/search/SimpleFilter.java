/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.parser.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;

import com.sun.xml.internal.fastinfoset.util.ContiguousCharArrayArray;

import cofix.common.config.Constant;
import cofix.common.util.JavaFile;
import cofix.common.util.Pair;
import cofix.core.match.CodeBlockMatcher;
import cofix.core.parser.NodeUtils;
import cofix.core.parser.ProjectInfo;
import cofix.core.parser.node.CodeBlock;

/**
 * @author Jiajun
 * @datae Jun 29, 2017
 */
public class SimpleFilter {
	private List<CodeBlock> _candidates = new ArrayList<>();
	private CodeBlock _buggyCode = null;
	
	public SimpleFilter(CodeBlock buggyCode) {
		_buggyCode = buggyCode;
	}
	
	public List<Pair<CodeBlock, Double>> filter(String srcPath, double guard){
		List<String> files = JavaFile.ergodic(srcPath, new ArrayList<String>());
		CollectorVisitor collectorVisitor = new CollectorVisitor();
		for(String file : files){
			String content = JavaFile.readFileToString(file);
			CompilationUnit unit = (CompilationUnit) JavaFile.genASTFromSource(content, ASTParser.K_COMPILATION_UNIT);
			collectorVisitor.setUnit(unit);
			unit.accept(collectorVisitor);
		}
		return filter(guard);
	}
	
	private List<Pair<CodeBlock, Double>> filter(double guard){
		List<Pair<CodeBlock, Double>> filtered = new ArrayList<>();
		int delta = Constant.MAX_BLOCK_LINE - _buggyCode.getCurrentLine();
		delta = delta > 0 ? delta : 0;
		guard = guard + ((0.99 - guard) * delta / Constant.MAX_BLOCK_LINE );
		System.out.println("Real guard value : " + guard);
		for(CodeBlock block : _candidates){
			Double similarity = CodeBlockMatcher.getSimilarity(_buggyCode, block);
			if(similarity < guard){
				continue;
			}
			filtered.add(new Pair<CodeBlock, Double>(block, similarity));
		}
		
		Collections.sort(filtered, new Comparator<Pair<CodeBlock, Double>>() {
			@Override
			public int compare(Pair<CodeBlock, Double> o1, Pair<CodeBlock, Double> o2) {
				if(o1.getSecond() < o2.getSecond()){
					return 1;
				} else if(o1.getSecond() > o2.getSecond()){
					return -1;
				} else {
					return 0;
				}
			}
		});
		return filtered;
	}
	
	class CollectorVisitor extends ASTVisitor{
		
		private CompilationUnit _unit = null;
		
		public void setUnit(CompilationUnit unit){
			_unit = unit;
		}
		
		public boolean visit(Block node){
			ASTNode parent = node.getParent();
			if(parent == null || parent instanceof MethodDeclaration || parent instanceof AnonymousClassDeclaration){
				return true;
			}
			
			int line = _unit.getLineNumber(node.getStartPosition());
			CodeSearch codeSearch = new CodeSearch(_unit, line, _buggyCode.getCurrentLine());
			CodeBlock codeBlock = new CodeBlock(_unit, codeSearch.getASTNodes());
			_candidates.add(codeBlock);
			return true;
		}
	}
	
}
