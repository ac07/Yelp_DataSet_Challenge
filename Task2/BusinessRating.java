package Task2;

import java.util.List;
import java.util.Date;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.json.simple.JSONArray;


import com.mongodb.BasicDBObject;
import com.mongodb.Block;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Function;
import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import com.mongodb.client.FindIterable;
import com.mongodb.client.AggregateIterable;
import com.mongodb.client.model.Projections;

import static java.util.Arrays.asList;
/*
 *	Evaluate influnce of an influencer on a business rating 
 *
 */
public class BusinessRating {
	static MongoClient mongoClient = new MongoClient();
	public static void main(String []args) throws ParseException, IOException
	{
		//This file is used by WEKKA for Logistic Regression
		String outPutFile = "src/main/resources/logistic_regression.csv";
		File file = new File(outPutFile);
		FileWriter fw;
		fw = new FileWriter(file);
		//These are features used by Wekka for logistic regression
		// Business ID, Review Count, # of check-ins, Tip count, Business Weight,
		// Avg Influencer Weight,Avg Stars, True Stars 
		fw.write("Business ID, Review Count, # of check-ins, Tip count, Business Weight, Avg Influencer Weight,Avg Stars, True Stars \n");
		
		getBusinessRating(fw);

		System.out.println("Done....");
		
		fw.close();
		mongoClient.close();
	}
	
