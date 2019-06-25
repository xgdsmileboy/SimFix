/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */

package cofix.core.search.struct;

import cofix.core.parser.node.CodeBlock;
import cofix.core.search.CodeSearcher;
import cofix.core.search.SearchResult;
import cofix.core.search.tool.Filter;

/**
 * @author: Jiajun
 * @date: 2019-06-25
 */
public class StructSearcher extends CodeSearcher {


    public StructSearcher(CodeBlock buggy, Filter filter) {
        super(buggy, filter);
    }

    @Override
    public SearchResult search(String filePath) {
        return null;
    }

}
