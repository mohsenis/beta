package com.webapp.api;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
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
import org.onebusaway.gtfs.model.FareRule;
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
	
	/*static final int CENSUS_LENGTH = 196621;
	static List<Census> censusArray = new ArrayList<Census>();
	
/*	public void fillArray(){
		if(censusArray.size()==0){
			CSVReader reader;
			Census c; 
			String [] nextLine;
			//int i =0;
			try {
				reader = new CSVReader(new FileReader("C:\\Users\\Administrator\\OR_2010_Block_Data\\OR_2010_Block_Data.txt"));
				reader.readNext();
				while ((nextLine = reader.readNext()) != null) {
					c = new Census();
					c.setId(nextLine[8]);
					c.setLatitude(Double.parseDouble(nextLine[12]));
					c.setLongitude(Double.parseDouble(nextLine[13]));
					c.setPopulation(Integer.parseInt(nextLine[16]));
					censusArray[i][0]= Double.parseDouble(nextLine[8]);
					censusArray[i][1]= Double.parseDouble(nextLine[12]);
					censusArray[i][2]= Double.parseDouble(nextLine[13]);
					censusArray[i][3]= Double.parseDouble(nextLine[16]);
					i++;
					censusArray.add(c);
			    }
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println(censusArray.get(1).getId());
			System.out.println(censusArray.get(2).getId());
			System.out.println(censusArray.get(3).getId());
			System.out.println(censusArray.get(4).getId());
		}
		
	}*/
	
	/*@GET
=======
/*	@GET
>>>>>>> refs/remotes/origin/master
    @Path("/NearBlocks")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getNearBlocks(@QueryParam("lat") String lat,@QueryParam("x") String x, @QueryParam("lon") String lon) throws JSONException {
		fillArray();
		CensusList response = new CensusList();
		double r = Double.parseDouble(x);
		double latitude = Double.parseDouble(lat);
		double longitude = Double.parseDouble(lon);
		Centroid c;
		int pop=0;
		//Census C;
		for(Census C: censusArray){
			if(ddistance(latitude,longitude,C.getLatitude(),C.getLongitude())<=r){
				c = new Centroid();
				c.setcentroid(C);
				response.centroids.add(c);
				pop+= C.getPopulation();
			}
		}
		System.out.println(pop);
		return response;
	}*/
	

	/*private double ddistance(double lat1, double lon1, double lat2, double lon2) {
=======
	@GET
    @Path("/NearBlocks")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getNearBlocks(@QueryParam("lat") double lat,@QueryParam("x") double x, @QueryParam("lon") double lon) throws JSONException {
		
		if (Double.isNaN(x) || x <= 0) {
            x = STOP_SEARCH_RADIUS;
        }
		x = x * 1609.34;
		List <Census> centroids = new ArrayList<Census> ();
        try {
			centroids =EventManager.getcentroids(x, lat, lon);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        CensusList response = new CensusList();		
		//Census C;
        for (Census c : centroids){
        	Centroid cntr = new Centroid();
        	cntr.setcentroid(c);
        	response.centroids.add(cntr);        	
        }
		return response;
	}
/*	private double ddistance(double lat1, double lon1, double lat2, double lon2) {
>>>>>>> refs/remotes/origin/master
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        if (dist>1) dist =1;
        if (dist<-1) dist = -1;
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = dist * 60 * 1.1515;      
        return (dist);
      }*/
    
    /*private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
      }
    
    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }*/

	
	@GET
    @Path("/NearBlocks")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getNearBlocks(@QueryParam("lat") double lat,@QueryParam("x") double x, @QueryParam("lon") double lon) throws JSONException {
        
        if (Double.isNaN(x) || x <= 0) {
            x = STOP_SEARCH_RADIUS;
        }
        x = x * 1609.34;
        List <Census> centroids = new ArrayList<Census> ();
        try {
            centroids =EventManager.getcentroids(x, lat, lon);
        } catch (FactoryException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        CensusList response = new CensusList();        
        //Census C;
        for (Census c : centroids){
            Centroid cntr = new Centroid();
            cntr.setcentroid(c);
            response.centroids.add(cntr);            
        }
        return response;
    }

	
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
	                		String shape = trip.getEpshape();
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
    	shape.points = tp.getEpshape();
    	shape.length = tp.getLength();    	    	 
    	shape.estlength = tp.getEstlength();
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
    		double TL = Math.max(instance.getLength(),instance.getEstlength());			
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
        
        try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
        
        progVal.remove(key);
                
    	return response;
    }
    	
    /**
     * Generates The Stops report
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
    			each.PopWithinX ="";
    			/*each.PopWithinX = String.valueOf(pops.get(k));
    			try{
    				each.PopWithinX = String.valueOf(EventManager.getpop(x, instance.getLat(), instance.getLon()));
    			} catch (FactoryException e) {    				
    				e.printStackTrace();
    			} catch (TransformException e) {    				
    				e.printStackTrace();
    			}    			
    			each.Routes = GtfsHibernateReaderExampleMain.QueryRouteIdsforStop(instance).toString();*/
    			each.Routes = "";
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
    			/*try{
    			each.PopWithinX = String.valueOf(EventManager.getpop(x, instance.getLat(), instance.getLon()));
    			} catch (FactoryException e) {    				
    				e.printStackTrace();
    			} catch (TransformException e) {    				
    				e.printStackTrace();
    			}*/
    			//each.Routes = GtfsHibernateReaderExampleMain.QueryRouteIdsforStop(instance).toString();
    			each.Routes = "";
    			response.StopR.add(each);
    			setprogVal(key, (int) Math.round(index*100/totalLoad));
    		}
    	}           
    	
    	try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	
    	progVal.remove(key);
        return response;
    }

    @GET
    @Path("/StopsRX")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getTASX(@QueryParam("agency") String agency, @QueryParam("x") double x, @QueryParam("key") double key, @QueryParam("stopids") String stops) throws JSONException {
    	
    	if (Double.isNaN(x) || x <= 0) {
            x = STOP_SEARCH_RADIUS;
        }
    	x = x * 1609.34;
    	
    	String[] stopIds = stops.split(",");
    	StopListR response = new StopListR();
    	response.AgencyName = "";
    	for (String instance: stopIds){
    		AgencyAndId stopId = new AgencyAndId(agency,instance);
    		Stop stop = GtfsHibernateReaderExampleMain.QueryStopbyid(stopId);
    		
	    	StopR each = new StopR();
			each.StopId = "";
			each.StopName = "";
			each.URL = "";
			each.PopWithinX = "";
			try{
			each.PopWithinX = String.valueOf(EventManager.getpop(x, stop.getLat(), stop.getLon()));
			} catch (FactoryException e) {    				
				e.printStackTrace();
			} catch (TransformException e) {    				
				e.printStackTrace();
			}
			each.Routes = GtfsHibernateReaderExampleMain.QueryRouteIdsforStop(stop).toString();
			response.StopR.add(each);
    	}
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
	    	//changed/////////////////////////////////////////////////////////////////////////////////
	    	List<Route> routes = GtfsHibernateReaderExampleMain.QueryRoutesbyAgency(instance);
	    	each.RoutesCount = String.valueOf(routes.size());
	    	float sumFare=0; 
	    	List<Float> fares = new ArrayList<Float>();
	    	for(Route route: routes){
	    		List <FareRule> fareRules = GtfsHibernateReaderExampleMain.QueryFareRuleByRoute(route);
	    		if(fareRules.size()!=0){
	    			sumFare+=fareRules.get(0).getFare().getPrice();
	    			fares.add(fareRules.get(0).getFare().getPrice());
	    		}
	    	}
	    	Collections.sort(fares);
	    	if (fares.size()>0){
	    		each.AverageFare = String.valueOf(sumFare/fares.size());
	    		each.MedianFare = String.valueOf(fares.get((int)Math.floor(fares.size()/2)));
	    	} else {
	    		each.AverageFare = "NA";
		    	each.MedianFare =  "NA";
	    	}
	    	////////////////////////////////////////////////////////////////////////////////////////////
			//each.RoutesCount = String.valueOf(GtfsHibernateReaderExampleMain.QueryRoutesbyAgency(instance).size()) ;
	        each.StopsCount = String.valueOf(GtfsHibernateReaderExampleMain.QueryStopsbyAgency(instance.getId()).size());        
	        each.PopServed = "0";
	        response.agencySR.add(each);
	        setprogVal(key, (int) Math.round(index*100/totalLoad));
	    }
	    try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
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
				each.RouteSName = thisroute.getShortName()+"";
				each.RouteLName = thisroute.getLongName()+"";
				each.RouteDesc = thisroute.getDesc()+"";
				each.RouteType = String.valueOf(thisroute.getType());
				each.StopsCount = String.valueOf(GtfsHibernateReaderExampleMain.QueryStopsbyRoute(thisroute.getId()).size());
				
			}
			double TL = Math.max(instance.getLength(),instance.getEstlength());			


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
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		progVal.remove(key);
		return response;    
	}
	
	/**
     * Generates The Route Schedule/Fare report
     */
    @GET
    @Path("/ScheduleR")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getSchedule(@QueryParam("agency") String agency, @QueryParam("route") String routeid, @QueryParam("day") String date, @QueryParam("key") double key) throws JSONException {
    	
    	ScheduleList response = new ScheduleList();
    	String[] dates = date.split(",");
		int[][] days = daysOfWeek(dates);
		//System.out.println(days[0][0]);
    	AgencyAndId routeId = new AgencyAndId(agency,routeid);
    	response.Agency = GtfsHibernateReaderExampleMain.QueryAgencybyid(agency).getName()+"";
    	Route route = GtfsHibernateReaderExampleMain.QueryRoutebyid(routeId);
    	response.Route = route.getId().getId()+"";
    	List <FareRule> fareRules = GtfsHibernateReaderExampleMain.QueryFareRuleByRoute(route);
    	if(fareRules.size()==0){
    		response.Fare = "N/A";
    	}else{
    		response.Fare = fareRules.get(0).getFare().getPrice()+"";
    	}
    	List <Trip> routeTrips = GtfsHibernateReaderExampleMain.QueryTripsbyRoute(route);
    	int totalLoad = routeTrips.size();
    	/*Schedule[] schedules = new Schedule[2]; 
    	schedules[0] = new Schedule();
    	schedules[1] = new Schedule();*/
    	response.directions[0]= new Schedule();
    	response.directions[1]= new Schedule();
    	Stoptime stoptime;
    	int[] maxSize={0,0};
    	int index =0;
    	String serviceAgency = routeTrips.get(0).getServiceId().getAgencyId();
	    int startDate;
	    int endDate;
		List <ServiceCalendar> agencyServiceCalendar = GtfsHibernateReaderExampleMain.QueryCalendarforAgency(serviceAgency);
	    List <ServiceCalendarDate> agencyServiceCalendarDates = GtfsHibernateReaderExampleMain.QueryCalendarDatesforAgency(serviceAgency);
Loop:  	for (Trip trip: routeTrips){
    		index++;
    		boolean isIn = false;
    		ServiceCalendar sc = null;
			if(agencyServiceCalendar!=null){
				for(ServiceCalendar scs: agencyServiceCalendar){
					if(scs.getServiceId().getId().equals(trip.getServiceId().getId())){
						sc = scs;
						break;
					}
				}  
			}
			List <ServiceCalendarDate> scds = new ArrayList<ServiceCalendarDate>();
			for(ServiceCalendarDate scdss: agencyServiceCalendarDates){
				if(scdss.getServiceId().getId().equals(trip.getServiceId().getId())){
					scds.add(scdss);
				}
			}
			
			for(ServiceCalendarDate scd: scds){
				if(days[0][0]==Integer.parseInt(scd.getDate().getAsString())){
					if(scd.getExceptionType()==1){
						isIn = true;
						break;
					}
					continue Loop;
				}
			}
			if (sc!=null && !isIn){
				startDate = Integer.parseInt(sc.getStartDate().getAsString());
        		endDate = Integer.parseInt(sc.getEndDate().getAsString());
        		if(!(days[0][0]>=startDate && days[0][0]<=endDate)){
					continue;
				}
        		switch (days[1][0]){
					case 1:
						if (sc.getSunday()==1){
							isIn = true;											
						}
						break;
					case 2:
						if (sc.getMonday()==1){
							isIn = true;
						}
						break;
					case 3:
						if (sc.getTuesday()==1){
							isIn = true;
						}
						break;
					case 4:
						if (sc.getWednesday()==1){
							isIn = true;
						}
						break;
					case 5:
						if (sc.getThursday()==1){
							isIn = true;
						}
						break;
					case 6:
						if (sc.getFriday()==1){
							isIn = true;
						}
						break;
					case 7:
						if (sc.getSaturday()==1){
							isIn = true;
						}
						break;
				}
			}
			if(isIn){
				//System.out.println("yes");
	    		AgencyAndId agencyandtrip = trip.getId();
	    		List <StopTime> stopTimes = GtfsHibernateReaderExampleMain.Querystoptimebytrip(agencyandtrip);
	    		TripSchedule ts = new TripSchedule();
	    		for (StopTime st: stopTimes){
	    			if(st.isArrivalTimeSet()){
	    				stoptime = new Stoptime();
	    				stoptime.StopTime = strArrivalTime(st.getArrivalTime());
	    				stoptime.StopName = st.getStop().getName()+"";
	    				stoptime.StopId = st.getStop().getId().getId();
	    				ts.stoptimes.add(stoptime);
	    			}
	    		}
	    		if(trip.getDirectionId()!=null && trip.getDirectionId().equals("1")){
		    		if(ts.stoptimes.size()>maxSize[1]){
		    			response.directions[1].stops = ts.stoptimes;
		    			//System.out.println(response.stops.get(0).StopId);
		    			maxSize[1]=ts.stoptimes.size();
		    		}
		    		response.directions[1].schedules.add(ts);
				}else{
					if(ts.stoptimes.size()>maxSize[0]){
						//System.out.println(ts.stoptimes.size());
						response.directions[0].stops = ts.stoptimes;
		    			//System.out.println(response.stops.get(0).StopId);
		    			maxSize[0]=ts.stoptimes.size();
		    		}
					response.directions[0].schedules.add(ts);
				}
			}
			setprogVal(key, (int) Math.round(index*100/totalLoad));
    	}
	    
	    try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	    
    	progVal.remove(key);
        return response;
    }
    
    public String strArrivalTime(int arrivalTime){
    	int hour = arrivalTime/3600;
    	int minute = (arrivalTime % 3600)/60;
    	String arrivalTimeSTR = zeroStartValue(hour)+":"+zeroStartValue(minute);
    	
    	return arrivalTimeSTR;
    }

    public String zeroStartValue(int value){
    	if(value<10){
    		return "0"+value;
    	}else{
    		return ""+value;
    	}
    }

    /**
	 * Generates The counties Summary report
	 */
	    
	@GET
	@Path("/GeoCSR")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	public Object getGCSR(@QueryParam("key") double key, @QueryParam("type") String type ) throws JSONException {
		List<County> allcounties = new ArrayList<County> ();
		try {
			allcounties = EventManager.getcounties();
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransformException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		GeoRList response = new GeoRList();
		response.type = "County";
	    int index =0;
		int totalLoad = allcounties.size();
	    for (County instance : allcounties){   
	    	index++;
	    	GeoR each = new GeoR();
	    	each.Name = instance.getName();
	    	each.id = instance.getCountyId();	    	
	    	each.ODOTRegion = instance.getRegionId();
	    	each.ODOTRegionName = instance.getRegionName();
	    	each.waterArea = String.valueOf(Math.round(instance.getWaterarea()/2.58999e4)/100.0);
	    	each.landArea = String.valueOf(Math.round(instance.getLandarea()/2.58999e4)/100.0);
	    	each.population = String.valueOf(instance.getPopulation());
	    	each.RoutesCount = String.valueOf(0);
	    	each.TractsCount = "0";
	    	try {
	    		each.TractsCount = String.valueOf(EventManager.gettractscountbycounty(instance.getCountyId()));
			} catch (FactoryException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (TransformException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}	    		    	
	    	each.AverageFare = "0";
	    	each.MedianFare = "0";
	    	/*float sumFare=0; 
	    	List<Float> fares = new ArrayList<Float>();
	    	for(Route route: routes){
	    		List <FareRule> fareRules = GtfsHibernateReaderExampleMain.QueryFareRuleByRoute(route);
	    		if(fareRules.size()!=0){
	    			sumFare+=fareRules.get(0).getFare().getPrice();
	    			fares.add(fareRules.get(0).getFare().getPrice());
	    		}
	    	}
	    	Collections.sort(fares);
	    	if (fares.size()>0){
	    		each.AverageFare = String.valueOf(sumFare/fares.size());
	    		each.MedianFare = String.valueOf(fares.get((int)Math.floor(fares.size()/2)));
	    	} else {
	    		each.AverageFare = "NA";
		    	each.MedianFare =  "NA";
	    	}*/
	    	each.StopsCount = String.valueOf(0);
	    	try {
	    		each.StopsCount = String.valueOf(EventManager.getstopscountbycounty(instance.getCountyId()));
			} catch (FactoryException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (TransformException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}	
	    	each.RoutesCount = String.valueOf(0);
	    	try {
	    		each.RoutesCount = String.valueOf(EventManager.getroutescountsbycounty(instance.getCountyId()));
			} catch (FactoryException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (TransformException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	        response.GeoR.add(each);
	        setprogVal(key, (int) Math.round(index*100/totalLoad));
	    }
	    try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	    progVal.remove(key);
	    return response;
	}
    /**
	 * Generates The Tracts (by county) Summary report
	 */
	    
	@GET
	@Path("/GeoCTSR")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	public Object getGCTSR(@QueryParam("county") String county, @QueryParam("key") double key, @QueryParam("type") String type ) throws JSONException {
		List<Tract> alltracts = new ArrayList<Tract> ();
		try {
			alltracts = EventManager.gettractsbycounty(county);
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransformException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		GeoRList response = new GeoRList();
		response.type = "Tract";
	    int index =0;
		int totalLoad = alltracts.size();
	    for (Tract instance : alltracts){   
	    	index++;
	    	GeoR each = new GeoR();	    	
	    	each.id = instance.getTractId();	    	
	    	each.waterArea = String.valueOf(Math.round(instance.getWaterarea()/2.58999e4)/100.0);
	    	each.landArea = String.valueOf(Math.round(instance.getLandarea()/2.58999e4)/100.0);
	    	each.population = String.valueOf(instance.getPopulation());
	    	each.RoutesCount = String.valueOf(0);
	    	each.BlocksCount = "0";
	    	try {
	    		each.BlocksCount = String.valueOf(EventManager.getblockscountbytract(instance.getTractId()));
			} catch (FactoryException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (TransformException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}	    		    	
	    	each.AverageFare = "0";
	    	each.MedianFare = "0";
	    	/*float sumFare=0; 
	    	List<Float> fares = new ArrayList<Float>();
	    	for(Route route: routes){
	    		List <FareRule> fareRules = GtfsHibernateReaderExampleMain.QueryFareRuleByRoute(route);
	    		if(fareRules.size()!=0){
	    			sumFare+=fareRules.get(0).getFare().getPrice();
	    			fares.add(fareRules.get(0).getFare().getPrice());
	    		}
	    	}
	    	Collections.sort(fares);
	    	if (fares.size()>0){
	    		each.AverageFare = String.valueOf(sumFare/fares.size());
	    		each.MedianFare = String.valueOf(fares.get((int)Math.floor(fares.size()/2)));
	    	} else {
	    		each.AverageFare = "NA";
		    	each.MedianFare =  "NA";
	    	}*/
	    	each.StopsCount = String.valueOf(0);
	    	try {
	    		each.StopsCount = String.valueOf(EventManager.getstopscountbytract(instance.getTractId()));
			} catch (FactoryException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (TransformException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}	
	    	each.RoutesCount = String.valueOf(0);
	    	try {
	    		each.RoutesCount = String.valueOf(EventManager.getroutescountsbytract(instance.getTractId()));
			} catch (FactoryException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (TransformException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	        response.GeoR.add(each);
	        setprogVal(key, (int) Math.round(index*100/totalLoad));
	    }
	    try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	    progVal.remove(key);
	    return response;
	}
    /**
	 * Generates The Census Places Summary report
	 */
	    
	@GET
	@Path("/GeoCPSR")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	public Object getGCPSR(@QueryParam("key") double key, @QueryParam("type") String type ) throws JSONException {
		List<Place> allplaces = new ArrayList<Place> ();
		try {
			allplaces = EventManager.getplaces();
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransformException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		GeoRList response = new GeoRList();
		response.type = "Place";
	    int index =0;
		int totalLoad = allplaces.size();
	    for (Place instance : allplaces){   
	    	index++;
	    	GeoR each = new GeoR();
	    	each.Name = instance.getName();
	    	each.id = instance.getPlaceId();	    	
	    	each.waterArea = String.valueOf(Math.round(instance.getWaterarea()/2.58999e4)/100.0);
	    	each.landArea = String.valueOf(Math.round(instance.getLandarea()/2.58999e4)/100.0);
	    	each.population = String.valueOf(instance.getPopulation());
	    	each.RoutesCount = String.valueOf(0);	    		    		    	
	    	each.AverageFare = "0";
	    	each.MedianFare = "0";
	    	/*float sumFare=0; 
	    	List<Float> fares = new ArrayList<Float>();
	    	for(Route route: routes){
	    		List <FareRule> fareRules = GtfsHibernateReaderExampleMain.QueryFareRuleByRoute(route);
	    		if(fareRules.size()!=0){
	    			sumFare+=fareRules.get(0).getFare().getPrice();
	    			fares.add(fareRules.get(0).getFare().getPrice());
	    		}
	    	}
	    	Collections.sort(fares);
	    	if (fares.size()>0){
	    		each.AverageFare = String.valueOf(sumFare/fares.size());
	    		each.MedianFare = String.valueOf(fares.get((int)Math.floor(fares.size()/2)));
	    	} else {
	    		each.AverageFare = "NA";
		    	each.MedianFare =  "NA";
	    	}*/
	    	each.StopsCount = String.valueOf(0);
	    	try {
	    		each.StopsCount = String.valueOf(EventManager.getstopscountbyplace(instance.getPlaceId()));
			} catch (FactoryException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (TransformException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}	
	    	each.RoutesCount = String.valueOf(0);
	    	try {
	    		each.RoutesCount = String.valueOf(EventManager.getroutescountsbyplace(instance.getPlaceId()));
			} catch (FactoryException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (TransformException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	        response.GeoR.add(each);
	        setprogVal(key, (int) Math.round(index*100/totalLoad));
	    }
	    try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	    progVal.remove(key);
	    return response;
	} 
    
}
