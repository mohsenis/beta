package com.webapp.api;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.Time;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeSet;

import javax.swing.JOptionPane;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.QueryParam;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jettison.json.JSONException;

import com.vividsolutions.jts.geom.Coordinate;
import com.webapp.api.model.*;
import com.webapp.api.utils.PolylineEncoder;
import com.webapp.api.utils.StringUtils;
import com.webapp.modifiers.DbUpdate;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.library.samples.*;
import com.library.util.Types;
import com.library.model.*;

import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.FareRule;
import org.onebusaway.gtfs.model.FeedInfo;
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
import org.onebusaway.gtfs.impl.Databases;


@Path("/transit")
@XmlRootElement
public class Queries {
	
	private static final double STOP_SEARCH_RADIUS = 0.1;
	private static final int LEVEL_OF_SERVICE = 2;
	private static final int default_dbindex = Databases.defaultDBIndex;
	static AgencyRouteList[] menuResponse = new AgencyRouteList[Databases.dbsize];
	static int dbsize = Databases.dbsize;	
	
	@GET
    @Path("/DBList")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getDBList() throws JSONException {
		
				
        String[] DBList = Databases.dbnames;
        DBList response = new DBList();
        
        for (int s=0; s<dbsize; s++){        	
        	response.DBelement.add(DBList[s]);
        }
        
        return response;
    }
	
