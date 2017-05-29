package cofix.common.parser;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class TypeParseVisitor extends ASTVisitor{
	public boolean visit(TypeDeclaration node) {
		String clazz = node.getName().getFullyQualifiedName();
		AST ast = AST.newAST(AST.JLS8);
		Type type = ast.newSimpleType(ast.newSimpleName(clazz));
		ProjectInfo.addFieldType(clazz, "THIS", type);
		Type suType = node.getSuperclassType();
		ProjectInfo.addFieldType(clazz, "SUPER", suType);
		FieldDeclaration fields[] = node.getFields();
		for (FieldDeclaration f : fields) {
			for (Object o : f.fragments()) {
				VariableDeclarationFragment vdf = (VariableDeclarationFragment) o;
				ProjectInfo.addFieldType(clazz, vdf.getName().toString(), f.getType());
			}
		}
		return true;
	}

	public boolean visit(MethodDeclaration node) {

		ASTNode parent = node.getParent();
		while(parent != null){
			if(parent instanceof TypeDeclaration){
				break;
			}
			parent = parent.getParent();
		}
		if(parent == null){
			return true;
		}
		
		String className = ((TypeDeclaration)parent).getName().getFullyQualifiedName();
		ProjectInfo.addMethodRetType(className, node.getName().getFullyQualifiedName(), node.getReturnType2());
		
		String methodName = node.getName().toString();
		String params = "";
		for(Object obj : node.parameters()){
			SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) obj;
			params += ","+singleVariableDeclaration.getType().toString();
		}
		methodName += params;
		Map<String, Type> map = new HashMap<>();
		for (Object o : node.parameters()) {
			SingleVariableDeclaration svd = (SingleVariableDeclaration) o;
			ProjectInfo.addMethodVariableType(className, methodName, svd.getName().toString(), svd.getType());
		}

		MethodVisitor mv = new MethodVisitor();
		node.accept(mv);

		for (Entry<String, Type> entry : mv.getVarMap().entrySet()) {
			ProjectInfo.addMethodVariableType(className, methodName, entry.getKey(), entry.getValue());
		}

		// System.out.println("MethodDeclaration = " + node);
		return true;
	}

	class MethodVisitor extends ASTVisitor {

		Map<String, Type> map = new HashMap<>();

		public Map<String, Type> getVarMap() {
			return map;
		}

		public boolean visit(ConditionalExpression node) {

//			System.out.println("ConditionalExpression -->" + node);
			return true;
		}

		public boolean visit(InfixExpression node) {
//			System.out.println("InfixExpression -->" + node);
			return true;
		}

		public boolean visit(InstanceofExpression node) {
//			System.out.println("InstanceofExpression -->" + node);
			return true;
		}

		public boolean visit(MethodInvocation node) {
//			System.out.println("MethodInvocation -->" + node);
			return true;
		}

		public boolean visit(Name node) {
//			System.out.println("Name -->" + node);
			return true;
		}

		public boolean visit(ParenthesizedExpression node) {
//			System.out.println("ParenthesizedExpression -->" + node);
			return true;
		}

		public boolean visit(PostfixExpression node) {
//			System.out.println("PostfixExpression -->" + node);
			return true;
		}

		public boolean visit(PrefixExpression node) {
//			System.out.println("PrefixExpression -->" + node);
			return true;
		}

		public boolean visit(TypeLiteral node) {
//			System.out.println("TypeLiteral -->" + node);
			return true;
		}

		public boolean visit(VariableDeclarationStatement node) {
//			Class<?> clazz = Utils.convert2Class(node.getType());
			for (Object o : node.fragments()) {
				VariableDeclarationFragment vdf = (VariableDeclarationFragment) o;
//				System.out.println(vdf.getName());
				map.put(vdf.getName().toString(), node.getType());
			}
//			System.out.println("VariableDeclarationStatement -->" + node);
			return true;
		}

		public boolean visit(VariableDeclarationExpression node) {
//			Class<?> clazz = Utils.convert2Class(node.getType());
			for (Object o : node.fragments()) {
				VariableDeclarationFragment vdf = (VariableDeclarationFragment) o;
//				System.out.println(vdf.getName());
				map.put(vdf.getName().toString(), node.getType());
			}
//			System.out.println("VariableDeclarationExpression -->" + node);
			return true;
		}
		
		public boolean visit(SingleVariableDeclaration node){
//			Class<?> clazz = Utils.convert2Class(node.getType());
			map.put(node.getName().toString(), node.getType());
//			System.out.println("SingleVariableDeclaration -->" + node);
			return true;
		}
	}
}
