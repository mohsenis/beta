/* This program is free software: you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public License
 as published by the Free Software Foundation, either version 3 of
 the License, or (props, at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>. */

package com.webapp.api.model;

import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.library.model.ClusteredStop;
import com.library.model.StopCluster;
import com.webapp.api.utils.StringUtils;


@XmlRootElement(name = "HubR")
public class HubR {
	
	
	@XmlAttribute
    @JsonSerialize
	public String clusterid;
	
	@XmlAttribute
    @JsonSerialize
	public String agencies;
	
	@XmlAttribute
    @JsonSerialize
    public String routes;
	
	@XmlAttribute
    @JsonSerialize
    public String services;
	
	@XmlElement(name = "Stops")
    public Collection<StopCLR> StopCLR = new ArrayList<StopCLR>();
	
	public void addCluster(StopCluster cluster, int clusterId){
		this.clusterid = String.valueOf(clusterId);
		this.agencies = StringUtils.join(cluster.getAgencies(), "; ");
		this.routes = StringUtils.join(cluster.routes, "; ");
		this.services = String.valueOf(cluster.visits);
		for (ClusteredStop instance: cluster.getStops()){
			StopCLR cstop = new StopCLR();
			cstop.agencyId = instance.getAgencyId();
			cstop.realAgencyIds = StringUtils.join(instance.getAgencies(), "; ");
			cstop.routeIds = StringUtils.join(instance.routes, "; ");
			cstop.services = String.valueOf(instance.getVisits());
			cstop.stopId = instance.id;
			cstop.stopName = instance.getName();
			this.StopCLR.add(cstop);
		}		
	}
}
