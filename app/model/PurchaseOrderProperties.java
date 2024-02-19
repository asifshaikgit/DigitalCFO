package model;

public class PurchaseOrderProperties {
	String Name;
	String Value;
	int Type;
	
	public PurchaseOrderProperties(String name,String value,int type){
		this.Name = name;
		this.Value=value;
		this.Type=type;		
	}

	public String getName() {
		return Name;
	}

	public void setName(String name) {
		Name = name;
	}

	public String getValue() {
		return Value;
	}

	public void setValue(String value) {
		Value = value;
	}

	public int getType() {
		return Type;
	}

	public void setType(int type) {
		Type = type;
	}

		
	
}
