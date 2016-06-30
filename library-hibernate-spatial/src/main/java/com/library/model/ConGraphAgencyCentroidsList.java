package com.library.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement( name = "ConGraphAgencyCentroidsList")
public class ConGraphAgencyCentroidsList {
	@XmlElement( name = "ConGraphAgencyCentroidsList")
	public List<ConGraphAgencyCentroids> ConGraphAgencyCentroidsList = new ArrayList<ConGraphAgencyCentroids>(); 
}
