package com.library.model;

import java.util.List;


public class agencyCluster {
	public String agencyId;
	public String agencyName;
    public long clusterSize;
    public float minGap;
    public float maxGap;
    public float meanGap;
    public List<String> agencyIds;
    public List<String> agencyNames;
    public List<Double> minGaps;
    public List<String> connections;
    
    public agencyCluster(){    	
    }

	public String getAgencyId() {
		return agencyId;
	}

	public void setAgencyId(String agencyId) {
		this.agencyId = agencyId;
	}

	public String getAgencyName() {
		return agencyName;
	}

	public void setAgencyName(String agencyName) {
		this.agencyName = agencyName;
	}

	public long getClusterSize() {
		return clusterSize;
	}

	public void setClusterSize(long clusterSize) {
		this.clusterSize = clusterSize;
	}

	public float getMinGap() {
		return minGap;
	}

	public void setMinGap(float minGap) {
		this.minGap = minGap;
	}

	public float getMaxGap() {
		return maxGap;
	}

	public void setMaxGap(float maxGap) {
		this.maxGap = maxGap;
	}

	public float getMeanGap() {
		return meanGap;
	}

	public void setMeanGap(float meanGap) {
		this.meanGap = meanGap;
	}

	public List<String> getAgencyIds() {
		return agencyIds;
	}

	public void setAgencyIds(List<String> agencyIds) {
		this.agencyIds = agencyIds;
	}

	public List<String> getAgencyNames() {
		return agencyNames;
	}

	public void setAgencyNames(List<String> agencyNames) {
		this.agencyNames = agencyNames;
	}

	public List<Double> getMinGaps() {
		return minGaps;
	}

	public void setMinGaps(List<Double> minGaps) {
		this.minGaps = minGaps;
	}

	public List<String> getConnections() {
		return connections;
	}

	public void setConnections(List<String> connections) {
		this.connections = connections;
	}   
}
