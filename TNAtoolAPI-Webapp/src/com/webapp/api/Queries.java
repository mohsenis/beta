package com.webapp.api;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.QueryParam;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jettison.json.JSONException;

import com.vividsolutions.jts.geom.Coordinate;
import com.webapp.api.model.*;
import com.webapp.api.utils.PolylineEncoder;
import com.library.samples.*;
import com.library.model.*;

import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.examples.GtfsHibernateReaderExampleMain;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;


@Path("/transit")
@XmlRootElement
public class Queries {
	
	private static final double STOP_SEARCH_RADIUS = 0.1;
	static AgencyRouteList menuResponse;
	
	@GET
    @Path("/saeed")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object saeed(@QueryParam("routerId") String routerId) throws JSONException {

        return new TransitError("This is a test. You entered: "+ routerId);
    }
	
	@GET
    @Path("/censustest")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object census(@QueryParam("radius") double x,@QueryParam("agency") String agency){
		if (Double.isNaN(x) || x <= 0) {
            x = STOP_SEARCH_RADIUS;
        }
    	x = x * 1609.34;
		//List <Census> centroids = new ArrayList <Census> ();
		long pop = 0;
		List <Stop> stops = GtfsHibernateReaderExampleMain.QueryStopsbyAgency(agency);
		List <Coordinate> stopcoords = new ArrayList<Coordinate>();
		for (Stop stop: stops){
			stopcoords.add(new Coordinate(stop.getLat(),stop.getLon()));
		}
        try {
			pop =EventManager.getunduppopbatch(x, stopcoords);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        List <Census> centroids = new ArrayList <Census> ();
        try {
        	centroids =EventManager.getundupcentbatch(x, stopcoords);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        //CensusList response = new CensusList();
        long popsum=0;
        for (Census c:centroids){
        	popsum+=c.getPopulation();
        }
        return new TransitError("Unduplicated Population for agency "+agency+" is: "+ String.valueOf(pop)+" The sum based on centroid list is: "+String.valueOf(popsum));
    }
	@GET
    @Path("/poparound")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getpop(@QueryParam("radius") double x,@QueryParam("lat") double lat,@QueryParam("lon") double lon){
		if (Double.isNaN(x) || x <= 0) {
            x = STOP_SEARCH_RADIUS;
        }
    	x = x * 1609.34;
		long response = 0;
        try {
			response =EventManager.getpop(x, lat, lon);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        List <Census> centroids = new ArrayList<Census>(); 
        try {
        	centroids =EventManager.getcentroids(x, lat, lon);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
        long sum = 0;
        for (Census centroid: centroids){
        	sum+=centroid.getPopulation();
        }
        return new TransitError("Sum of Population is: "+ response+" Som of centroids is: "+sum);
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
    	if (menuResponse==null || menuResponse.data.size()!=allagencies.size() ){
    		menuResponse = new AgencyRouteList();
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
	    		menuResponse.data.add(each);
	    	}
    	}
    	return menuResponse;    	
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
     * Returns the progress value
     * @throws IOException 
     */
    @GET
    @Path("/PorgVal")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getProgVal(@QueryParam("key") double key){
    	ProgVal progress = new ProgVal();	
    	progress.progVal = getprogVal(key);
       	return progress;
    	
    }
    
    static Map <Double, Integer> progVal = new HashMap<Double, Integer>();
    public void setprogVal(double key, int val){
    	progVal.put(key, val);
    }
    public int getprogVal(double key){
    	if(progVal.get(key)==null){
    		return 0;
    	}else{
    		return progVal.get(key);
    	}
    }
    
    public int[][] daysOfWeek(String[] dates){
    	Calendar calendar = Calendar.getInstance();
    	SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
    	int[][] days = new int[2][dates.length];
    	for(int i=0; i<dates.length; i++){
    		days[0][i] = Integer.parseInt(dates[i].split("/")[2] + dates[i].split("/")[0] + dates[i].split("/")[1]);
    		try {
				calendar.setTime(sdf.parse(dates[i]));
			} catch (ParseException e) {
				e.printStackTrace();
			}
    		days[1][i] = calendar.get(Calendar.DAY_OF_WEEK);
    	}
    	return days;
    }
    
    /**
     * Generates The Agency Extended report
     */ 
    @GET
    @Path("/AgencyXR")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getAXR(@QueryParam("agency") String agency, @QueryParam("day") String date,@QueryParam("x") double x, @QueryParam("key") double key) throws JSONException {
    	if (Double.isNaN(x) || x <= 0) {
            x = STOP_SEARCH_RADIUS;
        }
    	    	
    	String[] dates = date.split(",");
    	int[][] days = daysOfWeek(dates);
    	    	
    	AgencyXR response = new AgencyXR();
    	response.AgencyId = agency;
    	response.AgencyName = GtfsHibernateReaderExampleMain.QueryAgencybyid(agency).getName();
    	int StopCount = GtfsHibernateReaderExampleMain.QueryStopsbyAgency(agency).size();
    	response.StopCount =  String.valueOf(StopCount);
    	    	
    	List <Trip> alltrips = GtfsHibernateReaderExampleMain.QueryTripsforAgency_RouteSorted(agency);
    	
    	int totalLoad = alltrips.size();
    	
    	Route thisroute =  alltrips.get(0).getRoute();
    	String routeId =thisroute.getId().getId();
    	double RouteMiles = 0;    	
    	double length = 0;
    	double ServiceMiles = 0;    	
        double Stopportunity = 0;
        
        String serviceAgency = alltrips.get(0).getServiceId().getAgencyId();
        int startDate;
        int endDate;
                     
        List <ServiceCalendar> agencyServiceCalendar = GtfsHibernateReaderExampleMain.QueryCalendarforAgency(serviceAgency);
        List <ServiceCalendarDate> agencyServiceCalendarDates = GtfsHibernateReaderExampleMain.QueryCalendarDatesforAgency(serviceAgency);
        
        int index = 0;
        for (Trip instance: alltrips){   
           	index++ ; 
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
    		    		   		
    		ServiceCalendar sc = null;
    		if(agencyServiceCalendar!=null){
    			for(ServiceCalendar scs: agencyServiceCalendar){
    				if(scs.getServiceId().getId().equals(instance.getServiceId().getId())){
    					sc = scs;
    					break;
    				}
    			}  
    		}
    		
      		List <ServiceCalendarDate> scds = new ArrayList<ServiceCalendarDate>();
    		for(ServiceCalendarDate scdss: agencyServiceCalendarDates){
    			if(scdss.getServiceId().getId().equals(instance.getServiceId().getId())){
    				scds.add(scdss);
    			}
    		}	
    		    		    		
daysLoop:   for (int i=0; i<dates.length; i++){  
				for(ServiceCalendarDate scd: scds){
					if(days[0][i]==Integer.parseInt(scd.getDate().getAsString())){
						if(scd.getExceptionType()==1){
							
							frequency ++;
						}
						continue daysLoop;
					}
				}
				if (sc!=null){
					startDate = Integer.parseInt(sc.getStartDate().getAsString());
	        		endDate = Integer.parseInt(sc.getEndDate().getAsString());
	        		if(!(days[0][i]>=startDate && days[0][i]<=endDate)){
    					continue;
    				}
	        		switch (days[1][i]){
						case 1:
							if (sc.getSunday()==1){
								frequency ++;											
							}
							break;
						case 2:
							if (sc.getMonday()==1){
								frequency ++;
							}
							break;
						case 3:
							if (sc.getTuesday()==1){
								frequency ++;
							}
							break;
						case 4:
							if (sc.getWednesday()==1){
								frequency ++;
							}
							break;
						case 5:
							if (sc.getThursday()==1){
								frequency ++;
							}
							break;
						case 6:
							if (sc.getFriday()==1){
								frequency ++;
							}
							break;
						case 7:
							if (sc.getSaturday()==1){
								frequency ++;
							}
							break;
					}
				}
			}
    		    		
    		ServiceMiles += TL * frequency;  
    		Stopportunity += frequency * stops;
    		
    		setprogVal(key, (int) Math.round(index*100/totalLoad));
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
        
        progVal.remove(key);
                
    	return response;
    }
    	
    /**
     * Generates The Routes report
     */
    @GET
    @Path("/StopsR")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getTAS(@QueryParam("agency") String agency, @QueryParam("x") double x, @QueryParam("route") String routeid, @QueryParam("key") double key) throws JSONException {
    	
    	if (Double.isNaN(x) || x <= 0) {
            x = STOP_SEARCH_RADIUS;
        }
    	x = x * 1609.34;
    	StopListR response = new StopListR();
    	response.AgencyName = GtfsHibernateReaderExampleMain.QueryAgencybyid(agency).getName();
    	int index =0;
    	if (routeid != null){    		
    		AgencyAndId route = new AgencyAndId(agency,routeid);
    		List<Stop> stops = GtfsHibernateReaderExampleMain.QueryStopsbyRoute(route);
    		List <Coordinate> points = new ArrayList <Coordinate>();
    		for (Stop s : stops){    			
    			points.add(new Coordinate(s.getLat(), s.getLon()));
    		}
    		List <Long> pops = new ArrayList<Long>();
    		try{
    			pops = EventManager.getpopbatch(x, points);
    			} catch (FactoryException e) {    				
    				e.printStackTrace();
    			} catch (TransformException e) {    				
    				e.printStackTrace();
    			} 
    		int k = 0;    		
    		int totalLoad = stops.size();
    		for (Stop instance: stops){
    			index++;
    			StopR each = new StopR();
    			each.StopId = instance.getId().getId();
    			each.StopName = instance.getName();
    			each.URL = instance.getUrl();
    			each.PopWithinX = String.valueOf(pops.get(k));
    			//try{
    			//each.PopWithinX = String.valueOf(EventManager.getpop(x, instance.getLat(), instance.getLon()));
    			//} catch (FactoryException e) {    				
    			//	e.printStackTrace();
    			//} catch (TransformException e) {    				
    			//	e.printStackTrace();
    			//}    			
    			each.Routes = GtfsHibernateReaderExampleMain.QueryRouteIdsforStop(instance).toString();
    			response.StopR.add(each);
    			k++;
    			setprogVal(key, (int) Math.round(index*100/totalLoad));
    		}
    	} else{
    		List<Stop> stops = GtfsHibernateReaderExampleMain.QueryStopsbyAgency(agency);
    		
    		int totalLoad = stops.size();
    		for (Stop instance: stops){
    			index++;
    			StopR each = new StopR();
    			each.StopId = instance.getId().getId();
    			each.StopName = instance.getName();
    			each.URL = instance.getUrl();
    			each.PopWithinX = "";
    			try{
    			each.PopWithinX = String.valueOf(EventManager.getpop(x, instance.getLat(), instance.getLon()));
    			} catch (FactoryException e) {    				
    				e.printStackTrace();
    			} catch (TransformException e) {    				
    				e.printStackTrace();
    			}
    			each.Routes = GtfsHibernateReaderExampleMain.QueryRouteIdsforStop(instance).toString();
    			response.StopR.add(each);
    			setprogVal(key, (int) Math.round(index*100/totalLoad));
    		}
    	}           
    	progVal.remove(key);
        return response;
    }

	/**
	 * Generates The Agency Summary report
	 */
	    
	@GET
	@Path("/AgencySR")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	public Object getASR(@QueryParam("x") double x, @QueryParam("key") double key) throws JSONException {
		if (Double.isNaN(x) || x <= 0) {
	        x = STOP_SEARCH_RADIUS;
	    }
		x = x * 1609.34;	
		AgencyList allagencies = new AgencyList();
		allagencies.agencies = GtfsHibernateReaderExampleMain.QueryAllAgencies();            
	    AgencySRList response = new AgencySRList();    
	    int index =0;
		int totalLoad = allagencies.agencies.size();
	    for (Agency instance : allagencies.agencies){   
	    	index++;
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
	        setprogVal(key, (int) Math.round(index*100/totalLoad));
	    }
	    progVal.remove(key);
	    return response;
	}

	/**
	 * Generates The Routes report
	 */
	
	@GET
	@Path("/RoutesR")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	public Object getTAR(@QueryParam("agency") String agency, @QueryParam("x") double x, @QueryParam("day") String date, @QueryParam("key") double key) throws JSONException {
		if (Double.isNaN(x) || x <= 0) {
	        x = STOP_SEARCH_RADIUS;
	    }
		x = x * 1609.34;
		String[] dates = date.split(",");
		int[][] days = daysOfWeek(dates);
		
		List <Trip> alltrips = GtfsHibernateReaderExampleMain.QueryTripsforAgency_RouteSorted(agency);	
		RouteListR response = new RouteListR();
		response.AgencyName = GtfsHibernateReaderExampleMain.QueryAgencybyid(agency).getName();
		Route thisroute =  alltrips.get(0).getRoute();
		String routeId =thisroute.getId().getId();
		RouteR each = new RouteR();
		double length = 0;
		double ServiceMiles = 0;
	    double Stopportunity = 0;       
		each.RouteId = routeId+"";
		each.RouteSName = thisroute.getShortName();
		each.RouteLName = thisroute.getLongName();
		each.RouteDesc = thisroute.getDesc();
		each.RouteType = String.valueOf(thisroute.getType());
		each.StopsCount = String.valueOf(GtfsHibernateReaderExampleMain.QueryStopsbyRoute(thisroute.getId()).size());	 
		int index =0;
		int totalLoad = alltrips.size();
		
		String serviceAgency = alltrips.get(0).getServiceId().getAgencyId();
	    int startDate;
	    int endDate;
		List <ServiceCalendar> agencyServiceCalendar = GtfsHibernateReaderExampleMain.QueryCalendarforAgency(serviceAgency);
	    List <ServiceCalendarDate> agencyServiceCalendarDates = GtfsHibernateReaderExampleMain.QueryCalendarDatesforAgency(serviceAgency);
	    
		for (Trip instance: alltrips){
			index ++;
			
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
				each.RouteSName = thisroute.getShortName();
				each.RouteLName = thisroute.getLongName();
				each.RouteDesc = thisroute.getDesc();
				each.RouteType = String.valueOf(thisroute.getType());
				each.StopsCount = String.valueOf(GtfsHibernateReaderExampleMain.QueryStopsbyRoute(thisroute.getId()).size());
				
			}
			double TL = Math.max(instance.getlength(),instance.getestlength());			

			if (TL > length) 
				length = TL;
			
			ServiceCalendar sc = null;
			if(agencyServiceCalendar!=null){
				for(ServiceCalendar scs: agencyServiceCalendar){
					if(scs.getServiceId().getId().equals(instance.getServiceId().getId())){
						sc = scs;
						break;
					}
				}  
			}
			List <ServiceCalendarDate> scds = new ArrayList<ServiceCalendarDate>();
			for(ServiceCalendarDate scdss: agencyServiceCalendarDates){
				if(scdss.getServiceId().getId().equals(instance.getServiceId().getId())){
					scds.add(scdss);
				}
			}
		
daysLoop:   for (int i=0; i<dates.length; i++){  
				for(ServiceCalendarDate scd: scds){
					if(days[0][i]==Integer.parseInt(scd.getDate().getAsString())){
						if(scd.getExceptionType()==1){
							
							frequency ++;
						}
						continue daysLoop;
					}
				}
				if (sc!=null){
					startDate = Integer.parseInt(sc.getStartDate().getAsString());
	        		endDate = Integer.parseInt(sc.getEndDate().getAsString());
	        		if(!(days[0][i]>=startDate && days[0][i]<=endDate)){
						continue;
					}
	        		switch (days[1][i]){
						case 1:
							if (sc.getSunday()==1){
								frequency ++;											
							}
							break;
						case 2:
							if (sc.getMonday()==1){
								frequency ++;
							}
							break;
						case 3:
							if (sc.getTuesday()==1){
								frequency ++;
							}
							break;
						case 4:
							if (sc.getWednesday()==1){
								frequency ++;
							}
							break;
						case 5:
							if (sc.getThursday()==1){
								frequency ++;
							}
							break;
						case 6:
							if (sc.getFriday()==1){
								frequency ++;
							}
							break;
						case 7:
							if (sc.getSaturday()==1){
								frequency ++;
							}
							break;
					}
				}
			}
		    		
			ServiceMiles += TL * frequency;  
			Stopportunity += frequency * stops;
			
			setprogVal(key, (int) Math.round(index*100/totalLoad));
		}
		each.RouteLength = String.valueOf(Math.round(length*100.0)/100.0)+"";                
	    each.ServiceMiles = String.valueOf(Math.round(ServiceMiles*100.0)/100.0)+""; 
	    each.Stopportunity = String.valueOf(Math.round(Stopportunity))+"";
	    each.PopStopportunity = String.valueOf(0);
	    each.PopWithinX = String.valueOf(0);
		response.RouteR.add(each);	
		progVal.remove(key);
		return response;    
	}

}
