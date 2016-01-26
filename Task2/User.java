package Task2;

import java.util.ArrayList;
/*
 * yelp users
 * 
 */
public class User implements Comparable {
	private String name;
	private String user_id;
	private int friends;
	private ArrayList<String>categories;
	private ArrayList<Business> business;
	private int funnyVotes;
	private int usefulVotes;
	private int coolVotes;
	private int totalVotes;
	private double stars;
	private int review;
	private int noOfFans;
	private double weightage;
	public int isElite;
	
	public User(){
		business = new ArrayList<Business>();
		categories = new ArrayList<String>();
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	public double getStars()
	{
		return stars;
	}
	
	public void setStars(double star)
	{
		this.stars = star;
	}
	public int getfunnyVotes()
	{
		return this.funnyVotes;
	}
	
	public void setfunnyVotes(int funnyVotes)
	{
		this.funnyVotes += funnyVotes;
		this.weightage += funnyVotes;
		this.totalVotes += funnyVotes;
	}
	public int getusefulVotes()
	{
		return usefulVotes;
	}
	
	public void setusefulVotes(int usefulVotes)
	{
		this.usefulVotes += usefulVotes;
		this.weightage += usefulVotes;
		this.totalVotes += usefulVotes;
	}
	
	public int getcoolfulVotes()
	{
		return coolVotes;
	}
	
	public void setcoolVotes(int coolVotes)
	{
		this.coolVotes += coolVotes;
		this.weightage += coolVotes;
		this.totalVotes += coolVotes;
	}
	public int getTotalVotes()
	{
		return totalVotes;
	}
	
	public void setNoOfFans(int fans)
	{
		this.noOfFans = fans;
		this.weightage += fans;
		
	}
	public int getNoOfFans()
	{
		return this.noOfFans;
		
	}
	
	
	public void setReviewWeight(int reviewWeight)
	{
		this.review += reviewWeight;
		this.weightage += reviewWeight;
	}
	public int getReviewWeight()
	{
		return this.review;
	}
	
	public String getUserId()
	{
		return this.user_id;
	}
	
	public void setUserId(String userid)
	{
		this.user_id = userid;
	}
	
	public int getNoOfFriends()
	{
		return friends;
	}
	
	public void setBusiness(Business business)
	{
		this.business.add(business);
	}
	public double getweightage()
	{
		return weightage;
	}
//	public void setWeightage(double weightage)
//	{
//		this.weightage *= weightage;
//	}
	
	
	public ArrayList<Business> getBusinesses()
	{
		return business;
	}
	
	public void setNoOfFriends(int noOfFriend)
	{
		this.friends=noOfFriend;
		this.weightage += noOfFriend;
	}

	@Override
	public int compareTo(Object o) {
		int compareweightage=(int) ((User)o).getweightage();
        /* For Ascending order*/
//        return this.weightage-compareweightage;

        /* For Descending order do like this */
        return (int) (compareweightage-this.weightage);
		
	}
	
	
}