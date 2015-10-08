package com.webapp.api.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.library.model.StartEndDates;

@XmlRootElement(name = "StartEndDatesList")
public class StartEndDatesList {
	
	@XmlElement(name = "SEDList")
	public List<StartEndDates> SEDList = new ArrayList<StartEndDates>();

}