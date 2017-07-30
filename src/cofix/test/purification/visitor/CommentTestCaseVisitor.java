package cofix.test.purification.visitor;

import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;


public class CommentTestCaseVisitor extends ASTVisitor {

	private Set<String> _testsToBeCommented = null;
	
	public CommentTestCaseVisitor(Set<String> testcases) {
		_testsToBeCommented = testcases;
	}
	
	@Override
	public boolean visit(MethodDeclaration node) {
		String name = node.getName().getFullyQualifiedName();
		if(_testsToBeCommented.contains(name)){
			Block emptyBody = node.getAST().newBlock();
			node.setBody(emptyBody);
		}
		return true;
	}
	
}
