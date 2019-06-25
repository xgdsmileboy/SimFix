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
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Jiajun
 * @date: 2019-06-25
 */
public class WriteIndexExample {

    private static final String INDEX_DIR = Constant.HOME + "/lucene/index";

    public static void main(String[] args) throws Exception {
        IndexWriter writer = createWriter();
        List<Document> documents = new ArrayList<>();

        Document document1 = createDocument(1, "Lokesh", "Gupta", "howtodoinjava.com");
        documents.add(document1);

        Document document2 = createDocument(2, "Brian", "Schultz", "example.com");
        documents.add(document2);

        //Let's clean everything first
        writer.deleteAll();

        writer.addDocuments(documents);
        writer.commit();
        writer.close();
    }

    private static IndexWriter createWriter() throws IOException {
        FSDirectory dir = FSDirectory.open(Paths.get(INDEX_DIR));
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer());
        IndexWriter writer = new IndexWriter(dir, config);
        return writer;
    }

    private static Document createDocument(Integer id, String firstName, String lastName, String website) {
        Document document = new Document();
        document.add(new StringField("id", id.toString(), Field.Store.YES));
        document.add(new TextField("firstName", firstName, Field.Store.YES));
        document.add(new TextField("lastName", lastName, Field.Store.YES));
        document.add(new TextField("website", website, Field.Store.YES));
        return document;
    }
}
