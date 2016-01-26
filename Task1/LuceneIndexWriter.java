package Task1;


import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

public class LuceneIndexWriter {

    Analyzer analyzer;
    String jsonFilePath;
    String indexPath;
    IndexWriter indexWriter;
    Directory dir;
    IndexWriterConfig iwc;

    public LuceneIndexWriter(String jsonFilePath, String indexPath) {
        try {
            this.indexPath = indexPath;
            this.jsonFilePath = jsonFilePath;
            this.dir = FSDirectory.open(Paths.get(indexPath));
            this.analyzer = new StandardAnalyzer();
            this.iwc = new IndexWriterConfig(analyzer);
            this.iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            this.indexWriter = new IndexWriter(dir, iwc);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void createIndex(ArrayList<JSONObject> jsonObjects, String fieldName) {
        for (JSONObject jsonObject : jsonObjects) {
            addJSONObject(jsonObject, fieldName);
        }
    }


    private void addJSONObject(JSONObject jsonObject, String fieldName) {

        Document document;
        try {
            if (fieldName.equals("business_id")) {
                JSONArray categoriesArray = (JSONArray) jsonObject.get("categories");

                for (Object obj : categoriesArray) {
                    document = new Document();
                    document.add(new StringField("category", obj.toString(), Field.Store.YES));
                    document.add(new StringField("business_id", jsonObject.get("business_id").toString(), Field.Store.YES));
                    indexWriter.addDocument(document);
                }

            } else {
                document = new Document();
                document.add(new TextField(fieldName, jsonObject.get("text").toString(), Field.Store.YES));
                document.add(new StringField("business_id", jsonObject.get("business_id").toString(), Field.Store.YES));
                indexWriter.addDocument(document);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    public void finish() {
        try {
            indexWriter.forceMerge(1);
            indexWriter.commit();
            indexWriter.close();
            System.out.println("Done");
        } catch (IOException e) {
            System.out.println("We had a problem closing the index: " + e.getMessage());
        }
    }
}
