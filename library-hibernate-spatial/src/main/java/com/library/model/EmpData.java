package com.library.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@XmlRootElement( name = "EmpData")
public class EmpData {
	
	@XmlAttribute
	@JsonSerialize
	public String id;
	
	@XmlAttribute
	@JsonSerialize
	public String name;

	@XmlAttribute
	@JsonSerialize
	public int population;
	@XmlAttribute
	@JsonSerialize
	public int c000;
	@XmlAttribute
	@JsonSerialize
	public int ca01;
	@XmlAttribute
	@JsonSerialize
	public int ca02;
	@XmlAttribute
	@JsonSerialize
	public int ca03;	
	@XmlAttribute
	@JsonSerialize
	public int ce01;
	@XmlAttribute
	@JsonSerialize
	public int ce02;
	@XmlAttribute
	@JsonSerialize
	public int ce03;	
	@XmlAttribute
	@JsonSerialize
	public int cns01;
	@XmlAttribute
	@JsonSerialize
	public int cns02;	
	@XmlAttribute
	@JsonSerialize
	public int cns03;
	@XmlAttribute
	@JsonSerialize
	public int cns04;
	@XmlAttribute
	@JsonSerialize
	public int cns05;
	@XmlAttribute
	@JsonSerialize
	public int cns06;
	@XmlAttribute
	@JsonSerialize
	public int cns07;	
	@XmlAttribute
	@JsonSerialize
	public int cns08;
	@XmlAttribute
	@JsonSerialize
	public int cns09;
	@XmlAttribute
	@JsonSerialize
	public int cns10;
	@XmlAttribute
	@JsonSerialize
	public int cns11;
	@XmlAttribute
	@JsonSerialize
	public int cns12;	
	@XmlAttribute
	@JsonSerialize
	public int cns13;
	@XmlAttribute
	@JsonSerialize
	public int cns14;
	@XmlAttribute
	@JsonSerialize
	public int cns15;
	@XmlAttribute
	@JsonSerialize
	public int cns16;
	@XmlAttribute
	@JsonSerialize
	public int cns17;	
	@XmlAttribute
	@JsonSerialize
	public int cns18;
	@XmlAttribute
	@JsonSerialize
	public int cns19;
	@XmlAttribute
	@JsonSerialize
	public int cns20;
	
	@XmlAttribute
	@JsonSerialize
	public int cr01;
	@XmlAttribute
	@JsonSerialize
	public int cr02;	
	@XmlAttribute
	@JsonSerialize
	public int cr03;
	@XmlAttribute
	@JsonSerialize
	public int cr04;
	@XmlAttribute
	@JsonSerialize
	public int cr05;
	@XmlAttribute
	@JsonSerialize
	public int cr07;	
	
	@XmlAttribute
	@JsonSerialize
	public int ct01;
	@XmlAttribute
	@JsonSerialize
	public int ct02;
	
	@XmlAttribute
	@JsonSerialize
	public int cd01;
	@XmlAttribute
	@JsonSerialize
	public int cd02;	
	@XmlAttribute
	@JsonSerialize
	public int cd03;
	@XmlAttribute
	@JsonSerialize
	public int cd04;
	
	@XmlAttribute
	@JsonSerialize
	public int cs01;
	@XmlAttribute
	@JsonSerialize
	public int cs02;	
	
}
