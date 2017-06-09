/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */

package cofix.common.astnode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
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
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ExpressionMethodReference;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LambdaExpression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodReference;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
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
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeMethodReference;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;

import cofix.common.parser.NodeUtils;
import cofix.common.parser.ProjectInfo;
import cofix.common.util.LevelLogger;
import cofix.common.util.Pair;

public class CodeBlock {
	
	private CompilationUnit _cunit = null;
	private List<Statement> _nodes = null;
	private int _maxLines = 10;
	// <name, <type, count>>
	private Map<Variable, Integer> _variables = null;
	// <literal, count>
	private Map<Literal, Integer> _constants = null;
	// <if, else, for>
	private List<Structure> _structures = null;
	// {type.name(p0,p1)}
	private Map<MethodCall, Integer> _methodCalls = null;
//	private Set<MethodCall> _methodCalls = null;
	
	private List<Operator> _operators = null;
	
	private Stack<Structure> _exprs = new Stack<>();
	
	
	public CodeBlock(CompilationUnit cunit, List<Statement> nodes) {
		_cunit = cunit;
		_nodes = nodes;
	}
	
	public CodeBlock(CompilationUnit cunit, List<Statement> nodes, int maxLines) {
		_cunit = cunit;
		_nodes = nodes;
		_maxLines = maxLines;
	}
	
	public List<Statement> getNodes(){
		return _nodes;
	}
	
	public int getMaxLines(){
		return _maxLines;
	}
	
	public void accept(ASTVisitor visitor){
		for(Statement node : _nodes){
			node.accept(visitor);
		}
	}
	
