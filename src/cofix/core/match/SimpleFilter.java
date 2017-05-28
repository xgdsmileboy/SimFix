package cofix.core.match;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.dom.ASTVisitor;

import cofix.common.astnode.CodeBlock;

public class SimpleFilter {
	
	private List<CodeBlock> _candidates = new ArrayList<>();
	private CodeBlock _buggyCode = null;
	
	public SimpleFilter(CodeBlock buggyCode) {
		_buggyCode = buggyCode;
	}
	
	private List<CodeBlock> getFilteredCandidates(){
		return _candidates;
	}
	
	class CollectorVisitor extends ASTVisitor{
		
	}
	
}
