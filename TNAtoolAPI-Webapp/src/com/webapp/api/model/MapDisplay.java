package com.webapp.api.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "MapD")
public class MapDisplay {
	
	@XmlElement(name="MapTR")	
	public MapTransit MapTr;
	
	@XmlElement(name="MapG")	
	public MapGeo MapG;
}

