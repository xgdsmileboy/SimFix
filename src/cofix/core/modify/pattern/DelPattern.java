package cofix.core.modify.pattern;

import cofix.core.parser.node.Node;
import cofix.core.parser.node.Node.TYPE;

public class DelPattern extends Pattern {

	protected DelPattern(TYPE srcType) {
		super(srcType, Node.TYPE.UNKNOWN);
	}

}
