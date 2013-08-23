package com.att.encore.performance.jmx;

import java.util.Collection;
import java.util.Map;

import com.basho.riak.client.RiakLink;
import com.basho.riak.client.convert.RiakIndex;
import com.basho.riak.client.convert.RiakKey;
import com.basho.riak.client.convert.RiakLinks;
import com.basho.riak.client.convert.RiakUsermeta;

public class Person {

	 
		    
		    @RiakKey private String lastName;

		    // Marked transient so kryo doesn't serialize them
		    // The KryoPersonConverter will inject these from Riak
		    @RiakIndex(name = "full_name") transient private String fullName;
		    @RiakIndex(name = "job_title") transient private String jobTitle;
		    @RiakLinks transient private Collection<RiakLink> links;
		    @RiakUsermeta transient private Map<String, String> usermetaData;
		    
		    private String firstName;
		    private String address;
		    private String phone;
		    
		    public Person() {}
		    
		    public Person(String firstName, String lastName, String address, String phone, String title)
		    {
		        this.firstName = firstName;
		        this.lastName = lastName;
		        this.address = address;
		        this.phone = phone;
		        this.fullName = firstName + " " + lastName;
		        this.jobTitle = title;
		    }

		    public String getFirstName()
		    {
		        return firstName;
		    }

		    public void setFirstName(String firstName)
		    {
		        this.firstName = firstName;
		        this.setFullName(firstName + " " + lastName);
		    }

		    public String getLastName()
		    {
		        return lastName;
		    }
		    
		    public void setLastName(String lastName)
		    {
		        this.lastName = lastName;
		        this.setFullName(firstName + " " + lastName);
		    }
		    
		    public String getAddress()
		    {
		        return address;
		    }

		    public void setAddress(String address)
		    {
		        this.address = address;
		    }

		    public String getPhone()
		    {
		        return phone;
		    }

		    public void setPhone(String phone)
		    {
		        this.phone = phone;
		    }

		    public String getJobTitle()
		    {
		        return jobTitle;
		    }

		    public void setJobTitle(String jobTitle)
		    {
		        this.jobTitle = jobTitle;
		    }

		    public String getFullName()
		    {
		        return fullName;
		    }

		    public void setFullName(String fullName)
		    {
		        this.fullName = fullName;
		    }
		}


