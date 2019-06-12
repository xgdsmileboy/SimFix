package cofix.test.purification;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;

import cofix.common.run.Runner;
import cofix.common.util.JavaFile;
import cofix.common.util.Subject;
import sun.management.counter.Units;

public class TryPurifyByException {
	
	private String _fileName = null;
	private CompilationUnit _unit = null;
	private String _clazz = null;
	private String _testName = null;
	private MethodDeclaration _method = null;
	private Block _backupBody = null;
	private Set<String> _purifiedTestCases = null;
	private Subject _subject = null;
	
	public TryPurifyByException(Subject subject, String fileName, CompilationUnit unit, MethodDeclaration method, String clazz, String test) {
		_subject = subject;
		_fileName = fileName;
		_unit = unit;
		_method = method;
		_clazz = clazz;
		_testName = test;
	}
	
	public Set<String> purify(){
		if(_method != null && _method.getBody() != null){
			AST ast = AST.newAST(AST.JLS8);
			_backupBody = (Block) ASTNode.copySubtree(ast, _method.getBody());
			splitMethodCalls();
		}
		return _purifiedTestCases;
	}
	
	private void splitMethodCalls(){
		_purifiedTestCases = new HashSet<>();
		Set<Integer> set = findAllMethodCall();
		boolean shouldRecover = true;
		if (ifCommentAllPass(set)) {
			Set<Integer> failed = new HashSet<>();
			for(Integer mcallLine : set){
				_method.getBody().statements().clear();
				AST ast = _method.getAST();
				for(int i = 0; i < _backupBody.statements().size(); i++){
					if(set.contains(i) && mcallLine != i){
						_method.getBody().statements().add(tryCatchStmt(ast, (ASTNode) _backupBody.statements().get(i)));
					} else {
						_method.getBody().statements().add(ASTNode.copySubtree(ast, (ASTNode) _backupBody.statements().get(i)));
					}
				}
				JavaFile.writeStringToFile(_fileName, _unit.toString());
				if (!Runner.testSingleTest(_subject, _clazz, _testName)) {
					failed.add(mcallLine);
				}
			}
			if (failed.size() > 1) {
				shouldRecover = false;
				int methodID = 1;
				ASTNode parent = _method.getParent();
				while(parent != null){
					if(parent instanceof TypeDeclaration){
						break;
					}
					parent = parent.getParent();
				}
				if(parent != null){
					TypeDeclaration container = (TypeDeclaration) parent;
					for(Integer line : failed){
						AST ast = AST.newAST(AST.JLS8);
						MethodDeclaration newMethod = ast.newMethodDeclaration();
						String newName = _testName + "_purify_" + methodID;
						methodID ++;
						newMethod.setName(ast.newSimpleName(newName));
						newMethod.modifiers().addAll(ASTNode.copySubtrees(ast, _method.modifiers()));
						if(_method.thrownExceptionTypes().size() > 0){
							newMethod.thrownExceptionTypes().addAll(ASTNode.copySubtrees(ast, _method.thrownExceptionTypes()));
						}
						newMethod.setReturnType2(ast.newPrimitiveType(PrimitiveType.VOID));
						List<ASTNode> result = new ArrayList<>();
						for(int i = 0; i < _backupBody.statements().size(); i++){
							if(failed.contains(i) && i != line){
								result.add(tryCatchStmt(ast, (ASTNode) _backupBody.statements().get(i)));
							} else {
								result.add(ASTNode.copySubtree(ast, (ASTNode) _backupBody.statements().get(i)));
							}
						}
						Block body = ast.newBlock();
						body.statements().addAll(result);
						newMethod.setBody(body);
						container.bodyDeclarations().add(ASTNode.copySubtree(_method.getAST(), newMethod));
						_purifiedTestCases.add(_clazz + "::" + newName);
					}
					_method.getBody().statements().clear();
					JavaFile.writeStringToFile(_fileName, _unit.toString());
				}
			}
		}
		if(shouldRecover && _backupBody != null){
			_purifiedTestCases.add(_clazz + "::" + _testName);
			_method.setBody((Block) ASTNode.copySubtree(_method.getAST(), _backupBody));
		}
	}
	
	private boolean ifCommentAllPass(Set<Integer> stmts){
		if(stmts == null || stmts.size() == 1){
			return false;
		}
		AST ast = _method.getAST();
		_method.getBody().statements().clear();
		for(int i = 0; i < _backupBody.statements().size(); i++){
			if (stmts.contains(i)) {
				_method.getBody().statements().add(tryCatchStmt(ast, (ASTNode) _backupBody.statements().get(i)));
			} else {
				_method.getBody().statements().add(ASTNode.copySubtree(ast, (ASTNode) _backupBody.statements().get(i)));
			}
		}
		
		JavaFile.writeStringToFile(_fileName, _unit.toString());
		if(Runner.testSingleTest(_subject, _clazz, _testName)){
			return true;
		}
		
		return false;
	}
	
	private ASTNode tryCatchStmt(AST ast, ASTNode node){
		Block block = ast.newBlock();
		block.statements().add(ASTNode.copySubtree(ast, node));
		TryStatement tryStatement = ast.newTryStatement();
		tryStatement.setBody(block);
		CatchClause catchClause = ast.newCatchClause();
		SingleVariableDeclaration svd = ast.newSingleVariableDeclaration();
		svd.setType(ast.newSimpleType(ast.newSimpleName("Exception")));
		svd.setName(ast.newSimpleName("mException"));
		catchClause.setException(svd);
		tryStatement.catchClauses().add(catchClause);
		return tryStatement;
	}
	
	private Set<Integer> findAllMethodCall(){
		Set<Integer> methodStmt = new HashSet<>();
		if(_backupBody != null){
			Block body = _backupBody;
			for(int i = 0; i < body.statements().size(); i++){
				ASTNode stmt = (ASTNode) body.statements().get(i);
				if(stmt instanceof ExpressionStatement){
					stmt = ((ExpressionStatement) stmt).getExpression();
					if(stmt instanceof MethodInvocation){
						methodStmt.add(i);
					} else if(stmt instanceof Assignment){
						Assignment assign = (Assignment) stmt;
						if(assign.getRightHandSide() instanceof MethodInvocation){
							methodStmt.add(i);
						}
					}
				}
			}
			
		}
		return methodStmt;
	}
		
}
