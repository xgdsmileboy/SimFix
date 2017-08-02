package cofix.test.purification;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;

import cofix.common.util.JavaFile;


public class CommentTestCase {
	
	public static void comment(String fileBasePath, List<String> testcases, String avoid){
		Map<String, Set<String>> clazzAndMethods = new HashMap<>();
		for(String test : testcases){
			if(test.equals(avoid)){
				continue;
			}
			String[] testInfo = test.split("::");
			if(testInfo.length != 2){
				System.err.println("Test case format error : " + test);
				continue;
			}
			Set<String> methods = clazzAndMethods.get(testInfo[0]);
			if(methods == null){
				methods = new HashSet<>();
			}
			methods.add(testInfo[1]);
			clazzAndMethods.put(testInfo[0], methods);
		}
		
		for(Entry<String, Set<String>> entry : clazzAndMethods.entrySet()){
			String fileName = fileBasePath + "/" + entry.getKey().replace(".", "/") + ".java";
			CompilationUnit cUnit = JavaFile.genASTFromFile(fileName);
			CommentTestCaseVisitor visitor = new CommentTestCaseVisitor(entry.getValue());
			cUnit.accept(visitor);
			JavaFile.writeStringToFile(fileName, cUnit.toString());
		}
	}
	
	private static class CommentTestCaseVisitor extends ASTVisitor {

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
				for(int i = 0; i < node.modifiers().size(); i++){
					if(node.modifiers().get(i) instanceof NormalAnnotation){
						NormalAnnotation normalAnnotation = (NormalAnnotation) node.modifiers().get(i);
						AST ast = node.getAST();
						MarkerAnnotation annotation = ast.newMarkerAnnotation();
						annotation.setTypeName((Name) ASTNode.copySubtree(ast, normalAnnotation.getTypeName()));
						node.modifiers().set(i, annotation);
					} else if(node.modifiers().get(i) instanceof SingleMemberAnnotation){
						SingleMemberAnnotation singleMemberAnnotation = (SingleMemberAnnotation) node.modifiers().get(i);
						AST ast = node.getAST();
						MarkerAnnotation annotation = ast.newMarkerAnnotation();
						annotation.setTypeName((Name) ASTNode.copySubtree(ast, singleMemberAnnotation.getTypeName()));
						node.modifiers().set(i, annotation);
					}
				}
			}
			return true;
		}
	}
}
