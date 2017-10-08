/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */
package cofix.core.parser.node;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Type;

import cofix.core.metric.CondStruct;
import cofix.core.metric.Literal;
import cofix.core.metric.LoopStruct;
import cofix.core.metric.MethodCall;
import cofix.core.metric.NewFVector;
import cofix.core.metric.Operator;
import cofix.core.metric.OtherStruct;
import cofix.core.metric.Variable;
import cofix.core.metric.Variable.USE_TYPE;
import cofix.core.modify.Modification;
import cofix.core.parser.node.expr.Expr;

/**
 * @author Jiajun
 * @date Jun 23, 2017
 */
public abstract class Node {
	
	protected int _startLine = 0;
	protected int _endLine = 0;
	protected ASTNode _originalNode = null;
	protected Node _parent = null;
	protected NewFVector _fVector = null;
	protected TYPE _nodeType = TYPE.UNKNOWN;
	
	protected Node(int startLine, int endLine, ASTNode node){
		this(startLine, endLine, node, null);
	}
	
	protected Node(int startLine, int endLine, ASTNode node, Node parent){
		_startLine = startLine;
		_endLine = endLine;
		_originalNode = node;
		_parent = parent;
	}
	
	public int getStartLine(){
		return _startLine;
	}
	
	public int getEndLine(){
		return _endLine;
	}
	
	public ASTNode getOriginalAST(){
		return _originalNode;
	}
	
	public Node getParent(){
		return _parent;
	}
	
	public TYPE getNodeType(){
		return _nodeType;
	}
	
	public void setParent(Node parent){
		_parent = parent;
	}
	
	public NewFVector getFeatureVector(){
		if(_fVector == null){
			computeFeatureVector();
		}
		return _fVector;
	}
	
	public abstract boolean match(Node node, Map<String, String> varTrans, Map<String, Type> allUsableVariables,
			List<Modification> modifications);
	
	public abstract String simplify(Map<String, String> varTrans, Map<String, Type> allUsableVariables);

	public abstract StringBuffer toSrcString();
	
	public abstract USE_TYPE getUseType(Node child);
	
	public abstract List<Node> getChildren();
	
	public abstract List<Literal> getLiterals();
	
	public abstract List<Variable> getVariables();
	
	public abstract List<LoopStruct> getLoopStruct();
	
	public abstract List<CondStruct> getCondStruct();
	
	public abstract List<OtherStruct> getOtherStruct();
	
	public abstract List<MethodCall> getMethodCalls();
	
	public abstract List<Operator> getOperators();
	
	public abstract void computeFeatureVector();
	
	public abstract List<CodeBlock> reduce();
	
	public abstract boolean adapt(Modification modification);
	public abstract boolean restore(Modification modification);
	public abstract boolean backup(Modification modification);
	
	@Override
	public String toString() {
		return toSrcString().toString();
	}
	
	public static enum TYPE{
		
		ARRACC("ArrayAccess"),
		ARRCREAT("ArrayCreation"),
		ARRINIT("ArrayInitilaization"),
		ASSIGN("Assignment"),
		BLITERAL("BooleanLiteral"),
		CAST("CastExpression"),
		CLITERAL("CharacterLiteral"),
		CLASSCREATION("ClassInstanceCreation"),
		COMMENT("Annotation"),
		CONDEXPR("ConditionalExpression"),
		DLITERAL("DoubleLiteral"),
		FIELDACC("FieldAccess"),
		FLITERAL("FloatLiteral"),
		INFIXEXPR("InfixExpression"),
		INSTANCEOF("InstanceofExpression"),
		INTLITERAL("IntLiteral"),
		LABEL("Name"),
		LLITERAL("LongLiteral"),
		MINVOCATION("MethodInvocation"),
		NULL("NullLiteral"),
		NUMBER("NumberLiteral"),
		PARENTHESISZED("ParenthesizedExpression"),
		POSTEXPR("PostfixExpression"),
		PREEXPR("PrefixExpression"),
		QNAME("QualifiedName"),
		SNAME("SimpleName"),
		SLITERAL("StringLiteral"),
		SFIELDACC("SuperFieldAccess"),
		SMINVOCATION("SuperMethodInvocation"),
		SINGLEVARDECL("SingleVariableDeclation"),
		THIS("ThisExpression"),
		TLITERAL("TypeLiteral"),
		VARDECLEXPR("VariableDeclarationExpression"),
		VARDECLFRAG("VariableDeclarationFragment"),
		ANONYMOUSCDECL("AnonymousClassDeclaration"),
		ASSERT("AssertStatement"),
		BLOCK("Block"),
		BREACK("BreakStatement"),
		CONSTRUCTORINV("ConstructorInvocation"),
		CONTINUE("ContinueStatement"),
		DO("DoStatement"),
		EFOR("EnhancedForStatement"),
		FOR("ForStatement"),
		IF("IfStatement"),
		RETURN("ReturnStatement"),
		SCONSTRUCTORINV("SuperConstructorInvocation"),
		SWCASE("SwitchCase"),
		SWSTMT("SwitchStatement"),
		SYNC("SynchronizedStatement"),
		THROW("ThrowStatement"),
		TRY("TryStatement"),
		TYPEDECL("TypeDeclarationStatement"),
		VARDECLSTMT("VariableDeclarationStatement"),
		WHILE("WhileStatement"),
		UNKNOWN("Unknown");
		
		private String _name = null;
		private TYPE(String name){
			_name = name;
		}
		
		@Override
		public String toString() {
			return _name;
		}
	}
	
}
