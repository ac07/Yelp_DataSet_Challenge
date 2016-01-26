package Task2;

import java.util.ArrayList;
/*
 * Yelp Business
 * 
 */
public class Business{
	private String name;
	private String business_id;
	private String latitude;
	private String longitude;
	private String city;
	private ArrayList<String> categories;
	private ArrayList<Attributes> attributes;
	private int noOfReviews;
	private String stars;
	public Business(){
		attributes = new ArrayList<Attributes>();
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
	
	public String getStars()
	{
		return stars;
	}
	public void setStars(String stars)
	{
		this.stars = stars;
	}
	
	public int getNoOfReviews()
	{
		return noOfReviews;
	}
	public void setNoOfReviews(int reviews)
	{
		this.noOfReviews = reviews;
	}
	
	public String getBusinessId()
	{
		return business_id;
	}
	public void setBusinessId(String businessId)
	{
		this.business_id = businessId;
	}
	public String getLatitude()
	{
		return latitude;
	}
	public void setLatitude(String latitude)
	{
		this.latitude = latitude;
	}
	
	public String getLongitude()
	{
		return longitude;
	}
	public void setLongitude(String longitude)
	{
		this.longitude = longitude;
	}
	public String getCity()
	{
		return city;
	}
	public void setCity(String city)
	{
		this.city = city;
	}
	public ArrayList<String> getCategory()
	{
		return categories;
	}
	public void setCategory(ArrayList<String> categories)
	{
		this.categories.addAll(categories);
	}
	public ArrayList<Attributes> getAttributes()
	{
		return attributes;
	}
	public void setAttributes(String key, Object value)
	{
		Attributes at = new Attributes();
		at.key = key;
		at.value = value;
		this.attributes.add(at);
	}
	
	
	
}
class Attributes{
	String key;
	Object value;
}