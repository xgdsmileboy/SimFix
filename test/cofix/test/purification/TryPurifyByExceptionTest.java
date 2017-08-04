package cofix.test.purification;

import java.util.Set;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.junit.Test;

import cofix.common.config.Configure;
import cofix.common.config.Constant;
import cofix.common.util.JavaFile;
import cofix.common.util.Subject;

public class TryPurifyByExceptionTest {
	
	@Test
	public void test(){
		Constant.PROJECT_HOME = "/home/jiajun/d4j/projects";
		Configure.configEnvironment();
		Subject subject = Configure.getSubject("math", 49);
		String fileName = "/home/jiajun/d4j/projects/math/math_49_buggy/src/test/java/org/apache/commons/math/linear/SparseRealVectorTest.java";
		String clazz = "org.apache.commons.math.linear.SparseRealVectorTest";
		String test = "testConcurrentModification";
		FindMethodDeclaration findMethodDeclaration = new FindMethodDeclaration(test);
		CompilationUnit unit = JavaFile.genASTFromFile(fileName);
		unit.accept(findMethodDeclaration);
		MethodDeclaration method = findMethodDeclaration.getmethod();
		TryPurifyByException tryPurifyByException = new TryPurifyByException(subject, fileName, unit, method, clazz, test);
		Set<String> teStrings = tryPurifyByException.purify();
		for(String string : teStrings){
			System.out.println(string);
		}
	}
	
	
	private class FindMethodDeclaration extends ASTVisitor{
		
		private String _name = null;
		private MethodDeclaration _node = null;
		
		public FindMethodDeclaration(String name) {
			_name = name;
		}
		
		public MethodDeclaration getmethod(){
			return _node;
		}
		
		@Override
		public boolean visit(MethodDeclaration node) {
			if(node.getName().getFullyQualifiedName().equals(_name)){
				_node = node;
				return false;
			}
			return true;
		}
	}
	
}
