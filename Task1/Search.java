package Task1;


import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class Search {
    public IndexReader reader;
    public String indexPath;
    public IndexSearcher searcher;
    public Set<String> businessIdSet;
    public Analyzer analyzer;
    public ArrayList<Document> docs;

    public Search(String indexPath) {
        try {
            this.indexPath = indexPath;
            this.reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
            this.searcher = new IndexSearcher(reader);
            this.businessIdSet = new HashSet<>();
            this.analyzer = new StandardAnalyzer();
            this.docs = new ArrayList<>();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void readIndex() {
        int totalNumberOfDocs = reader.maxDoc();
        System.out.println(totalNumberOfDocs);
    }

    public void makeFiles(List<String> businessIdList) {

        for (String id : businessIdList) {

            StringBuilder fullText = new StringBuilder();
            try {
                Query query = getTermQuery("business_id", id);

                int count = getCount(query);
                if (count > 0) {
                    TopDocs docs = searcher.search(query, count);

                    ScoreDoc[] hits = docs.scoreDocs;

                    makeText(fullText, hits);

                    writeToFile(fullText.toString(), id, "reviewFiles");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void makeText(StringBuilder fullText, ScoreDoc[] hits) throws IOException {
        for (ScoreDoc hit : hits) {
            Document doc = searcher.doc(hit.doc);
            fullText.append(doc.get("REVIEW"));
        }
    }

    public Query getTermQuery(String field, String value) {
        Term term = new Term(field, value);
        return new TermQuery(term);
    }

    public Query getTextQuery(String field, String value) {
        QueryParser parser = new QueryParser(field, new StandardAnalyzer());
        Query query = null;
        try {
            query = parser.parse(QueryParser.escape(value));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return query;
    }

    public int getCount(Query query) throws IOException {
        TotalHitCountCollector totalHitCountCollector = new TotalHitCountCollector();
        searcher.search(query, totalHitCountCollector);

        return totalHitCountCollector.getTotalHits();
    }


    private void writeToFile(String fullText, String id, String directoryName) {
        String workingDirectory = System.getProperty("user.dir");
        File file = new File(workingDirectory + "/" + directoryName + "/" + id + ".txt");
        try {

            BufferedWriter br = new BufferedWriter(new FileWriter(file, true));

            br.write(fullText);
            System.out.println(directoryName);
            br.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void makeSetBusinessIds(String business_id) {
        businessIdSet.add(business_id);
    }


    public List<Document> findHits(int count, Query query) {
//        ArrayList<Document> docs = new ArrayList<>();
        this.docs.clear();
        try {
            TopDocs results = searcher.search(query, count);

            ScoreDoc[] hits = results.scoreDocs;

            for (ScoreDoc hit : hits) {
                Document doc = searcher.doc(hit.doc);
                this.docs.add(doc);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return docs;
    }

    public List<String> makeUniqueIdList() {
        List<LeafReaderContext> leafReaderContexts = reader.getContext().leaves();
        System.out.println(leafReaderContexts.size());

        List<String> businessIdList = new ArrayList<>();
        for (LeafReaderContext leafReaderContext : leafReaderContexts) {

            int startDoc = leafReaderContext.docBase;
            int numberDocs = leafReaderContext.reader().maxDoc();
            for (int i = startDoc; i < numberDocs; i++) {
                try {
                    String business_id = searcher.doc(i).get("business_id");
                    businessIdList.add(business_id);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        return businessIdList;
    }

    public void makeCategoryBusinessIdMap(List<String> categoryList) {
        HashMap<String, List<String>> map = new HashMap<>();
        try {
            for (String category : categoryList) {
                List<String> ids = new ArrayList<>();
                Query query = getTermQuery("category", category);

                int count = getCount(query);

                if (count > 0) {

                    TopDocs docs = searcher.search(query, count);

                    ScoreDoc[] hits = docs.scoreDocs;

                    for (ScoreDoc hit : hits) {
                        Document doc = searcher.doc(hit.doc);
                        ids.add(doc.get("business_id"));
                    }
                }
                map.put(category, ids);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        make60PercentText(map);
    }

    private void make60PercentText(HashMap<String, List<String>> map) {
        int i = 1;
        for (String category : map.keySet()) {
            List<String> ids = map.get(category);
            System.out.println(i);


            for (String id : ids) {
                try {
                    IndexReader reviewReader = DirectoryReader.open(FSDirectory.open(Paths.get("reviewIndex")));
                    searcher = new IndexSearcher(reviewReader);

                    Query query = getTermQuery("business_id", id);

                    int count = getCount(query);

                    if (count > 0) {
                        int sixtyPercent = (int) (count * 0.6);

                        TopDocs results = searcher.search(query, count);

                        ScoreDoc[] hits = results.scoreDocs;

                        StringBuilder trainText = new StringBuilder();
                        StringBuilder testText = new StringBuilder();

                        int reviewCount = 0;

                        for (ScoreDoc hit : hits) {
                            Document doc = searcher.doc(hit.doc);

                            if (reviewCount <= sixtyPercent) {
                                trainText.append(doc.get("REVIEW"));
                                reviewCount++;
                            } else {
                                testText.append(doc.get("REVIEW"));
                                testText.append("~~~");  //delimiter
                            }

                        }
                        if (trainText.toString().length() > 0) {
                            writeToFile(trainText.toString(), category.replace("/", ""), "trainCategory");
                        }
                        if (testText.toString().length() > 0) {
                            writeToFile(testText.toString(), category.replace("/", ""), "testCategory");
                        }

                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            ++i;
        }
    }

    public List<String> makeUniqueCategoryList() {
        List<String> categoryList = new ArrayList<>();

        try {
            Fields fields = MultiFields.getFields(reader);
            Terms terms = fields.terms("category");
            TermsEnum termsEnum = terms.iterator();
            while (termsEnum.next() != null) {
                categoryList.add(termsEnum.term().utf8ToString());
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return categoryList;
    }

    public List<String> getAllFieldsNames() {
        ArrayList<String> list = new ArrayList<>();

        try {
            Fields fields = MultiFields.getFields(reader);
            System.out.println(fields.size());

            for (String field : fields) {
                list.add(field);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return list;
    }

}
