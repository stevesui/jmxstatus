package com.att.encore.performance.jmx;

import static com.basho.riak.client.convert.KeyUtil.getKey;

import java.util.Collection;
import java.util.Map;

import com.basho.riak.client.IRiakObject;
import com.basho.riak.client.RiakLink;
import com.basho.riak.client.builders.RiakObjectBuilder;
import com.basho.riak.client.cap.VClock;
import com.basho.riak.client.convert.ConversionException;
import com.basho.riak.client.convert.Converter;
import com.basho.riak.client.convert.NoKeySpecifedException;
import com.basho.riak.client.convert.RiakIndexConverter;
import com.basho.riak.client.convert.RiakLinksConverter;
import com.basho.riak.client.convert.UsermetaConverter;
import com.basho.riak.client.http.util.Constants;
import com.basho.riak.client.query.indexes.RiakIndexes;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.ObjectBuffer;

public class KryoPersonConverter implements Converter<Person>
    {

        private String bucket;
        private final UsermetaConverter<Person> usermetaConverter;
        private final RiakIndexConverter<Person> riakIndexConverter;
        private final RiakLinksConverter<Person> riakLinksConverter;
        
        public KryoPersonConverter(String bucket)
        {
            this.bucket = bucket;
            this.usermetaConverter = new UsermetaConverter<Person>();
            this.riakIndexConverter = new RiakIndexConverter<Person>();
            this.riakLinksConverter = new RiakLinksConverter<Person>();
        }
        
        
        public IRiakObject fromDomain(Person domainObject, VClock vclock) throws ConversionException
        {
            //throw new UnsupportedOperationException("Not supported yet.");
            
            String key = getKey(domainObject);
            
            if (key == null)
            {
                throw new NoKeySpecifedException(domainObject);
            }
            
            Kryo kryo = new Kryo();
            kryo.register(Person.class);
            
            ObjectBuffer ob = new ObjectBuffer(kryo);
            byte[] value = ob.writeObject(domainObject);
            
            Map<String, String> usermetaData = usermetaConverter.getUsermetaData(domainObject);
            RiakIndexes indexes = riakIndexConverter.getIndexes(domainObject);
            Collection<RiakLink> links = riakLinksConverter.getLinks(domainObject);
            
            return RiakObjectBuilder.newBuilder(bucket, key)
                .withValue(value)
                .withVClock(vclock)
                .withUsermeta(usermetaData)
                .withIndexes(indexes)
                .withLinks(links)
                .withContentType(Constants.CTYPE_OCTET_STREAM)
                .build();
            
            
        }

        public Person toDomain(IRiakObject riakObject) throws ConversionException
        {
            
            if (riakObject == null)
                return null;
            
            Kryo kryo = new Kryo();
            kryo.register(Person.class);
            ObjectBuffer ob = new ObjectBuffer(kryo);
            
            Person domainObject = ob.readObject(riakObject.getValue(), Person.class);
            
            usermetaConverter.populateUsermeta(riakObject.getMeta(), domainObject);
            riakIndexConverter.populateIndexes(new RiakIndexes(riakObject.allBinIndexes(), riakObject.allIntIndexes()),
                                                   domainObject);
            riakLinksConverter.populateLinks(riakObject.getLinks(), domainObject);
            
            return domainObject;
            
        }
    }


