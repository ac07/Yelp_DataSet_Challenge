package Task2;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Projections;
import org.bson.Document;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
/*
 * Get top influencers for a given category and a location
 * 
 * 
 */
public class Influencers {
	static MongoClient mongoClient = new MongoClient();
	public static void main(String []args) throws ParseException, IOException
	{
		
		getInfluencers("Active Life","Las Vegas","");
		mongoClient.close();
	}
	/*
	 * Get top 20 influencer for a given category and location
	 * 
	 */
	public static void getInfluencers(String category, String location, String business_id) throws ParseException, IOException
	{
		String outPutFile = "src/main/resources/outputTask2.csv";
		File file = new File(outPutFile);
		FileWriter fw;
		fw = new FileWriter(file);
		
		final MongoDatabase db = mongoClient.getDatabase("Yelp");
		ArrayList<Business> businesses = new ArrayList();
		ArrayList<String> businessIds = new ArrayList<String>();
		Map<String, Business> map = new HashMap<>();
		FindIterable<Document> it;
		//Get all businesses with given city and business category
		if(business_id == null || business_id.isEmpty())
		{
			it = db.getCollection("business").find(new Document("city",location).append("categories",new Document("$elemMatch",new Document("$eq",category)))).projection(Projections.include("business_id","name","city","categories"));
		}
		else
		{
		    it = db.getCollection("business").find(new Document("city",location).append("business_id",business_id).append("categories",new Document("$elemMatch",new Document("$eq",category)))).projection(Projections.include("business_id","name","city","categories"));
		}
		if(!it.iterator().hasNext()) {
			System.out.println("No data!");
			return;
		}
		 for (Document document : it) {
			Business business = new Business();
			business.setBusinessId(document.getString("business_id"));
			business.setName(document.getString("name"));
	    	businessIds.add(document.getString("business_id"));
	    	business.setCategory((ArrayList<String>) document.get("categories"));
	    	business.setCity(document.get("city").toString());
	    	//System.out.println(document.toJson());
	    	map.put(document.getString("business_id"), business);
	    	businesses.add(business);
	    	
		}
		
		FindIterable<Document> it1 = db.getCollection("review").find(new Document("business_id",new Document("$in",businessIds)));
		ArrayList<User> users = new ArrayList<User>();
		ArrayList<String> userIds = new ArrayList<String>();
		Map<String, User> map1 = new HashMap<>();
		if(!it1.iterator().hasNext()) {
			System.out.println("No data!");
			return;
		}
		/*
		 * Calculate weightage according to number of reviews, 
		 * review date, number of fans, number of friends, no of 
		 * total votes received on a comment
		 */
		for (Document document : it1) {
			User user;
			if(!map1.containsKey(document.get("user_id").toString()))
			{
				user = new User();
				user.setUserId(document.get("user_id").toString());
	    		map1.put(document.get("user_id").toString(), user);
		    	userIds.add(document.get("user_id").toString());

			}
			else
			{
				user = 	map1.get(document.get("user_id").toString());
			}
			
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
		FindIterable<Document> it2 = db.getCollection("user").find(new Document("user_id",new Document("$in",userIds)));
		if(!it.iterator().hasNext()) {
			System.out.println("No data!");
			return;
		}
		for (Document userDoc : it2)
		{
			ArrayList<String> friends = (ArrayList<String>) userDoc.get("friends");
			User user = map1.get(userDoc.get("user_id"));
			user.setName(userDoc.getString("name"));
			user.setNoOfFriends(friends.size());
			user.setNoOfFans((int)userDoc.get("fans"));
			//For evaluation. Check influencer is elite member or not.
			ArrayList<Object> str =  (ArrayList<Object>) userDoc.get("elite");
			if(str.size()>0)
			{
				user.isElite = 1;
			}
			else
			{
				user.isElite = 0;
			}
			
			users.add(user);
			
		}
		
		Collections.sort(users);
		int noOfUsers = users.size()>15?15:users.size();
		for (int i = 0; i < noOfUsers; i++) {
//			System.out.println("User: "+users.get(i).getUserId()+ " Fans and Friends: "+ 
//								(users.get(i).getNoOfFans() + users.get(i).getNoOfFriends())+
//								" Reiview: " + users.get(i).getReviewWeight() +
//								" Weightage: " + users.get(i).getweightage());
			fw.write(users.get(i).getUserId()+","+users.get(i).getweightage()+","+users.get(i).isElite);
			fw.write("\n");
			
			System.out.println("---------------------Recommendation By: "+users.get(i).getName()+"----------------------");
			for (Business b : users.get(i).getBusinesses()) {
				System.out.println(b.getName());	
			}
			System.out.println("User: "+users.get(i).getUserId());
		}
    	System.out.println("Done");
    	fw.close();
		
	}

}