package model;

import java.util.List;

public class PurchaseOrderActionBody {
	String title;
	List properties;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public List getProperties() {
		return properties;
	}
	public void setProperties(List properties) {
		this.properties = properties;
	}
	
	
}
