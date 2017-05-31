package cofix.common.astnode;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.Type;

public class Operator extends Expr{
	// compare
	public final static String EQ = "==";
	public final static String NE = "!=";
	public final static String GT = ">";
	public final static String GE = ">=";
	public final static String LT = "<";
	public final static String LE = "<=";
	// logical
	public final static String LAND = "&&";
	public final static String LOR = "||";
	public final static String LNOT = "!";
	// compare & logical
	// TODO : " cond : a ? b " should be converted to "if(cond) a else b"
	// bit operation
	public final static String BAND = "&";
	public final static String BOR = "|";
	public final static String BNOT = "~";
	public final static String BOX = "^";
	// arithmetic
	public final static String PLUS = "+";
	public final static String MINUS = "-";
	public final static String TIMES = "*";
	public final static String DIV = "/";
	public final static String MOD = "%";
	// class identification
	public final static String INSTANSOF = "instanceof";
	
	// assignment
	public final static String ASSIGN = "=";
	
	// array access
	public final static String ARRAC = "[]";
	
	// field access
	public final static String FIELDAC = ".";
	
	private String _value = null;
	private Expr _leftOprand = null;
	private Expr _rightOperand = null;
	public Operator(String value){
		_value = value;
	}
	
	public void setLeftOprand(Expr leftOprand){
		_leftOprand = leftOprand;
	}
	
	public void setRightOperand(Expr rightOperand) {
		_rightOperand = rightOperand;
	}
	
	public Expr getLeftOperand() {
		return _leftOprand;
	}
	
	public Expr getRightOperand() {
		return _rightOperand;
	}
	
	@Override
	public int hashCode() {
		return _value.hashCode();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null){
			return false;
		}
		if(!(obj instanceof Operator)){
			return false;
		}
		Operator other = (Operator) obj;
		return _value.equals(other._value);
	}
	
	@Override
	public String toString() {
		StringBuffer stringBuffer = new StringBuffer();
		if(_leftOprand != null){
			stringBuffer.append(_leftOprand);
		}
		stringBuffer.append(_value);
		if(_rightOperand != null){
			stringBuffer.append(_rightOperand);
		}
		return stringBuffer.toString();
	}

	@Override
	public Type getType() {
		// TODO : concrete type ?
		AST ast = AST.newAST(AST.JLS8);
		return ast.newSimpleType(ast.newSimpleName("OperationType"));
	}
	
}
