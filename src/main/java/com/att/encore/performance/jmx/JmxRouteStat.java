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
    public  List<String> getAllRoutesAsList() {
    	
    	JMXServiceURL url = null;
    	JMXConnector jmxc = null;
    	MBeanServerConnection server = null;
    	List<String>  jmxList = new ArrayList<String>();
    	
    	try {
            url = new JMXServiceURL(jmxUrl);
            jmxc = JMXConnectorFactory.connect(url);
            server = jmxc.getMBeanServerConnection();
       }
       catch(Exception ex){
               System.out.println("Exception caught at JmxRoutStat constructor. Reason :  "+ex.getMessage());
       } 
    	
    	try {
		
			//make JMX requests.
			ObjectName objName = new ObjectName("org.apache.camel:type=routes,*");
			List<ObjectName> cacheList = new LinkedList(server.queryNames(objName, null));
			
			for (Iterator<ObjectName> iter = cacheList.iterator(); iter.hasNext();)
			{
			    objName = iter.next();
			    String keyProps = objName.getCanonicalKeyPropertyListString();
			
			    ObjectName objectInfoName = new ObjectName("org.apache.camel:" + keyProps);
			    String routeId = (String) server.getAttribute(objectInfoName, "RouteId");
			   
			   //save routeId into the list.
	           jmxList.add(routeId);
			   
			} //for loop
		}
		catch(Exception ex)
		{
			System.out.println("JMX exception caught. "+ ex);
			System.exit(-1);
		}
		return jmxList;
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
		   //map.put("maxTime", 860);
		   map.put("maxTime", MaxProcessingTime);
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
	
	public void resetJMXParameters() {

		ObjectName objectRouteName = null;
		
		
		 try {
             
			 //url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://172.16.240.118:8999/jmxrmi");
			 url = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:8999/jmxrmi");
             jmxc = JMXConnectorFactory.connect(url);
             server = jmxc.getMBeanServerConnection();
            }
            catch(Exception ex){
                    System.out.println("Exception caught at JmxRoutStat constructor. Reason :  "+ex.getMessage());
            } 

		try{
			
		
				ObjectName objName = new ObjectName("org.apache.camel:type=routes,*");
				List<ObjectName> cacheList = new LinkedList(server.queryNames(objName, null));
				for (Iterator<ObjectName> iter = cacheList.iterator(); iter.hasNext();)
				{
				    objName = iter.next();
				    String keyProps = objName.getCanonicalKeyPropertyListString();
				    System.out.println("keyprop value = "+keyProps);
				    String routeId = (String) server.getAttribute(objName, "RouteId");
				    if(keyProps.contains(routeId))
				    {
				    	
				         objectRouteName = new ObjectName("org.apache.camel:" + keyProps);
				    	
				    	 
				        Object[] params = {};
				        String[] sig = {};
				        
				        try{
				        	 server.invoke(objectRouteName, "reset", params, sig);
				        	 System.out.println("call jmx reset function for Route : "+routeId);
				        }catch(Exception ex) {
				        	System.out.println("exception occurred while invoking method reset "+ ex.getMessage());
				        }
				        
				     
				    }
				}
		} catch (Exception ex) {
			
			ex.getStackTrace();
		}
	
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		JmxRouteStat stat = new JmxRouteStat();
		stat.resetJMXParameters();

	}
}

