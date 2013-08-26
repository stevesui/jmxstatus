package com.att.encore.performance.jmx;

import java.util.*;

import com.basho.riak.client.IRiakClient;
import com.basho.riak.client.IRiakObject;
import com.basho.riak.client.query.functions.JSSourceFunction;
import com.basho.riak.client.query.indexes.BinIndex;
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
import com.basho.riak.client.query.MapReduceResult;
import com.basho.riak.client.raw.RawClient;
import com.basho.riak.client.raw.config.Configuration;
import com.basho.riak.client.raw.pbc.PBClientAdapter;
import com.basho.riak.client.raw.pbc.PBClientConfig;
import com.basho.riak.client.raw.query.indexes.BinValueQuery;
import com.basho.riak.client.raw.query.indexes.IndexQuery;
import com.basho.riak.client.http.mapreduce.JavascriptFunction;

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
// curl -X POST \
//-H "content-type: application/json" \
//-d @- \
//http://localhost:8098/mapred \
//<<EOF
// {"inputs":{
//       "bucket":"att.contacts.aab.importContact.0.1",
//       "index":"2ikeyref_bin",
//       "key":"att.contacts.aab.importContact.0.1"
//       },
//   "query":[
//      {"map":{"language":"javascript",
//            "source":"function(value, keyData, arg) 	
//            { 
//                return [[value.bucket,value.key]];
//            }",
//            "keep":false}
//      }
//     ,
//      {"map":{"language":"javascript",
//           "source":"function(value, keyData, arg)
//           { 
//               var data = Riak.mapValuesJson(value)[0];
//               var obj = {};
//               obj[value.key] = data.maxTime;
//               return [obj];
//            }",
//           "keep":false}
//     }
//  ,
//     {"reduce":{"language":"javascript",
//           "source":"function(values)
//           { 
//               if(values.length == 0)
//        return [];
//      else
//        return [values.reduce(function(prev,next){
//          return (prev > next) ? prev : next;
//        })];
//            }",
//           "keep":true}}
//  ]
//}
//EOF
	 
	 */
	
	/**
	 * Set mapreduce result into exchange out body.
	 * 
	 * @param exchange
	 */
	public void doMapReduce(Exchange exchange) {
		String result = this.doMapReduce("");
		exchange.getOut().setBody(result);
		
	}
	
	/**
	 * Generic mapreduce method with parameter as String value.
	 * @param input
	 * @return
	 */
	public String doMapReduce(String input) {
		
		String IndexfunctionSource = new StringBuilder()
				.append("function(value, keyData, arg){return [[value.bucket,value.key]];}").toString();
		
		String RetrieveMaxProcessorFunctionSource = new StringBuilder()
				.append("function(value, keyData, arg)")
				.append("{var data = Riak.mapValuesJson(value)[0]; var obj = {};obj[value.key] = data.maxTime; return [obj];}").toString();
				 
	   String  RetrieveMaxProcessorReduceFunctionSource = new StringBuilder()
	           .append("function(values)")
	           .append("{if(values.length == 0) return [];")
	           .append(" else return [values.reduce(function(prev,next){ return (prev > next) ? prev : next; })]; }").toString();
				
		
		String bucket="att.contacts.aab.importContact.0.1";
		String indexName ="2ikeyref_bin";
		String indexValue ="att.contacts.aab.importContact.0.1";
		
		MapReduceResult result = null;
		
		IndexQuery iq = new BinValueQuery(BinIndex.named(indexName),
				bucket, indexValue);
		try{
		     result = riakClient.mapReduce(iq)
		                                .addMapPhase(new JSSourceFunction(IndexfunctionSource))
		                                .addMapPhase(new JSSourceFunction(RetrieveMaxProcessorFunctionSource))
		                                .addReducePhase(new JSSourceFunction(RetrieveMaxProcessorReduceFunctionSource))
		                                .execute();
		}
		catch(Exception ex) {
			ex.printStackTrace();
		}
		
		return result.getResultRaw();
	}
		

	public void test2iIndexStore() {
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
	}

	/**
	 * @param args
	 * @throws ConversionException 
	 * @throws UnresolvedConflictException 
	 * @throws RiakRetryFailedException 
	 */
	public static void main(String[] args) throws RiakRetryFailedException, UnresolvedConflictException, ConversionException {
		
		RiakDump dump = new RiakDump();
		dump.init();
		String result = dump.doMapReduce("");
		System.out.println(result);
		/*
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
		*/
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
