package cofix.core.modify.diff.line;

import cofix.common.config.Constant;
import cofix.core.modify.diff.Delete;

public class DelLine extends Line implements Delete {

	public DelLine(String text) {
		super(text);
		_leading = Constant.PATCH_DEL_LEADING;
	}
	
}