	  /**
     * Lists stops within a certain distance of a 
     * given stop while filtering the agencies.
     * Used in Connected agencies on-map report.
     * 
     */
    @GET
    @Path("/castops")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getCAS(@QueryParam("lat") double lat, @QueryParam("lon") double lon, @QueryParam ("agencies") String agencies,
 		   @QueryParam("radius") Integer gap, @QueryParam("dbindex") Integer dbindex) throws JSONException {
    	if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        }
    	double temp = gap / 3.28084;
    	gap = (int) temp;
    	CAStopsList results = PgisEventManager.getConnectedStops(lat, lon, gap, agencies, dbindex);
    	try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
 	   return results;	   
    }
    
    
	/** Generates Counties P&R Report*/
	@GET
	@Path("/CountiesPnR")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	public Object countiesPnR(@QueryParam("key") double key, @QueryParam("dbindex") Integer dbindex ) throws JSONException {
		if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        }
		ParknRideCountiesList response = new ParknRideCountiesList();
		response = PgisEventManager.getCountiesPnrs(dbindex);
		
		response.metadata = "Report Type:Park&Ride Summary Report;Report Date:"+new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime())+";"+
    	    	"Selected Database:" +Databases.dbnames[dbindex];
			    
	    setprogVal(key, 0);
	    
	    try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	    progVal.remove(key);
	    return response;
	}
	
	/** Generates P&R Report for a given county*/
	@GET
	@Path("/pnrsInCounty")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	public Object pnrsInCounty(@QueryParam("key") double key, @QueryParam("countyId") String countyId, 
			@QueryParam("radius") String radius, @QueryParam("dbindex") Integer dbindex,
			@QueryParam("username") String username) throws JSONException {
		if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        }
		PnrInCountyList response = new PnrInCountyList();
		
		int tmpRadius = (int) (Integer.parseInt(radius) / 3.2804);
		response = PgisEventManager.getPnrsInCounty(Integer.parseInt(countyId), tmpRadius, dbindex, username);
		
		response.metadata = "Report Type:Park&Ride Summary Report;Report Date:"+new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime())+";"+
    	    	"Selected Database:" +Databases.dbnames[dbindex];
			    
	    setprogVal(key, 0);
	    
	    try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	    progVal.remove(key);
	    return response;
	}
	
	@GET
    @Path("/NearBlocks")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getNearBlocks(@QueryParam("lat") double lat,@QueryParam("x") double x, @QueryParam("lon") double lon, @QueryParam("dbindex") Integer dbindex ) throws JSONException {
        
        if (Double.isNaN(x) || x <= 0) {
            x = STOP_SEARCH_RADIUS;
        }
        if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        }        	
        x = x * 1609.34;
        List <Census> centroids = new ArrayList<Census> ();
        try {
            centroids =EventManager.getcentroids(x, lat, lon, dbindex);
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
    @Path("/poparound")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getpop(@QueryParam("radius") double x,@QueryParam("lat") double lat,@QueryParam("lon") double lon, @QueryParam("dbindex") Integer dbindex){
		if (Double.isNaN(x) || x <= 0) {
            x = STOP_SEARCH_RADIUS;
        }
		if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        } 
    	x = x * 1609.34;
		long response = 0;
        try {
			response =EventManager.getpop(x, lat, lon, dbindex);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        List <Census> centroids = new ArrayList<Census>(); 
        try {
        	centroids =EventManager.getcentroids(x, lat, lon, dbindex);
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
    * Generates The on map report
    *  
    */
   @GET
   @Path("/onmapreport")
   @Produces({ MediaType.APPLICATION_JSON , MediaType.APPLICATION_XML, MediaType.TEXT_XML})
   public Object getOnMapReport(@QueryParam("lat") String lats,@QueryParam("lon") String lons, @QueryParam("day") String date, @QueryParam("x") double x, @QueryParam("dbindex") Integer dbindex, @QueryParam("username") String username) throws JSONException { 
   	if (Double.isNaN(x) || x <= 0) {
           x = 0;
       }
   	//x = Math.round(x*100.00)/100.00;
   	if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
       	dbindex = default_dbindex;
       } 
   	//final int sdbindex = dbindex;
//   	x = x * 1609.34;
   	String[] latss = lats.split(",");
   	double[] lat = new double[latss.length];
   	int ind = 0;
   	for(String la: latss){
   		lat[ind]=Double.parseDouble(la);
   		ind++;
   	}
   	String[] lonss = lons.split(",");
   	double[] lon = new double[lonss.length];
   	ind = 0;
   	for(String ln: lonss){
   		lon[ind]=Double.parseDouble(ln);
   		ind++;
   	}
   	//String username = "admin";
   	String[] dates = date.split(",");
   	String[][] datedays = daysOfWeekString(dates);
   	String[] fulldates = datedays[0];
   	String[] days = datedays[1];	   	
   	MapDisplay response = new MapDisplay();
   	MapTransit stops = PgisEventManager.onMapStops(fulldates,days,username, x, lat, lon, dbindex);
   	MapGeo blocks = PgisEventManager.onMapBlocks(x, lat, lon, dbindex);
   	response.MapTr = stops;
   	response.MapG = blocks;
   	MapPnR pnr=new MapPnR();
   	List<ParknRide> PnRs=new ArrayList<ParknRide>();
	try {
		if (lat.length==1){
			PnRs=EventManager.getPnRs(x, lat[0], lon[0], dbindex);
		}else{
			PnRs=EventManager.getPnRs(lat, lon, dbindex);
	   	}		
	} catch (FactoryException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (TransformException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	Map<String,List<MapPnrRecord>> mapPnr= new HashMap<String, List<MapPnrRecord>>(); 
	List<MapPnrCounty> mapPnrCounties=new ArrayList<MapPnrCounty>();
	MapPnrRecord mapPnrRecord;
	MapPnrCounty mapPnrCounty;
	for (ParknRide p:PnRs){
		mapPnrRecord=new MapPnrRecord();
		mapPnrRecord.countyId=p.getCountyid();
		mapPnrRecord.countyName=p.getCounty();
		mapPnrRecord.id=p.getPnrid()+"";
		mapPnrRecord.lat=p.getLat()+"";
		mapPnrRecord.lon=p.getLon()+"";
		mapPnrRecord.lotName=p.getLotname();
		mapPnrRecord.spaces=p.getSpaces()+"";
		mapPnrRecord.transitSerives=p.getTransitservice();
		mapPnrRecord.availability=p.getAvailability();
		
		
		if (!mapPnr.containsKey(p.getCountyid())){
			mapPnr.put(p.getCountyid(), new ArrayList<MapPnrRecord>());
			mapPnr.get(p.getCountyid()).add(mapPnrRecord);
			mapPnrCounty=new MapPnrCounty();
			mapPnrCounty.countyId=p.getCountyid();
			mapPnrCounty.countyName=p.getCounty();
			mapPnrCounties.add(mapPnrCounty);
		}else{
			mapPnr.get(p.getCountyid()).add(mapPnrRecord);
		}
	}
	
	int Spaces;
	int totalSpaces=0;
	int totalPnrs=0;
	List<MapPnrRecord> mapPnrRecords;
	for (MapPnrCounty mp:mapPnrCounties){
		Spaces=0;
		mapPnrRecords = mapPnr.get(mp.countyId);
		mp.MapPnrRecords=mapPnrRecords;
		mp.totalPnRs=mapPnrRecords.size()+"";
		totalPnrs+=mapPnrRecords.size();
		for (MapPnrRecord t:mapPnrRecords){
			Spaces+=Integer.parseInt(t.spaces);
		}
		totalSpaces+=Spaces;
		mp.totalSpaces=Spaces+"";
	}   	
		
   	pnr.totalPnR=totalPnrs;
   	pnr.totalSpaces=totalSpaces;
   	pnr.MapPnrCounty=mapPnrCounties;
   	
   	response.MapPnR=pnr;
   	return response;
    }
   


   /**
    * Identifies the stops and routes within 
    * a given radius of a park&ride lot.
    * 
    * @return MapPnrRecord
    */
   @GET
   @Path("/pnrstopsroutes")
   @Produces({ MediaType.APPLICATION_JSON , MediaType.APPLICATION_XML, MediaType.TEXT_XML})
   public Object getPnrStopsRoutes(@QueryParam("pnrId") String pnrId, @QueryParam("pnrCountyId") String pnrCountyId,
		   @QueryParam("lat") Double lat, @QueryParam("lng") Double lng, @QueryParam ("radius") Double radius,
		   @QueryParam("dbindex") Integer dbindex, @QueryParam("username") String username) throws JSONException {
	   
	   MapPnrRecord response = new MapPnrRecord();
	   response.id = pnrId;
	   MapStop mapPnrStop;
	   MapRoute mapPnrRoute;
	   List<String> agencyList = DbUpdate.getSelectedAgencies(username);
	   System.out.println(agencyList);
	   List<GeoStop> pnrGeoStops = new ArrayList<GeoStop>();
	   List<GeoStopRouteMap> sRoutes = new ArrayList<GeoStopRouteMap>();
		try { 
			pnrGeoStops = EventManager.getstopswithincircle2(radius, lat, lng, dbindex, agencyList);
			for (GeoStop s:pnrGeoStops){
				mapPnrStop=new MapStop();
				mapPnrStop.AgencyId=s.getAgencyId();
				mapPnrStop.Id=s.getStopId();
				mapPnrStop.Lat=s.getLat()+"";
				mapPnrStop.Lng=s.getLon()+"";
				mapPnrStop.Name=s.getName();
				
				response.MapPnrSL.add(mapPnrStop);
				
				List<GeoStopRouteMap> stmpRoutes = EventManager.getroutebystop(s.getStopId(), s.getAgencyId(), dbindex);
				for(GeoStopRouteMap r: stmpRoutes){
					if(!sRoutes.contains(r)){
						sRoutes.add(r);
					}
				}
			}
			for(GeoStopRouteMap r: sRoutes){
				mapPnrRoute = new MapRoute();
				Route _r = GtfsHibernateReaderExampleMain.QueryRoutebyid(new AgencyAndId(r.getagencyId(), r.getrouteId()), dbindex);
				mapPnrRoute.AgencyId = _r.getId().getAgencyId();
				mapPnrRoute.Id=_r.getId().getId();
				mapPnrRoute.Name=_r.getLongName();
				List<Trip> ts = GtfsHibernateReaderExampleMain.QueryTripsbyRoute(_r, dbindex);
				mapPnrRoute.Shape=ts.get(0).getEpshape();
				response.MapPnrRL.add(mapPnrRoute);
			}
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	   return response;
   }
	
	/**
     * Generates a sorted by agency id list of routes for the LHS menu
     *  
     */
    @GET
    @Path("/menu")
    @Produces({ MediaType.APPLICATION_JSON , MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    public Object getmenu(@QueryParam("day") String date, @QueryParam("dbindex") Integer dbindex, @QueryParam("username") String username) throws JSONException {  
    	if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        }
    	String[] fulldates = null;
       	String[] days = null; 
//       	username = "admin";
    	if (date!=null && !date.equals("") && !date.equals("null")){
    		String[] dates = date.split(",");
           	String[][] datedays = daysOfWeekString(dates);
           	fulldates = datedays[0];
           	days = datedays[1];
           	AgencyRouteList response = PgisEventManager.agencyMenu(fulldates, days, username, dbindex);
           	return response;
    	} else {
    		if(!username.equals("admin")){
    			AgencyRouteList response = PgisEventManager.agencyMenu(null, null, username, dbindex);
    			return response;
    		}
	    	Collection <Agency> allagencies = GtfsHibernateReaderExampleMain.QueryAllAgencies(dbindex);
	    	if (menuResponse[dbindex]==null || menuResponse[dbindex].data.size()!=allagencies.size() ){
	    		menuResponse[dbindex] = new AgencyRouteList();   	
	    		menuResponse[dbindex] = PgisEventManager.agencyMenu(null, null, username, dbindex);
	    	}    	
	    	return menuResponse[dbindex];
    	}
    }
    
    	/**
     * Return a list of all stops for a given agency in the database
     */	
    @GET
    @Path("/stops")
   @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object dbstopsforagency(@QueryParam("agency") String agency, @QueryParam("dbindex") Integer dbindex){
    	if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        }
    	StopList response = new StopList();    			
		List<Stop> stops = GtfsHibernateReaderExampleMain.QueryStopsbyAgency(agency, dbindex);		
		for (Stop stop : stops){
			  response.stops.add(new StopType(stop, false));
		  } 		
		return response;
    }
    
  
    
 	/**
     * Return shape for a given trip and agency
     */	
    @GET
    @Path("/shape")
   @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object dbshapesforagency(@QueryParam("agency") String agency,@QueryParam("trip") String trip, @QueryParam("dbindex") Integer dbindex){
    	if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        }
    	AgencyAndId agencyandtrip = new AgencyAndId();
    	agencyandtrip.setAgencyId(agency);
    	agencyandtrip.setId(trip);
    	Trip tp = GtfsHibernateReaderExampleMain.getTrip(agencyandtrip, dbindex);    	
    	Rshape shape = new Rshape();
    	shape.points = tp.getEpshape();
    	shape.length = tp.getLength();  
    	shape.agency = agency;
    	if(tp.getTripHeadsign()==null){
    		shape.headSign = "N/A";
    	}else{
    		shape.headSign = tp.getTripHeadsign();
    	}
    	shape.estlength = tp.getEstlength();
    	Agency agencyObject = GtfsHibernateReaderExampleMain.QueryAgencybyid(agency, dbindex);
    	shape.agencyName = agencyObject.getName();
		return shape;
    }
  
	/**
     * Return length for a given trip and agency
     */	
    @GET
    @Path("/tlength")
   @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object triplength(@QueryParam("agency") String agency,@QueryParam("trip") String trip, @QueryParam("dbindex") Integer dbindex){
    	if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        }
    	AgencyAndId agencyandtrip = new AgencyAndId();
    	agencyandtrip.setAgencyId(agency);
    	agencyandtrip.setId(trip);
    	List<ShapePoint> shapes = GtfsHibernateReaderExampleMain.Queryshapebytrip(agencyandtrip, dbindex);
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
    public Object dbstopsforroute(@QueryParam("agency") String agency,@QueryParam("route") String route, @QueryParam("dbindex") Integer dbindex){
    	if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        }
    	StopList response = new StopList();
		AgencyAndId routeandid = new AgencyAndId();
		routeandid.setAgencyId(agency);
		routeandid.setId(route);
    	//StopList response = new StopList();		
		List<Stop> stops = GtfsHibernateReaderExampleMain.QueryStopsbyRoute(routeandid, dbindex);
		
		for (Stop stop : stops){
			  response.stops.add(new StopType(stop, false));
		  } 		
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
    
    /**
     * Returns a 2D array , [0][i] is date is YYYYMMDD format, [1][i] is day of week as integer 1(sunday) to 7(friday)
     */    
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
     * Returns a 2D array , [0][i] is date in YYYYMMDD format, [1][i] is day of week string (all lower case): sunday, monday, tuesday, wednesday, friday
     */
    public String[][] daysOfWeekString(String[] dates){
    	Calendar calendar = Calendar.getInstance();
    	String[] weekdays = {"sunday","monday", "tuesday", "wednesday", "thursday", "friday", "saturday"};
    	SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
    	String[][] days = new String[2][dates.length];
    	for(int i=0; i<dates.length; i++){
    		days[0][i] = dates[i].split("/")[2] + dates[i].split("/")[0] + dates[i].split("/")[1];
    		try {
				calendar.setTime(sdf.parse(dates[i]));
			} catch (ParseException e) {
				e.printStackTrace();
			}
    		days[1][i] = weekdays[calendar.get(Calendar.DAY_OF_WEEK)-1];
    	}
    	return days;
    }
    /**
     * Returns full date for the dates selected on calendar in EEE dd MMM yyyy fromat
     */
    public String[] fulldate(String[] dates){
    	//Calendar calendar = Calendar.getInstance();
    	SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
    	SimpleDateFormat tdf = new SimpleDateFormat("EEE dd MMM yyyy");    	
    	String[] result = new String [dates.length];     	
    	for(int i=0; i<dates.length; i++){    		
    		try {
				result[i] = tdf.format(sdf.parse(dates[i]));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  		
    	}
    	return result;
    }
    /**
     * Generates The Agency Extended report
     */ 
    @GET
    @Path("/AgencyXR")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getAXR(@QueryParam("agency") String agency, @QueryParam("day") String date,@QueryParam("x") double x, @QueryParam("key") double key, @QueryParam("dbindex") Integer dbindex) throws JSONException {
    	
       	if (Double.isNaN(x) || x <= 0) {
            x = STOP_SEARCH_RADIUS;
        }
       	x = x * 1609.34; 
       	if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        }
    	String[] dates = date.split(",");
    	int[][] days = daysOfWeek(dates);
    	    	
    	AgencyXR response = new AgencyXR();
    	response.metadata = "Report Type:Transit Agency Extended Report;Report Date:"+new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime())+";"+
    	    	"Selected Database:" +Databases.dbnames[dbindex]+";Selected Date(s):"+date+"Population Search Radius(miles):"+String.valueOf(x)+
    	    	";Selected Transit Agency:"+agency;
    	response.AgencyId = agency;
    	response.AgencyName = GtfsHibernateReaderExampleMain.QueryAgencybyid(agency, dbindex).getName();
    	int StopCount = GtfsHibernateReaderExampleMain.QueryStopsbyAgency(agency, dbindex).size();
    	response.StopCount =  String.valueOf(StopCount);
    	    	
    	List <Trip> alltrips = GtfsHibernateReaderExampleMain.QueryTripsforAgency_RouteSorted(agency, dbindex);
    	
    	int totalLoad = alltrips.size();
    	int tripCount=0;
    	int stopsC=0;
    	Route thisroute =  alltrips.get(0).getRoute();
    	String routeId =thisroute.getId().getId();
    	String uid = "";
    	long trippop = 0;
    	double RouteMiles = 0;    	
    	double length = 0;
    	double ServiceMiles = 0;
    	double ServiceHours = 0;
        double Stopportunity = 0;
        long PopStopportunity = 0;
        
        String serviceAgency = alltrips.get(0).getServiceId().getAgencyId();
        int startDate;
        int endDate;
                     
        List <ServiceCalendar> agencyServiceCalendar = GtfsHibernateReaderExampleMain.QueryCalendarforAgency(serviceAgency, dbindex);
        List <ServiceCalendarDate> agencyServiceCalendarDates = GtfsHibernateReaderExampleMain.QueryCalendarDatesforAgency(serviceAgency, dbindex);
        
        int index = 0;
        ArrayList <String> activeTrips = new ArrayList<String>();
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
							if (!activeTrips.contains(instance.getId().getAgencyId()+instance.getId().getId()))
								activeTrips.add(instance.getId().getAgencyId()+instance.getId().getId());
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
								if (!activeTrips.contains(instance.getId().getAgencyId()+instance.getId().getId()))
									activeTrips.add(instance.getId().getAgencyId()+instance.getId().getId());
							}
							break;
						case 2:
							if (sc.getMonday()==1){
								frequency ++;
								if (!activeTrips.contains(instance.getId().getAgencyId()+instance.getId().getId()))
									activeTrips.add(instance.getId().getAgencyId()+instance.getId().getId());
							}
							break;
						case 3:
							if (sc.getTuesday()==1){
								frequency ++;
								if (!activeTrips.contains(instance.getId().getAgencyId()+instance.getId().getId()))
									activeTrips.add(instance.getId().getAgencyId()+instance.getId().getId());
							}
							break;
						case 4:
							if (sc.getWednesday()==1){
								frequency ++;
								if (!activeTrips.contains(instance.getId().getAgencyId()+instance.getId().getId()))
									activeTrips.add(instance.getId().getAgencyId()+instance.getId().getId());
							}
							break;
						case 5:
							if (sc.getThursday()==1){
								frequency ++;
								if (!activeTrips.contains(instance.getId().getAgencyId()+instance.getId().getId()))
									activeTrips.add(instance.getId().getAgencyId()+instance.getId().getId());
							}
							break;
						case 6:
							if (sc.getFriday()==1){
								frequency ++;
								if (!activeTrips.contains(instance.getId().getAgencyId()+instance.getId().getId()))
									activeTrips.add(instance.getId().getAgencyId()+instance.getId().getId());
							}
							break;
						case 7:
							if (sc.getSaturday()==1){
								frequency ++;
								if (!activeTrips.contains(instance.getId().getAgencyId()+instance.getId().getId()))
									activeTrips.add(instance.getId().getAgencyId()+instance.getId().getId());
							}
							break;
					}
				}
			}
    		if (frequency>0 && !instance.getUid().equals(uid)){    			
				trippop=0;
				uid = instance.getUid();
				List <Stop> tripstops = GtfsHibernateReaderExampleMain.QueryStopsbyTrip(instance.getId(), dbindex);
	    		List <Coordinate> tripstopcoords = new ArrayList<Coordinate>();
	    		for (Stop stop: tripstops){
	    			tripstopcoords.add(new Coordinate(stop.getLat(),stop.getLon()));
	    		}
	            try {
	    			trippop =EventManager.getunduppopbatch(x, tripstopcoords, dbindex);
	    		} catch (FactoryException e) {
	    			// TODO Auto-generated catch block
	    			e.printStackTrace();
	    		} catch (TransformException e) {
	    			// TODO Auto-generated catch block
	    			e.printStackTrace();
	    		}    			   		
    		}
    		
    		ServiceMiles += TL * frequency;  
    		Stopportunity += frequency * stops;
    		ServiceHours += frequency * instance.getTlength();
    		PopStopportunity += frequency * trippop;
    		setprogVal(key, (int) Math.round(index*100/totalLoad));
    		tripCount+= frequency;
    		stopsC+= stops*frequency;
    	}
        RouteMiles += length;
        response.ServiceStops = String.valueOf(Math.round(Stopportunity));
        response.ServiceMiles = String.valueOf(Math.round(ServiceMiles*100.0)/100.0); 
        response.RouteMiles = String.valueOf(Math.round(RouteMiles*100.0)/100.0);
        response.ServiceHours = String.valueOf(Math.round(ServiceHours/36.0)/100.0);
        long pop = 0;
        //System.out.println("count:"+tripCount);
        //System.out.println("Scount:"+stopsC);
        /*List <Stop> stops = GtfsHibernateReaderExampleMain.QueryStopsbyAgency(agency);
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
		}*/
        response.PopServed = String.valueOf(pop);
        response.PopServedByService = String.valueOf(PopStopportunity);
        if (RouteMiles >0)
        	response.StopPerRouteMile = String.valueOf(Math.round((StopCount*10000.0)/(RouteMiles))/10000.0);
        else 
        	response.StopPerRouteMile = "NA";
        if (activeTrips.size()>0) {
        	String Hos = GtfsHibernateReaderExampleMain.QueryServiceHours(activeTrips, dbindex);
        	int HOSstart =  Integer.parseInt(Hos.split("-")[0]);
        	int HOSend = Integer.parseInt(Hos.split("-")[1]);
        	response.HoursOfService = StringUtils.timefromint(HOSstart)+"-"+ StringUtils.timefromint(HOSend);
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
    @Path("/AgencyXRS")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getAXRS(@QueryParam("agency") String agency,@QueryParam("x") double x, @QueryParam("dbindex") Integer dbindex){
    	if (Double.isNaN(x) || x <= 0) {
            x = STOP_SEARCH_RADIUS;
        }
       	x = x * 1609.34;
       	if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        }
    	AgencyXR response = new AgencyXR();
    	long pop = 0;
    	List <Stop> stops = GtfsHibernateReaderExampleMain.QueryStopsbyAgency(agency, dbindex);
		List <Coordinate> stopcoords = new ArrayList<Coordinate>();
		for (Stop stop: stops){
			stopcoords.add(new Coordinate(stop.getLat(),stop.getLon()));
		}
        try {
			pop =EventManager.getunduppopbatch(x, stopcoords, dbindex);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        response.PopServed = String.valueOf(pop);
    	return response;
    }
    
    /**
     * Generates The Stops report
     */
    @GET
    @Path("/StopsR")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getTAS(@QueryParam("agency") String agency, @QueryParam("x") double x, @QueryParam("route") String routeid, @QueryParam("key") double key, @QueryParam("dbindex") Integer dbindex) throws JSONException {
    	
    	if (Double.isNaN(x) || x <= 0) {
            x = STOP_SEARCH_RADIUS;
        }    	
    	if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        }
    	StopListR response = new StopListR();
    	response.metadata = "Report Type:Transit Stops Report;Report Date:"+new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime())+";"+
    	    	"Selected Database:" +Databases.dbnames[dbindex]+";Population Search Radius(miles):"+String.valueOf(x)+";Selected Transit Agency:"+agency;
    	x = x * 1609.34;
    	response.AgencyName = "";
    	response.AgencyName = GtfsHibernateReaderExampleMain.QueryAgencybyid(agency, dbindex).getName();
    	int index =0;
    	if (routeid != null){    		
    		AgencyAndId route = new AgencyAndId(agency,routeid);
    		List<Stop> stops = GtfsHibernateReaderExampleMain.QueryStopsbyRoute(route, dbindex);    		 		
    		int totalLoad = stops.size();
    		for (Stop instance: stops){
    			index++;
    			StopR each = new StopR();
    			each.StopId = instance.getId().getId();
    			each.StopName = instance.getName();
    			each.URL = instance.getUrl();
    			each.PopWithinX ="";    			
    			each.Routes = "";
    			response.StopR.add(each);    			
    			setprogVal(key, (int) Math.round(index*100/totalLoad));
    		}
    	} else{
    		List<Stop> stops = GtfsHibernateReaderExampleMain.QueryStopsbyAgency(agency, dbindex);
    		
    		int totalLoad = stops.size();
    		for (Stop instance: stops){
    			index++;
    			StopR each = new StopR();
    			each.StopId = instance.getId().getId();
    			each.StopName = instance.getName();
    			each.URL = instance.getUrl();
    			each.PopWithinX = "";    			
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
    public Object getTASX(@QueryParam("agency") String agency, @QueryParam("x") double x, @QueryParam("key") double key, @QueryParam("stopids") String stops, @QueryParam("dbindex") Integer dbindex) throws JSONException {
    	
    	if (Double.isNaN(x) || x <= 0) {
            x = STOP_SEARCH_RADIUS;
        }
    	x = x * 1609.34;
    	if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        }
    	String[] stopIds = stops.split(",");
    	StopListR response = new StopListR();    	
    	List<Stop> tmpStops = GtfsHibernateReaderExampleMain.QueryStopsbyAgency(agency, dbindex);
    	String defAgency = tmpStops.get(0).getId().getAgencyId();
    	for (String instance: stopIds){
    		
    		AgencyAndId stopId = new AgencyAndId(defAgency,instance);
    		Stop stop = GtfsHibernateReaderExampleMain.QueryStopbyid(stopId, dbindex);    		
	    	StopR each = new StopR();
			each.StopId = "";
			each.StopName = "";
			each.URL = "";
			each.PopWithinX = "";
			
			try{
			each.PopWithinX = String.valueOf(EventManager.getpop(x, stop.getLat(), stop.getLon(), dbindex));
			} catch (FactoryException e) {    				
				e.printStackTrace();
			} catch (TransformException e) {    				
				e.printStackTrace();
			}
			each.Routes = GtfsHibernateReaderExampleMain.QueryRouteIdsforStop(stop, dbindex).toString();
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
	public Object getASR(@QueryParam("x") double x, @QueryParam("key") double key, @QueryParam("dbindex") Integer dbindex, @QueryParam("username") String username) throws JSONException {
		if (Double.isNaN(x) || x <= 0) {
	        x = STOP_SEARCH_RADIUS;
	    }		
		if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        }
		AgencyList allagencies = new AgencyList();
		List<String> selectedAgencies = DbUpdate.getSelectedAgencies(username);
		allagencies.agencies = GtfsHibernateReaderExampleMain.QuerySelectedAgencies(selectedAgencies, dbindex);            
	    AgencySRList response = new AgencySRList();
	    response.metadata = "Report Type:Transit Agency Summary Report;Report Date:"+new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime())+";"+
    	    	"Selected Database:" +Databases.dbnames[dbindex]+";Population Search Radius(miles):"+String.valueOf(x); 
	    x = x * 1609.34;
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
	    	List<Route> routes = GtfsHibernateReaderExampleMain.QueryRoutesbyAgency(instance, dbindex);
	    	each.RoutesCount = String.valueOf(routes.size());
	    	float sumFare=0; 
	    	List<Float> fares = new ArrayList<Float>();
	    	for(Route route: routes){
	    		List <FareRule> fareRules = GtfsHibernateReaderExampleMain.QueryFareRuleByRoute(route, dbindex);
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
	    	List <Stop> stops = GtfsHibernateReaderExampleMain.QueryStopsbyAgency(instance.getId(), dbindex);
	        each.StopsCount = String.valueOf(stops.size()); 	       
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
	public Object getTAR(@QueryParam("agency") String agency, @QueryParam("x") double x, @QueryParam("day") String date, @QueryParam("key") double key, @QueryParam("dbindex") Integer dbindex) throws JSONException {
		if (Double.isNaN(x) || x <= 0) {
	        x = STOP_SEARCH_RADIUS;
	    }		
		if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        }
		String[] dates = date.split(",");
		int[][] days = daysOfWeek(dates);
		
		List <Trip> alltrips = GtfsHibernateReaderExampleMain.QueryTripsforAgency_RouteSorted(agency, dbindex);	
		RouteListR response = new RouteListR();
		response.metadata = "Report Type:Routes Report;Report Date:"+new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime())+";"+
    	    	"Selected Database:" +Databases.dbnames[dbindex]+";Selected Date(s):"+date+";Population Search Radius(miles):"+String.valueOf(x)+
    	    	";Selected Transit Agency:"+agency;
		x = x * 1609.34;
		response.AgencyName = GtfsHibernateReaderExampleMain.QueryAgencybyid(agency, dbindex).getName()+"";
		Route thisroute =  alltrips.get(0).getRoute();
		String routeId =thisroute.getId().getId();
		RouteR each = new RouteR();
		double length = 0;
		double ServiceMiles = 0;
	    double Stopportunity = 0;
	    double PopStopportunity = 0;
	    double ServiceHours = 0;
	    String uid = "";
	    long pop = 0;
	    String tripuid = "";
	    long trippop = 0;
		each.RouteId = routeId+"";
		each.RouteSName = thisroute.getShortName()+"";
		each.RouteLName = thisroute.getLongName()+"";
		each.RouteDesc = thisroute.getDesc()+"";
		each.RouteType = String.valueOf(thisroute.getType());
		each.StopsCount = String.valueOf(GtfsHibernateReaderExampleMain.QueryStopsbyRoute(thisroute.getId(), dbindex).size());	 
		int index =0;
		int totalLoad = alltrips.size();		
		String serviceAgency = alltrips.get(0).getServiceId().getAgencyId();
	    int startDate;
	    int endDate;
		List <ServiceCalendar> agencyServiceCalendar = GtfsHibernateReaderExampleMain.QueryCalendarforAgency(serviceAgency, dbindex);
	    List <ServiceCalendarDate> agencyServiceCalendarDates = GtfsHibernateReaderExampleMain.QueryCalendarDatesforAgency(serviceAgency, dbindex);	    
		for (Trip instance: alltrips){
			index ++;			
			int frequency = 0;
			int stops = instance.getStopscount();			
			if (!routeId.equals(instance.getRoute().getId().getId())){		
				each.RouteLength = String.valueOf(Math.round(length*100.0)/100.0);                
		        each.ServiceMiles = String.valueOf(Math.round(ServiceMiles*100.0)/100.0); 
		        each.Stopportunity = String.valueOf(Math.round(Stopportunity));
		        each.PopStopportunity = String.valueOf(Math.round(Stopportunity));
		        each.ServiceHours = String.valueOf(Math.round(ServiceHours/36.0)/100.0);
		        if (!instance.getUid().equals(uid)){
		        	uid = instance.getUid();
		        	pop =0;
			        List <Stop> Rstops = GtfsHibernateReaderExampleMain.QueryStopsbyRoute(instance.getRoute().getId(), dbindex);
					List <Coordinate> Rstopcoords = new ArrayList<Coordinate>();				
					for (Stop stop: Rstops){
						Rstopcoords.add(new Coordinate(stop.getLat(),stop.getLon()));
					}
			        try {
						pop =EventManager.getunduppopbatch(x, Rstopcoords, dbindex);
					} catch (FactoryException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (TransformException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}		        
		        }
		        each.PopWithinX = String.valueOf(pop);		        
				response.RouteR.add(each);
				//initialize all again
				thisroute =  instance.getRoute();
				routeId = thisroute.getId().getId();			
				each = new RouteR();
				length = 0;
				ServiceMiles = 0;
			    Stopportunity = 0;
			    PopStopportunity = 0;
			    ServiceHours = 0;
				each.RouteId = routeId+"";			
				each.RouteSName = thisroute.getShortName()+"";
				each.RouteLName = thisroute.getLongName()+"";
				each.RouteDesc = thisroute.getDesc()+"";
				each.RouteType = String.valueOf(thisroute.getType());
				each.StopsCount = String.valueOf(GtfsHibernateReaderExampleMain.QueryStopsbyRoute(thisroute.getId(), dbindex).size());
				
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
			if (frequency>0 && !instance.getUid().equals(tripuid)){
				trippop = 0;
				tripuid = instance.getUid();
				List <Stop> tripstops = GtfsHibernateReaderExampleMain.QueryStopsbyTrip(instance.getId(), dbindex);
	    		List <Coordinate> tripstopcoords = new ArrayList<Coordinate>();    		
	    		for (Stop stop: tripstops){
	    			tripstopcoords.add(new Coordinate(stop.getLat(),stop.getLon()));
	    		}
	            try {
	    			trippop =EventManager.getunduppopbatch(x, tripstopcoords, dbindex);
	    		} catch (FactoryException e) {
	    			// TODO Auto-generated catch block
	    			e.printStackTrace();
	    		} catch (TransformException e) {
	    			// TODO Auto-generated catch block
	    			e.printStackTrace();
	    		}
			}
            PopStopportunity += frequency * trippop;
			ServiceMiles += TL * frequency;  
			Stopportunity += frequency * stops;
			ServiceHours += frequency * instance.getTlength();
			
			setprogVal(key, (int) Math.round(index*100/totalLoad));
		}
		each.RouteLength = String.valueOf(Math.round(length*100.0)/100.0)+"";                
	    each.ServiceMiles = String.valueOf(Math.round(ServiceMiles*100.0)/100.0)+""; 
	    each.Stopportunity = String.valueOf(Math.round(Stopportunity))+"";
	    each.PopStopportunity = String.valueOf(Math.round(PopStopportunity))+"";
	    each.ServiceHours = String.valueOf(Math.round(ServiceHours/36.0)/100.0)+"";
	    //this is population for the last trip which cannot be computed in the above loop
	    pop = 0;
        List <Stop> Rstops = GtfsHibernateReaderExampleMain.QueryStopsbyRoute(alltrips.get(alltrips.size()-1).getRoute().getId(), dbindex);
		List <Coordinate> Rstopcoords = new ArrayList<Coordinate>();
		for (Stop stop: Rstops){
			Rstopcoords.add(new Coordinate(stop.getLat(),stop.getLon()));
		}
        try {
			pop =EventManager.getunduppopbatch(x, Rstopcoords, dbindex);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        each.PopWithinX = String.valueOf(pop);
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
    public Object getSchedule(@QueryParam("agency") String agency, @QueryParam("route") String routeid, @QueryParam("day") String date, @QueryParam("key") double key, @QueryParam("dbindex") Integer dbindex) throws JSONException {
    	if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        }
    	ScheduleList response = new ScheduleList();
    	response.metadata = "Report Type:Route Schedule/Fare Report;Report Date:"+new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime())+";"+
    	    	"Selected Database:" +Databases.dbnames[dbindex]+";Selected Date(s):"+date+";Selected Transit Agency:"+agency+";Selected Route:"+routeid;
    	String[] dates = date.split(",");
		int[][] days = daysOfWeek(dates);
		//System.out.println(days[0][0]);
    	AgencyAndId routeId = new AgencyAndId(agency,routeid);
    	response.Agency = GtfsHibernateReaderExampleMain.QueryAgencybyid(agency, dbindex).getName()+"";
    	Route route = GtfsHibernateReaderExampleMain.QueryRoutebyid(routeId, dbindex);
    	response.Route = route.getId().getId()+"";
    	List <FareRule> fareRules = GtfsHibernateReaderExampleMain.QueryFareRuleByRoute(route, dbindex);
    	if(fareRules.size()==0){
    		response.Fare = "N/A";
    	}else{
    		response.Fare = fareRules.get(0).getFare().getPrice()+"";
    	}
    	List <Trip> routeTrips = GtfsHibernateReaderExampleMain.QueryTripsbyRoute(route, dbindex);
    	int totalLoad = routeTrips.size();    	
    	response.directions[0]= new Schedule();
    	response.directions[1]= new Schedule();
    	Stoptime stoptime;
    	int[] maxSize={0,0};
    	int index =0;
    	String serviceAgency = routeTrips.get(0).getServiceId().getAgencyId();
	    int startDate;
	    int endDate;
		List <ServiceCalendar> agencyServiceCalendar = GtfsHibernateReaderExampleMain.QueryCalendarforAgency(serviceAgency, dbindex);
	    List <ServiceCalendarDate> agencyServiceCalendarDates = GtfsHibernateReaderExampleMain.QueryCalendarDatesforAgency(serviceAgency, dbindex);
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
	    		AgencyAndId agencyandtrip = trip.getId();
	    		List <StopTime> stopTimes = GtfsHibernateReaderExampleMain.Querystoptimebytrip(agencyandtrip, dbindex);
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
     * Employment Summary Reports
     */
    @GET
	@Path("/emp")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	public Object getEmp2(@QueryParam("dataSet") String dataSet, @QueryParam("report") String reportType, @QueryParam("dbindex") Integer dbindex, @QueryParam("username") String username ) throws JSONException {
    	EmpDataList results = new EmpDataList();
    	results.metadata = "Report Type: "+reportType+" Employment Report;Report Date:"+new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime())+";"+
    	    	"Selected Database:" +Databases.dbnames[dbindex];
    	results = PgisEventManager.getEmpData(dataSet, reportType, dbindex, username);
    	return results;
    }

    /**
     * Title VI Report
     */
    @GET
	@Path("/titlevi")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	public Object getTitleVIData(@QueryParam("report") String reportType, @QueryParam("dbindex") Integer dbindex, @QueryParam("username") String username ) throws JSONException {
    	TitleVIDataList results = new TitleVIDataList();
    	results.metadata = "Report Type: "+reportType+" Employment Report;Report Date:"+new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime())+";"+
    	    	"Selected Database:" +Databases.dbnames[dbindex];
    	System.out.println(results.metadata);
    	results = PgisEventManager.getTitleVIData(reportType, dbindex, username);
    	return results;
    }
    
    
    /**
	 * Generates The counties Summary report
	 */	    
	@GET
	@Path("/GeoCSR")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	public Object getGCSR(@QueryParam("key") double key, @QueryParam("dbindex") Integer dbindex, @QueryParam("username") String username ) throws JSONException {
		if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        }
		List<County> allcounties = new ArrayList<County> ();
		List<String> selectedAgencies = DbUpdate.getSelectedAgencies(username);
		try {
			allcounties = EventManager.getcounties(dbindex);
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransformException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		GeoRList response = new GeoRList();
		response.metadata = "Report Type:Counties Summary Report;Report Date:"+new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime())+";"+
    	    	"Selected Database:" +Databases.dbnames[dbindex];
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
	    	each.TractsCount = "0";
	    	try {
	    		each.TractsCount = String.valueOf(EventManager.gettractscountbycounty(instance.getCountyId(), dbindex));
			} catch (FactoryException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (TransformException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}	    		    	
	    	each.AverageFare = "0";
	    	each.MedianFare = "0";	    	
	    	each.StopsCount = String.valueOf(0);
	    	try {
	    		each.StopsCount = String.valueOf(EventManager.getstopscountbycounty(instance.getCountyId(), selectedAgencies, dbindex));
			} catch (FactoryException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (TransformException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}	
	    	each.RoutesCount = String.valueOf(0);
	    	try {
	    		each.RoutesCount = String.valueOf(EventManager.getroutescountsbycounty(instance.getCountyId(), selectedAgencies, dbindex));
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
	public Object getGCTSR(@QueryParam("county") String county, @QueryParam("key") double key, @QueryParam("type") String type, @QueryParam("dbindex") Integer dbindex, @QueryParam("username") String username) throws JSONException {
		if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        }
		List<Tract> alltracts = new ArrayList<Tract> ();
		List<String> selectedAgencies = DbUpdate.getSelectedAgencies(username);
		try {
			alltracts = EventManager.gettractsbycounty(county, dbindex);
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransformException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		GeoRList response = new GeoRList();
		response.metadata = "Report Type:Census Tracts Summary Report;Report Date:"+new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime())+";"+
    	    	"Selected Database:" +Databases.dbnames[dbindex];
		response.type = "Tract";
	    int index =0;
		int totalLoad = alltracts.size();
	    for (Tract instance : alltracts){   
	    	index++;
	    	GeoR each = new GeoR();	    	
	    	each.id = instance.getTractId();
	    	each.Name = instance.getLongname();
	    	each.waterArea = String.valueOf(Math.round(instance.getWaterarea()/2.58999e4)/100.0);
	    	each.landArea = String.valueOf(Math.round(instance.getLandarea()/2.58999e4)/100.0);
	    	each.population = String.valueOf(instance.getPopulation());
	    	each.RoutesCount = String.valueOf(0);	    	    		    	
	    	each.AverageFare = "0";
	    	each.MedianFare = "0";	    	
	    	each.StopsCount = String.valueOf(0);
	    	try {
	    		each.StopsCount = String.valueOf(EventManager.getstopscountbytract(instance.getTractId(), selectedAgencies, dbindex));
			} catch (FactoryException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (TransformException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}	
	    	each.RoutesCount = String.valueOf(0);
	    	try {
	    		each.RoutesCount = String.valueOf(EventManager.getroutescountsbytract(instance.getTractId(), selectedAgencies, dbindex));
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
	public Object getGCPSR(@QueryParam("key") double key, @QueryParam("dbindex") Integer dbindex, @QueryParam("username") String username) throws JSONException {
		if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        }
		List<Place> allplaces = new ArrayList<Place> ();
		List<String> selectedAgencies = DbUpdate.getSelectedAgencies(username);
		try {
			allplaces = EventManager.getplaces(dbindex);
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransformException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		GeoRList response = new GeoRList();
		response.metadata = "Report Type:Census Places Summary Report;Report Date:"+new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime())+";"+
    	    	"Selected Database:" +Databases.dbnames[dbindex];
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
	    	each.StopsCount = String.valueOf(0);
	    	try {
	    		each.StopsCount = String.valueOf(EventManager.getstopscountbyplace(instance.getPlaceId(),selectedAgencies, dbindex));
			} catch (FactoryException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (TransformException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}	
	    	each.RoutesCount = String.valueOf(0);
	    	try {
	    		each.RoutesCount = String.valueOf(EventManager.getroutescountsbyplace(instance.getPlaceId(),selectedAgencies, dbindex));
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
	 * Generates The Aggregated urban/rural Summary report
	 */
	    
    @GET
	@Path("/GeoURSR")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	public Object getGURSR(@QueryParam("pop") Integer upop, @QueryParam("key") double key, @QueryParam("type") String type, @QueryParam("dbindex") Integer dbindex, @QueryParam("username") String username) throws JSONException {
		if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        }
		if (upop==null || upop<=0){
       		upop=50000;
       	}
		List<Urban> allurbanareas = new ArrayList<Urban> ();
		List<String> selectedAgencies = DbUpdate.getSelectedAgencies(username);
		try {
			allurbanareas = EventManager.geturbansbypop(upop,dbindex);
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransformException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		GeoRList response = new GeoRList();
		response.metadata = "Report Type:Aggregated Urban Areas Transit Summary Report;Report Date:"+new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime())+";"+
    	    	"Selected Database:" +Databases.dbnames[dbindex]+";Urban population Filter:"+String.valueOf(upop);
		response.type = "UrbanArea";	
	    int index =0;
		int totalLoad = allurbanareas.size();
		GeoR each = new GeoR();
		each.Name = "Oregon Urbanized Areas with "+ String.valueOf(upop)+"+ Population";
		each.UrbansCount = String.valueOf(allurbanareas.size());
    	each.id = "00001";
    	long landarea=0;
    	long waterarea = 0;
    	long population = 0;
    	//int routescount = 0;
    	int stopscount = 0;
    	List<String> routeL = new ArrayList<String>();
	    for (Urban instance : allurbanareas){   
	    	index++;
	    	landarea+=instance.getLandarea();	    			
	    	waterarea +=instance.getWaterarea();
	    	population += instance.getPopulation();
	    	//int routescnt = 0;
	    	try {
	    		List<GeoStopRouteMap> routesL = EventManager.getroutesbyurban(instance.getUrbanId(), selectedAgencies, dbindex);
	    		for(int x=0;x<routesL.size();x++){
	    			String routeID = routesL.get(x).getrouteId()+routesL.get(x).getagencyId();
	    			if(!routeL.contains(routeID)){
	    				routeL.add(routeID);
	    			}
	    		}
			} catch (FactoryException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (TransformException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	    	//routescount += routescnt;
	    	int stopscnt = 0;
	    	try {
	    		stopscnt = (int)EventManager.getstopscountbyurban(instance.getUrbanId(), selectedAgencies, dbindex);
			} catch (FactoryException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (TransformException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	    	stopscount += stopscnt;        
	        setprogVal(key, (int) Math.round(index*100/totalLoad));
	    }
	    try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	    progVal.remove(key);
	    each.waterArea = String.valueOf(Math.round(waterarea/2.58999e4)/100.0);
    	each.landArea = String.valueOf(Math.round(landarea/2.58999e4)/100.0);
    	each.population = String.valueOf(population);
    	//each.RoutesCount = String.valueOf(routescount);
    	each.RoutesCount = String.valueOf(routeL.size());
    	each.StopsCount = String.valueOf(stopscount);
    	response.GeoR.add(each);
	    return response;
	}
    
    /**
     * Generates list of stops for a given agency.
     * Used to generated Connected Agencies On-map Report.
     * 
     */
    @GET
	@Path("/agenStops")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	public Object getAgenStops(@QueryParam("agency") String agencyId, @QueryParam("dbindex") Integer dbindex, @QueryParam("username") String username) throws JSONException {
		if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        }
		CAStopsList response = new CAStopsList();
		response = PgisEventManager.getAgenStops(agencyId, dbindex);
		return response;		
    }
    
    
	/**
	 * Generates The urban areas Summary report
	 */
	    
    @GET
	@Path("/GeoUASR")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	public Object getGUASR(@QueryParam("key") double key, @QueryParam("dbindex") Integer dbindex, @QueryParam("username") String username) throws JSONException {
		if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        }
		List<Urban> allurbanareas = new ArrayList<Urban> ();
		List<String> selectedAgencies = DbUpdate.getSelectedAgencies(username);
		try {
			allurbanareas = EventManager.geturban(dbindex);
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransformException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		GeoRList response = new GeoRList();
		response.metadata = "Report Type:Urban Areas Summary Report;Report Date:"+new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime())+";"+
    	    	"Selected Database:" +Databases.dbnames[dbindex];
		response.type = "UrbanArea";
	    int index =0;
		int totalLoad = allurbanareas.size();
	    for (Urban instance : allurbanareas){   
	    	index++;
	    	GeoR each = new GeoR();
	    	each.Name = instance.getName();
	    	each.id = instance.getUrbanId();	    	
	    	each.waterArea = String.valueOf(Math.round(instance.getWaterarea()/2.58999e4)/100.0);
	    	each.landArea = String.valueOf(Math.round(instance.getLandarea()/2.58999e4)/100.0);
	    	each.population = String.valueOf(instance.getPopulation());
	    	each.RoutesCount = String.valueOf(0);	    	
	    	try {
	    		each.RoutesCount = String.valueOf(EventManager.getroutescountbyurban(instance.getUrbanId(), selectedAgencies, dbindex));
			} catch (FactoryException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (TransformException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	    	each.StopsCount = String.valueOf(0);	    	
	    	try {
	    		each.StopsCount = String.valueOf(EventManager.getstopscountbyurban(instance.getUrbanId(), selectedAgencies, dbindex));
			} catch (FactoryException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (TransformException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	    	each.AverageFare = "0";
	    	each.MedianFare = "0";	    	
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
	 * Generates The Congressional Districts Summary report
	 */
	
	@GET
	@Path("/GeoCDSR")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	public Object getGCDSR(@QueryParam("key") double key, @QueryParam("type") String type, @QueryParam("dbindex") Integer dbindex, @QueryParam("username") String username) throws JSONException {
		if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        }
		List<CongDist> allcongdists = new ArrayList<CongDist> ();
		List<String> selectedAgencies = DbUpdate.getSelectedAgencies(username);
		try {
			allcongdists = EventManager.getcongdist(dbindex);
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransformException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		GeoRList response = new GeoRList();
		response.metadata = "Report Type:Congressional Districts Summary Report;Report Date:"+new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime())+";"+
    	    	"Selected Database:" +Databases.dbnames[dbindex];
		response.type = "CongressionalDistrict";
	    int index =0;
		int totalLoad = allcongdists.size();
	    for (CongDist instance : allcongdists){   
	    	index++;
	    	GeoR each = new GeoR();
	    	each.Name = instance.getName();
	    	each.id = instance.getCongdistId();	    	
	    	each.waterArea = String.valueOf(Math.round(instance.getWaterarea()/2.58999e4)/100.0);
	    	each.landArea = String.valueOf(Math.round(instance.getLandarea()/2.58999e4)/100.0);
	    	each.population = String.valueOf(instance.getPopulation());
	    	each.RoutesCount = String.valueOf(0);	    	
	    	try {
	    		each.RoutesCount = String.valueOf(EventManager.getroutescountbycongdist(instance.getCongdistId(), selectedAgencies, dbindex));
			} catch (FactoryException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (TransformException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	    	each.StopsCount = String.valueOf(0);	    	
	    	try {
	    		each.StopsCount = String.valueOf(EventManager.getstopscountbycongdist(instance.getCongdistId(), selectedAgencies, dbindex));
			} catch (FactoryException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (TransformException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	    	each.AverageFare = "0";
	    	each.MedianFare = "0";	    	
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
	 * Generates ODOT Regions Summary report
	 */
	    
    @GET
	@Path("/GeoORSR")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	public Object getGORSR(@QueryParam("key") double key, @QueryParam("type") String type, @QueryParam("dbindex") Integer dbindex, @QueryParam("username") String username) throws JSONException {
		if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        }
		List<County> allcounties = new ArrayList<County> ();
		List<String> selectedAgencies = DbUpdate.getSelectedAgencies(username);
		try {
			allcounties = EventManager.getcounties(dbindex);
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransformException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		
		GeoRList response = new GeoRList();
		response.type = "ODOT Region";
		response.metadata = "Report Type:ODOT Transit Regions Summary Report;Report Date:"+new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime())+";"+
    	    	"Selected Database:" +Databases.dbnames[dbindex];
	    int index =0;
		int totalLoad = allcounties.size();
		String regionId = "";
		double waterArea = 0;
		double landArea = 0;
		long population = 0;		
		long countiesCount = 0;
		String regionName = "";
		boolean notfirst = false;
	    for (County instance : allcounties){   
	    		    	
	    	if (!(regionId.equals(instance.getRegionId()))){
	    		if (notfirst){
		    		GeoR each = new GeoR();
		    		each.ODOTRegion = regionId;
		    		each.ODOTRegionName = regionName;
		    		each.landArea = String.valueOf(Math.round(landArea/2.58999e4)/100.0);
		    		each.waterArea = String.valueOf(Math.round(waterArea/2.58999e4)/100.0);
		    		each.population = String.valueOf(population);		    		
		    		each.CountiesCount = String.valueOf(countiesCount);
		    		each.AverageFare = "0";
			    	each.MedianFare = "0";
			    	each.StopsCount = String.valueOf(0);
			    	try {
			    		each.StopsCount = String.valueOf(EventManager.getstopscountbyregion(regionId, selectedAgencies, dbindex));
					} catch (FactoryException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (TransformException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
			    	each.RoutesCount = String.valueOf(0);
			    	try {
			    		each.RoutesCount = String.valueOf(EventManager.getroutescountsbyregion(regionId, selectedAgencies, dbindex));
					} catch (FactoryException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (TransformException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
			    	response.GeoR.add(each);			    	
	    		} else {
	    			notfirst = true;
	    		}
		    	regionId = instance.getRegionId();
		    	regionName = instance.getRegionName();
		    	waterArea = instance.getWaterarea();
		    	landArea = instance.getLandarea();
		    	population = instance.getPopulation();	    	
		    	countiesCount = 1;	    	
	    	} else {	    		
	    		waterArea += instance.getWaterarea();
		    	landArea += instance.getLandarea();
		    	population += instance.getPopulation();	    	
		    	countiesCount ++;
		    	index++;
	    	}
	    	setprogVal(key, (int) Math.round(index*100/totalLoad));
	    }
	    GeoR each = new GeoR();
		each.ODOTRegion = regionId;
		each.ODOTRegionName = regionName;
		each.landArea = String.valueOf(Math.round(landArea/2.58999e4)/100.0);
		each.waterArea = String.valueOf(Math.round(waterArea/2.58999e4)/100.0);
		each.population = String.valueOf(population);		    		
		each.CountiesCount = String.valueOf(countiesCount);
		each.AverageFare = "0";
    	each.MedianFare = "0";
    	each.StopsCount = String.valueOf(0);
    	try {
    		each.StopsCount = String.valueOf(EventManager.getstopscountbyregion(regionId, dbindex));
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransformException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	each.RoutesCount = String.valueOf(0);
    	try {
    		each.RoutesCount = String.valueOf(EventManager.getroutescountsbyregion(regionId, dbindex));
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransformException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	response.GeoR.add(each);
	    try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	    progVal.remove(key);
	    return response;
	}
    
    /**
	 * Generates The connected agencies summary report
	 */
	    
    @GET
	@Path("/ConAgenSR")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	public Object getGURSRd(@QueryParam("gap") double gap, @QueryParam("key") double key, @QueryParam("dbindex") Integer dbindex, @QueryParam("username") String username) throws JSONException {
		if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
       	dbindex = default_dbindex;
       }
		if (gap<=0){
      		gap=500;
      	}
		//String username = "admin";
		ClusterRList response = new ClusterRList();
		response.metadata = "Report Type:Connected Transit Agencies Summary Report;Report Date:"+new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime())+";"+
    	    	"Selected Database:" +Databases.dbnames[dbindex]+";Minimum Spatial Gap (ft.):"+String.valueOf(gap);
		gap = gap / 3.28084;
		response.type = "AgencyGapReport";
		//PgisEventManager.makeConnection(dbindex);
		List<agencyCluster> results= new ArrayList<agencyCluster>();
		results = PgisEventManager.agencyCluster(gap, username, dbindex);
		int totalLoad = results.size();
		int index = 0;
		for (agencyCluster acl: results){
			index++;
			ClusterR instance = new ClusterR();
			instance.id = acl.getAgencyId();
			instance.name = acl.getAgencyName();
			instance.size = String.valueOf(acl.getClusterSize());
			instance.ids = StringUtils.join(acl.getAgencyIds(), ";");
			instance.names = StringUtils.join(acl.getAgencyNames(), ";");
			instance.distances = StringUtils.join(acl.getMinGaps(), ";");
			response.ClusterR.add(instance);
			setprogVal(key, (int) Math.round(index*100/totalLoad));
		}
		//PgisEventManager.dropConnection();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}        
        progVal.remove(key); 
		return response;
		
    }
	
	@GET
	@Path("/ConAgenXR")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	public Object getGURXRd(@QueryParam("agency") String agencyId, @QueryParam("gap") double gap, @QueryParam("key") double key, @QueryParam("dbindex") Integer dbindex, @QueryParam("username") String username) throws JSONException {
		if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
       	dbindex = default_dbindex;
       }
		if (gap<=0){
      		gap=500;
      	}		
		ClusterRList response = new ClusterRList();
		response.metadata = "Report Type:Connected Transit Agencies Extended Report;Report Date:"+new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime())+";"+
    	    	"Selected Database:" +Databases.dbnames[dbindex]+";Minimum Spatial Gap (ft.):"+String.valueOf(gap)+";Selected Agency:"+agencyId;
		response.type = "ExtendedGapReport";
		response.agency = GtfsHibernateReaderExampleMain.QueryAgencybyid(agencyId, dbindex).getName();
		gap = gap / 3.28084;		
		List<agencyCluster> results= new ArrayList<agencyCluster>();
		results = PgisEventManager.agencyClusterDetails(gap, agencyId, username, dbindex);
		int totalLoad = results.size();
		int index = 0;
		for (agencyCluster acl: results){
			index++;
			ClusterR instance = new ClusterR();
			instance.id = acl.getAgencyId();
			instance.name = acl.getAgencyName();
			instance.size = String.valueOf(acl.getClusterSize());
			instance.minGap = String.valueOf(acl.getMinGap());
			instance.maxGap = String.valueOf(acl.getMaxGap());
			instance.meanGap = String.valueOf(acl.getMeanGap());
			for (int i=0;i<acl.getClusterSize();i++){
				ClusterR inst = new ClusterR();
				inst.id = acl.destStopIds.get(i);
				inst.name = acl.sourceStopNames.get(i);
				inst.names = acl.destStopNames.get(i);
				inst.scoords = acl.sourceStopCoords.get(i);
				inst.dcoords = acl.destStopCoords.get(i);
				inst.minGap = acl.minGaps.get(i);
				instance.connections.add(inst);
			}						
			response.ClusterR.add(instance);
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
	 * Generates The connected networks summary report
	 */
	@GET
	@Path("/ConNetSR")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	public Object getCTNSR(@QueryParam("gap") double gap, @QueryParam("key") double key, @QueryParam("dbindex") Integer dbindex, @QueryParam("username") String username) throws JSONException {
		if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
       	dbindex = default_dbindex;
       }
		if (gap<=0){
      		gap=500;
      	}
		//String username = "admin";
		ClusterRList response = new ClusterRList();
		response.metadata = "Report Type:Connected Transit Networks Summary Report;Report Date:"+new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime())+";"+
    	    	"Selected Database:" +Databases.dbnames[dbindex]+";Minimum Spatial Gap (ft.):"+String.valueOf(gap);
		gap = gap / 3.28084;
		List<agencyCluster> agencies= new ArrayList<agencyCluster>();
		agencies = PgisEventManager.agencyCluster(gap, username, dbindex);
		int totalLoad = agencies.size();
		int index = 1;
		List<NetworkCluster> res = new ArrayList<NetworkCluster>();
		boolean changed = false;
		NetworkCluster current = new NetworkCluster();
		agencyCluster buffer = new agencyCluster();
		current.clusterId = index;
		boolean added = false;
		int clsize = totalLoad;
		while (clsize>0){
			changed = false;
			Iterator<agencyCluster> iterator = agencies.iterator();
			while (iterator.hasNext()){
				added = false;
				buffer = iterator.next();				
				added = added||current.addAgencyCluster(buffer);				
				if (added){					
					iterator.remove();
					clsize--;					
				}
				changed = changed||added;
			}			
			if (!changed){
				index ++;
				res.add(current);
				current = new NetworkCluster();
				current.clusterId = index;				
			}			
			setprogVal(key, (int) Math.round((totalLoad-clsize)*100/totalLoad));
		}
		if (current.clusterSize>0)
			res.add(current);
			for (NetworkCluster ncl: res){
				ClusterR instance = new ClusterR();
				instance.id = String.valueOf(ncl.clusterId);
				instance.ids = StringUtils.join(ncl.getAgencyIds(), ";");
				instance.names = StringUtils.join(ncl.getAgencyNames(), ";");
				instance.size = String.valueOf(ncl.agencyIds.size());
				response.ClusterR.add(instance);
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
	 * Generates The Summary Statewide report
	 */
	    
	@GET
	@Path("/stateSR")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	public Object getStateSR(@QueryParam("key") double key, @QueryParam("dbindex") Integer dbindex, @QueryParam("username") String username) throws JSONException {
		if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
       	dbindex = default_dbindex;
       }
		List<String> selectedAgencies = DbUpdate.getSelectedAgencies(username);
		int totalLoad = 2;
		int index = 0;
		setprogVal(key, (int) Math.round(index*100/totalLoad));
		GeoRList response = new GeoRList();
		response.metadata = "Report Type:Statewide Summary Report;Report Date:"+new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime())+";"+
    	    	"Selected Database:" +Databases.dbnames[dbindex];
		response.type = "StatewideReport";
		HashMap<String, Long> geocounts = new HashMap<String, Long>();
		try {
			geocounts = EventManager.getGeoCounts(dbindex);
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransformException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		
		GeoR each = new GeoR();
		each.Name = "Oregon";
		each.CountiesCount = String.valueOf(geocounts.get("county"));
		each.TractsCount = String.valueOf(geocounts.get("tract"));
		each.PlacesCount = String.valueOf(geocounts.get("place"));
		each.UrbansCount = String.valueOf(geocounts.get("urban"));
		each.RegionsCount = String.valueOf(geocounts.get("region"));
		each.CongDistsCount = String.valueOf(geocounts.get("congdist"));
		each.population = String.valueOf(geocounts.get("pop"));
		each.landArea = String.valueOf(Math.round(geocounts.get("landarea")/2.58999e4)/100.0);
		each.urbanpop = String.valueOf(geocounts.get("urbanpop"));
		each.ruralpop = String.valueOf(geocounts.get("ruralpop"));		
		index++;
		setprogVal(key, (int) Math.round(index*100/totalLoad));
		HashMap<String, Integer> transcounts = new HashMap<String, Integer>();
		transcounts = GtfsHibernateReaderExampleMain.QueryCounts(dbindex,selectedAgencies);
		each.StopsCount = String.valueOf(transcounts.get("stop"));
		each.RoutesCount = String.valueOf(transcounts.get("route"));
		each.AgenciesCount = String.valueOf(transcounts.get("agency"));
		response.GeoR.add(each);
		index++;
		setprogVal(key, (int) Math.round(index*100/totalLoad));
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}        
        progVal.remove(key);		
		return response;
		
    }
	
	/**
	 * Generates The Extended statewide report
	 */
	    
	@GET
	@Path("/stateXR")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	public Object getStateXR(@QueryParam("day") String date,@QueryParam("x") double x, @QueryParam("l") Integer L, @QueryParam("key") double key, @QueryParam("dbindex") Integer dbindex, @QueryParam("username") String username) throws JSONException {
		if (Double.isNaN(x) || x <= 0) {
            x = STOP_SEARCH_RADIUS;
        }       			
		if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
       	dbindex = default_dbindex;
        }		
		if (L==null || L<0){
       		L = LEVEL_OF_SERVICE;
       	}
		List<String> selectedAgencies = DbUpdate.getSelectedAgencies(username);
		String[] dates = date.split(",");
    	String[][] datedays = daysOfWeekString(dates);
    	String[] fulldates = fulldate(dates);
    	String[] sdates = datedays[0]; //date in YYYYMMDD format
    	String[] days = datedays[1]; //day of week string (all lower case)
    	//String username = "admin";
    	GeoXR response = new GeoXR();
    	response.metadata = "Report Type:Statewide Extended Report;Report Date:"+new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime())+";"+
    	    	"Selected Database:" +Databases.dbnames[dbindex]+";Selected Date(s):"+date+";Population Search Radius(miles):"+String.valueOf(x)+
    	    	";Minimum Level of Service(times):"+String.valueOf(L);
    	x = x * 1609.34;
    	int totalLoad = 10;
		int index = 0;
		setprogVal(key, (int) Math.round(index*100/totalLoad));
		response.AreaName = "Oregon";
		HashMap<String, Float> FareData = new HashMap<String, Float>();
		FareData = GtfsHibernateReaderExampleMain.QueryFareData(selectedAgencies, dbindex);
		index ++;
		setprogVal(key, (int) Math.round(index*100/totalLoad));
		response.MinFare = String.valueOf(FareData.get("min"));
		response.AverageFare = String.valueOf(FareData.get("avg"));
		response.MaxFare = String.valueOf(FareData.get("max"));
		int FareCount = FareData.get("count").intValue();
		float FareMedian = GtfsHibernateReaderExampleMain.QueryFareMedian(selectedAgencies, FareCount, dbindex);
		index ++;
		setprogVal(key, (int) Math.round(index*100/totalLoad));
		response.MedianFare = String.valueOf(FareMedian);
		Double RouteMiles = GtfsHibernateReaderExampleMain.QueryRouteMiles(selectedAgencies, dbindex);
		index ++;
		setprogVal(key, (int) Math.round(index*100/totalLoad));
		response.RouteMiles = String.valueOf(RouteMiles);
		Long StopsCount = GtfsHibernateReaderExampleMain.QueryStopsCount(selectedAgencies, dbindex);
		index ++;
		setprogVal(key, (int) Math.round(index*100/totalLoad));
		HashMap<String, Long> geocounts = new HashMap<String, Long>();
		try {
			geocounts = EventManager.getGeoCounts(dbindex);
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransformException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		
		response.StopsPersqMile = String.valueOf(Math.round(StopsCount*25899752356.00/geocounts.get("landarea"))/10000.00);
		index+=5;
		setprogVal(key, (int) Math.round(index*100/totalLoad));
		HashMap<String,String> serviceMetrics = PgisEventManager.StatewideServiceMetrics(sdates, days, fulldates, username, L,  x, dbindex);
		double ServiceMiles = Float.parseFloat(serviceMetrics.get("svcmiles"));
		long PopatLOS = (Long.parseLong(serviceMetrics.get("upopatlos"))+Long.parseLong(serviceMetrics.get("rpopatlos")));
		float svcPop = (Float.parseFloat(serviceMetrics.get("uspop"))+Float.parseFloat(serviceMetrics.get("rspop")));
		response.ServiceMiles = serviceMetrics.get("svcmiles");
		response.ServiceHours = serviceMetrics.get("svchours");
		response.ServiceStops = serviceMetrics.get("svcstops");
		response.PopServedAtLoService = String.valueOf(Math.round(10000.0*PopatLOS/geocounts.get("pop"))/100.0);				
		response.PopServedByService = String.valueOf(svcPop);
		String serviceDays = serviceMetrics.get("svcdays");
		if (serviceDays.length()>2){
			serviceDays = serviceDays.replace("\"", "");
			serviceDays= serviceDays.substring(1,serviceDays.length()-1);
			String[] svcdays = serviceDays.split(",");
			serviceDays = StringUtils.join(Arrays.asList(svcdays), ";");
        }
		response.ServiceDays = serviceDays;
		response.MilesofServicePerCapita = (geocounts.get("pop")>0) ? String.valueOf(Math.round((ServiceMiles*10000.00)/geocounts.get("pop"))/10000.00): "NA";		
		response.StopPerServiceMile = (ServiceMiles>0.01)? String.valueOf(Math.round((StopsCount*100)/Float.parseFloat(serviceMetrics.get("svcmiles")))/100.0): "NA";
		response.ServiceMilesPersqMile = (geocounts.get("landarea")>0.01) ? String.valueOf(Math.round((ServiceMiles*258999752.356)/geocounts.get("landarea"))/10000.00):"NA";
		int HOSstart =Integer.parseInt(serviceMetrics.get("fromtime"));
		int HOSend = Integer.parseInt(serviceMetrics.get("totime"));			
        response.HoursOfService = ((HOSstart==-1)?"NA":StringUtils.timefromint(HOSstart))+"-"+ ((HOSend==-1)?"NA":StringUtils.timefromint(HOSend));
        
		long PopWithinX = PgisEventManager.PopWithinX(x, username, dbindex);
		index++;
		setprogVal(key, (int) Math.round(index*100/totalLoad));
		response.PopWithinX = String.valueOf(PopWithinX);
		response.PopUnServed = String.valueOf(Math.round(1E4-((10000.00*PopWithinX/geocounts.get("pop"))))/100.0);
		response.PopServed = String.valueOf(Math.round((10000.00*PopWithinX/geocounts.get("pop")))/100.00);		
	
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}        
        progVal.remove(key);		
		return response;
		
    }
	
	/**
	 * Generates geographic area Extended reports
	 * types: 0=counties, 1=census tracts, 2=census places, 3=Urban Areas, 4=ODOT Regions, 5=Congressional districts
	 * 
	 */
	    
	@GET
	@Path("/geoAreaXR")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	public Object getGeoXR(@QueryParam("areaid") String areaId, @QueryParam("type") int type,@QueryParam("day") String date,@QueryParam("x") double x, @QueryParam("l") Integer L, @QueryParam("key") double key, @QueryParam("dbindex") Integer dbindex, @QueryParam("username") String username) throws JSONException {
		if (Double.isNaN(x) || x <= 0) {
            x = STOP_SEARCH_RADIUS;
        }       			
		if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
       	dbindex = default_dbindex;
        }		
		if (L==null || L<0){
       		L = LEVEL_OF_SERVICE;
       	}
		String[] dates = date.split(",");
    	String[][] datedays = daysOfWeekString(dates);
    	String[] fulldates = fulldate(dates);
    	String[] sdates = datedays[0];
    	String[] days = datedays[1];
    	
    	
    	//String username = "admin";
    	GeoXR response = new GeoXR();
    	GeoArea instance = EventManager.QueryGeoAreabyId(areaId, type, dbindex);
    	response.metadata = "Report Type:"+instance.getTypeName()+" Extended Report;Report Date:"+new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime())+";"+
    	    	"Selected Database:" +Databases.dbnames[dbindex]+";Selected Date(s):"+date+";Population Search Radius(miles):"+String.valueOf(x)+
    	    	";Minimum Level of Service(times):"+String.valueOf(L);
    	x = x * 1609.34;
    	int totalLoad = 6;
		int index = 0;
		setprogVal(key, (int) Math.round(index*100/totalLoad));
		response.AreaId = areaId;		
		response.AreaName = instance.getName();			
		
		HashMap<String, Float> FareData =PgisEventManager.FareInfo(type,sdates,days,areaId,username,dbindex);		
		response.MinFare = String.valueOf(FareData.get("minfare"));
		response.AverageFare = String.valueOf(FareData.get("averagefare"));
		response.MaxFare = String.valueOf(FareData.get("maxfare"));
		response.MedianFare = String.valueOf(FareData.get("medianfare"));
				
		index ++;
		setprogVal(key, (int) Math.round(index*100/totalLoad));		
		
		float RouteMiles = PgisEventManager.RouteMiles(type, areaId, username, dbindex);
		response.RouteMiles = String.valueOf(RouteMiles);
		index ++;
		setprogVal(key, (int) Math.round(index*100/totalLoad));
		
		long[] stopspop= PgisEventManager.stopsPop(type,areaId,username,x,dbindex);
		index ++;
		setprogVal(key, (int) Math.round(index*100/totalLoad));
		response.StopsPersqMile = String.valueOf(Math.round(stopspop[0]*25899752356.00/instance.getLandarea())/10000.00);
		response.PopWithinX = String.valueOf(stopspop[1]+stopspop[2]);
		response.UPopWithinX = String.valueOf(stopspop[1]);
		response.RPopWithinX = String.valueOf(stopspop[2]);
		response.PopServed = String.valueOf(Math.round((10000.00*(stopspop[1]+stopspop[2])/instance.getPopulation()))/100.00);
		response.UPopServed = String.valueOf(Math.round((10000.00*(stopspop[1])/instance.getPopulation()))/100.00);	
		response.RPopServed = String.valueOf(Math.round((10000.00*(stopspop[2])/instance.getPopulation()))/100.00);	
		response.PopUnServed = String.valueOf(Math.round(1E4-((10000.00*(stopspop[1]+stopspop[2])/instance.getPopulation())))/100.0);
		HashMap<String, String> servicemetrics = PgisEventManager.ServiceMetrics(type,sdates,days,fulldates,areaId,username,L,x,dbindex);
		index ++;
		setprogVal(key, (int) Math.round(index*100/totalLoad));
		double ServiceMiles = Float.parseFloat(servicemetrics.get("svcmiles"));
		long PopatLOS = (Long.parseLong(servicemetrics.get("upopatlos"))+Long.parseLong(servicemetrics.get("rpopatlos")));
		float svcPop = (Float.parseFloat(servicemetrics.get("uspop"))+Float.parseFloat(servicemetrics.get("rspop")));
		response.ServiceMiles = servicemetrics.get("svcmiles");
		response.ServiceHours = servicemetrics.get("svchours");
		response.ServiceStops = servicemetrics.get("svcstops");
		response.PopServedAtLoService = String.valueOf(Math.round(10000.0*PopatLOS/instance.getPopulation())/100.0);
		response.UPopServedAtLoService = String.valueOf(Long.parseLong(servicemetrics.get("upopatlos")));
		response.RPopServedAtLoService = String.valueOf(Long.parseLong(servicemetrics.get("rpopatlos")));
		
		String serviceDays = servicemetrics.get("svcdays");
		if (serviceDays.length()>2){
			serviceDays = serviceDays.replace("\"", "");
			serviceDays= serviceDays.substring(1,serviceDays.length()-1);
			String[] svcdays = serviceDays.split(",");
			serviceDays = StringUtils.join(Arrays.asList(svcdays), ";");
        }
		response.ServiceDays = serviceDays;
		response.MilesofServicePerCapita = (instance.getPopulation()>0) ? String.valueOf(Math.round((ServiceMiles*10000.00)/instance.getPopulation())/10000.00): "NA";
		response.StopPerServiceMile = (ServiceMiles>0.01)? String.valueOf(Math.round((stopspop[0]*100)/Float.parseFloat(servicemetrics.get("svcmiles")))/100.0): "NA";
		response.ServiceMilesPersqMile = (instance.getLandarea()>0.01) ? String.valueOf(Math.round((ServiceMiles*258999752.356)/instance.getLandarea())/10000.00):"NA";
		int HOSstart =Integer.parseInt(servicemetrics.get("fromtime"));
		int HOSend = Integer.parseInt(servicemetrics.get("totime"));			
        response.HoursOfService = ((HOSstart==-1)?"NA":StringUtils.timefromint(HOSstart))+"-"+ ((HOSend==-1)?"NA":StringUtils.timefromint(HOSend));
        String connections = servicemetrics.get("connections")+"";
		if (connections.length()>2){
			connections = connections.replace("\"", "");
			connections= connections.substring(1,connections.length()-1);
			String[] conns = connections.split(",");
			connections = StringUtils.join(Arrays.asList(conns), " ;");
        }
		response.ConnectedCommunities = connections;
		response.PopServedByService = String.valueOf(svcPop);
		response.UPopServedByService = String.valueOf(Float.parseFloat(servicemetrics.get("uspop")));	
		response.RPopServedByService = String.valueOf(Float.parseFloat(servicemetrics.get("rspop")));	
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}        
        progVal.remove(key);		
		return response;
		
    }
	
	/**
	 * Generates The multimodal hubs report
	 */
	    
	@GET
	@Path("/hubsR")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	public Object gethubsR(@QueryParam("day") String date,@QueryParam("x") double x, @QueryParam("key") double key, @QueryParam("dbindex") Integer dbindex, @QueryParam("username") String username) throws JSONException {
		if (Double.isNaN(x) || x <= 0) {
            x = STOP_SEARCH_RADIUS;
        }       			
		if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
       	dbindex = default_dbindex;
        }		
		String[] dates = date.split(",");
    	String[][] datedays = daysOfWeekString(dates);
    	//String username = "admin";
    	//String[] fulldates = fulldate(dates);
    	String[] fulldates = datedays[0];
    	String[] days = datedays[1];
    	int index = 0;
    	int progress = 0;
    	HubRList response = new HubRList();    	
    	response.metadata = "Report Type:Transit Hubs Report;Report Date:"+new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime())+";"+
    	"Selected Database:" +Databases.dbnames[dbindex]+";Stop Cluster Radius(miles):"+String.valueOf(x);
    	x = x * 1609.34;
    	setprogVal(key, 5);
    	TreeSet<StopCluster> clusterList = new TreeSet<StopCluster>();  
    	clusterList = PgisEventManager.stopClusters(fulldates, days, username, x, dbindex);  
    	setprogVal(key, 40);
    	int totalLoad = clusterList.size();
    	int ctn =  clusterList.size();
    	while (!clusterList.isEmpty()){     		
    		StopCluster instance = clusterList.pollLast();    		
    		progress++;
    		if (instance.stops.size()>0){    			
	    		index++;	
	    		HubR res = new HubR();
	    		res.addCluster(instance, index);
	    		response.HubR.add(res);
	    		TreeSet<StopCluster> tempClusterList =  new TreeSet<StopCluster>(); 
	    		Iterator<StopCluster> iter = clusterList.iterator();
	    		while(iter.hasNext()){
	    			StopCluster temp = iter.next();
	    			boolean result = temp.removeStops(instance.getStops());    			
	    			if (result){
	    				//System.out.println("Cluster # "+index+ " Result is "+result);
	    				iter.remove();
	    				if (temp.stops.size()>0){
	    					temp.syncParams();
	    					tempClusterList.add(temp);
	    				}	    				
	    			}
	    		}
	    		clusterList.addAll(tempClusterList);
    		}
    		ctn = clusterList.size();
    		setprogVal(key, 40+((int) Math.round(progress*60/totalLoad)));
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
     * Get calendar range for agency
     */
    @GET
    @Path("/agencyCalendarRange")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object agencyCalendarRange(@QueryParam("agency") String agency, @QueryParam("dbindex") Integer dbindex){
    	if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        }
    	StartEndDates seDates = new StartEndDates();
    	String defaultAgency = GtfsHibernateReaderExampleMain.QueryAgencybyid(agency, dbindex).getDefaultId();
    	FeedInfo feed = GtfsHibernateReaderExampleMain.QueryFeedInfoByDefAgencyId(defaultAgency, dbindex).get(0);
    	
    	seDates.Startdate = feed.getStartDate().getAsString();
    	seDates.Enddate = feed.getEndDate().getAsString();
    	
		return seDates;
    }
    
    /**
     * Get overall calendar range
     */
    @GET
    @Path("/calendarRange")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object calendarRange(@QueryParam("dbindex") Integer dbindex){
    	if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        }
    	StartEndDates seDates = new StartEndDates();
    	int start = 100000000;
		int end = 0;
		String s;
		String e;
		Collection<FeedInfo> feeds = GtfsHibernateReaderExampleMain.QueryAllFeedInfos(dbindex);
		
		for(FeedInfo feed: feeds){
			s = feed.getStartDate().getAsString();
			if(Integer.parseInt(s)<start){
				start = Integer.parseInt(s);
				seDates.Startdateunion = s;
			}
			
			e = feed.getEndDate().getAsString();
			if(Integer.parseInt(e)>end){
				end = Integer.parseInt(e);
				seDates.Enddateunion = e;
			}
		}
    	
		return seDates;
    }	
}