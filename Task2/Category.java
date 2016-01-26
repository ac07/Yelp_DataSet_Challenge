package Task2;

import java.util.ArrayList;


public class Category {
	private String name;
	private ArrayList<String> subcategory;
	
	public Category(String name)
	{
		this.name = name;
		subcategory = new ArrayList<String>();
	}
	
	public String getName()
	{
		return this.name;
	}
	
	public ArrayList<String> getSubcategories()
	{
		return this.subcategory;
	}
	public void addSubcategories(String category)
	{
		this.subcategory.add(category);
	}

public static void main()
{
	Category c = new Category("Root");
	c.addSubcategories("a");
	c.addSubcategories("b");
	for (String node : c.getSubcategories()) {
		System.out.println(node);
	}
}
}


