package Task1;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by jay on 11/28/15.
 */
public class POSIndex {


    public StandardAnalyzer analyzer;
    public String pathToDirectory;
    public String indexPath;
    public IndexWriter indexWriter;
    public Directory dir;
    public IndexWriterConfig iwc;
    public String pathToReviewDirectory;

    public POSIndex(String pathToReviewDirectory, String indexPath) {
        try {
            this.pathToReviewDirectory = pathToReviewDirectory;
            this.indexPath = indexPath;
            this.dir = FSDirectory.open(Paths.get(indexPath));
            this.analyzer = new StandardAnalyzer();
            this.iwc = new IndexWriterConfig(analyzer);
            this.iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
            this.indexWriter = new IndexWriter(dir, iwc);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public void makeIndex() {
        File file = new File(this.pathToReviewDirectory);
        List<String> tags = new ArrayList<>(Arrays.asList("NN", "NNS", "NNPS", "NNP", "JJ", "POS", "FW"));

        if (file.isDirectory()) {
            for (File categoryFile : file.listFiles()) {

                String categoryName = categoryFile.getName();
                if (categoryName.endsWith(".txt")) {
                    System.out.println(categoryName);

                    String reviewString = getReviewString(categoryName, pathToReviewDirectory);

                    String malletString = getMalletString(categoryName);
//                String posString = getNounsAdjectives(tags, categoryName);

                    addToLucene(categoryName, reviewString, malletString);
                }
            }
            finish();


        }

    }

    private String getMalletString(String categoryName) {
        File malletFile;

        if (categoryName.contains("Restaurants")) {
            malletFile = new File("malletOutput" + File.separator + "Restaurants_keys.txt");
        } else {
            malletFile = new File("malletOutput" + File.separator + categoryName.replace(".txt", "").replace(" ", "_") + "_keys.txt");
        }
        String line;
        StringBuilder stringBuilder = new StringBuilder();

        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(malletFile));
            while ((line = bufferedReader.readLine()) != null) {
                String[] split = line.split("\\t");

                stringBuilder.append(split[2]).append(" ");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(stringBuilder.toString());
        return stringBuilder.toString();
    }

    private String getNounsAdjectives(List<String> tags, String categoryFile) {
//        POSTagger posTagger = new POSTagger();
        File file = new File(pathToReviewDirectory + File.separator + categoryFile);
        StringBuilder posStringBuilder = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            String posString;
            while ((line = br.readLine()) != null) {
//                posString = posTagger.tag(line, (ArrayList<String>) tags);
//                posStringBuilder.append(posString);
            }
            System.out.println(posStringBuilder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return posStringBuilder.toString();
    }


    private String getReviewString(String categoryFile, String pathToReviewDirectory) {
        File file = new File(pathToReviewDirectory + File.separator + categoryFile);
        StringBuilder stringBuilder = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                stringBuilder.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    private void addToLucene(String categoryName, String reviewText, String malletString) {

        try {
            Document document = new Document();
            document.add(new StringField("category", categoryName, Field.Store.YES));
            document.add(new TextField("review", reviewText, Field.Store.YES));
            document.add(new TextField("mallet", malletString, Field.Store.YES));

            indexWriter.addDocument(document);
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
