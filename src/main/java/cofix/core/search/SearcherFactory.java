/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */

package cofix.core.search;

import cofix.core.parser.node.CodeBlock;
import cofix.core.search.simple.SimpleFilter;
import cofix.core.search.learn.DeepSearcher;
import cofix.core.search.struct.StructSearcher;
import cofix.core.search.text.TextSearcher;
import cofix.core.search.filter.Filter;
import cofix.core.search.filter.StandardFilter;

/**
 * @author: Jiajun
 * @date: 2019-06-25
 */
public class SearcherFactory {

    public static CodeSearcher getSearcher(CodeBlock buggy, SearchType type) {
        return getSearcher(buggy, new StandardFilter(), type);
    }

    public static CodeSearcher getSearcher(CodeBlock buggy, Filter filter, SearchType type) {
        switch (type) {
            case SIMFIX:
                return new SimpleFilter(buggy, 0.3);
            case TEXT:
                return new TextSearcher(buggy, filter);
            case STRUCT:
                return new StructSearcher(buggy, filter);
            case LEARN:
                return new DeepSearcher(buggy, filter);
        }
        return null;
    }

}
