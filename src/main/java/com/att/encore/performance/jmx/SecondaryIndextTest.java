package com.att.encore.performance.jmx;

import com.basho.riak.client.IRiakClient;
import com.basho.riak.client.RiakException;
import com.basho.riak.client.RiakFactory;
import com.basho.riak.client.bucket.Bucket;

import com.basho.riak.client.convert.RiakKey;

import com.basho.riak.client.RiakLink;
import com.basho.riak.client.convert.RiakIndex;
import com.basho.riak.client.convert.RiakKey;
import com.basho.riak.client.convert.RiakLinks;
import com.basho.riak.client.convert.RiakUsermeta;
//import com.esotericsoftware.kryo.Optional;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import com.basho.riak.client.RiakLink;
import java.util.Collection;
import com.basho.riak.client.query.indexes.RiakIndexes;
import java.util.Map;
import com.basho.riak.client.convert.RiakLinksConverter;
import com.basho.riak.client.convert.RiakIndexConverter;
import com.basho.riak.client.convert.UsermetaConverter;
import com.basho.riak.client.query.indexes.BinIndex;
import com.basho.riak.client.http.util.Constants;
import com.basho.riak.client.builders.RiakObjectBuilder;
import com.esotericsoftware.kryo.ObjectBuffer;
import com.esotericsoftware.kryo.Kryo;
import com.basho.riak.client.IRiakObject;
import com.basho.riak.client.cap.VClock;
import com.basho.riak.client.convert.ConversionException;
import com.basho.riak.client.convert.Converter;

import com.basho.riak.client.convert.NoKeySpecifedException;

import static com.basho.riak.client.convert.KeyUtil.getKey;


public class SecondaryIndextTest {
	
   
       public void testSecondaryIndex() throws RiakException
       {
    	   IRiakClient client = RiakFactory.httpClient();
           
           Person p = new Person("Brian", "Roach", "1111 Basho Drive", "555-1212", "engineer");
           Person p2 = new Person("Joe", "Smith", "1111 Basho Drive", "555-1211", "engineer");
           
           Bucket bucket = client.fetchBucket("PersonBucket").execute();
           
           bucket.store(p).withConverter(new KryoPersonConverter("PersonBucket")).execute();
           bucket.store(p2).withConverter(new KryoPersonConverter("PersonBucket")).execute();
           
   		
           // Get the list of keys using the index name we declared in our Person Object
           List<String> engineers = bucket.fetchIndex(BinIndex.named("job_title")).withValue("engineer").execute();
   		
   	for (String s : engineers)
   	{
               p = new Person();
               p.setLastName(s);
               p = bucket.fetch(p).withConverter(new KryoPersonConverter("PersonBucket")).execute();
               System.out.println(p.getFullName());
               System.out.println(p.getAddress());
               System.out.println(p.getPhone());
               System.out.println(p.getJobTitle());
               System.out.println();
           }
           client.shutdown();
       }
    

	  	public static void main( String[] args ) throws RiakException
	        {
	  		SecondaryIndextTest test = new SecondaryIndextTest();
	  		test.testSecondaryIndex();
	  		     
	            
	        }
	}

	


