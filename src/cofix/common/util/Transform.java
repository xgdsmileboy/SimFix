/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */

package cofix.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;

public class Transform {
	
	public static ASTNode transform(ASTNode node){
		AST ast = AST.newAST(AST.JLS8);
		ASTNode newNode = ASTNode.copySubtree(ast, node);
		newNode.accept(new RemoveIdentifierVisitor());
		return newNode;
	}
	
	public static ASTNode createStatement(String string, int type){
		ASTParser astParser = ASTParser.newParser(AST.JLS8);
		Map<?, ?> options = JavaCore.getOptions();
		JavaCore.setComplianceOptions(JavaCore.VERSION_1_7, options);
		astParser.setCompilerOptions(options);
		astParser.setSource(string.toCharArray());
		astParser.setKind(type);
		astParser.setResolveBindings(true);
		ASTNode node = astParser.createAST(null);
		return node;
	}
	
	/**
	 * this is a test method
	 * @param args
	 */
	public static void main(String[] args) {
		String code = "if(a > 0x1f && c <= 0x7f){}";
		String code2 = "if(target != null && target.getType(arg).getName() == Token.STRING){}";
		
		String code3 = "for(int a = 1; a < 10; a++){}";
		
		Block node = (Block)Transform.createStatement(code, ASTParser.K_STATEMENTS);
		Block node2 = (Block)Transform.createStatement(code2, ASTParser.K_STATEMENTS);
		
		Statement statement = (Statement) node.statements().get(0);
		Statement statement2 = (Statement) node2.statements().get(0);
		
		System.out.println(statement.getClass());
		System.out.println(statement2.getClass());
		
		System.out.println(statement.getClass() == statement2.getClass());
		
//		System.out.println(node.subtreeMatch(new ASTMatcher(true), node2));
		
		ASTNode newNode = Transform.transform(node2);
		System.out.println(newNode.toString());
		
	}
}

class RemoveIdentifierVisitor extends ASTVisitor{
	public RemoveIdentifierVisitor() {
	}
	
	public boolean visit(InfixExpression node){
		Expression lhExpression = node.getLeftOperand();
		Expression rhExpression = node.getRightOperand();
		node.setLeftOperand((Expression) ASTNode.copySubtree(node.getAST(), processExp(lhExpression)));
		node.setRightOperand((Expression) ASTNode.copySubtree(node.getAST(), processExp(rhExpression)));
		return true;
	}
	
	public boolean visit(Assignment node){
		Expression lhExpression = node.getLeftHandSide();
		Expression rhExpression = node.getRightHandSide();
		node.setLeftHandSide((Expression)ASTNode.copySubtree(node.getAST(), processExp(lhExpression)));
		node.setRightHandSide((Expression)ASTNode.copySubtree(node.getAST(), processExp(rhExpression)));
		return true;
	}
	
	public boolean visit(ArrayAccess node){
		return true;
	}
	
	public boolean visit(MethodInvocation node){
		node.setExpression((Expression)ASTNode.copySubtree(node.getAST(), processExp(node.getExpression())));
		node.setName(node.getAST().newSimpleName("CALL"));

		List<Expression> expressions = new ArrayList<>();
		for(Object object : node.arguments()){
			if(object instanceof Expression){
				Expression exp = (Expression) object;
				Expression trans = processExp(exp);
				expressions.add(trans);
			}
		}
		node.arguments().clear();
		for(Expression exp : expressions){
			node.arguments().add(ASTNode.copySubtree(node.getAST(), exp));
		}
		
		return true;
	}
	
	private Expression processExp(Expression expression){
		Expression result = (Expression)ASTNode.copySubtree(AST.newAST(AST.JLS8), expression);
		if(expression instanceof Name){
			result = AST.newAST(AST.JLS8).newSimpleName("ID");
		} else if(expression instanceof MethodInvocation){
			MethodInvocation methodInvocation = AST.newAST(AST.JLS8).newMethodInvocation();
			SimpleName name = methodInvocation.getAST().newSimpleName("ID");
			methodInvocation.setName(name);
			
			MethodInvocation mInvocation = (MethodInvocation) expression;
			
			methodInvocation.setExpression((Expression) ASTNode.copySubtree(methodInvocation.getAST(), mInvocation.getExpression()));
			
			for(Object obj : mInvocation.arguments()){
				if(obj instanceof Expression){
					Expression exp = (Expression) obj;
					Expression trans = processExp(exp);
					methodInvocation.arguments().add(ASTNode.copySubtree(methodInvocation.getAST(), trans));
				}
			}
			
		} else if(expression instanceof Assignment){
			
		} else if(expression instanceof ConditionalExpression){
			
		}
		return result;
	}

}
