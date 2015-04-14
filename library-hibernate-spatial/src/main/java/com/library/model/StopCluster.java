package com.library.model;

import java.util.ArrayList;
import java.util.List;

public class StopCluster implements Comparable<StopCluster> {
	public List<String>agencies;
	public List<String>routes;
	public int size;
	public int visits;
	public List<ClusteredStop>stops = new ArrayList<ClusteredStop>();
	
	public List<String> getAgencies() {
		return agencies;
	}
	public void setAgencies(List<String> agencies) {
		this.agencies = agencies;
	}
	public List<String> getRoutes() {
		return routes;
	}
	public void setRoutes(List<String> routes) {
		this.routes = routes;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		this.size = size;
	}
	public int getVisits() {
		return visits;
	}
	public void setVisits(int visits) {
		this.visits = visits;
	}
	public List<ClusteredStop> getStops() {
		return stops;
	}
	public void addStop(ClusteredStop stop){
		stops.add(stop);
	}
	public void setStops(List<ClusteredStop> stops) {
		this.stops = stops;
	}
	public boolean removeStops(List<ClusteredStop> stoplist){
		boolean response = false;
		for (ClusteredStop stop: stoplist){
			response |=stops.remove(stop);
		}
		return response;
	}
	public void syncParams(){
		size = 0;
		visits = 0;
		agencies = new ArrayList<String>();
		routes = new ArrayList<String>();
		if (!stops.isEmpty()){
			for (ClusteredStop stop: stops){
				size++;
				visits+=stop.getVisits();
				List<String> stoproutes = stop.getRoutes();
				for (String route: stoproutes){
					if (!routes.contains(route))
						routes.add(route);
				}			
				List<String> stopagencies = stop.getAgencies();
				for (String agency: stopagencies){
					if (!agencies.contains(agency))
						agencies.add(agency);
				}}}		
	}
	
	@Override
	public int compareTo(StopCluster o) {
		if (this.agencies.size()==o.agencies.size()){
			if (this.routes.size()==o.routes.size()){
				if (this.visits==o.visits){
					if (this.stops.size()==o.stops.size()){
						return this.agencies.get(0).toString().compareTo(o.agencies.get(0).toString());
					} else return (this.stops.size()>o.stops.size() ? 1:-1);
				} else return (this.visits>o.visits ? 1:-1);
			} else return (this.routes.size()>o.routes.size() ? 1:-1);
		} else return (this.agencies.size()>o.agencies.size() ? 1:-1);		
	}
}