	public static void getBusinessRating(FileWriter fw) throws ParseException, IOException
	{
		List<String> str = new ArrayList<>();
		//Connect to the DB Yelp
		final MongoDatabase db = mongoClient.getDatabase("Yelp");
		ArrayList<Business> businesses = new ArrayList();
		ArrayList<String> businessIds = new ArrayList<String>();
		Map<String, Business> map = new HashMap<>();
		FindIterable<Document> it;
		FindIterable<Document> it1;
		AggregateIterable<Document> it2;
		//File containing all food categories
		String fileName = "src/main/resources/foodCategory";
		//Create Index on frequent columns
		//Just create once.
		db.getCollection("business").createIndex(new Document("business_id",1).append("categories",1));
		db.getCollection("review").createIndex(new Document("business_id",1));
		db.getCollection("checkin").createIndex(new Document("business_id",1));
		db.getCollection("user").createIndex(new Document("friends",1));
		db.getCollection("tip").createIndex(new Document("business_id",1));
		
			
		
		Set<String> foodcategories = new HashSet<String>();
		// FileReader reads text files in the default encoding.
        BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));

        String line;
        //Read all food categories from a given file
		while((line = bufferedReader.readLine()) != null)
		{
			foodcategories.add(line.trim());
		}   
		//Get all businesses from food categories
		it = db.getCollection("business").find(new Document("categories",new Document("$in",foodcategories)));
	
		List<String> businessList = new ArrayList<>();

		for (Document document : it) {
			String s = document.get("stars").toString();
			Business business = new Business();
			business.setBusinessId(document.getString("business_id"));
			business.setName(document.getString("name"));
	    	businessIds.add(document.getString("business_id"));
	    	business.setCategory((ArrayList<String>) document.get("categories"));
	    	business.setCity(document.get("city").toString());
	    	business.setNoOfReviews((int)document.get("review_count"));
	    	business.setStars(document.get("stars").toString());
	    	map.put(document.getString("business_id"), business);
	    	businesses.add(business);
	    	businessList.add(document.getString("business_id"));
		    	
			}
		
		for (String businessId : businessList) 
		{
			//Get reviews for each business
			it1 = db.getCollection("review").find(new Document("business_id",businessId));
			if(!it1.iterator().hasNext())
			{
				continue;
			}
		
			ArrayList<User> users = new ArrayList<User>();
			ArrayList<String> userIds = new ArrayList<String>();
			Map<String, User> map1 = new HashMap<>();
			//Get user for each review
			for (Document document : it1) 
			{
				User user;
				if(!map1.containsKey(document.get("user_id").toString()))
				{
					user = new User();
					user.setUserId(document.get("user_id").toString());
					user.setStars(Double.parseDouble(document.get("stars").toString()));
		    		map1.put(document.get("user_id").toString(), user);
			    	userIds.add(document.get("user_id").toString());
				}
				else
				{
					user = 	map1.get(document.get("user_id").toString());
				}
				//Calculate user weightage
				Object obj = document.get("votes");
		    	Document doc = (Document) obj;
		    	String reviewyr = document.getString("date");
		    	DateFormat format = new SimpleDateFormat("yyyy-MM-dd");	
		    	Date date = (Date) format.parse(reviewyr);
		    	Calendar calender = Calendar.getInstance();
		    	calender.setTime(date);
		    	int reviewYear = calender.get(Calendar.YEAR);
		    	Date d = new Date();
		    	calender.setTime(d);
		    	int currentYear = calender.get(Calendar.YEAR);
		    	int reviewSince = currentYear-reviewYear;
		    	int weightage = 0;
		    	if(reviewSince == 0)
		    		weightage = 1000;
		    	else if(reviewSince == 1)
		    		weightage = 10;
		    	else if(reviewSince == 2)
		    		weightage = 5;
		    	else
		    		weightage = 1;
		    	user.setfunnyVotes((int) doc.get("funny"));
		    	user.setusefulVotes((int) doc.get("useful"));
		    	user.setcoolVotes((int) doc.get("cool"));
		    	user.setReviewWeight(weightage);
		    	user.setBusiness(map.get(document.get("business_id")));
	 	}
	 	//For all users get their information
		it = db.getCollection("user").find(new Document("user_id",new Document("$in",userIds)));
		if(!it.iterator().hasNext()) 
		{
			System.out.println("No data!");
			return;
		}
		for (Document userDoc : it)
		{
			User user = map1.get(userDoc.get("user_id"));
			if(!users.contains(user))
			{
				
				ArrayList<String> friends = (ArrayList<String>) userDoc.get("friends");
				user.setName(userDoc.get("name").toString());
				user.setNoOfFriends(friends.size());
				user.setNoOfFans((int)userDoc.get("fans"));
				users.add(user);
			}
		}
		//Sort users according to their weightage and take top 20
		Collections.sort(users);
		int noOfUsers = users.size()>20?20:users.size();
		double avgWeight = 0.0;
		double avgRating = 0.0;
		for (int i = 0; i < noOfUsers; i++)
		{
			//Calculate avg weight and their rating for a business
			avgWeight += users.get(i).getweightage();
			avgRating += users.get(i).getStars();
		}
		int reviewCount = map.get(businessId).getNoOfReviews();
		
		//Calculate business weightage 
		//Features are : number of tips, how busy it is (checkin-info),
		//number of revies by influncers and by others
		Document doc = db.getCollection("tip").aggregate(asList(
		        new Document("$match", new Document("business_id",businessId)),
		        new Document("$group", new Document("_id", "business_id").append("count", new Document("$sum", 1))))).first();
		Document doc1 = db.getCollection("checkin").find(new Document("business_id",businessId)).first();
		int checkinCount=0;
		if(doc1!=null)
		{
			Object obj = doc1.get("checkin_info").toString();
			String str1 = (String) obj;
			String[] strs = str1.split(",");
			for (String string : strs) 
			{
				String[] st = string.split("=");
				for (int i = 1; i < st.length; i=i+2)
				{
					st[i]=st[i].replaceAll("}}"," ");
					checkinCount = checkinCount+ Integer.parseInt(st[i].trim());
				}
				
			}
		}
		int tipCount = 0;
		if(	doc != null)
			tipCount = (int)doc.get("count");
		double s = (avgWeight/noOfUsers);
    	//Create csv file. This file is used by Wekka for Logistic regression
    	fw.write(businessId+","+reviewCount+","+checkinCount+","+tipCount+","+
    			(Math.abs(reviewCount-noOfUsers)*25 )+","+Double.toString(s)+","+ Double.toString(avgRating/noOfUsers)+","+map.get(businessId).getStars());
    	fw.write("\n");
		}   	
	}

}