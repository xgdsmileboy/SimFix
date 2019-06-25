/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */

package cofix.core.search;

import cofix.common.util.Pair;
import cofix.core.parser.node.CodeBlock;

import java.util.LinkedList;
import java.util.List;

/**
 * @author: Jiajun
 * @date: 2019-06-25
 */
public class SearchResult {

    private List<Pair<CodeBlock, Double>> _blocks;

    public SearchResult() {
        _blocks = new LinkedList<>();
    }

    public void add(CodeBlock block, double value) {
        _blocks.add(new Pair<CodeBlock, Double>(block, value));
    }

    public void setBlocks(List<Pair<CodeBlock, Double>> blocks) {
        _blocks = blocks;
    }

    public List<Pair<CodeBlock, Double>> getCodeBlocks() {
        return _blocks;
    }
}
