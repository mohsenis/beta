package com.webapp.api.model;

import java.util.ArrayList;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;


@XmlRootElement(name="FeedNames")
public class FeedNames {
	
	@XmlElement(name="feeds")	
	public ArrayList<String> feeds = new ArrayList<String>(); 
	
	@XmlElement(name="names")	
	public ArrayList<String> names = new ArrayList<String>(); 

}
