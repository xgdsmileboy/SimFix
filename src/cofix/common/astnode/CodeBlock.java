/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */

package cofix.common.astnode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.CreationReference;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodReference;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SuperMethodReference;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeMethodReference;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import cofix.common.util.Pair;

public class CodeBlock {
	
	private CompilationUnit _cunit = null;
	private List<ASTNode> _nodes = null;
	// <name, <type, count>>
	private Map<String, Pair<Type, Integer>> _variables = null;
	// <literal, count>
	private Map<Literal, Integer> _constants = null;
	// <if, else, for>
	private List<Structure> _structures = null;
	// {type.name(p0,p1)}
	private Set<MethodCall> _methodCalls = null;
	// <+, 4>
	private Map<Operator, Integer> _operators = null;
	
	
	public CodeBlock(CompilationUnit cunit, List<ASTNode> nodes) {
		_cunit = cunit;
		_nodes = nodes;
	}
	
	public List<ASTNode> getNodes(){
		return _nodes;
	}
	
	public void accept(ASTVisitor visitor){
		for(ASTNode node : _nodes){
			node.accept(visitor);
		}
	}
	
	public Map<String, Pair<Type, Integer>> getVariables(){
		if(_variables == null){
			parseNode();
		}
		return _variables;
	}
	
	public Map<Literal, Integer> getConstants(){
		if(_constants == null){
			parseNode();
		}
		return _constants;
	}
	
	public List<Structure> getStructures(){
		if(_structures == null){
			parseNode();
		}
		return _structures;
	}
	
	public Set<MethodCall> getMethodCalls(){
		if(_methodCalls == null){
			parseNode();
		}
		return _methodCalls;
	}
	
	public Map<Operator, Integer> getOperators(){
		if(_operators == null){
			parseNode();
		}
		return _operators;
	}
	
	private void parseNode(){
		_variables = new HashMap<>();
		_constants = new HashMap<>();
		_structures = new ArrayList<>();
		_methodCalls = new HashSet<>();
		_operators = new HashMap<>();
		ParseVisitor parseVisitor = new ParseVisitor();
		for(ASTNode node : _nodes){
			node.accept(parseVisitor);
		}
	}
	
	class ParseVisitor extends ASTVisitor{
		
		public boolean visit(AssertStatement node) {
			return true;
		}
		
		public boolean visit(BreakStatement node) {
			_structures.add(Structure.BREAK);
			return true;
		}
		
		public boolean visit(Block node) {
			return true;
		}
		
		public boolean visit(ConstructorInvocation node) {
			ASTNode parent = node.getParent();
			String methodName = null;
			String className = null;
			while(parent != null){
				if(parent instanceof MethodDeclaration){
					methodName = ((MethodDeclaration)parent).getName().getFullyQualifiedName();
				} else if(parent instanceof TypeDeclaration){
					className = ((TypeDeclaration)parent).getName().getFullyQualifiedName();
				}
				parent = parent.getParent();
			}
			
			return true;
		}
		
		public boolean visit(ContinueStatement node) {
			return true;
		}
		
		public boolean visit(DoStatement node) {
			return true;
		}
		
		public boolean visit(EnhancedForStatement node) {
			return true;
		}
		
		public boolean visit(ExpressionStatement node) {
			return true;
		}
		
		public boolean visit(ForStatement node) {
			return true;
		}
		
		public boolean visit(IfStatement node) {
			return true;
		}
		
		public boolean visit(LabeledStatement node) {
			return true;
		}
		
		public boolean visit(ReturnStatement node) {
			return true;
		}
		
		public boolean visit(SuperConstructorInvocation node) {
			return true;
		}
		
		public boolean visit(SwitchCase node) {
			return true;
		}
		
		public boolean visit(SwitchStatement node) {
			return true;
		}
		
		public boolean visit(SynchronizedStatement node) {
			return true;
		}
		
		public boolean visit(ThrowStatement node) {
			return true;
		}
		
		public boolean visit(TryStatement node) {
			return true;
		}
		
		public boolean visit(TypeDeclarationStatement node){
			return true;
		}
		
		public boolean visit(VariableDeclarationStatement node){
			return true;
		}
		
		public boolean visit(WhileStatement node) {
			return true;
		}
		/*********************** Expression *********************************/
		public boolean visit(ArrayAccess node) {
			return true;
		}
		
		public boolean visit(ArrayCreation node) {
			return true;
		}
		
		public boolean visit(ArrayInitializer node) {
			return true;
		}
		
		public boolean visit(Assignment node) {
			return true;
		}
		
		public boolean visit(BooleanLiteral node) {
			return true;
		}
		
		public boolean visit(CastExpression node) {
			return true;
		}
		
		public boolean visit(CharacterLiteral node) {
			return true;
		}
		
		public boolean visit(ClassInstanceCreation node) {
			return true;
		}
		
		public boolean visit(ConditionalExpression node) {
			return true;
		}
		
		public boolean visit(CreationReference node) {
			return true;
		}
		
		public boolean visit(ExpressionMethodReference node) {
			return true;
		}
		
		public boolean visit(FieldAccess node) {
			return true;
		}
		
		public boolean visit(InfixExpression node) {
			return true;
		}
		
		public boolean visit(InstanceofExpression node) {
			return true;
		}
		
		public boolean visit(LambdaExpression node) {
			return true;
		}
		
		public boolean visit(MethodInvocation node) {
			return true;
		}

		public boolean visit(MethodReference node) {
			return true;
		}

		public boolean visit(Name node) {
			return true;
		}

		public boolean visit(NullLiteral node) {
			return true;
		}

		public boolean visit(NumberLiteral node) {
			return true;
		}

		public boolean visit(ParenthesizedExpression node) {
			return true;
		}

		public boolean visit(PostfixExpression node) {
			return true;
		}

		public boolean visit(PrefixExpression node) {
			return true;
		}

		public boolean visit(StringLiteral node) {
			return true;
		}

		public boolean visit(SuperFieldAccess node) {
			return true;
		}

		public boolean visit(SuperMethodInvocation node) {
			return true;
		}

		public boolean visit(SuperMethodReference node) {
			return true;
		}
		
		public boolean visit(ThisExpression node){
			return true;
		}

		public boolean visit(TypeLiteral node) {
			return true;
		}

		public boolean visit(TypeMethodReference node) {
			return true;
		}

		public boolean visit(VariableDeclarationExpression node) {
			return true;
		}
		
	}
	
}
