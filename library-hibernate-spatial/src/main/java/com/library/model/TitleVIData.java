package com.library.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@XmlRootElement( name = "TitleVIData")
public class TitleVIData {

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
	public int popDisabled;
	
	@XmlAttribute
	@JsonSerialize
	public int popBP;

	@XmlAttribute
	@JsonSerialize
	public int popAP;

	@XmlAttribute
	@JsonSerialize
	public int engSpk;

	@XmlAttribute
	@JsonSerialize
	public int espSpk;

	@XmlAttribute
	@JsonSerialize
	public int otherIndoSpk;
	
	@XmlAttribute
	@JsonSerialize
	public int asianSpk;
	
	@XmlAttribute
	@JsonSerialize
	public int otherLngSpk;

	@XmlAttribute
	@JsonSerialize
	public int hhTotal;

	@XmlAttribute
	@JsonSerialize
	public int hhWhite;

	@XmlAttribute
	@JsonSerialize
	public int hhHispanic;

	@XmlAttribute
	@JsonSerialize
	public int hhBlack;

	@XmlAttribute
	@JsonSerialize
	public int hhAmericanIndian;

	@XmlAttribute
	@JsonSerialize
	public int hhAsian;

	@XmlAttribute
	@JsonSerialize
	public int hhPacificIslander;

	@XmlAttribute
	@JsonSerialize
	public int hhPacificOther;

	@XmlAttribute
	@JsonSerialize
	public int hhWhiteNotHisp;

	@XmlAttribute
	@JsonSerialize
	public int hhOver65;

	@XmlAttribute
	@JsonSerialize
	public int hhUnder65;
}
