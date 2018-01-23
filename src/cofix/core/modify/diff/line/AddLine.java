package cofix.core.modify.diff.line;

import cofix.common.config.Constant;
import cofix.core.modify.diff.Add;

public class AddLine extends Line implements Add {

	public AddLine(String text) {
		super(text);
		_leading = Constant.PATCH_ADD_LEADING;
	}
}
