package Task1;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Category {
    private String name;
    private ArrayList<Category> subcategory;

    public Category(String name) {
        this.name = name;
        subcategory = new ArrayList<Category>();
    }

    public String getName() {
        return this.name;
    }

    public ArrayList<Category> getSubcateories() {
        return this.subcategory;
    }

    public void addSubcategories(Category category) {
        this.subcategory.add(category);
    }

    public static Map<String, ArrayList<String>> getCategories() throws IOException {
        JSONParser parser = new JSONParser();
        Map<String, ArrayList<String>> map = new HashMap<>();
        try {
            Object obj = parser.parse(new FileReader("categories.json"));
            JSONArray jsonObjs = (JSONArray) obj;
            for (Object jsonObj : jsonObjs) {
                JSONObject jb = (JSONObject) jsonObj;
                JSONArray parents = (JSONArray) jb.get("parents");
                String subcategory = jb.get("title").toString();
                for (Object object : parents) {
                    if (map.containsKey(object.toString()))
                        map.get(object.toString()).add(subcategory);
                    else {
                        map.put(object.toString(), new ArrayList<String>());
                        map.get(object.toString()).add(subcategory);
                    }
                }

            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return map;
    }
}


