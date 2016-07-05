package com.library.model.congrapph;

import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement (name = "ConGraphObjSet") 
public class ConGraphObjSet {

	@XmlElement (name = "set")
	public Set<ConGraphObj> set = new HashSet<ConGraphObj>();
}
