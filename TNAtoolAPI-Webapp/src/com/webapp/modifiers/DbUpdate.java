package com.webapp.modifiers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.QueryParam;
import javax.xml.bind.annotation.XmlRootElement;

import com.webapp.api.model.*;
import com.webapp.api.utils.PolylineEncoder;
import com.webapp.api.utils.SphericalDistance;

import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.examples.GtfsHibernateReaderExampleMain;
import org.onebusaway.gtfs.GtfsDatabaseLoaderMain;

@Path("/dbupdate")
@XmlRootElement
public class DbUpdate {
	
	@GET
    @Path("/updatetrips")
   @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object dbshapesforagency(@QueryParam("agency") String agency){
    	ShapeList response = new ShapeList();
		List<Trip> triplist = GtfsHibernateReaderExampleMain.QueryTripsforAgency(agency);
		String shapeId = "";
		    	
    	String pe = "";	    		    	
    	double length = 0;
    	double estlength = 0;
    	String Desc = "";
		for (Trip trip: triplist){
			AgencyAndId agencyandtrip = trip.getId();	
	    	Trip tp = GtfsHibernateReaderExampleMain.getTrip(agencyandtrip);
	    	List<ShapePoint> shapes = GtfsHibernateReaderExampleMain.Queryshapebytrip(agencyandtrip);
	    	
	    	//computing the shape length
	    	if (shapes.size()>1) {
		    	if (!(trip.getShapeId().getId().equals(shapeId))){		    		
		    		pe = PolylineEncoder.createEncodings(shapes, 1);
			    	length = SphericalDistance.sLength(shapes);
			    	shapeId = trip.getShapeId().getId();
		    	}	    		
		    	Desc = "Trip" + tp.getId().getId() + "has shape data";
	    	} else {
	    		//estimating the trip length and shape (straight line distance) in case there is no shape data
	    		List<StopTime> st = GtfsHibernateReaderExampleMain.Querystoptimebytrip(agencyandtrip);		    		
	        	List<ShapePoint> stops = new ArrayList<ShapePoint>();
	        	for (StopTime instance: st){
	        		Stop stop =instance.getStop();
	        		ShapePoint sp = new ShapePoint();
	        		sp.setLat(stop.getLat());
	        		sp.setLon(stop.getLon());
	        		stops.add(sp);
	        	}
	        	pe = PolylineEncoder.createEncodings(stops, 1);
	        	estlength = SphericalDistance.sLength(stops);	        		        	
	    		Desc = "Trip" + tp.getId().getId() + "does not have shape data";
	    	}		
    	Rshape shape = new Rshape();
    	shape.points = pe;
    	shape.length = SphericalDistance.sLength(shapes);    		 
    	shape.estlength = estlength;
    	shape.description = Desc;
    	response.shapelist.add(shape);
    	
    	// now setting the new values for the trip and updating the DB
    	tp.setshape(pe);
    	tp.setlength(length);
    	tp.setestlength(estlength);
    	GtfsHibernateReaderExampleMain.updateTrip(tp);
		}
		return response;
    }
	@GET
    @Path("/addfeed")
   @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object addfeed(@QueryParam("feedname") String feedname) throws IOException{
		String [] args = new String[5];
		args[0] = "--driverClass=\"org.postgresql.Driver\"";
		args[1] = "--url=\"jdbc:postgresql://localhost:5432/gtfsdb\"";
		args[2] = "--username=\"postgres\"";
		args[3] = "--password=\"123123\"";
		args[4] = "C:/feeds/"+feedname;
		GtfsDatabaseLoaderMain.main(args);		
		return new TransitError(feedname +"Has been added to the database");
	}
	
}
