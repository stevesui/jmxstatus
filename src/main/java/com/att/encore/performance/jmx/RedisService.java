package com.att.encore.performance.jmx;

import java.util.Iterator;
import java.util.List;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.SortingParams;

public class RedisService {
	private String host;
	private int port;
	private static String REDIS_SETNAME = "RouteBucket";

	public String getHost() {
		return host;
	}


	public void setHost(String host) {
		this.host = host;
	}


	public int getPort() {
		return port;
	}


	public void setPort(int port) {
		this.port = port;
	}


	public void addSetValue(String value) {
		
		//Connecting to Redis 
		  Jedis jedis = new Jedis(host);
		  //adding a new key
		  jedis.sadd(REDIS_SETNAME, value);
	}
	
	
	public List<String>getSortedSet() {
		//Connecting to Redis
		  Jedis jedis = new Jedis(host);
		  //return a sorted list
		  return jedis.sort(REDIS_SETNAME, new SortingParams().alpha());
		
	}
		
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
	    String cacheKey = "languages";
		RedisService redisservice = new RedisService();
		redisservice.addSetValue("stevesui");
		redisservice.addSetValue( "johnsmith");
		
		
		 Jedis jedis = new Jedis("localhost");
		 //Adding a set as value
		 jedis.sadd(cacheKey,"Java","C#","Python");//SADD
		 
		 //Getting all values in the set: SMEMBERS
		 System.out.println("Languages: " + jedis.smembers(cacheKey));
		 //Adding new values
		 jedis.sadd(cacheKey,"Java","Ruby");
		 //Getting the values... it doesn't allow duplicates
		 System.out.println("Languages: " + jedis.sort(cacheKey, new SortingParams().alpha()));
		 List<String> list = jedis.sort(cacheKey, new SortingParams().alpha());
			Iterator<String> itr = list.iterator();
			while(itr.hasNext()) {
				System.out.println(itr.next());
			}
		

			/*


		RedisService redisservice = new RedisService();
		redisservice.addSetValue("mykey", "stevesui");
		redisservice.addSetValue("mykey", "johnsmith");
	
		List<String> list = redisservice.getSortedSet("mykey");
		Iterator itr = list.iterator();
		while(itr.hasNext()) {
			System.out.println(itr.next().toString());
		}
		*/

	}

}
