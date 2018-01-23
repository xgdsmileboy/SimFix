package cofix.core.modify.pattern;

import cofix.core.parser.node.Node;
import cofix.core.parser.node.Node.TYPE;

public class InsPattern extends Pattern {

	protected InsPattern(TYPE srcType, TYPE tarType) {
		super(Node.TYPE.UNKNOWN, tarType);
	}

}
