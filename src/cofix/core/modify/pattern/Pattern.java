package cofix.core.modify.pattern;

import cofix.core.parser.node.Node;

public abstract class Pattern {

	protected Node.TYPE _srcType;
	protected Node.TYPE _tarType;
	protected int _freq = 0;
	
	protected Pattern(Node.TYPE srcType, Node.TYPE tarType) {
		_srcType = srcType;
		_tarType = tarType;
	}
	
	public void setFrequency(int freq) {
		_freq = freq;
	}
	
	public void incFrequency() {
		_freq ++;
	}
	
	public int getFrequency() {
		return _freq;
	}
	
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
	
	@Override
	public String toString() {
		return _srcType.toString() + "(" + _tarType.toString() + ")";
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null) {
			return false;
		}
		if(obj instanceof Pattern) {
			return false;
		}
		Pattern pattern = (Pattern) obj;
		return _srcType == pattern._srcType && _tarType == pattern._tarType;
	}
	
}
