package com.att.encore.performance.jmx;

import java.util.List;

import org.apache.camel.Exchange;

public class webCacheService {
	private RedisService redis;
	private JmxRouteStat jmsservice;
	

	public RedisService getRedis() {
		return redis;
	}


	public void setRedis(RedisService redis) {
		this.redis = redis;
	}

	public List<String> getBucketList() {
		return redis.getSortedSet();
	}
	
	public void getBucketList(Exchange exchange) {
		List<String> list = this.getBucketList();
		if (list.size()>0) {
			exchange.getOut().setBody(list);
		}
		else {
			//get all routeId by invoking JMX service call.
			list = jmsservice.getAllRoutesAsList();
			
			//store all the routeId into Redis set
			int size = list.size();
			for (int i = 0; i < size; i++) {
				redis.addSetValue(list.get(i));
			}
			exchange.getOut().setBody(list);
		}
			
	}

	public JmxRouteStat getJmsservice() {
		return jmsservice;
	}


	public void setJmsservice(JmxRouteStat jmsservice) {
		this.jmsservice = jmsservice;
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
