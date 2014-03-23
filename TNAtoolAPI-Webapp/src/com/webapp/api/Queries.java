package com.webapp.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.QueryParam;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jettison.json.JSONException;

import com.webapp.api.model.*;
import com.webapp.api.utils.PolylineEncoder;

import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.examples.GtfsHibernateReaderExampleMain;


@Path("/transit")
@XmlRootElement
public class Queries {
	
	private static final double STOP_SEARCH_RADIUS = 0.1;
	
	@GET
    @Path("/saeed")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object saeed(@QueryParam("routerId") String routerId) throws JSONException {

        return new TransitError("This is a test. You entered: "+ routerId);
    }
	
	/**
     * Generates a sorted by agency id list of routes for the LHS menu
     * , 
     */
    @GET
    @Path("/menu")
    @Produces({ MediaType.APPLICATION_JSON , MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    public Object getmenu() throws JSONException {    	
    	Collection <Agency> allagencies = GtfsHibernateReaderExampleMain.QueryAllAgencies();
    	AgencyRouteList response = new AgencyRouteList();
    	for (Agency instance : allagencies){    		
    		AgencyRoute each = new AgencyRoute();
    		Attr attribute = new Attr();
    		attribute.id = instance.getId();
        	attribute.type = "agency";        	
        	each.state = "closed";        	
        	each.attr = attribute;
    		List <Route> routes = GtfsHibernateReaderExampleMain.QueryRoutesbyAgency(instance);    		
    		for (Route route : routes){
    			RouteListm eachO = new RouteListm();
    			String str = null;
                if (route.getLongName()!= null) str = route.getLongName();		                
                if ((route.getShortName()!= null)){
                	if (str != null){
                		str = str + "(" + route.getShortName()+ ")";		                		
                	} else {
                		str = route.getShortName();
                	}
                }
                eachO.data = str;
                eachO.state = "closed";
                attribute = new Attr();
                attribute.id = route.getId().getId();
                attribute.type = "route";			                
                eachO.attr = attribute;
                List<Trip> trips = GtfsHibernateReaderExampleMain.QueryTripsbyRoute(route);
                List<String> shapeids = new ArrayList<String>();
                List<String> shapes = new ArrayList<String>();
                for (Trip trip: trips){
                	AgencyAndId sid = trip.getShapeId();
                	if (sid !=null) {
                		if (!(shapeids.contains(sid.getId()))){
                    		shapeids.add(sid.getId());
                    		VariantListm eachV = new VariantListm();
                        	attribute = new Attr();
                        	String name = "";
                        	if (trip.getTripHeadsign()!=null ) {
                        		name = trip.getTripHeadsign();
                        	}else {
                        		if (trip.getTripShortName()!=null){
                        			name = trip.getTripShortName();
                        		}else{
                        			List<StopTime> st = GtfsHibernateReaderExampleMain.Querystoptimebytrip(trip.getId());
                        			
                        			name = "From "+ st.get(0).getStop().getName() + " to "+ st.get(st.size()-1).getStop().getName();
                        		}
                        	}
        	                eachV.data = name;
        	                eachV.state = "leaf";
        	                attribute.id = trip.getId().getId();
        	                attribute.type = "variant";		                
        	                eachV.attr = attribute;
        	            	eachO.children.add(eachV) ; 
                    	}
                	} else{
                		String shape = trip.getshape();
                		if (!(shapes.contains(shape))){
                    		shapes.add(shape);
                    		VariantListm eachV = new VariantListm();
                        	attribute = new Attr();
                        	String name = "";
                        	if (trip.getTripHeadsign()!=null ) {
                        		name = trip.getTripHeadsign();
                        	}else {
                        		if (trip.getTripShortName()!=null){
                        			name = trip.getTripShortName();
                        		}else{
                        			List<StopTime> st = GtfsHibernateReaderExampleMain.Querystoptimebytrip(trip.getId());
                        			name = "From "+ st.get(0).getStopHeadsign() + " to "+ st.get(st.size()-1).getStopHeadsign();
                        		}
                        	}
                        	eachV.data = name;
        	                eachV.state = "leaf";
        	                attribute.id = trip.getId().getId();
        	                attribute.type = "variant";		                
        	                eachV.attr = attribute;
        	            	eachO.children.add(eachV) ; 
                    	}
                	}
                }
                each.children.add(eachO);
    		}
    		each.data = instance.getName();
        	response.data.add(each);
    	}
    	return response;    	
    }
    
    	/**
     * Return a list of all stops for a given agency in the database
     */	
    @GET
    @Path("/stops")
   @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object dbstopsforagency(@QueryParam("agency") String agency){
		StopList response = new StopList();
    	//StopList response = new StopList();		
		List<Stop> stops = GtfsHibernateReaderExampleMain.QueryStopsbyAgency(agency);		
		for (Stop stop : stops){
			  response.stops.add(new StopType(stop, false));
		  } 
		//response.stops = stops;
		//return response; 
		//response.stops = GtfsHibernateReaderExampleMain.QueryStops(agency);
		
		return response;
    }
    
 	/**
     * Return shape for a given trip and agency
     */	
    @GET
    @Path("/shape")
   @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object dbshapesforagency(@QueryParam("agency") String agency,@QueryParam("trip") String trip){
    	AgencyAndId agencyandtrip = new AgencyAndId();
    	agencyandtrip.setAgencyId(agency);
    	agencyandtrip.setId(trip);
    	Trip tp = GtfsHibernateReaderExampleMain.getTrip(agencyandtrip);    	
    	Rshape shape = new Rshape();
    	shape.points = tp.getshape();
    	shape.length = tp.getlength();    	    	 
    	shape.estlength = tp.getestlength();
		return shape;
    }
  
	/**
     * Return length for a given trip and agency
     */	
    @GET
    @Path("/tlength")
   @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object triplength(@QueryParam("agency") String agency,@QueryParam("trip") String trip){
    	AgencyAndId agencyandtrip = new AgencyAndId();
    	agencyandtrip.setAgencyId(agency);
    	agencyandtrip.setId(trip);
    	List<ShapePoint> shapes = GtfsHibernateReaderExampleMain.Queryshapebytrip(agencyandtrip);
    	String pe = PolylineEncoder.createEncodings(shapes, 1);
    	Rshape shape = new Rshape();
    	shape.points = pe;
		return shape;
    }
    
    /**
     * Return a list of all stops for a given route id in the database
     */	
    @GET
    @Path("/stopsbyroute")
   @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object dbstopsforroute(@QueryParam("agency") String agency,@QueryParam("route") String route){
		StopList response = new StopList();
		AgencyAndId routeandid = new AgencyAndId();
		routeandid.setAgencyId(agency);
		routeandid.setId(route);
    	//StopList response = new StopList();		
		List<Stop> stops = GtfsHibernateReaderExampleMain.QueryStopsbyRoute(routeandid);
		
		for (Stop stop : stops){
			  response.stops.add(new StopType(stop, false));
		  } 
		//response.stops = stops;
		//return response; 
		//response.stops = GtfsHibernateReaderExampleMain.QueryStops(agency);
		
		return response;
    }
    
    /**
     * Generates The Agency Extended report
     */ 
    @GET
    @Path("/AgencyXR")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getAXR(@QueryParam("agency") String agency, @QueryParam("day") Integer day,@QueryParam("x") double x) throws JSONException {
    	if (Double.isNaN(x) || x <= 0) {
            x = STOP_SEARCH_RADIUS;
        }
    	x = x * 1609.34;
    	ArrayList<Integer> days = new ArrayList<Integer>();    	
    	while (day > 0) {
    	    days.add( day % 10);
    	    day = day / 10;
    	}
    	AgencyXR response = new AgencyXR();
    	response.AgencyId = agency;
    	response.AgencyName = GtfsHibernateReaderExampleMain.QueryAgencybyid(agency).getName();
    	int StopCount = GtfsHibernateReaderExampleMain.QueryStopsbyAgency(agency).size();
    	response.StopCount =  String.valueOf(StopCount);
    	List <Trip> alltrips = GtfsHibernateReaderExampleMain.QueryTripsforAgency_RouteSorted(agency);
    	Route thisroute =  alltrips.get(0).getRoute();
    	String routeId =thisroute.getId().getId();
    	double RouteMiles = 0;    	
    	double length = 0;
    	double ServiceMiles = 0;    	
        double Stopportunity = 0;
        for (Trip instance: alltrips){    		
    		int frequency = 0;
    		int stops = instance.getStopscount();    		
    		if (!routeId.equals(instance.getRoute().getId().getId())){		
    			RouteMiles += length;  	        
    			//initialize all again
    			thisroute =  instance.getRoute();
    			routeId = thisroute.getId().getId();   			
    			length = 0;    						
    		}
    		double TL = Math.max(instance.getlength(),instance.getestlength());			
    		if (TL > length) 
    			length = TL;
    		ServiceCalendar sc = GtfsHibernateReaderExampleMain.QueryCalendarforTrip(instance);
    		if (sc!=null){
    			for (int dday :days){
    				switch (dday){
    					case 1:
    						if (sc.getSaturday()==1){
    							frequency ++;											
    						}
    						break;
    					case 2:
    						if (sc.getSunday()==1){
    							frequency ++;
    						}
    						break;
    					case 3:
    						if (sc.getMonday()==1){
    							frequency ++;
    						}
    						break;
    					case 4:
    						if (sc.getTuesday()==1){
    							frequency ++;
    						}
    						break;
    					case 5:
    						if (sc.getWednesday()==1){
    							frequency ++;
    						}
    						break;
    					case 6:
    						if (sc.getThursday()==1){
    							frequency ++;
    						}
    						break;
    					case 7:
    						if (sc.getFriday()==1){
    							frequency ++;
    						}
    						break;
    				}}
    		}else {
    			//set all calendar dependent metrics to nan
    		}			
    		ServiceMiles += TL * frequency;    		
    		Stopportunity += frequency * stops;			
    	}
        response.ServiceStops = String.valueOf(Math.round(Stopportunity));
        response.ServiceMiles = String.valueOf(Math.round(ServiceMiles*100.0)/100.0); 
        response.RouteMiles = String.valueOf(Math.round(RouteMiles*100.0)/100.0);
        response.PopServed = String.valueOf(0);
        response.PopServedByService = String.valueOf(0);
        if (RouteMiles >0)
        	response.StopPerRouteMile = String.valueOf(Math.round((StopCount*10000.0)/(RouteMiles))/10000.0);
        else 
        	response.StopPerRouteMile = "NA";
    	return response;
    }
    /**
     * Generates The Routes report
     */
    @GET
    @Path("/StopsR")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getTAS(@QueryParam("agency") String agency, @QueryParam("x") double x, @QueryParam("route") String routeid) throws JSONException {
    	
    	if (Double.isNaN(x) || x <= 0) {
            x = STOP_SEARCH_RADIUS;
        }
    	x = x * 1609.34;
    	StopListR response = new StopListR();
    	response.AgencyName = GtfsHibernateReaderExampleMain.QueryAgencybyid(agency).getName();
    	if (routeid != null){    		
    		AgencyAndId route = new AgencyAndId(agency,routeid);
    		List<Stop> stops = GtfsHibernateReaderExampleMain.QueryStopsbyRoute(route);
    		for (Stop instance: stops){
    			StopR each = new StopR();
    			each.StopId = instance.getId().getId();
    			each.StopName = instance.getName();
    			each.URL = instance.getUrl();
    			each.PopWithinX = "0";
    			each.Routes = GtfsHibernateReaderExampleMain.QueryRouteIdsforStop(instance).toString();
    			response.StopR.add(each);
    		}
    	} else{
    		List<Stop> stops = GtfsHibernateReaderExampleMain.QueryStopsbyAgency(agency);
    		for (Stop instance: stops){
    			StopR each = new StopR();
    			each.StopId = instance.getId().getId();
    			each.StopName = instance.getName();
    			each.URL = instance.getUrl();
    			each.PopWithinX = "0";    			
    			each.Routes = GtfsHibernateReaderExampleMain.QueryRouteIdsforStop(instance).toString();
    			response.StopR.add(each);
    		}
    	}                        
        return response;
    }

/**
 * Generates The Agency Summary report
 */
    
@GET
@Path("/AgencySR")
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
public Object getASR(@QueryParam("x") double x) throws JSONException {
	if (Double.isNaN(x) || x <= 0) {
        x = STOP_SEARCH_RADIUS;
    }
	x = x * 1609.34;	
	AgencyList allagencies = new AgencyList();
	allagencies.agencies = GtfsHibernateReaderExampleMain.QueryAllAgencies();            
    AgencySRList response = new AgencySRList();    	
    for (Agency instance : allagencies.agencies){    	
    	AgencySR each = new AgencySR();
    	each.AgencyName = instance.getName();
    	each.AgencyId = instance.getId();
    	each.Phone = instance.getPhone();
    	each.URL = instance.getUrl();
    	each.FareURL = instance.getFareUrl();    	
		each.RoutesCount = String.valueOf(GtfsHibernateReaderExampleMain.QueryRoutesbyAgency(instance).size()) ;
        each.StopsCount = String.valueOf(GtfsHibernateReaderExampleMain.QueryStopsbyAgency(instance.getId()).size());        
        each.PopServed = "0";
        response.agencySR.add(each);
    }
return response;
}

/**
 * Generates The Routes report
 */

@GET
@Path("/RoutesR")
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
public Object getTAR(@QueryParam("agency") String agency, @QueryParam("x") double x, @QueryParam("day") Integer day) throws JSONException {
	
	if (Double.isNaN(x) || x <= 0) {
        x = STOP_SEARCH_RADIUS;
    }
	x = x * 1609.34;
	ArrayList<Integer> days = new ArrayList<Integer>();    	
	while (day > 0) {
	    days.add( day % 10);
	    day = day / 10;
	}
	List <Trip> alltrips = GtfsHibernateReaderExampleMain.QueryTripsforAgency_RouteSorted(agency);	
	RouteListR response = new RouteListR();
	response.AgencyName = GtfsHibernateReaderExampleMain.QueryAgencybyid(agency).getName()+"";
	Route thisroute =  alltrips.get(0).getRoute();
	String routeId =thisroute.getId().getId();
	RouteR each = new RouteR();
	double length = 0;
	double ServiceMiles = 0;
    double Stopportunity = 0;       
	each.RouteId = routeId+"";
	each.RouteSName = thisroute.getShortName()+"";
	each.RouteLName = thisroute.getLongName()+"";
	each.RouteDesc = thisroute.getDesc()+"";
	each.RouteType = String.valueOf(thisroute.getType());
	each.StopsCount = String.valueOf(GtfsHibernateReaderExampleMain.QueryStopsbyRoute(thisroute.getId()).size());	 
	//int index =0;
	for (Trip instance: alltrips){		
		//System.out.println(index);
		//index ++;
		
		int frequency = 0;
		int stops = instance.getStopscount();
		
		//int stops = 0;
		if (!routeId.equals(instance.getRoute().getId().getId())){		
			each.RouteLength = String.valueOf(Math.round(length*100.0)/100.0);                
	        each.ServiceMiles = String.valueOf(Math.round(ServiceMiles*100.0)/100.0); 
	        each.Stopportunity = String.valueOf(Math.round(Stopportunity));
	        each.PopStopportunity = String.valueOf(0);
	        each.PopWithinX = String.valueOf(0);
			response.RouteR.add(each);
			//initialize all again
			thisroute =  instance.getRoute();
			routeId = thisroute.getId().getId();			
			each = new RouteR();
			length = 0;
			ServiceMiles = 0;
		    Stopportunity = 0;		        
			each.RouteId = routeId+"";			
			each.RouteSName = thisroute.getShortName()+"";
			each.RouteLName = thisroute.getLongName()+"";
			each.RouteDesc = thisroute.getDesc()+" ";
			each.RouteType = String.valueOf(thisroute.getType());
			each.StopsCount = String.valueOf(GtfsHibernateReaderExampleMain.QueryStopsbyRoute(thisroute.getId()).size());
			
		}
		double TL = Math.max(instance.getlength(),instance.getestlength());			
		if (TL > length) 
			length = TL;
		ServiceCalendar sc = GtfsHibernateReaderExampleMain.QueryCalendarforTrip(instance);
		if (sc!=null){
			for (int dday :days){
				switch (dday){
					case 1:
						if (sc.getSaturday()==1){
							frequency ++;											
						}
						break;
					case 2:
						if (sc.getSunday()==1){
							frequency ++;
						}
						break;
					case 3:
						if (sc.getMonday()==1){
							frequency ++;
						}
						break;
					case 4:
						if (sc.getTuesday()==1){
							frequency ++;
						}
						break;
					case 5:
						if (sc.getWednesday()==1){
							frequency ++;
						}
						break;
					case 6:
						if (sc.getThursday()==1){
							frequency ++;
						}
						break;
					case 7:
						if (sc.getFriday()==1){
							frequency ++;
						}
						break;
				}}
		}else {
			//set all calendar dependent metrics to nan
		}			
		ServiceMiles += TL * frequency;			
		Stopportunity += frequency * stops;			
	}
	each.RouteLength = String.valueOf(Math.round(length*100.0)/100.0)+"";                
    each.ServiceMiles = String.valueOf(Math.round(ServiceMiles*100.0)/100.0)+""; 
    each.Stopportunity = String.valueOf(Math.round(Stopportunity))+"";
    each.PopStopportunity = String.valueOf(0);
    each.PopWithinX = String.valueOf(0);
	response.RouteR.add(each);	
	return response;    
}

/**
 * Generates The Routes report (an inefficient implementation)
 */
@GET
@Path("/RoutesRX")
@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
public Object getTARr(@QueryParam("agency") String agency, @QueryParam("x") double x, @QueryParam("day") Integer day) throws JSONException {
	
	if (Double.isNaN(x) || x <= 0) {
        x = STOP_SEARCH_RADIUS;
    }
	x = x * 1609.34;
	ArrayList<Integer> days = new ArrayList<Integer>();    	
	while (day > 0) {
	    days.add( day % 10);
	    day = day / 10;
	}
	List <Route> routes = GtfsHibernateReaderExampleMain.QueryRoutesbyAgency(GtfsHibernateReaderExampleMain.QueryAgencybyid(agency));
	RouteListR response = new RouteListR();
	response.AgencyName = GtfsHibernateReaderExampleMain.QueryAgencybyid(agency).getName();
	List<Trip> alltrips = GtfsHibernateReaderExampleMain.QueryTripsforAgency(agency);
	for (Route instance: routes){
		RouteR each = new RouteR();
		each.RouteId = instance.getId().getId();
		each.RouteSName = instance.getShortName();
		each.RouteLName = instance.getLongName();
		each.RouteDesc = instance.getDesc();
		each.RouteType = String.valueOf(instance.getType());
		each.StopsCount = String.valueOf(GtfsHibernateReaderExampleMain.QueryStopsbyRoute(instance.getId()).size());
		//each.StopsCount = "0";
		each.PopWithinX = String.valueOf(0);
		//List<Trip> trips = GtfsHibernateReaderExampleMain.QueryTripsbyRoute(instance);
		double length = 0;
		double ServiceMiles = 0;
	    double Stopportunity = 0;
	    //double PopStopportunity = 0;		
		for (Trip trip: alltrips){
			if (trip.getRoute().getId().getId().equals(instance.getId().getId())){
			int frequency = 0;	
			int stops = GtfsHibernateReaderExampleMain.Querystoptimebytrip(trip.getId()).size();
			double TL = Math.max(trip.getlength(),trip.getestlength());
			if (TL > length) 
				length = TL;
			ServiceCalendar sc = GtfsHibernateReaderExampleMain.QueryCalendarforTrip(trip);
			if (sc!=null){
				for (int dday :days){
					switch (dday){
						case 1:
							if (sc.getSaturday()==1){
								frequency ++;											
							}
							break;
						case 2:
							if (sc.getSunday()==1){
								frequency ++;
							}
							break;
						case 3:
							if (sc.getMonday()==1){
								frequency ++;
							}
							break;
						case 4:
							if (sc.getTuesday()==1){
								frequency ++;
							}
							break;
						case 5:
							if (sc.getWednesday()==1){
								frequency ++;
							}
							break;
						case 6:
							if (sc.getThursday()==1){
								frequency ++;
							}
							break;
						case 7:
							if (sc.getFriday()==1){
								frequency ++;
							}
							break;
					}}
			}else {
				//set all calendar dependent metrics to nan
			}			
			ServiceMiles += TL * frequency;			
			Stopportunity += frequency * stops;
			//PopStopportunity += frequency * stops* varpop;			
		}
		each.RouteLength = String.valueOf(Math.round(length*100.0)/100.0);                
        each.ServiceMiles = String.valueOf(Math.round(ServiceMiles*100.0)/100.0); 
        each.Stopportunity = String.valueOf(Math.round(Stopportunity));;
        each.PopStopportunity = String.valueOf(0);		
		response.RouteR.add(each);		
	}}
	return response;    
}

}
