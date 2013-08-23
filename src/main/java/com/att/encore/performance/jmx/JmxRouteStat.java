package com.att.encore.performance.jmx;

import java.util.*;
import java.io.*;

import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import net.sf.json.*;

import org.apache.camel.Exchange;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class JmxRouteStat {
	
	protected String jmxUrl;
	JMXServiceURL url;
	JMXConnector jmxc;
	MBeanServerConnection server;
	
	public String getJmxUrl() {
		return jmxUrl;
	}

	public void setJmxUrl(String jmxUrl) {
		this.jmxUrl = jmxUrl;
	}

	public JmxRouteStat(){
	}

        public void init() {
          try {
                 url = new JMXServiceURL(jmxUrl);
                 jmxc = JMXConnectorFactory.connect(url);
                 server = jmxc.getMBeanServerConnection();
                }
                catch(Exception ex){
                        System.out.println("Exception caught at JmxRoutStat constructor. Reason :  "+ex.getMessage());
                } 

        }
	
    // Make Camel routes JMX query and loop through all elements returned.
	public void doJmxJob(Exchange xchange){
      
		List<String>  jmxList = new ArrayList<String>();
		
       try {
			
		//make JMX requests.
		ObjectName objName = new ObjectName("org.apache.camel:type=routes,*");
		List<ObjectName> cacheList = new LinkedList(server.queryNames(objName, null));
		
		for (Iterator<ObjectName> iter = cacheList.iterator(); iter.hasNext();)
		{
		    objName = iter.next();
		    String keyProps = objName.getCanonicalKeyPropertyListString();
		    System.out.println("KeyProps = "+keyProps);
		    
		    ObjectName objectInfoName = new ObjectName("org.apache.camel:" + keyProps);
		    //System.out.println("Key attribute = "+objectInfoName.getKeyProperty("CamelId"));
		    
		    //System.out.println("ObjectName = "+objectInfoName.toString());
		    //System.out.println("ObjectName list String  = "+objectInfoName.getKeyPropertyListString());
		    
		    String endURL = (String) server.getAttribute(objectInfoName, "EndpointUri");
		    //System.out.println("End URL = "+endURL);
		    
		    Long  MaxProcessingTime = (Long) server.getAttribute(objectInfoName, "MaxProcessingTime");
		    //System.out.println("MaxProcessingTime = "+MaxProcessingTime.longValue());
		    
		    Long  ExchangesCompleted = (Long) server.getAttribute(objectInfoName, "ExchangesCompleted");
		    //System.out.println("ExchangesCompleted (iterations) = "+ExchangesCompleted.longValue());
			    
		    String routeId = (String) server.getAttribute(objectInfoName, "RouteId");
		    String description = (String) server.getAttribute(objectInfoName, "Description");
		    String state = (String) server.getAttribute(objectInfoName, "State");
		    
		    System.out.println("RoutID = "+routeId+" ; description = "+description+ " ; state = "+state);
		   
		   
		   HashMap<String,Object> map = new HashMap<String,Object>();
		   map.put("id",routeId);
		   //map.put("description", description);
		   map.put("endURL", endURL);
		   map.put("state", state);
		   map.put("maxTime", 860);
		   //map.put("maxTime", Long.toString(MaxProcessingTime));
		   map.put("exchangecompleted",ExchangesCompleted);
		   
		   //get the current date and time and save them as the access_time field.
		   DateTime dt = new DateTime();
		   DateTimeFormatter fmt = DateTimeFormat.forPattern("MM-dd-yyyy-HH-mm-ss");
		   String dtStr = fmt.print(dt);
		   map.put("access_time", dtStr);
		   
		   //serialized the hashmap into JSON object.
		   JSONObject jsonObject = (JSONObject) JSONSerializer.toJSON( map );  

		   //save JSON object stringified value as an element in the list array.
           jmxList.add(jsonObject.toString());
		   
		} //for loop
	     
		 //save the complete list array into Camel exchange in body.
		  xchange.getIn().setBody(jmxList);
		 
		  //save the list array as Camel exchange out body (for sending them to request client)
         xchange.getOut().setBody(jmxList.toString());
		}
		catch(Exception ex)
		{
			System.out.println("JMX exception caught. "+ ex);
			System.exit(-1);
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}
	
	
	
	class JmxData {
		String routeId;
		String description;
		String endPointUri;
		String state;
		long maxProcessingTime;
		long exchangeCompleted;
		
		public String getRouteId() {
			return routeId;
		}
		public void setRouteId(String routeId) {
			this.routeId = routeId;
		}
		public String getDescription() {
			return description;
		}
		public void setDescription(String description) {
			this.description = description;
		}
		public String getEndPointUri() {
			return endPointUri;
		}
		public void setEndPointUri(String endPointUri) {
			this.endPointUri = endPointUri;
		}
		public String getState() {
			return state;
		}
		public void setState(String state) {
			this.state = state;
		}
		public long getMaxProcessingTime() {
			return maxProcessingTime;
		}
		public void setMaxProcessingTime(long maxProcessingTime) {
			this.maxProcessingTime = maxProcessingTime;
		}
		public long getExchangeCompleted() {
			return exchangeCompleted;
		}
		public void setExchangeCompleted(long exchangeCompleted) {
			this.exchangeCompleted = exchangeCompleted;
		}
	        public String toString() {
                 
                 //String str = " RouteId = "+getRouteId() + " Description " + getDescription()+ " EndPointUri " + getEndPointUri() + " State " + getState() + " MaxProcessingTime " + getMaxProcessingTime()+ " ExchangeCompleted " + getExchangeCompleted();
                 String str = " RouteId = "+getRouteId() + " EndPointUri " + getEndPointUri() + " State " + getState() + " MaxProcessingTime " + getMaxProcessingTime()+ " ExchangeCompleted " + getExchangeCompleted();
                 return str;
                }
		
	}

}

