package cofix.core.parser;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import cofix.common.util.Pair;

public class TypeParseVisitor extends ASTVisitor {
	public boolean visit(TypeDeclaration node) {
		Pair<String, String> clazzAndMethodName = NodeUtils.getTypeDecAndMethodDec(node.getName());
		String clazz = clazzAndMethodName.getFirst();
		AST ast = AST.newAST(AST.JLS8);
		Type type = ast.newSimpleType(ast.newSimpleName(clazz));
		ProjectInfo.addFieldType(clazz, "THIS", type);
		Type suType = node.getSuperclassType();
		if(suType != null){
			ProjectInfo.addFieldType(clazz, "SUPER", suType);
			ProjectInfo.addSuperClass(clazz, suType.toString());
		}
		
		List<Object> sInterfaces = node.superInterfaceTypes();
		if(sInterfaces != null){
			for(Object object : sInterfaces){
				if(object instanceof Type){
					Type interfaceType = (Type) object;
					ProjectInfo.addSuperInterface(clazz, interfaceType.toString());
				}
			}
		}
		
		FieldDeclaration fields[] = node.getFields();
		for (FieldDeclaration f : fields) {
			for (Object o : f.fragments()) {
				VariableDeclarationFragment vdf = (VariableDeclarationFragment) o;
				Type tmpType = f.getType();
				if(vdf.getExtraDimensions() > 0){
					tmpType = ast.newArrayType((Type) ASTNode.copySubtree(ast, tmpType), vdf.getExtraDimensions());
				}
				ProjectInfo.addFieldType(clazz, vdf.getName().toString(), tmpType);
			}
		}
		return true;
	}

	public boolean visit(MethodDeclaration node) {

//		ASTNode parent = node.getParent();
//		while(parent != null){
//			if(parent instanceof TypeDeclaration){
//				break;
//			}
//			parent = parent.getParent();
//		}
//		if(parent == null){
//			return true;
//		}
//		
//		String className = ((TypeDeclaration)parent).getName().getFullyQualifiedName();
//		ProjectInfo.addMethodRetType(className, node.getName().getFullyQualifiedName(), node.getReturnType2());
//		
//		String methodName = node.getName().toString();
//		String params = "";
//		for(Object obj : node.parameters()){
//			SingleVariableDeclaration singleVariableDeclaration = (SingleVariableDeclaration) obj;
//			params += ","+singleVariableDeclaration.getType().toString();
//		}
//		methodName += params;
		
		if(node.getBody() == null || node.getParent() instanceof AnonymousClassDeclaration){
			return true;
		}
		
		Pair<String, String> classAndMethodName = NodeUtils.getTypeDecAndMethodDec(node.getBody());
		String className = classAndMethodName.getFirst();
		String methodName = classAndMethodName.getSecond();
		
		ProjectInfo.addMethodRetType(className, node.getName().getFullyQualifiedName(), node.getReturnType2());
		
		for (Object o : node.parameters()) {
			SingleVariableDeclaration svd = (SingleVariableDeclaration) o;
			ProjectInfo.addMethodVariableType(className, methodName, svd.getName().toString(), getType(svd));
		}

		MethodVisitor mv = new MethodVisitor();
		node.accept(mv);

		for (Entry<String, Type> entry : mv.getVarMap().entrySet()) {
			ProjectInfo.addMethodVariableType(className, methodName, entry.getKey(), entry.getValue());
		}

		return true;
	}
	
	private Type getType(SingleVariableDeclaration svd){
		Type type = svd.getType();
		if(type != null){
			String content = svd.toString().trim().replace(" ", "");
			if(content.indexOf(type.toString() + "...") >= 0){
				AST ast = AST.newAST(AST.JLS8);
				int dimention = 0;
				if(type.isArrayType()){
					dimention = ((ArrayType)type).getDimensions();
					type = ((ArrayType)type).getElementType();
				}
				type = ast.newArrayType((Type) ASTNode.copySubtree(ast, type), dimention + 1);
			}
			if(svd.getExtraDimensions() > 0){
				AST ast = AST.newAST(AST.JLS8);
				type = ast.newArrayType((Type) ASTNode.copySubtree(ast, type), svd.getExtraDimensions());
			}
		}
		return type;
	}

	class MethodVisitor extends ASTVisitor {

		Map<String, Type> map = new HashMap<>();

		public Map<String, Type> getVarMap() {
			return map;
		}
		public boolean visit(VariableDeclarationStatement node) {
			if(isAnonymousClass(node)){
				return true;
			}
			for (Object o : node.fragments()) {
				VariableDeclarationFragment vdf = (VariableDeclarationFragment) o;
				Type type = node.getType();
				if(vdf.getExtraDimensions() > 0){
					AST ast = AST.newAST(AST.JLS8);
					type = ast.newArrayType((Type) ASTNode.copySubtree(ast, type), vdf.getExtraDimensions());
				}
				map.put(vdf.getName().toString(), type);
			}
			return true;
		}

		public boolean visit(VariableDeclarationExpression node) {
			if(isAnonymousClass(node)){
				return true;
			}
			for (Object o : node.fragments()) {
				VariableDeclarationFragment vdf = (VariableDeclarationFragment) o;
				Type type = node.getType();
				if(vdf.getExtraDimensions() > 0){
					AST ast = AST.newAST(AST.JLS8);
					type = ast.newArrayType((Type) ASTNode.copySubtree(ast, type), vdf.getExtraDimensions());
				}
				map.put(vdf.getName().toString(), type);
			}
			return true;
		}
		
		public boolean visit(SingleVariableDeclaration node){
			if(isAnonymousClass(node)){
				return true;
			}
			map.put(node.getName().toString(), getType(node));
			return true;
		}
		
		private boolean isAnonymousClass(ASTNode node){
			ASTNode parent = node.getParent();
			while(parent != null && !(parent instanceof MethodDeclaration)){
				parent = parent.getParent();
			}
			if(parent == null || parent.getParent() instanceof AnonymousClassDeclaration){
				return true;
			}
			return false;
		}
		
	}
}
