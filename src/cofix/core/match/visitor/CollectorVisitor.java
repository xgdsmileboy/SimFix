package cofix.core.match.visitor;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;

public class CollectorVisitor extends ASTVisitor{
	
	protected ASTNode _buggyCode = null;
	protected CompilationUnit _buggyUnit = null;
	protected float _similarityThreshold = 0.5f;
//	protected List<E>
	
	public CollectorVisitor(ASTNode buggyCode, CompilationUnit buggyUnit){
		_buggyCode = buggyCode;
		_buggyUnit = buggyUnit;
	}
	
	public CollectorVisitor(ASTNode buggyCode, CompilationUnit buggyUnit, float similarityThreshold){
		_buggyCode = buggyCode;
		_buggyUnit = buggyUnit;
		_similarityThreshold = similarityThreshold;
	}
	
//	public boolean visit(Block){
//		
//	}
	
}
