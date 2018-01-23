package cofix.core.modify.diff.line;

import cofix.common.config.Constant;

public class KeepLine extends Line {

	public KeepLine(String text) {
		super(text);
		_leading = Constant.PATCH_KEEP_LEADING;
	}
}
