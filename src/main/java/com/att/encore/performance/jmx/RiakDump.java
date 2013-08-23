package com.att.encore.performance.jmx;

import java.util.*;

import com.basho.riak.client.IRiakClient;
import com.basho.riak.client.IRiakObject;
import com.basho.riak.client.http.RiakObject;
import com.basho.riak.client.builders.RiakObjectBuilder;
import com.basho.riak.client.RiakClient;
import com.basho.riak.client.RiakException;
import com.basho.riak.client.RiakFactory;
import com.basho.riak.client.RiakRetryFailedException;
import com.basho.riak.client.bucket.Bucket;
import com.basho.riak.client.cap.Quora;
import com.basho.riak.client.cap.UnresolvedConflictException;
import com.basho.riak.client.convert.ConversionException;
import com.basho.riak.client.operations.StoreObject;
import com.basho.riak.client.raw.RawClient;
import com.basho.riak.client.raw.config.Configuration;
import com.basho.riak.client.raw.pbc.PBClientAdapter;
import com.basho.riak.client.raw.pbc.PBClientConfig;

import net.sf.json.*;

import org.apache.camel.Exchange;
import org.joda.time.*;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

@SuppressWarnings("deprecation")
public class RiakDump {

	static String riakHost = "198.225.136.81";
	private Configuration conf;
	private IRiakClient riakClient;
	
	
	public static String getRiakHost() {
		return riakHost;
	}


	public static void setRiakHost(String riakHost) {
		RiakDump.riakHost = riakHost;
	}

	public void init() {
		
		//conf = new PBClientConfig.Builder().withHost("localhost").withPort(8098).build();
    	//conf = new PBClientConfig.Builder().withHost("localhost").withPort(8098).build();
    	try
    	{	
    	  
    		riakClient = RiakFactory.httpClient();
    	    System.out.println("HEY! RiakClient is created..........");
    	 
    	}
    	catch (RiakException ex)
    	{
    		System.out.println("Exception caught: Message :"+ex.getMessage());
    	}
        
       if (riakClient == null)
       {
    	   System.out.println("riakClient eq null. exting..");
           System.exit(0);
       }
	}
	
