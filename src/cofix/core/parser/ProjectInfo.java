package cofix.core.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Type;

import cofix.common.util.JavaFile;
import cofix.common.util.Subject;

public class ProjectInfo {

	private final static String __name__ = "@ProjectInfo ";

	private static Map<String, ClassInfo> _classMap = new HashMap<>();
	
	public static void init(Subject subject){
		_classMap = new HashMap<>();
		String srcPath = subject.getHome() + subject.getSsrc();
		List<String> files = JavaFile.ergodic(srcPath, new ArrayList<String>());
		TypeParseVisitor typeParseVisitor = new TypeParseVisitor();
		for(String file : files){
			CompilationUnit unit = (CompilationUnit) JavaFile.genASTFromSource(JavaFile.readFileToString(file), ASTParser.K_COMPILATION_UNIT);
			unit.accept(typeParseVisitor);
		}
	}
	
	public static boolean isParentType(String childType, String parentType){
		if(_classMap.containsKey(childType)){
			return _classMap.get(childType).isParentType(parentType);
		}
		return false;
	}
	
	public static void addMethodRetType(String className, String methodName, Type retType){
		if(_classMap.containsKey(className)){
			_classMap.get(className).addMethodType(methodName, retType);
		} else {
			ClassInfo classInfo = new ClassInfo(className);
			classInfo.addMethodType(methodName, retType);
			_classMap.put(className, classInfo);
		}
	}
	
	public static void addSuperClass(String className, String superClass){
		if(_classMap.containsKey(className)){
			_classMap.get(className).addSuperClass(superClass);
		} else {
			ClassInfo classInfo = new ClassInfo(className);
			classInfo.addSuperClass(superClass);
			_classMap.put(className, classInfo);
		}
	}
	
	public static void addSuperInterface(String className, String superInterface){
		if(_classMap.containsKey(className)){
			_classMap.get(className).addSuperInterface(superInterface);
		} else {
			ClassInfo classInfo = new ClassInfo(className);
			classInfo.addSuperInterface(superInterface);
			_classMap.put(className, classInfo);
		}
	}
	
	public static void addFieldType(String className, String fieldName, Type type) {
		if (_classMap.containsKey(className)) {
			_classMap.get(className).addFieldType(fieldName, type);
		} else {
			ClassInfo classInfo = new ClassInfo(className);
			classInfo.addFieldType(fieldName, type);
			_classMap.put(className, classInfo);
		}
	}

	public static void addMethodVariableType(String className, String methodName, String varName, Type type) {
		if (_classMap.containsKey(className)) {
			_classMap.get(className).addMethodVariableType(methodName, varName, type);
		} else {
			ClassInfo classInfo = new ClassInfo(className);
			classInfo.addMethodVariableType(methodName, varName, type);
			_classMap.put(className, classInfo);
		}
	}

	public static Type getVariableType(String className, String methodName, String varName) {
		if(className == null){
			return null;
		}
		ClassInfo classInfo = _classMap.get(className);
		if (classInfo == null) {
//			System.out.println(__name__ + "#getVariableType Parse variable type failed : " + className + "::"
//					+ methodName + "::" + varName);
			return null;
		}
		return classInfo.getVariableType(methodName, varName);
	}
	
	public static Type getMethodRetType(String className, String methodName){
		if(className == null){
			return null;
		}
		ClassInfo classInfo = _classMap.get(className);
		if(classInfo == null){
//			System.out.println(__name__ + "#getMethodRetType Parse method return type failed : " + className + "::"
//					+ methodName);
			return null;
		}
		return classInfo.getMethodRetType(methodName);
	}

}

class ClassInfo {
	private Map<String, Type> _fieldTypeMap = new HashMap<>();
	private Map<String, Map<String, Type>> _localTypeMap = new HashMap<>();
	private Map<String, Type> _methodRetTypeMap = new HashMap<>();
	private String _superClass = null;
	private Set<String> _superInterface = new HashSet<>();
	private String _className = null;
	
	public ClassInfo(String className){
		_className = className;
	}
	
	public boolean isParentType(String parent){
		boolean is = false;
		if(_superClass != null){
			if(!_superClass.equals(_className)){
				if(_superClass.equals(parent)){
					is = true;
				} else {
					is = ProjectInfo.isParentType(_superClass, parent);
				}
			}
		}
		if(!is && _superInterface.size() > 0){
			for(String inter : _superInterface){
				if(inter.equals(parent)){
					is = true;
					break;
				} else {
					is = ProjectInfo.isParentType(inter, parent);
				}
			}
		}
		return is;
	}
	
	public boolean addSuperClass(String superClass){
		_superClass = superClass;
		return true;
	}
	
	public boolean addSuperInterface(String superInterface){
		_superInterface.add(superInterface);
		return true;
	}
	
	public boolean addMethodType(String methodName, Type retType){
		_methodRetTypeMap.put(methodName, retType);
		return true;
	}

	public boolean addFieldType(String fieldName, Type type) {
		if (_fieldTypeMap.containsKey(fieldName) && !_fieldTypeMap.get(fieldName).equals(type)
				&& !_fieldTypeMap.get(fieldName).toString().equals(type.toString())) {
			System.out.println("Field type inconsistancy '" + fieldName + "' with types : "
					+ _fieldTypeMap.get(fieldName) + " and " + type);
			return false;
		}
		_fieldTypeMap.put(fieldName, type);
		return true;
	}

	public boolean addMethodVariableType(String methodName, String varName, Type type) {
		if (!_localTypeMap.containsKey(methodName)) {
			Map<String, Type> map = new HashMap<>();
			map.put(varName, type);
			_localTypeMap.put(methodName, map);
			return true;
		} else {
			Map<String, Type> map = _localTypeMap.get(methodName);
			if (map.containsKey(varName) && !map.get(varName).equals(type)
					&& !map.get(varName).toString().equals(type.toString())) {
				System.out.println("Variable type inconsistancy of '" + varName + "' in method '" + methodName
						+ "' with types : " + map.get(varName) + " and " + type);
				return false;
			}
			map.put(varName, type);
			return true;
		}
	}

	public Type getVariableType(String methodName, String varName) {
		if(varName == null){
			return null;
		}
		if (methodName != null && _localTypeMap.containsKey(methodName) && _localTypeMap.get(methodName).get(varName) != null) {
			return _localTypeMap.get(methodName).get(varName);
		} else {
			if(_fieldTypeMap.get(varName) != null){
				return _fieldTypeMap.get(varName);
			} else if(_superClass != null){
				return ProjectInfo.getVariableType(_superClass, methodName, varName);
			} else {
				return null;
			}
		}
	}
	
	public Type getMethodRetType(String methodName){
		Type type = _methodRetTypeMap.get(methodName);
		if(type == null){
			if(_superClass != null){
				type = ProjectInfo.getMethodRetType(_superClass, methodName);
			}
			if(type == null){
				for(String superInterface : _superInterface){
					type = ProjectInfo.getMethodRetType(superInterface, methodName);
					if(type != null){
						return type;
					}
				}
			}
		}
		return type;
	}

	public static Class<?> convert2Class(Type type) {

		System.out.println("type : " + type);

		switch (type.toString()) {
		case "void":
			return void.class;
		case "int":
			return int.class;
		case "char":
			return char.class;
		case "short":
			return short.class;
		case "long":
			return long.class;
		case "float":
			return float.class;
		case "double":
			return double.class;
		case "byte":
			return byte.class;
		default:
		}

		if (type.toString().contains("[")) {
			return Arrays.class;
		}
		return null;
	}
}