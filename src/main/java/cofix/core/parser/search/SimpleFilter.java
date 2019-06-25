/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.parser.search;

import cofix.common.config.Constant;
import cofix.common.util.JavaFile;
import cofix.common.util.Pair;
import cofix.core.match.CodeBlockMatcher;
import cofix.core.metric.CondStruct;
import cofix.core.metric.MethodCall;
import cofix.core.metric.OtherStruct;
import cofix.core.metric.Variable;
import cofix.core.parser.NodeUtils;
import cofix.core.parser.ProjectInfo;
import cofix.core.parser.node.CodeBlock;
import cofix.core.search.CodeSearcher;
import cofix.core.search.SearchResult;
import org.eclipse.jdt.core.dom.*;

import java.util.*;

/**
 * @author Jiajun
 * @date Jun 29, 2017
 */
public class SimpleFilter extends CodeSearcher {
	private List<CodeBlock> _candidates = new ArrayList<>();
	private CodeBlock _buggyCode;
	private Set<Variable> _variables;
	private Set<CondStruct.KIND> _condStruct;
	private Set<OtherStruct.KIND> _otherStruct;
	private Set<String> _methods;
	private int _max_line = 0;
	private int DELTA_LINE = 10;
	private double _simGuard = 0.5;

	public SimpleFilter(CodeBlock buggyCode, double similarity) {
		super(buggyCode, null);
		_simGuard = similarity;
		_buggyCode = buggyCode;
		_variables = new HashSet<>(buggyCode.getVariables());
		_condStruct = new HashSet<>();
		for(CondStruct condStruct : buggyCode.getCondStruct()){
			_condStruct.add(condStruct.getKind());
		}
		_otherStruct = new HashSet<>();
		for(OtherStruct otherStruct : buggyCode.getOtherStruct()){
			_otherStruct.add(otherStruct.getKind());
		}
		_methods = new HashSet<>();
		for(MethodCall call : _buggyCode.getMethodCalls()){
			_methods.add(call.getName());
		}
		_max_line = _buggyCode.getCurrentLine() + DELTA_LINE;
	}

	@Override
	public SearchResult search(String filePath) {
		SearchResult result = new SearchResult();
		result.setBlocks(filter(filePath));
		return result;
	}