	public void cleanup()
	{
		if (riakClient != null)
			riakClient.shutdown();
	}
	/**
	 * @param Camel exchange
	 * saves all JSON data into RIAK. Use route-ID as bucket. Use access_time field as the Key. Also use Route-ID as secondary index.(Route-ID links to all 
	 * related keys.
	 */
	public void dump(Exchange exchange){
		
		IRiakObject riakObject = null;
		Bucket myBucket = null;
		
		System.out.println("Exchange value in dump mehtod = "+exchange.getIn().getBody().toString());
		
		try{
		
			JSONArray jsonArray = (JSONArray) JSONSerializer.toJSON( exchange.getIn().getBody().toString());  
			int size = jsonArray.size();
			for (int i=0; i < size; i++) {
				JSONObject jsonObject = (JSONObject) jsonArray.getJSONObject( i );  
				String id = jsonObject.getString("id");
				String access_time = jsonObject.getString("access_time");
				System.out.println("ID value from JSON = " + id+ ". access_time value = "+access_time);
				
				//use id as bucket and access_time as key.
			    riakObject = RiakObjectBuilder.newBuilder(id,access_time).
	            		withValue(jsonObject.toString()).withContentType("application/json").addIndex("2iKeyRef", id).build();
			 System.out.println("RiakObject = "+riakObject);
			 System.out.println("RiakObject bucket = "+riakObject.getBucket());
			 System.out.println("RiakObject contenttype = "+riakObject.getContentType());
			 System.out.println("RiakObject key = "+riakObject.getKey());
			 System.out.println("RiakObject value = "+riakObject.getValue());
			 
			 riakClient.fetchBucket(id).execute().store(riakObject).execute();
			
			 
			System.out.println("Finished saving to Riak...");
			
			}
				
		}catch (RiakRetryFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnresolvedConflictException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ConversionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
        /*
        Bucket myBucket = null;
		JSONArray jsonArray = (JSONArray) JSONSerializer.toJSON( exchange.getIn().getBody().toString());  
		int size = jsonArray.size();
		for (int i=0; i < size; i++) {
			JSONObject jsonObject = (JSONObject) jsonArray.getJSONObject( i );  
			String id = jsonObject.getString("id");
			System.out.println("ID value from JSON = " + id);
			
			 
			try {
				myBucket = riakClient.fetchBucket(id).execute();
			} catch (RiakRetryFailedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			DateTime dt = new DateTime();
			DateTimeFormatter fmt = DateTimeFormat.forPattern("MM-dd-yyyy-HH-mm-ss");
			String dtStr = fmt.print(dt);
			
			System.out.println("data as key = "+dtStr);
			System.out.println("JsonObject value = "+ jsonObject.toString());
			
		
			
	        StoreObject<IRiakObject> storeObject = myBucket.store(dtStr, jsonObject.toString());
	        try {
				storeObject.w(Quora.ONE).pw(Quora.ONE).execute();
				System.out.println("Successfully stored the data into RIAK !!!!!!! ");
			} catch (RiakRetryFailedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnresolvedConflictException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ConversionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		*/
	       
		}
	
		

	

	/**
	 * @param args
	 * @throws ConversionException 
	 * @throws UnresolvedConflictException 
	 * @throws RiakRetryFailedException 
	 */
	public static void main(String[] args) throws RiakRetryFailedException, UnresolvedConflictException, ConversionException {
		// TODO Auto-generated method stub
		// Protocol Buffer Client
		IRiakClient pbClient;
		PBClientAdapter pbClientAdapter;
		IRiakClient riakClient;
		try {
        	// Initialize Protocol Buffer Client

        	pbClient = RiakFactory.pbcClient("127.0.0.1",8098);
            //pbClient.ping();
			pbClientAdapter = new PBClientAdapter("127.0.0.1",8098);
			
			riakClient = RiakFactory.httpClient();
            

			  for (int i = 0; i < 5; i++)
			  {
			
				 IRiakObject riakObject = RiakObjectBuilder.newBuilder("2iTest","key_on_key-"+i).
		            		withValue("{\"name\":\"steve\"}".getBytes()).withContentType("application/json").addIndex("anotherlook", "knowledgeworker").build();
				 System.out.println("RiakObject = "+riakObject);
				 System.out.println("RiakObject bucket = "+riakObject.getBucket());
				 System.out.println("RiakObject contenttype = "+riakObject.getContentType());
				 System.out.println("RiakObject key = "+riakObject.getKey());
				 System.out.println("RiakObject value = "+riakObject.getValue());
				 
				 riakClient.fetchBucket("2iTest").execute().store(riakObject).execute();
				 //pbClientAdapter.store(riakObject);
				 
					System.out.println("Finished...");
			  }
			 
        } catch(Exception ex) {
            System.out.println("exception: "+ex.getMessage());
            ex.printStackTrace();
          
        }
		
		/*
		IRiakClient pbClient;
		RawClient raw_client;
		
		try {
            // Initialize Protocol Buffer Client
//                    	pbClient = RiakFactory.pbClient(nodeIP,nodePort)
			
			raw_client = new RiakClient("127.0.0.1",8098);
			pbClient = new PBClientAdapter(raw_client);
            //pbClient = RiakFactory.pbcClient("127.0.0.1",8098);
            //raw_client = new PBClientAdapter("127.0.0.1",8098);
            //pbClient.ping();
            System.out.println("after ping");
            
            IRiakObject riakobj = RiakObjectBuilder.newBuilder("2iTest","key_on_key-2").
            		withValue("{name:steve,title:engineer}").withContentType("application/json").addIndex("anotherlook", "knowledgeworker").build();
            
            raw_client.store(riakobj);
            raw_client.shutdown();
            
            System.out.println("Finished...");
            
            //println "RiakService initialized successfully"
        } catch(Exception ex) {
            System.out.println("exception: "+ex.getMessage());
            ex.printStackTrace();
          
        }
		*/
		/*
		
		
		IRiakClient riakClient=null;
		try {
			riakClient = RiakFactory.httpClient();
		} catch (RiakException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JmxDataMap dataMap = new JmxDataMap();
		dataMap.setGroupKey("SteveSui_Key");
		//dataMap.setKey("key_set_on_hash");
		dataMap.put("lastname","Smith");
		dataMap.put("firstname","John");
		dataMap.put("realkey","key_set_on_hash");
		
        Bucket myBucket = riakClient.fetchBucket("2iTest").execute();
        RawClient raw_client 
        
        Bucket newBucket = RawClient raw_client
        //myBucket.store
       // StoreObject<IRiakObject> storeObject = myBucket.store("key1", "mydata");
        IRiakObject riakobj = RiakObjectBuilder.newBuilder("2iTest","key_on_key").
        		withValue("{name:steve,title:engineer}").withContentType("application/json").addIndex("anotherlook", "knowledgeworker").build();
        
        
        StoreObject<IRiakObject> storeObject = myBucket.store(riakobj);
        storeObject.w(Quora.ONE).pw(Quora.ONE).execute();
        riakClient.shutdown();
		*/
      

	}

}
