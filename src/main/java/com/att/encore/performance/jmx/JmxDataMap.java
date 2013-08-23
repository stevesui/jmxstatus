package com.att.encore.performance.jmx;
import java.util.HashMap;

import com.basho.riak.client.convert.RiakIndex;
import com.basho.riak.client.convert.RiakKey;


public class JmxDataMap extends HashMap<String, Object> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@RiakIndex(name="groupKey") private String groupKey;
	@RiakKey private String key;
   

	public String getKey() {
		return key;
	}



	public void setKey(String key) {
		this.key = key;
	}



	public String getGroupKey() {
		return groupKey;
	}



	public void setGroupKey(String groupKey) {
		this.groupKey = groupKey;
	}



	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
