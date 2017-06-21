package cofix.common.code.search;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;

import cofix.common.astnode.CodeBlock;
import cofix.common.astnode.expr.Variable;
import cofix.common.parser.NodeUtils;
import cofix.common.parser.ProjectInfo;
import cofix.common.util.JavaFile;
import cofix.common.util.Pair;
import cofix.core.match.Utils;

public class SimpleFilter {
	
	private List<CodeBlock> _candidates = new ArrayList<>();
	private CodeBlock _buggyCode = null;
	
	public SimpleFilter(CodeBlock buggyCode) {
		_buggyCode = buggyCode;
	}
	
	public List<Pair<CodeBlock, Float>> filter(String srcPath, double guard){
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
	
	private List<Pair<CodeBlock, Float>> filter(double guard){
		List<Pair<CodeBlock, Float>> filtered = new ArrayList<>();
		for(CodeBlock block : _candidates){
			Float similarity = Utils.computeSimilarity(_buggyCode, block);
			if(similarity < guard){
				continue;
			}
			filtered.add(new Pair<CodeBlock, Float>(block, similarity));
		}
		
		Collections.sort(filtered, new Comparator<Pair<CodeBlock, Float>>() {
			@Override
			public int compare(Pair<CodeBlock, Float> o1, Pair<CodeBlock, Float> o2) {
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
		
		@Override
		public boolean visit(SimpleName node) {
			String name = node.getFullyQualifiedName();
			Pair<String, String> classAndMethodName = NodeUtils.getTypeDecAndMethodDec(node);
			Type type = ProjectInfo.getVariableType(classAndMethodName.getFirst(), classAndMethodName.getSecond(), name);
			Variable variable = new Variable(node, type, name);
			if(_buggyCode.getVariables().containsKey(variable)){
				ASTNode parent = node.getParent();
				while(parent != null && !(parent instanceof MethodDeclaration)){
					parent = parent.getParent();
				}
				// filter out anonymous classes
				if(parent != null && !(parent.getParent() instanceof AnonymousClassDeclaration)){
					int line = _unit.getLineNumber(node.getStartPosition());
					CodeSearch codeSearch = new CodeSearch(_unit, line, _buggyCode.getCurrentLine());
					CodeBlock codeBlock = new CodeBlock(_unit, codeSearch.getASTNodes());
					_candidates.add(codeBlock);
				}
			}
			return true;
		}
		
	}
	
}
