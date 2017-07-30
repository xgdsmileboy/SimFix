package cofix.test.purification.visitor;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;

public class InsertTestCaseVisitor extends ASTVisitor {

	private List<MethodDeclaration> _insertedTestCases = null;
	
	public InsertTestCaseVisitor(List<MethodDeclaration> testcases){
		_insertedTestCases = testcases;
	}
	
	public boolean visit(TypeDeclaration node){
		for(MethodDeclaration test : _insertedTestCases){
			node.bodyDeclarations().add(ASTNode.copySubtree(node.getAST(), test));
		}
		return true;
	}
	
}