	public Map<Variable, Integer> getVariables(){
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
	
	public Map<MethodCall, Integer> getMethodCalls(){
		if(_methodCalls == null){
			parseNode();
		}
		return _methodCalls;
	}
	
	public List<Operator> getOperators(){
		if(_operators == null){
			parseNode();
		}
		return _operators;
	}
	
	private void parseNode(){
		_variables = new HashMap<>();
		_constants = new HashMap<>();
		_structures = new ArrayList<>();
		_methodCalls = new HashMap<>();
		_operators = new ArrayList<>();
		
		for(Statement node : _nodes){
			process(node);
		}
	}
	
	/************************** visit start : Statement ***********************/
	private Expr visit(AssertStatement node) {
		return null;
	}
	
	private Expr visit(BreakStatement node) {
		Structure structure = new Structure(node, Structure.BREAK);
		if(!_exprs.isEmpty()){
			_exprs.peek().addExpr(structure);
		}
		_structures.add(structure);
		return null;
	}
	
	private Expr visit(Block node) {
		for(Object object : node.statements()){
			process((ASTNode) object);
		}
		return null;
	}
	
	private Expr visit(ConstructorInvocation node) {
		
		Pair<String, String> decls = NodeUtils.getTypeDecAndMethodDec(node);
		String className = decls.first();
		String methodName = decls.second();
		MethodCall expr = null;
		if(methodName != null && className != null){
			List<Expr> params = new ArrayList<>();
			for(Object object : node.arguments()){
				params.add(process((ASTNode)object));
			}
			Type type = ProjectInfo.getVariableType(className, methodName, "THIS");
			expr = new MethodCall(node, type, null, "THIS", params);
			if(!_exprs.isEmpty()){
				_exprs.peek().addExpr(expr);
			}
			Integer count = _methodCalls.get(expr);
			if(count == null){
				count = 0;
			}
			_methodCalls.put(expr, count + 1);
		}
		
		return expr;
	}
	
	private Expr visit(ContinueStatement node) {
		Structure structure = new Structure(node, Structure.CONTINUE);
		if(!_exprs.isEmpty()){
			_exprs.peek().addExpr(structure);
		}
		_structures.add(structure);
		return null;
	}
	
	private Expr visit(DoStatement node) {
		Structure structure = new Structure(node, Structure.WHILE);
		if(!_exprs.isEmpty()){
			_exprs.peek().addExpr(structure);
		}
		_exprs.push(structure);
		_structures.add(structure);
		Expr expr = process(node.getExpression());
		if(expr != null){
			structure.addExpr(expr);
		}
		process(node.getBody());
		_exprs.pop();
		return null;
	}
	
	private Expr visit(EmptyStatement node) {
		return null;
	}
	
	private Expr visit(EnhancedForStatement node) {
		Structure structure = new Structure(node, Structure.FOR);
		if(!_exprs.isEmpty()){
			_exprs.peek().addExpr(structure);
		}
		_exprs.push(structure);
		_structures.add(structure);
		process(node.getParameter());
		Expr expr = process(node.getExpression());
		if(expr != null){
			structure.addExpr(expr);
		}
		process(node.getBody());
		_exprs.pop();
		return null;
	}
	
	private Expr visit(ExpressionStatement node) {
		Expr expr = process(node.getExpression());
		if(!_exprs.isEmpty()){
			_exprs.peek().addExpr(expr);
		}
		return expr;
	}
	
	private Expr visit(ForStatement node) {
		Structure structure = new Structure(node, Structure.FOR);
		if(!_exprs.isEmpty()){
			_exprs.peek().addExpr(structure);
		}
		_exprs.push(structure);
		_structures.add(structure);
		for(Object object : node.initializers()){
			process((ASTNode)object);
		}
		Expr expr = process(node.getExpression());
		if(expr != null){
			structure.addExpr(expr);
		}
		for(Object object : node.updaters()){
			process((ASTNode)object);
		}
		process(node.getBody());
		_exprs.pop();
		return null;
	}
	
	private Expr visit(IfStatement node) {
		Structure structure = new Structure(node, Structure.IF);
		if(!_exprs.isEmpty()){
			_exprs.peek().addExpr(structure);
		}
		_exprs.push(structure);
		_structures.add(structure);
		Expr expr = process(node.getExpression());
		if(expr != null){
			structure.addExpr(expr);
		}
		if(node.getThenStatement() != null){
			process(node.getThenStatement());
		}
		_exprs.pop();
		if(node.getElseStatement() != null){
			Structure struct = new Structure(node, Structure.ELSE);
			if(!_exprs.isEmpty()){
				_exprs.peek().addExpr(struct);
			}
			_exprs.add(struct);
			_structures.add(struct);
			process(node.getElseStatement());
			_exprs.pop();
		}
		return null;
	}
	
	private Expr visit(LabeledStatement node) {
		return null;
	}
	
	private Expr visit(ReturnStatement node) {
		Structure structure = new Structure(node, Structure.RETURN);
		if(!_exprs.isEmpty()){
			_exprs.peek().addExpr(structure);
		}
		_exprs.add(structure);
		_structures.add(structure);
		Expr expr = process(node.getExpression());
		if(expr != null){
			structure.addExpr(expr);
		}
		_exprs.pop();
		return null;
	}
	
	private Expr visit(SuperConstructorInvocation node) {
		
		Pair<String, String> decls = NodeUtils.getTypeDecAndMethodDec(node);
		String className = decls.first();
		String methodName = decls.second();
		MethodCall expr = null;
		if(methodName != null && className != null){
			List<Expr> params = new ArrayList<>();
			for(Object object : node.arguments()){
				params.add(process((ASTNode) object));
			}
			Type type = ProjectInfo.getVariableType(className, methodName, "SUPER");
			expr = new MethodCall(node, type, null, "SUPER", params);
			if(!_exprs.isEmpty()){
				_exprs.peek().addExpr(expr);
			}
			Integer count = _methodCalls.get(expr);
			if(count == null){
				count = 0;
			}
			_methodCalls.put(expr, count + 1);
		}
		
		return expr;
	}
	
	private Expr visit(SwitchCase node) {
		return null;
	}
	
	private Expr visit(SwitchStatement node) {
		Expression expression = node.getExpression();
		Expr switchExp = process(expression);
		// TODO : switchExp should not be null ?
		if(switchExp.getType().isPrimitiveType()){
			Structure structure = null;
			for(Object object : node.statements()){
				if(object instanceof SwitchCase){
					structure = new Structure((ASTNode) object, Structure.IF);
					if(!_exprs.isEmpty()){
						_exprs.peek().addExpr(structure);
					}
					_exprs.add(structure);
					_structures.add(structure);
					AST ast = AST.newAST(AST.JLS8);
					Operator operator = new Operator((ASTNode) object, ast.newPrimitiveType(PrimitiveType.BOOLEAN), Operator.EQ);
					operator.setLeftOprand(switchExp);
					operator.setRightOperand(process(((SwitchCase)object).getExpression()));
					_operators.add(operator);
					structure.addExpr(operator);
					_exprs.pop();
				} else {
					_exprs.push(structure);
					process((ASTNode)object);
					_exprs.pop();
				}
			}
		} else {
			Structure structure = null;
			for(Object object : node.statements()){
				if(object instanceof SwitchCase){
					structure = new Structure((ASTNode) object, Structure.IF);
					if(!_exprs.isEmpty()){
						_exprs.peek().addExpr(structure);
					}
					_exprs.add(structure);
					_structures.add(structure);
					Expr expr = process(((SwitchCase)object).getExpression());
					List<Expr> params = new ArrayList<>();
					params.add(expr);
					AST ast = AST.newAST(AST.JLS8);
					Type type = ast.newPrimitiveType(PrimitiveType.BOOLEAN);
					MethodCall methodCall = new MethodCall((ASTNode) object, type, switchExp, "equals", params);
					structure.addExpr(methodCall);
					Integer count = _methodCalls.get(methodCall);
					if(count == null){
						count = 0;
					}
					_methodCalls.put(methodCall, count + 1);
					_exprs.pop();
				} else {
					_exprs.push(structure);
					process((ASTNode)object);
					_exprs.pop();
				}
			}
		}
		
		return null;
	}
	
	private Expr visit(SynchronizedStatement node) {
		return null;
	}
	
	private Expr visit(ThrowStatement node) {
		Structure structure = new Structure(node, Structure.THRWO);
		if(!_exprs.isEmpty()){
			_exprs.peek().addExpr(structure);
		}
		_exprs.add(structure);
		_structures.add(structure);
		Expr expr = process((ASTNode)node.getExpression());
		if(expr != null){
			structure.addExpr(expr);
		}
		_exprs.pop();
		return null;
	}
	
	private Expr visit(TryStatement node) {
		// TODO : catch and finally block
		process((ASTNode)node.getBody());
		return null;
	}
	
	private Expr visit(TypeDeclarationStatement node){
		return null;
	}
	
	private Expr visit(VariableDeclarationStatement node){
		Type type = node.getType();
		for(Object object : node.fragments()){
			VariableDeclarationFragment vdf = (VariableDeclarationFragment) object;
			String varName = vdf.getName().getFullyQualifiedName();
			Variable variable = new Variable(vdf, type, varName);
			Integer count = _variables.get(variable);
			if(count == null){
				count = 0;
			}
			_variables.put(variable, count + 1);
			boolean hasInitializer = false;
			Operator operator = null;
			if(vdf.getInitializer() != null){
				hasInitializer = true;
				AST ast = AST.newAST(AST.JLS8);
				operator = new Operator(vdf, ast.newWildcardType(), Operator.ASSIGN);
				operator.setLeftOprand(variable);
				operator.setRightOperand(process(vdf.getInitializer()));
				_operators.add(operator);
			}
			if(hasInitializer){
				if(!_exprs.isEmpty()){
					_exprs.peek().addExpr(operator);
				}
			} else {
				if(!_exprs.isEmpty()){
					_exprs.peek().addExpr(variable);
				}
			}
		}
		return null;
	}
	
	private Expr visit(WhileStatement node) {
		Structure structure = new Structure(node, Structure.WHILE);
		if(!_exprs.isEmpty()){
			_exprs.peek().addExpr(structure);
		}
		_exprs.push(structure);
		_structures.add(structure);
		Expr expr = process(node.getExpression());
		if(expr != null){
			structure.addExpr(expr);
		}
		process(node.getBody());
		_exprs.pop();
		return null;
	}
	/*********************** Visit Expression *********************************/
	private Expr visit(Annotation node){
		return null;
	}
	
	private Expr visit(ArrayAccess node) {
		ASTNode parent = node.getParent();
		Pair<String, String> classAndMethodName = NodeUtils.getTypeDecAndMethodDec(node);
		String nodeStr = node.toString();
		int index = nodeStr.indexOf("[");
		if(index >= 0){
			nodeStr = nodeStr.substring(0, index);
		}
		Type type = ProjectInfo.getVariableType(classAndMethodName.first(), classAndMethodName.second(), nodeStr);
		
		if(type != null){
			if(type instanceof ArrayType){
				ArrayType arrayType = (ArrayType) type;
				type = arrayType.getElementType();
			} else {
				System.out.println("ArrayAccess type error : not array type ! " + node.toString());
			}
		}
		
		Operator operator = new Operator(node, type, Operator.ARRAC);
		operator.setLeftOprand(process(node.getArray()));
		operator.setRightOperand(process(node.getIndex()));
		_operators.add(operator);
		return operator;
	}
	
	private Expr visit(ArrayCreation node) {
		ArrayType type = node.getType();
		NewArray newArray = new NewArray(node, type, null, type.getElementType().toString());
		
		List<Expr> dimensions = new ArrayList<>();
		for(Object object : node.dimensions()){
			dimensions.add(process((ASTNode)object));
		}
		newArray.setDimension(dimensions);
		
		ArrayInitializer initializer = node.getInitializer();
		List<Expr> initializers = new ArrayList<>();
		if(initializer != null){
			for(Object object : initializer.expressions()){
				initializers.add(process((ASTNode)object));
			}
			newArray.setInitializers(initializers);
		}
		
		Integer count = _methodCalls.get(newArray);
		if(count == null){
			count = 0;
		}
		_methodCalls.put(newArray, count + 1);
		return newArray;
	}
	
	private Expr visit(ArrayInitializer node) {
		// TODO : should be taken into consideration ?
		return null;
	}
	
	private Expr visit(Assignment node) {
		AST ast = AST.newAST(AST.JLS8);
		Operator operator = new Operator(node, ast.newWildcardType(), Operator.ASSIGN);
		operator.setLeftOprand(process(node.getLeftHandSide()));
		operator.setRightOperand(process(node.getRightHandSide()));
		_operators.add(operator);
		return operator;
	}
	
	private Expr visit(BooleanLiteral node) {
		BoolLiteral literal = new BoolLiteral(node, node.booleanValue());
		Integer count = _constants.get(literal);
		if(count == null){
			count = 0;
		}
		_constants.put(literal, count + 1);
		return literal;
	}
	
	private Expr visit(CastExpression node) {
		return process(node.getExpression());
	}
	
	private Expr visit(CharacterLiteral node) {
		CharLiteral literal = new CharLiteral(node, node.charValue());
		Integer count = _constants.get(literal);
		if(count == null){
			count = 0;
		}
		_constants.put(literal, count + 1);
		return literal;
	}
	
	private Expr visit(ClassInstanceCreation node) {
		Pair<String, String> clazzAndMethodName = NodeUtils.getTypeDecAndMethodDec(node);
		String className = clazzAndMethodName.first();
		String methodName = clazzAndMethodName.second();
		MethodCall expr = null;
		if(className != null && methodName != null){
			List<Expr> params = new ArrayList<>();
			for(Object object : node.arguments()){
				params.add(process((ASTNode) object));
			}
			Expr ep = process(node.getExpression());
			expr = new MethodCall(node, node.getType(), ep, node.getType().toString(), params);
			Integer count = _methodCalls.get(expr);
			if(count == null){
				count = 0;
			}
			_methodCalls.put(expr, count + 1);
		}
		return expr;
	}
	
	private Expr visit(ConditionalExpression node) {
		_structures.add(new Structure(node, Structure.IF));
		_structures.add(new Structure(node, Structure.ELSE));
		process(node.getExpression());
		Expr expr = process(node.getThenExpression());
		process(node.getElseExpression());
		return expr;
	}
	
	private Expr visit(CreationReference node) {
		return null;
	}
	
	private Expr visit(ExpressionMethodReference node) {
		return null;
	}
	
	private Expr visit(FieldAccess node) {
		// TODO : now simply handle this.field
		Pair<String, String> classAndMethodName = NodeUtils.getTypeDecAndMethodDec(node);
		Type type = ProjectInfo.getVariableType(classAndMethodName.first(), classAndMethodName.second(), node.getName().toString());
		Operator operator = new Operator(node, type, Operator.FIELDAC);
		operator.setLeftOprand(process(node.getExpression()));
		operator.setRightOperand(process(node.getName()));
		_operators.add(operator);
		return operator;
	}
	
	private Expr visit(InfixExpression node) {
		Operator operator = new Operator(node, null, node.getOperator().toString());
		operator.setLeftOprand(process(node.getLeftOperand()));
		operator.setRightOperand(process(node.getRightOperand()));
		_operators.add(operator);
		return operator;
	}
	
	private Expr visit(InstanceofExpression node) {
		AST ast = AST.newAST(AST.JLS8);
		Operator operator = new Operator(node, ast.newPrimitiveType(PrimitiveType.BOOLEAN), Operator.INSTANSOF);
		operator.setLeftOprand(process(node.getLeftOperand()));
		operator.setRightOperand(process(node.getRightOperand()));
		_operators.add(operator);
		return operator;
	}
	
	private Expr visit(LambdaExpression node) {
		return null;
	}
	
	private Expr visit(MethodInvocation node) {
//		Pair<String, String> classAndMethodName = NodeUtils.getTypeDecAndMethodDec(node);
//		String className = classAndMethodName.first();
//		String methodName = classAndMethodName.second();
		Expression expression = node.getExpression();
		Expr expr = process(expression);
		String className = null;
		String methodName = node.getName().getFullyQualifiedName();
		if(expr != null){
			if(expr.getType() != null){
				className = expr.getType().toString();
			} else {
				LevelLogger.error("parse type error for method invocation !");
			}
		} else {
			Pair<String, String> classAndMethodName = NodeUtils.getTypeDecAndMethodDec(node);
			className = classAndMethodName.first();
		}
		Type type = ProjectInfo.getMethodRetType(className, methodName);
		List<Expr> params = new ArrayList<>();
		for(Object object : node.arguments()){
			params.add(process((ASTNode) object));
		}
		MethodCall methodCall = new MethodCall(node, type, expr, node.getName().getFullyQualifiedName(), params);
		Integer count = _methodCalls.get(methodCall);
		if(count == null){
			count = 0;
		}
		_methodCalls.put(methodCall, count + 1);
		return methodCall;
	}

	private Expr visit(MethodReference node) {
		return null;
	}

	private Expr visit(Name node) {
		Expr expr = null;
		if(node instanceof SimpleName){
			Pair<String, String> classAndMethodName = NodeUtils.getTypeDecAndMethodDec(node);
			Type type = ProjectInfo.getVariableType(classAndMethodName.first(), classAndMethodName.second(), node.toString());
			String name = node.getFullyQualifiedName();
			if(type == null){
				Pattern pattern = Pattern.compile("[A-Z][a-zA-Z_0-9]*");
				Matcher matcher = pattern.matcher(name);
				if(matcher.matches()){
					AST ast = AST.newAST(AST.JLS8);
					type = ast.newSimpleType(ast.newSimpleName(name));
				}
			}
			Variable variable = new Variable(node, type, name);
			Integer count = _variables.get(variable);
			if(count == null){
				count = 0;
			}
			_variables.put(variable, count + 1);
			expr = variable;
		} else if(node instanceof QualifiedName){
			
			QualifiedName qualifiedName = (QualifiedName) node;
			Name qName = qualifiedName.getQualifier();
			SimpleName simpleName = qualifiedName.getName();
			
			Pattern CONSTpattern = Pattern.compile("[A-Z_0-9]+");
			Matcher CONSTmatcher = CONSTpattern.matcher(simpleName.getFullyQualifiedName());
			Pattern CLASSpattern = Pattern.compile("[A-Z][a-z_A-Z0-9]*");
			Matcher CLASSmatcher = CLASSpattern.matcher(qName.getFullyQualifiedName());
			
			if(qName instanceof SimpleName && CONSTmatcher.matches() && CLASSmatcher.matches()){
				EnumLiteral enumLiteral = new EnumLiteral(node, node.getFullyQualifiedName());
				Integer count = _constants.get(enumLiteral);
				if(count == null){
					count = 0;
				}
				_constants.put(enumLiteral, count + 1);
				expr = enumLiteral;
			} else {
				Operator operator = new Operator(node, null, Operator.FIELDAC);
				operator.setLeftOprand(process(qName));
				operator.setRightOperand(process(simpleName));
				_operators.add(operator);
				expr = operator;
			}
		}
		
		return expr;
	}

	private Expr visit(NullLiteral node) {
		NilLiteral literal = new NilLiteral(node);
		Integer count = _constants.get(literal);
		if(count == null){
			count = 0;
		}
		_constants.put(literal, count + 1);
		return literal;
	}

	private Expr visit(NumberLiteral node) {
		String token = node.getToken();
		Literal expr = null;
		try{
			Integer value = Integer.parseInt(token);
			IntLiteral literal = new IntLiteral(node, value);
			expr = literal;
		} catch (Exception e){}
		
		if(expr == null){
			try{
				long value = Long.parseLong(token);
				LongLiteral literal = new LongLiteral(node, value);
				expr = literal;
			} catch (Exception e){}
		}
		
		if(expr == null){
			try{
				float value = Float.parseFloat(token);
				FloatLiteral literal = new FloatLiteral(node, value);
				expr = literal;
			} catch (Exception e){}
		}
		
		if(expr == null){
			try{
				double value = Double.parseDouble(token);
				DoubleLiteral literal = new DoubleLiteral(node, value);
				expr = literal;
			} catch (Exception e){}
		}
		
		if(expr == null){
			// should be hexadecimal number or octal number 
			token = token.replace("X", "x");
			token = token.replace("F", "f");
			StrLiteral literal = new StrLiteral(node, token);
			expr = literal;
		}
		
		if(expr != null){
			Integer count = _constants.get(expr);
			if(count == null){
				count = 0;
			}
			_constants.put(expr, count + 1);
		}
		
		return expr;
	}

	private Expr visit(ParenthesizedExpression node) {
		ParenthesizedExpression pExpression = (ParenthesizedExpression) node;
		return process(pExpression.getExpression());
	}

	private Expr visit(PostfixExpression node) {
		Operator operator = new Operator(node, null, node.getOperator().toString());
		operator.setLeftOprand(process(node.getOperand()));
		_operators.add(operator);
		return operator;
	}

	private Expr visit(PrefixExpression node) {
		Operator operator = new Operator(node, null, node.getOperator().toString());
		operator.setRightOperand(process(node.getOperand()));
		_operators.add(operator);
		return operator;
	}

	private Expr visit(StringLiteral node) {
		StrLiteral literal = new StrLiteral(node, node.getLiteralValue());
		Integer count = _constants.get(literal);
		if(count == null){
			count = 0;
		}
		_constants.put(literal, count + 1);
		return literal;
	}

	private Expr visit(SuperFieldAccess node) {
		// TODO : No need?
		return null;
	}

	private Expr visit(SuperMethodInvocation node) {
		// TODO : No need?
		return null;
	}

	private Expr visit(SuperMethodReference node) {
		return null;
	}
	
	private Expr visit(ThisExpression node){
		Pair<String, String> classAndMethodName = NodeUtils.getTypeDecAndMethodDec(node);
		Type type = ProjectInfo.getVariableType(classAndMethodName.first(), classAndMethodName.second(), "THIS");
		Variable variable = new Variable(node, type, "THIS");
		Integer count = _variables.get(variable);
		if(count == null){
			count = 0;
		}
		_variables.put(variable, count + 1);
		return variable;
	}

	private Expr visit(TypeLiteral node) {
		TLiteral literal = new TLiteral(node, node.getType());
		Integer count = _constants.get(literal);
		if(count == null){
			count = 0;
		}
		_constants.put(literal, count + 1);
		return literal;
	}

	private Expr visit(TypeMethodReference node) {
		return null;
	}

	private Expr visit(VariableDeclarationExpression node) {
		return null;
	}
	
	private Expr process(ASTNode node){
		if(node == null){
			return null;
		}
		 if(node instanceof AssertStatement){
			 return visit((AssertStatement)node);
		 } else if(node instanceof Block){
			 return visit((Block)node);
		 } else if(node instanceof BreakStatement){
			 return visit((BreakStatement)node);
		 } else if(node instanceof ConstructorInvocation){
			 return visit((ConstructorInvocation)node);
		 } else if(node instanceof ContinueStatement){
			 return visit((ContinueStatement)node);
		 } else if(node instanceof DoStatement){
			 return visit((DoStatement)node);
		 } else if(node instanceof EmptyStatement){
			 return visit((EmptyStatement)node);
		 } else if(node instanceof EnhancedForStatement){
			 return visit((EnhancedForStatement)node);
		 } else if(node instanceof ExpressionStatement){
			 return visit((ExpressionStatement)node);
		 } else if(node instanceof ForStatement){
			 return visit((ForStatement)node);
		 } else if(node instanceof IfStatement){
			 return visit((IfStatement)node);
		 } else if(node instanceof LabeledStatement){
			 return visit((LabeledStatement)node);
		 } else if(node instanceof ReturnStatement){
			 return visit((ReturnStatement)node);
		 } else if(node instanceof SuperConstructorInvocation){
			 return visit((SuperConstructorInvocation)node);
		 } else if(node instanceof SwitchCase){
			 return visit((SwitchCase)node);
		 } else if(node instanceof SwitchStatement){
			 return visit((SwitchStatement)node);
		 } else if(node instanceof SynchronizedStatement){
			 return visit((SynchronizedStatement)node);
		 } else if(node instanceof ThrowStatement){
			 return visit((ThrowStatement)node);
		 } else if(node instanceof TryStatement){
			 return visit((TryStatement)node);
		 } else if(node instanceof TypeDeclarationStatement){
			 return visit((TypeDeclarationStatement)node);
		 } else if(node instanceof VariableDeclarationStatement){
			 return visit((VariableDeclarationStatement)node);
		 } else if(node instanceof WhileStatement){
			 return visit((WhileStatement)node);
		 } else if(node instanceof Annotation){
			 return visit((Annotation)node);
		 } else if(node instanceof ArrayAccess){
			 return visit((ArrayAccess)node);
		 } else if(node instanceof ArrayCreation){
			 return visit((ArrayCreation)node);
		 } else if(node instanceof ArrayInitializer){
			 return visit((ArrayInitializer)node);
		 } else if(node instanceof Assignment){
			 return visit((Assignment)node);
		 } else if(node instanceof BooleanLiteral){
			 return visit((BooleanLiteral)node);
		 } else if(node instanceof CastExpression){
			 return visit((CastExpression)node);
		 } else if(node instanceof CharacterLiteral){
			 return visit((CharacterLiteral)node);
		 } else if(node instanceof ClassInstanceCreation){
			 return visit((ClassInstanceCreation)node);
		 } else if(node instanceof ConditionalExpression){
			 return visit((ConditionalExpression)node);
		 } else if(node instanceof CreationReference){
			 return visit((CreationReference)node);
		 } else if(node instanceof ExpressionMethodReference){
			 return visit((ExpressionMethodReference)node);
		 } else if(node instanceof FieldAccess){
			 return visit((FieldAccess)node);
		 } else if(node instanceof InfixExpression){
			 return visit((InfixExpression)node);
		 } else if(node instanceof InstanceofExpression){
			 return visit((InstanceofExpression)node);
		 } else if(node instanceof LambdaExpression){
			 return visit((LambdaExpression)node);
		 } else if(node instanceof MethodInvocation){
			 return visit((MethodInvocation)node);
		 } else if(node instanceof MethodReference){
			 return visit((MethodReference)node);
		 } else if(node instanceof Name){
			 return visit((Name)node);
		 } else if(node instanceof NullLiteral){
			 return visit((NullLiteral)node);
		 } else if(node instanceof NumberLiteral){
			 return visit((NumberLiteral)node);
		 } else if(node instanceof ParenthesizedExpression){
			 return visit((ParenthesizedExpression)node);
		 } else if(node instanceof PostfixExpression){
			 return visit((PostfixExpression)node);
		 } else if(node instanceof PrefixExpression){
			 return visit((PrefixExpression)node);
		 } else if(node instanceof StringLiteral){
			 return visit((StringLiteral)node);
		 } else if(node instanceof SuperFieldAccess){
			 return visit((SuperFieldAccess)node);
		 } else if(node instanceof SuperMethodInvocation){
			 return visit((SuperMethodInvocation)node);
		 } else if(node instanceof SuperMethodReference){
			 return visit((SuperMethodReference)node);
		 } else if(node instanceof ThisExpression){
			 return visit((ThisExpression)node);
		 } else if(node instanceof TypeLiteral){
			 return visit((TypeLiteral)node);
		 } else if(node instanceof TypeMethodReference){
			 return visit((TypeMethodReference)node);
		 } else if(node instanceof VariableDeclarationExpression){
			 return visit((VariableDeclarationExpression)node);
		 } else if(node instanceof Type){
			return new Variable(node, (Type)node, node.toString()); 
		 } else {
			 return null;
		 }
	}
	
}
