package cofix.core.modify.diff;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import cofix.common.config.Constant;
import cofix.common.util.JavaFile;
import cofix.common.util.Pair;
import cofix.core.modify.pattern.match.Matcher;
import cofix.core.parser.node.CodeBlock;
import cofix.core.parser.node.Node;

public abstract class Diff<T> {

	protected Node _src;
	protected Node _tar;
	protected List<T> _source;
	
	public Diff(Node src, Node tar) {
		_src = src;
		_tar = tar;
		extractDiff();
	}
	
	public boolean exist() {
		return _source != null && _source.size() > 0;
	}
	
	public List<T> getFullDiff() {
		return _source;
	}

	public List<T> getMiniDiff() {
		List<T> miniDiff = new ArrayList<>(_source.size());
		for(T t : _source) {
			if(t instanceof Add || t instanceof Delete) {
				miniDiff.add(t);
			}
		}
		return miniDiff;
	}
	
	protected abstract void extractDiff(); 
	public abstract void accurateMatch();
	
	public String miniDiff() {
		StringBuffer stringBuffer = new StringBuffer();
		for(T t : _source) {
			if(t instanceof Add || t instanceof Delete) {
				stringBuffer.append(t.toString());
				stringBuffer.append(Constant.NEW_LINE);
			}
		}
		return stringBuffer.toString();
	}
	
	@Override
	public String toString() {
		StringBuffer stringBuffer = new StringBuffer();
		if(_source != null) {
			for(T t : _source) {
				stringBuffer.append(t.toString());
				stringBuffer.append(Constant.NEW_LINE);
			}
		}
		return stringBuffer.toString();
	}
	
	public static List<Diff> extractFileDiff(String srcFile, String tarFile, Class<? extends Diff> clazz) {
		List<Diff> diffs = new LinkedList<>();
		CompilationUnit srcUnit = JavaFile.genASTFromFile(srcFile);
		CompilationUnit tarUnit = JavaFile.genASTFromFile(tarFile);
		List<Pair<MethodDeclaration, MethodDeclaration>> matchMap = Matcher.match(srcUnit, tarUnit);
		for(Pair<MethodDeclaration, MethodDeclaration> pair : matchMap) {
			Node srcNode = new CodeBlock(srcFile, srcUnit, Arrays.asList((ASTNode)pair.getFirst().getBody()));
			String src = srcNode.toSrcString().toString();
			Node tarNode = new CodeBlock(tarFile, tarUnit, Arrays.asList((ASTNode)pair.getSecond().getBody()));
			String tar = tarNode.toSrcString().toString();
			
			if(src.equals(tar)) {
				continue;
			}
			
			try {
				Diff diff = clazz.getConstructor(Node.class, Node.class).newInstance(srcNode, tarNode);
				diffs.add(diff);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		return diffs;
	}
	
}
