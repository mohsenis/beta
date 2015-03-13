package com.webapp.api.utils;

import java.util.Collection;
import java.util.Iterator;

public class StringUtils {
	public static String join(Collection s, String delimiter) {
	    StringBuffer buffer = new StringBuffer();
	    Iterator iter = s.iterator();
	    while (iter.hasNext()) {
	        buffer.append(iter.next());
	        if (iter.hasNext()) {
	            buffer.append(delimiter);
	        }
	    }
	    return buffer.toString();
	}
	
	public static String roundjoin(Collection s, String delimiter) {
	    StringBuffer buffer = new StringBuffer();
	    Iterator iter = s.iterator();
	    while (iter.hasNext()) {
	        buffer.append(String.valueOf(Math.round(328.084*((Double)iter.next()))/100.00));
	        if (iter.hasNext()) {
	            buffer.append(delimiter);
	        }
	    }
	    return buffer.toString();
	}

}
