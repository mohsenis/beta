package com.library.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@XmlRootElement( name = "EmpData")
public class EmpData {

	@XmlAttribute
	@JsonSerialize
	public int id;
	

	@XmlAttribute
	@JsonSerialize
	public String name;
	

	@XmlAttribute
	@JsonSerialize
	public int population;
	

	@XmlAttribute
	@JsonSerialize
	public double popserved;
	

	@XmlAttribute
	@JsonSerialize
	public int all_naics_sectors;
	

	@XmlAttribute
	@JsonSerialize
	public int all_naics_sectors_served;
	

	@XmlAttribute
	@JsonSerialize
	public int agriculture_forestry_fishing_hunting;
	

	@XmlAttribute
	@JsonSerialize
	public int agriculture_forestry_fishing_hunting_served;
	

	@XmlAttribute
	@JsonSerialize
	public int mining_quarrying_oil_gas_extraction;
	

	@XmlAttribute
	@JsonSerialize
	public int mining_quarrying_oil_gas_extraction_served;
	

	@XmlAttribute
	@JsonSerialize
	public int utilities;
	

	@XmlAttribute
	@JsonSerialize
	public int utilities_served;
	

	@XmlAttribute
	@JsonSerialize
	public int construction;
	

	@XmlAttribute
	@JsonSerialize
	public int construction_served;
	

	@XmlAttribute
	@JsonSerialize
	public int wholesale_trade;
	

	@XmlAttribute
	@JsonSerialize
	public int wholesale_trade_served;
	

	@XmlAttribute
	@JsonSerialize
	public int information;
	

	@XmlAttribute
	@JsonSerialize
	public int information_served;

	@XmlAttribute
	@JsonSerialize
	public int finance_insurance;
	

	@XmlAttribute
	@JsonSerialize
	public int finance_insurance_served;
	

	@XmlAttribute
	@JsonSerialize
	public int real_estate_rental_leasing;
	

	@XmlAttribute
	@JsonSerialize
	public int real_estate_rental_leasing_served;
	

	@XmlAttribute
	@JsonSerialize
	public int professional_scientific_technical_services;
	

	@XmlAttribute
	@JsonSerialize
	public int professional_scientific_technical_services_served;
	

	@XmlAttribute
	@JsonSerialize
	public int management_of_companies_enterprises;
	

	@XmlAttribute
	@JsonSerialize
	public int management_of_companies_enterprises_served;

	@XmlAttribute
	@JsonSerialize
	public int administrative_support_waste_management_remediation_services;
	

	@XmlAttribute
	@JsonSerialize
	public int administrative_support_waste_management_remediation_services_served;
	

	@XmlAttribute
	@JsonSerialize
	public int educational_services;
	

	@XmlAttribute
	@JsonSerialize
	public int educational_services_served;
	

	@XmlAttribute
	@JsonSerialize
	public int health_care_social_assistance;
	

	@XmlAttribute
	@JsonSerialize
	public int health_care_social_assistance_served;
	

	@XmlAttribute
	@JsonSerialize
	public int arts_entertainment_recreation;
	

	@XmlAttribute
	@JsonSerialize
	public int arts_entertainment_recreation_served;
	

	@XmlAttribute
	@JsonSerialize
	public int accommodation_food_services;
	

	@XmlAttribute
	@JsonSerialize
	public int accommodation_food_services_served;
	

	@XmlAttribute
	@JsonSerialize
	public int other_services_except_public_administration;
	

	@XmlAttribute
	@JsonSerialize
	public int other_services_except_public_administration_served;

	@XmlAttribute
	@JsonSerialize
	public int public_administration;
	

	@XmlAttribute
	@JsonSerialize
	public int public_administration_served;
	

	@XmlAttribute
	@JsonSerialize
	public int manufacturing;
	

	@XmlAttribute
	@JsonSerialize
	public int manufacturing_served;

	@XmlAttribute
	@JsonSerialize
	public int retail_trade;
	

	@XmlAttribute
	@JsonSerialize
	public int retail_trade_served;
	

	@XmlAttribute
	@JsonSerialize
	public int transportation_warehousing;
	

	@XmlAttribute
	@JsonSerialize
	public int transportation_warehousing_served;
	
}
