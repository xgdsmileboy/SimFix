package cofix.common.astnode;

public class Operator {
	// compare
	public final static String EQ = "==";
	public final static String GT = ">";
	public final static String GE = ">=";
	public final static String LT = "<";
	public final static String LE = "<=";
	// logical
	public final static String LAND = "&&";
	public final static String LOR = "||";
	//compare & logical
	// TODO : " cond : a ? b " should be converted to "if(cond) a else b"
	// bit operation
	public final static String BAND = "&";
	public final static String BOR = "|";
	public final static String BNOT = "~";
	public final static String BOX = "^";
	//arithmetic
	public final static String PLUS = "+";
	public final static String MINUS = "-";
	public final static String TIMES = "*";
	public final static String DIV = "/";
	public final static String MOD = "%";
	//class identification
	public final static String INSTANSOF = "instanceof";
	
	private String _value = null;
	private Identifier _leftOprand = null;
	private Identifier _rightOperand = null;
	public Operator(String value){
		_value = value;
	}
	
	public void setLeftOprand(Identifier leftOprand){
		_leftOprand = leftOprand;
	}
	
	public void setRightOperand(Identifier rightOperand) {
		_rightOperand = rightOperand;
	}
	
	public Identifier getLeftOperand() {
		return _leftOprand;
	}
	
	public Identifier getRightOperand() {
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
		return _value;
	}
	
}
