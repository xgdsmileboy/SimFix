/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */

package cofix.core.search;

import cofix.core.parser.node.CodeBlock;
import cofix.core.search.tool.Filter;

/**
 * @author: Jiajun
 * @date: 2019-06-25
 */
public abstract class CodeSearcher {

    protected CodeBlock _buggy;
    protected Filter _filter;

    protected CodeSearcher(CodeBlock buggy, Filter filter) {
        _buggy = buggy;
        _filter = filter;
    }

    public abstract SearchResult search(String filePath);
}
