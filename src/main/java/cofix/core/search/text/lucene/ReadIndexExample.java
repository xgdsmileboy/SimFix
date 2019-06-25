/**
 * Copyright (C) SEI, PKU, PRC. - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited Proprietary and Confidential.
 * Written by Jiajun Jiang<jiajun.jiang@pku.edu.cn>.
 */

package cofix.core.search.text.lucene;

import cofix.common.config.Constant;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * @author: Jiajun
 * @date: 2019-06-25
 */
public class ReadIndexExample {

    private static final String INDEX_DIR = Constant.HOME + "/lucene/index";

    public static void main(String[] args) throws Exception {
        IndexSearcher searcher = createSearcher();

        //Search by ID
        TopDocs foundDocs = searchById(1, searcher);

        System.out.println("Total Results :: " + foundDocs.totalHits);

        for (ScoreDoc sd : foundDocs.scoreDocs) {
            Document d = searcher.doc(sd.doc);
            System.out.println(String.format(d.get("firstName")));
        }

        //Search by firstName
        TopDocs foundDocs2 = searchByFirstName("Brian", searcher);

        System.out.println("Total Results :: " + foundDocs2.totalHits);

        for (ScoreDoc sd : foundDocs2.scoreDocs) {
            Document d = searcher.doc(sd.doc);
            System.out.println(String.format(d.get("id")));
        }
    }

    private static TopDocs searchByFirstName(String firstName, IndexSearcher searcher) throws Exception {
        QueryParser qp = new QueryParser("firstName", new StandardAnalyzer());
        Query firstNameQuery = qp.parse(firstName);
        TopDocs hits = searcher.search(firstNameQuery, 10);
        return hits;
    }

    private static TopDocs searchById(Integer id, IndexSearcher searcher) throws Exception {
        QueryParser qp = new QueryParser("id", new StandardAnalyzer());
        Query idQuery = qp.parse(id.toString());
        TopDocs hits = searcher.search(idQuery, 10);
        return hits;
    }

    private static IndexSearcher createSearcher() throws IOException {
        Directory dir = FSDirectory.open(Paths.get(INDEX_DIR));
        IndexReader reader = DirectoryReader.open(dir);
        IndexSearcher searcher = new IndexSearcher(reader);
        return searcher;
    }
}
