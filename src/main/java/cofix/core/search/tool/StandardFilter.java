/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */

package cofix.core.search.tool;

import cofix.core.parser.node.CodeBlock;
import cofix.core.search.SearchResult;

/**
 * @author: Jiajun
 * @date: 2019-06-25
 */
public class StandardFilter implements Filter {

    @Override
    public SearchResult filter(SearchResult result) {
        return result;
    }

    @Override
    public boolean filter(CodeBlock block) {
        return false;
    }
}