	private List<Pair<CodeBlock, Double>> filter(String srcPath){
		List<String> files = JavaFile.ergodic(srcPath, new ArrayList<String>());
		List<Pair<CodeBlock, Double>> filtered = new ArrayList<>();
		CollectorVisitor collectorVisitor = new CollectorVisitor();
		for(String file : files){
			CompilationUnit unit = JavaFile.genAST(file);
			collectorVisitor.setUnit(file, unit);
			unit.accept(collectorVisitor);
			filtered = filter(filtered, _simGuard);
		}
		
		Set<String> exist = new HashSet<>();
		for(Pair<CodeBlock, Double> pair : filtered){
			if(exist.contains(pair.getFirst().toSrcString().toString())){
				continue;
			}
			exist.add(pair.getFirst().toSrcString().toString());
			double similarity = CodeBlockMatcher.getRewardSimilarity(_buggyCode, pair.getFirst()) + pair.getSecond();
			pair.setSecond(similarity);
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
	
	private List<Pair<CodeBlock, Double>> filter(List<Pair<CodeBlock, Double>> filtered, double guard){
//		List<Pair<CodeBlock, Double>> filtered = new ArrayList<>();
		int delta = Constant.MAX_BLOCK_LINE - _buggyCode.getCurrentLine();
		delta = delta > 0 ? delta : 0;
		guard = guard + ((0.7 - guard) * delta / Constant.MAX_BLOCK_LINE ); // 0.9
//		System.out.println("Real guard value : " + guard);
		Set<String> codeRec = new HashSet<>();
		for(CodeBlock block : _candidates){
			if(_otherStruct.size() + _condStruct.size() > 0){
				if((block.getCondStruct().size() + block.getOtherStruct().size()) == 0){
					continue;
				}
			}
			Double similarity = CodeBlockMatcher.getSimilarity(_buggyCode, block);
//			System.out.println(block.toSrcString().toString());
			if(similarity < guard){
//				System.out.println("Filtered by similiraty value : " + similarity);
				continue;
			}
//			similarity += CodeBlockMatcher.getRewardSimilarity(_buggyCode, block);
//			if (codeRec.contains(block.toSrcString().toString()) || _buggyCode.hasIntersection(block)) {
//				System.out.println("Duplicate >>>>>>>>>>>>>>>>");
//			} else {
				if(block.getCurrentLine() == 1 && _buggyCode.getCurrentLine() != 1){
					continue;
				}
				int i = 0;
				boolean hasIntersection = false;
				int replace = -1;
				for(; i < filtered.size(); i++){
					Pair<CodeBlock, Double> pair = filtered.get(i);
					if(pair.getFirst().hasIntersection(block)){
						hasIntersection = true;
						if(similarity > pair.getSecond()){
							replace = i;
						}
						break;
					}
				}
				
				if(hasIntersection){
					if(replace != -1){
						filtered.remove(replace);
						codeRec.add(block.toSrcString().toString());
						filtered.add(new Pair<CodeBlock, Double>(block, similarity));
					}
				} else {
					codeRec.add(block.toSrcString().toString());
					filtered.add(new Pair<CodeBlock, Double>(block, similarity));
				}
//			}
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
		_candidates = new ArrayList<>();
		if(filtered.size() > 1000){
			for(int i = filtered.size() - 1; i > 1000; i--){
				filtered.remove(i);
			}
		}
		return filtered;
	}

	class CollectorVisitor extends ASTVisitor{
		
		private CompilationUnit _unit = null;
		private String _fileName = null;
		
		public void setUnit(String fileName, CompilationUnit unit){
			_fileName = fileName;
			_unit = unit;
		}
		
		@Override
		public boolean visit(SimpleName node) {
			String name = node.getFullyQualifiedName();
			if(Character.isUpperCase(name.charAt(0))){
				return true;
			}
			Pair<String, String> classAndMethodName = NodeUtils.getTypeDecAndMethodDec(node);
			Type type = ProjectInfo.getVariableType(classAndMethodName.getFirst(), classAndMethodName.getSecond(), name);
			Variable variable = new Variable(null, name, type);
			boolean match = false;
			if(_variables.contains(variable) || _methods.contains(name) || sameStructure(node)){
				match = true;
			} else {
				ASTNode parent = node.getParent();
				while(parent != null && !(parent instanceof Statement)){
					if(parent instanceof MethodInvocation){
						if(_methods.contains(((MethodInvocation) parent).getName().getFullyQualifiedName())){
							match = true;
						}
						break;
					}
					parent = parent.getParent();
				}
			}
			if(match){
				ASTNode parent = node.getParent();
				Statement statement = null;
				while(parent != null && !(parent instanceof MethodDeclaration)){
					parent = parent.getParent();
					if(statement == null && parent instanceof Statement){
						statement = (Statement) parent;
					}
				}
				// filter out anonymous classes
				if(parent != null && !(parent.getParent() instanceof AnonymousClassDeclaration)){
					int line = _unit.getLineNumber(node.getStartPosition());
					CodeSearch codeSearch = new CodeSearch(_unit, line, _buggyCode.getCurrentLine(), statement);
					CodeBlock codeBlock = new CodeBlock(_fileName, _unit, codeSearch.getASTNodes());
					if(codeBlock.getCurrentLine() < _max_line){
						_candidates.add(codeBlock);
					}
				}
			}
			return true;
		}
		
		private boolean sameStructure(SimpleName name){
			return false;
//			if(_condStruct.size() == 0 && _otherStruct.size() == 0){
//				return false;
//			}
//			ASTNode parent = name.getParent();
//			Object kind = null;
//			while(parent != null){
//				if(parent instanceof MethodDeclaration){
//					break;
//				} else if(parent instanceof IfStatement){
//					kind = CondStruct.KIND.IF;
//					break;
//				} else if(parent instanceof SwitchStatement){
//					kind = CondStruct.KIND.SC;
//					break;
//				} else if(parent instanceof ReturnStatement){
//					kind = OtherStruct.KIND.RETURN;
//					break;
//				} else if(parent instanceof ConditionalExpression){
//					kind = CondStruct.KIND.CE;
//					break;
//				} else if(parent instanceof ThrowStatement){
//					kind = OtherStruct.KIND.THROW;
//					break;
//				}
//				parent = parent.getParent();
//			}
//			if(kind == null){
//				return false;
//			}
//			if(_condStruct.contains(kind) || _otherStruct.contains(kind)){
//				return true;
//			}
//			return false;
		}
	}
	
}
