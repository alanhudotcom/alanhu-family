package com.daqi.tools.toolunit;

import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

public class OrderProperties extends Properties {
 
    private final LinkedHashSet<Object> keys = new LinkedHashSet<Object>();
 
    public Enumeration<Object> keys() {
        return Collections.<Object> enumeration(keys);
    }
 
    public Object put(Object key, Object value) {
        keys.add(key);
        return super.put(key, value);
    }
 
    public Set<Object> keySet() {
        return keys;
    }
 
    public Set<String> stringPropertyNames() {
        Set<String> set = new LinkedHashSet<String>();
 
        for (Object key : this.keys) {
            set.add((String) key);
        }
 
        return set;
    }
    /*
    @Override
    public synchronized Object setProperty(String key, String value) {
    	try {
			// TODO Auto-generated method stub
    		String encode = System.getProperty("file.encoding");
			String value_zh = new String(value.getBytes(encode), "ISO-8859-1");
			value = value_zh;
		} catch (UnsupportedEncodingException e) {
		// TODO Auto-generated catch block
			e.printStackTrace();
		}  
		return super.setProperty(key, value);
	}*/
}
