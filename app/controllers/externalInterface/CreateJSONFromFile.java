package controllers.externalInterface;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;


	public class CreateJSONFromFile {
		/*public static void main(String[] args) {
			
			try {
				
				/*KaizalaAuthorization kaAu = new KaizalaAuthorization();
				String accessToken = kaAu.getAccessToken();
				String groupId = kaAu.getGroupId(accessToken);
				
				System.out.println("acessToken " + accessToken);
				System.out.println("groupId " + groupId);
				JSONSerializer serializer = new JSONSerializer();	 
				PurchaseOrderProperties shippingAddpop = new PurchaseOrderProperties("SA","abc",0);
				PurchaseOrderProperties shippingStatepop = new PurchaseOrderProperties("SS","shippingState",0);
				PurchaseOrderProperties actionTopop = new PurchaseOrderProperties("aTo","abcd.startingmember",0);
				PurchaseOrderProperties VIDpop = new PurchaseOrderProperties("VID","pqur.vendormem",0);
				PurchaseOrderProperties PODpop = new PurchaseOrderProperties("POD","1500357780666",3);
				PurchaseOrderProperties orderStatepop = new PurchaseOrderProperties("state","0",1);
				String str= serializer.exclude("*.class").serialize( shippingAddpop );
				System.out.println(str);
				ArrayList propertiesArray = new ArrayList();
				propertiesArray.add(shippingAddpop);
				propertiesArray.add(shippingStatepop);
				propertiesArray.add(actionTopop);
				propertiesArray.add(VIDpop);
				propertiesArray.add(PODpop);
				propertiesArray.add(orderStatepop);
				
				
				PurchaseOrderActionBody poa = new PurchaseOrderActionBody();
				poa.setTitle("IDOS Title");
				poa.setProperties(propertiesArray);
				String poastr= serializer.exclude("*.class").include("properties").serialize( poa );
				System.out.println(poastr);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}*/
		
		

		     public static void main(String []args){
		        Product p1 = new Product("buttons","red buttons",200,2,12345);
		        Product p2 = new Product("mts","Red silk cloth",20,500,22345);
		        List<Product> productList = new ArrayList<Product>(); 
		        productList.add(p1);
		        productList.add(p2);
		      
		        printProductList(productList);  
		        
		        String message = getJsonForProductList(productList, "purchaseOrderNumber","senderAddress","senderState","c2f28a57-bd05-45a7-90cf-f1d6d65f1f78","c2f28a57-bd05-45a7-90cf-f1d6d65f1f78","purchaseOrderDate",0,"com.microsoft.kaizala.miniapps.GST");
				
		        
				System.out.println(message);
		            
		     }
		     
		    
		     static void printProductList(List<Product> productList){
		         for (int i=0;i<productList.size();i++)
		         {
		               System.out.println("PRODUCT "+i+ " --->");
		               productList.get(i).print();
		         }
		     }
		     
		     static String getJsonForProductList(List<Product> productList, String purchaseOrderNumber, String senderAddress, String senderState, String assignedTo, String vendorId, String purchaseOrderDate, int state, String packageId){
		    	 String message="";
		    	 try{
		        
				
				JSONObject messageJSON = new JSONObject();
		         
		        JSONObject actionBody = new JSONObject();
		        actionBody.put("title", purchaseOrderNumber);
		        
		        JSONArray properties = new JSONArray();

				JSONObject PLProperty = new JSONObject();
				PLProperty.put("Name", "PL");
				PLProperty.put("Type", 6);
		        JSONArray productListArray = new JSONArray();
		        for (int i=0;i<productList.size();i++)
		        {
		            JSONObject item = new JSONObject();
		            item.put("PU",productList.get(i).unit);
		            item.put("PN",productList.get(i).name);
		            item.put("PQ",productList.get(i).quantity+"");
		            item.put("PPPU",productList.get(i).pricePerUnit+"");
		            item.put("PF",productList.get(i).HSNCode+"");
		            productListArray.put(item);
		        }
		        PLProperty.put("Value", productListArray.toString());
				properties.put(PLProperty);
				
				JSONObject SAProperty = new JSONObject();
				SAProperty.put("Name", "SA");
				SAProperty.put("Type", 0);
				SAProperty.put("Value", senderAddress);
				properties.put(SAProperty);
				
				
				JSONObject SSProperty = new JSONObject();
				SSProperty.put("Name", "SS");
				SSProperty.put("Type", 0);
				SSProperty.put("Value", senderState);
				properties.put(SSProperty);
				
				JSONObject ATProperty = new JSONObject();
				ATProperty.put("Name", "aTo");
				ATProperty.put("Type", 0);
				ATProperty.put("Value", assignedTo);
				properties.put(ATProperty);
				
				JSONObject VIDProperty = new JSONObject();
				VIDProperty.put("Name", "VID");
				VIDProperty.put("Type", 0);
				VIDProperty.put("Value", vendorId);
				properties.put(VIDProperty);
				
				JSONObject StateProperty = new JSONObject();
				StateProperty.put("Name", "state");
				StateProperty.put("Type", 1);
				StateProperty.put("Value", state+"");
				properties.put(StateProperty);
				
		        
				actionBody.put("properties", properties);
				
				messageJSON.put("id", packageId);
				messageJSON.put("actionBody", actionBody);
				
		        message = messageJSON.toString();
		         
		     
		     }catch(Exception e){
		    	 e.printStackTrace();
		     }
		     
		     return message;
	}
	}

		class Product{
		    String unit;
		    String name;
		    int quantity;
		    int pricePerUnit;
		    int HSNCode;
		    
		    Product(String unit,String name,int quantity,int pricePerUnit,int HSNCode){
		        this.unit = unit;
		        this.name = name;
		        this.quantity = quantity;
		        this.pricePerUnit = pricePerUnit;
		        this.HSNCode = HSNCode;
		    }
		    
		    
		    void print()
		    {
		        System.out.println("unit : "+ this.unit);
		        System.out.println("name : "+ this.name);
		        System.out.println("quantity : "+ this.quantity);
		        System.out.println("pricePerUnit : "+ this.pricePerUnit);
		        System.out.println("HSNCode : "+ this.HSNCode);
		    }
		    
		}
		
	

