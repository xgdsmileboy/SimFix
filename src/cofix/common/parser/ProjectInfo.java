package cofix.common.parser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.core.dom.Type;

public class ProjectInfo {

	private final static String __name__ = "@ProjectInfo ";

	private static Map<String, ClassInfo> _classMap = new HashMap<>();

	public static void addFieldType(String className, String fieldName, Type type) {
		if (_classMap.containsKey(className)) {
			_classMap.get(className).addFieldType(fieldName, type);
		} else {
			ClassInfo classInfo = new ClassInfo();
			classInfo.addFieldType(fieldName, type);
			_classMap.put(className, classInfo);
		}
	}

	public static void addMethodVariableType(String className, String methodName, String varName, Type type) {
		if (_classMap.containsKey(className)) {
			_classMap.get(className).addMethodVariableType(methodName, varName, type);
		} else {
			ClassInfo classInfo = new ClassInfo();
			classInfo.addMethodVariableType(methodName, varName, type);
			_classMap.put(className, classInfo);
		}
	}

	public static Type getVariableType(String className, String methodName, String varName) {
		ClassInfo classInfo = _classMap.get(className);
		if (classInfo == null) {
			System.out.println(__name__ + "#getVariableType Parse variable type failed !" + className + "::"
					+ methodName + "::" + varName);
			return null;
		}
		return classInfo.getVariableType(methodName, varName);
	}

}

class ClassInfo {
	private Map<String, Type> fieldTypeMap = new HashMap<>();
	private Map<String, Map<String, Type>> localTypeMap = new HashMap<>();

	public void resetAll() {
		fieldTypeMap = new HashMap<>();
		localTypeMap = new HashMap<>();
	}

	public boolean addFieldType(String fieldName, Type type) {
		if (fieldTypeMap.containsKey(fieldName) && !fieldTypeMap.get(fieldName).equals(type)
				&& !fieldTypeMap.get(fieldName).toString().equals(type.toString())) {
			System.out.println("Field type inconsistancy '" + fieldName + "' with types : "
					+ fieldTypeMap.get(fieldName) + " and " + type);
			return false;
		}
		fieldTypeMap.put(fieldName, type);
		return true;
	}

	public boolean addMethodVariableType(String methodName, String varName, Type type) {
		if (!localTypeMap.containsKey(methodName)) {
			Map<String, Type> map = new HashMap<>();
			map.put(varName, type);
			localTypeMap.put(methodName, map);
			return true;
		} else {
			Map<String, Type> map = localTypeMap.get(methodName);
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
		if (localTypeMap.containsKey(methodName) && localTypeMap.get(methodName).get(varName) != null) {
			return localTypeMap.get(methodName).get(varName);
		} else {
			return fieldTypeMap.get(varName);
		}
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