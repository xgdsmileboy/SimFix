package cofix.core.modify.diff.line;

public abstract class Line {

	protected String _text;
	protected String _leading = "";
	
	public Line(String text) {
		_text = text;
	}
	
	public StringBuffer toSrcString() {
		return new StringBuffer(_text);
	}
	
	@Override
	public String toString() {
		return _leading + _text;
	}
}
