package cofix.common.parser.astnode;

import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.Type;

import cofix.common.parser.NodeUtils;
import cofix.common.parser.astnode.expr.Variable;
import cofix.core.adapt.Modification;
import cofix.core.adapt.Revision;

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
	private Type _type = null;
	private Expr _leftOprand = null;
	private Expr _rightOperand = null;
	public Operator(ASTNode node, Type type, String value){
		_value = value;
		_srcNode = node;
		_type = type;
	}
	
	public void setLeftOprand(Expr leftOprand){
		_leftOprand = leftOprand;
	}
	
	public void setRightOperand(Expr rightOperand) {
		_rightOperand = rightOperand;
	}
	
	public String getValue(){
		return _value;
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
		stringBuffer.append(" ");
		stringBuffer.append(_value);
		stringBuffer.append(" ");
		if(_rightOperand != null){
			stringBuffer.append(_rightOperand);
		}
		return stringBuffer.toString();
	}

	@Override
	public Type getType() {
		if(_type != null){
			return _type;
		} else {
			AST ast = AST.newAST(AST.JLS8);
			switch(_value){
			case EQ :
			case NE :
			case GT :
			case GE :
			case LT :
			case LE :
			// logical
			case LAND :
			case LOR :
			case LNOT :
			case INSTANSOF :
				_type = ast.newPrimitiveType(PrimitiveType.BOOLEAN);
				break;
			// compare & logical
			// TODO : " cond : a ? b " should be converted to "if(cond) a else b"
			// bit operation
			case BAND :
			case BOR :
			case BNOT :
			case BOX :
			case MOD :
				_type = ast.newPrimitiveType(PrimitiveType.INT);
				break;
			// arithmetic
			case PLUS :
			case MINUS :
			case TIMES :
			case DIV :
			// assignment
			case ASSIGN :
			// array access
			case ARRAC :
			// field access
			case FIELDAC :
			
			default :
				_type = ast.newWildcardType();
			}
		}
		return _type;
	}

	@Override
	public boolean matchType(Expr expr, Map<String, Type> allUsableVariables, List<Modification> modifications) {
		if(expr instanceof Operator){
			Operator other = (Operator) expr;
			boolean left = true;
			boolean right = true;
			if(_leftOprand == null){
				left = other.getLeftOperand() == null;
			} else {
				List<Modification> tmpModify = new ArrayList<>();
				left = _leftOprand.matchType(other.getLeftOperand(), allUsableVariables, tmpModify);
				if(left){
					modifications.addAll(tmpModify);
				}
			}
			if(_rightOperand == null){
				right = other.getRightOperand() == null;
			} else {
				List<Modification> tmpModify = new ArrayList<>();
				right = _rightOperand.matchType(other.getRightOperand(), allUsableVariables, modifications);
				if(right){
					modifications.addAll(tmpModify);
				}
			}
			
			if(_value.equals(other._value)){
				return left || right;
			} else {
				if(left && right){
					Revision revision = new Revision(this);
					AST ast = AST.newAST(AST.JLS8);
					ASTNode astNode = ASTNode.copySubtree(ast, _srcNode);
					if(astNode instanceof InfixExpression){
						((InfixExpression) astNode).setOperator(InfixExpression.Operator.toOperator(other.getValue()));
					}
					revision.setTar(expr, astNode);
					revision.setModificationComplexity(1);
					modifications.add(revision);
					return true;
				}
			}
		}
		
		return false;
	}

	@Override
	public Expr adapt(Expr tar, Modification modify, Map<String, Type> allUsableVarMap) {
		if(modify instanceof Revision){
			Revision revision = (Revision) modify;
			_srcNode = NodeUtils.replace(_srcNode, revision.getReplaceAST());
		}
		if(tar instanceof Operator){
			Operator operator = (Operator) tar;
			this._value = operator.getValue();
		}
		return this;
	}

	@Override
	public List<Variable> getVariables() {
		List<Variable> variables = new ArrayList<>();
		if(_leftOprand != null){
			variables.addAll(_leftOprand.getVariables());
		}
		if(_rightOperand != null){
			variables.addAll(_rightOperand.getVariables());
		}
		return variables;
	}

	@Override
	public void backup() {
		AST ast = AST.newAST(AST.JLS8);
		ASTNode node = ASTNode.copySubtree(ast, _srcNode);
		_backup = new Operator(node, _type, _value);
		((Operator)_backup).setLeftOprand(_leftOprand);
		((Operator)_backup).setRightOperand(_rightOperand);
	}

	@Override
	public void restore() {
		Operator operator = (Operator)_backup;
		this._srcNode = operator.getOriginalASTnode();
		this._type = operator.getType();
		this._value = operator.getValue();
		this._leftOprand = operator.getLeftOperand();
		this._rightOperand = operator.getRightOperand();
	}
	
}
