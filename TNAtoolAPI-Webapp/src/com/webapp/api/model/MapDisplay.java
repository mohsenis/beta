package com.webapp.api.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.library.model.MapGeo;
import com.library.model.MapPnR;
import com.library.model.MapTransit;

@XmlRootElement(name = "MapD")
public class MapDisplay {
	
	@XmlElement(name="MapTR")	
	public MapTransit MapTR;
	
	@XmlElement(name="MapG")	
	public MapGeo MapG;
	
	@XmlElement(name="MapPnR")	
	public MapPnR MapPnR;
}

