package com.library.model;

import java.util.List;

public class ClusteredStop {
	public String name;
	public String id;
	public String agencyId;
	public List<String> agencies;
	public List<String> routes;
	public int visits;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getAgencyId() {
		return agencyId;
	}
	public void setAgencyId(String agencyId) {
		this.agencyId = agencyId;
	}
	public List<String> getAgencies() {
		return agencies;
	}
	public void setAgencies(List<String> agencies) {
		this.agencies = agencies;
	}
	public void addAgency(String agency){
		agencies.add(agency);
	}
	public List<String> getRoutes() {
		return routes;
	}
	public void setRoutes(List<String> routes) {
		this.routes = routes;
	}
	public void addRoute(String route){
		routes.add(route);
	}
	public int getVisits() {
		return visits;
	}
	public void setVisits(int visits) {
		this.visits = visits;
	}	
}
