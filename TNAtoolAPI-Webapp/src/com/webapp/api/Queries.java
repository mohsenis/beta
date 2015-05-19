package com.webapp.api;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

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
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.library.samples.*;
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
	
	static MapDisplay mapResponse;
	//static Map<String, TmpMapRoute> tripKey;
	/**
     * Generates a sorted by agency id list of routes for the LHS menu
     *  
     */
    @GET
    @Path("/onmapreport")
    @Produces({ MediaType.APPLICATION_JSON , MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    public Object getSecondmenu(@QueryParam("lat") String lats,@QueryParam("lon") String lons, @QueryParam("day") String date, @QueryParam("x") double x, @QueryParam("dbindex") Integer dbindex) throws JSONException { 
    	if (Double.isNaN(x) || x <= 0) {
            x = STOP_SEARCH_RADIUS;
        }
    	//JOptionPane.showMessageDialog(null, dbindex);
    	if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        } 
    	final int sdbindex = dbindex;
//    	x = x * 1609.34;
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
    	String[] dates = date.split(",");
    	int[][] days = daysOfWeek(dates);
    	mapResponse = new MapDisplay();
    	
    	class TransitR extends Thread {
    		private double[] lat;
    		private double[] lon;
    		private double x;
    		private String date;
    		private String[] dates;
    		private int[][] days;
    		
    		public TransitR(double[] lat, double[] lon, double x, String date, String[] dates, int[][] days){
    			this.lat = lat;
    			this.lon = lon;
    			this.x = x;
    			this.date = date;
    			this.dates = dates;
    			this.days = days;
    		}
    		
    		public void run() {
    			System.out.println("Running 1 running");
    			MapTransit mtr = new MapTransit();
    			int totalRoutes=0;
    			int totalStops=0;
    			float averageFare=0;
    			List<Float> medianFare= new ArrayList<Float>();
    			
    			List<GeoStop> stops;
    			Collection<Agency> agencies = GtfsHibernateReaderExampleMain.QueryAllAgencies(sdbindex);
    			Map<String, Agency> agencyRef = new HashMap<String, Agency>();
    			for(Agency agency: agencies){
    				agencyRef.put(agency.getId(), agency);
    			}
    			Collection<Route> routes = GtfsHibernateReaderExampleMain.QueryAllRoutes(sdbindex);
  		        Map<String, Route> routeRef= new HashMap<String, Route>();
  		        for(Route route: routes){
  		        	routeRef.put(route.getAgency().getId()+route.getId().getId(), route);
  		        }
  		        Map<String, TmpMapRoute> tripKey = new HashMap<String, TmpMapRoute>();
  		        List<Trip> trips = (List<Trip>) GtfsHibernateReaderExampleMain.QueryAllTrips(sdbindex);
				String currentAgency  = trips.get(0).getId().getAgencyId();
				List <ServiceCalendar> agencyServiceCalendar = GtfsHibernateReaderExampleMain.QueryCalendarforAgency(currentAgency, sdbindex);
				List <ServiceCalendarDate> agencyServiceCalendarDates = GtfsHibernateReaderExampleMain.QueryCalendarDatesforAgency(currentAgency, sdbindex);
				for(Trip trip: trips){
					String ai = trip.getId().getAgencyId();
					
					String tk = ai+trip.getRoute().getId().getId();
					if(!tripKey.containsKey(tk)){
						TmpMapRoute tmpr = new TmpMapRoute();
						tripKey.put(tk, tmpr);
					}
					TmpMapRoute value = tripKey.get(tk);
					int startDate;
			        int endDate;
			        
			        if(!ai.equals(currentAgency)){
				        agencyServiceCalendar = GtfsHibernateReaderExampleMain.QueryCalendarforAgency(trip.getServiceId().getAgencyId(), sdbindex);
				        agencyServiceCalendarDates = GtfsHibernateReaderExampleMain.QueryCalendarDatesforAgency(trip.getServiceId().getAgencyId(), sdbindex);
				        currentAgency = ai;
			        }
			        double TL = Math.max(trip.getLength(),trip.getEstlength());	
			        if(trip.getDirectionId()==null){
			        	if (TL > value.getLength()){ 
			    			value.setLength(TL);
			    			value.setShape(trip.getEpshape());
			    		}
			        }else{
			        	if(trip.getDirectionId().equals("0")){
				        	if (TL > value.getLength0()){ 
				    			value.setLength0(TL);
				    			value.setShape0(trip.getEpshape());
				    		}  
				        }else{
				        	if (TL > value.getLength1()){ 
				    			value.setLength1(TL);
				    			value.setShape1(trip.getEpshape());
				    		} 
				        }
			        }
		    		 		
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
		daysLoop:   for (int i=0; i<dates.length; i++){  
						for(ServiceCalendarDate scd: scds){
							if(days[0][i]==Integer.parseInt(scd.getDate().getAsString())){
								if(scd.getExceptionType()==1){
									
									value.incrementFrequency();
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
										value.incrementFrequency();											
									}
									break;
								case 2:
									if (sc.getMonday()==1){
										value.incrementFrequency();
									}
									break;
								case 3:
									if (sc.getTuesday()==1){
										value.incrementFrequency();
									}
									break;
								case 4:
									if (sc.getWednesday()==1){
										value.incrementFrequency();
									}
									break;
								case 5:
									if (sc.getThursday()==1){
										value.incrementFrequency();
									}
									break;
								case 6:
									if (sc.getFriday()==1){
										value.incrementFrequency();
									}
									break;
								case 7:
									if (sc.getSaturday()==1){
										value.incrementFrequency();
									}
									break;
							}
						}
					}
					
				}
				Map<String, MapAgency> agencyKey = new HashMap<String, MapAgency>();
	            Map<String, MapRoute> routeKey = new HashMap<String, MapRoute>();
    			try {
    				List <GeoStopRouteMap> asrms = EventManager.getstoproutemaps(sdbindex);
					Map<String, List<GeoStopRouteMap>> stopRouteMapKey = new HashMap<String, List<GeoStopRouteMap>>();
					String kk="";
					for(GeoStopRouteMap gstm: asrms){
						kk = gstm.getstopId()+gstm.getagencyId_def();
						if(!stopRouteMapKey.containsKey(kk)){
							stopRouteMapKey.put(kk, new ArrayList<GeoStopRouteMap>());
						}
						stopRouteMapKey.get(kk).add(gstm);
					}
					if(lat.length==1){
						stops = EventManager.getstopswithincircle(x, lat[0], lon[0], sdbindex);
					}else{
						stops = EventManager.getstopswithinrectangle(lat, lon, sdbindex);
						//stops = EventManager.getstopswithincircle(x, lat[0], lon[0]);
					}
    				
    				//mtr.TotalStops = stops.size()+"";
    				/////////////////
    				for(GeoStop stop: stops){
		    			kk = stop.getStopId()+stop.getAgencyId();
		    			if(!stopRouteMapKey.containsKey(kk)){
		    				continue;
		    			}
		    			List <GeoStopRouteMap> srms = stopRouteMapKey.get(kk);
		    			if(srms.size()==0){
		    				continue;
		    			}
		    			MapStop ms = new MapStop();
		    			ms.Id = stop.getStopId()+"";
		    			ms.AgencyId = srms.get(0).getagencyId()+"";
		    			ms.Name = stop.getName()+"";
		    			ms.Lat = stop.getLat()+"";
		    			ms.Lng = stop.getLon()+"";
		    			ms.Frequency = 0;
		    			
		    			if(!agencyKey.containsKey(ms.AgencyId)){
		    				MapAgency ma = new MapAgency();
		    				Agency ag = agencyRef.get(ms.AgencyId);
		    				ma.Id = ag.getId()+"";
		    				ma.Name = ag.getName()+"";
		    				agencyKey.put(ms.AgencyId, ma);
		    			}
		    			MapAgency tmpMA = agencyKey.get(ms.AgencyId);
		    		
		    			for(GeoStopRouteMap srm: srms){
		    				String k = srm.getagencyId()+srm.getrouteId();
    		    			ms.RouteIds.add(srm.getrouteId()+"");
    		    			if(!routeKey.containsKey(k)){
    		    				TmpMapRoute tmr = tripKey.get(k);
    		    				if(tmr.getFrequency()==0){
    		    					continue;
    		    				}
    		    				MapRoute mr = new MapRoute();
    		    				Route rou = routeRef.get(k);
    		    				mr.Id = rou.getId().getId()+"";
    		    				mr.Name = rou.getShortName()+"";
    		    				mr.AgencyId = rou.getAgency().getId()+"";
    		    				if(tmr.getLength()==0){
    		    					mr.hasDirection = true;
    		    					mr.Shape0 = tmr.getShape0()+"";
    		    					mr.Shape1 = tmr.getShape1()+"";
    		    					mr.Length = (tmr.getLength0()+tmr.getLength1())+"";
    		    				}else{
    		    					mr.hasDirection = false;
    		    					mr.Shape = tmr.getShape()+"";
        		    				mr.Length = tmr.getLength()+"";
    		    				}
    		    				
    		    				mr.Frequency = tmr.getFrequency();
    		    				List <FareRule> fareRules = GtfsHibernateReaderExampleMain.QueryFareRuleByRoute(rou, sdbindex);
    					    	if(fareRules.size()==0){
    					    		mr.Fare = "N/A";
    					    	}else{
    					    		mr.Fare = fareRules.get(0).getFare().getPrice()+"";
    					    		averageFare+=fareRules.get(0).getFare().getPrice();
        			    			medianFare.add(fareRules.get(0).getFare().getPrice());
    					    	}
    					    	totalRoutes++;
    		    				routeKey.put(k, mr);
    		    				tmpMA.MapRoutes.add(mr);
    		    			}
    		    			ms.Frequency += routeKey.get(k).Frequency;
    		    			
    		    		}
		    			if(ms.Frequency==0){
		    				continue;
		    			}
		    			tmpMA.MapStops.add(ms);
		    			tmpMA.ServiceStop += ms.Frequency;
		    			
		    		}
    				mtr.TotalRoutes = totalRoutes+"";
	    			Collections.sort(medianFare);
			    	if (medianFare.size()>0){
			    		mtr.AverageFare = averageFare/medianFare.size()+"";
			    		mtr.MedianFare = medianFare.get((int)Math.floor(medianFare.size()/2))+"";
			    	} else {
			    		mtr.AverageFare = "NA";
				    	mtr.MedianFare = "NA";
			    	}
			    	for(Entry<String, MapAgency> entry : agencyKey.entrySet()){
			    		if(entry.getValue().ServiceStop==0){
			    			continue;
			    		}
						mtr.MapAgencies.add(entry.getValue());
						totalStops+=entry.getValue().MapStops.size();
		            }
			    	mtr.TotalStops=totalStops+"";
    			} catch (FactoryException e) {
    				e.printStackTrace();
    			} catch (TransformException e) {
    				e.printStackTrace();
    			}
    		      
    		     System.out.println("Thread 1 exiting.");
    		     mapResponse.MapTr = mtr;
    		     
    		}
    	}
    	
    	class GeoR extends Thread {
    		private double[] lat;
    		private double[] lon;
    		private double x;
    		
    		public GeoR(double[] lat, double[] lon, double x){
    			this.lat = lat;
    			this.lon = lon;
    			this.x = x;
    		}
    		
    		public void run() {
    		    System.out.println("Running 2 running");
    		    
    		    MapGeo mg = new MapGeo();
    		    int totalLand=0;
    		    int totalPop=0;
    		    int totalTract=0;
    		    List<County> counties;
    		    List<Tract> tracts;
  		        List<Census> centroids;
  		        Map<String, County> countyRef = new HashMap<String, County>();
  		        Map<String, Tract> tractRef= new HashMap<String, Tract>();
		        try {
		        	if(lat.length==1){
		        		centroids =EventManager.getcentroids(x, lat[0], lon[0], sdbindex);
		        	}else{
		        		centroids =EventManager.getcentroidswithinrectangle(lat, lon, sdbindex);
		        		//centroids =EventManager.getcentroids(x, lat[0], lon[0]);
		        	}
		            
		            mg.TotalBlocks = centroids.size()+"";
		            tracts = EventManager.gettracts(sdbindex);
		            counties = EventManager.getcounties(sdbindex);
		            for(County cou: counties){
		            	countyRef.put(cou.getCountyId(), cou);
		            }
		            for(Tract tra: tracts){
		            	tractRef.put(tra.getTractId(), tra);
		            }
		            Map<String, MapCounty> countyKey = new HashMap<String, MapCounty>();
		            Map<String, MapTract> tractKey = new HashMap<String, MapTract>();
		            for (Census c : centroids){
		            	MapBlock mb = new MapBlock();
		            	mb.ID = c.getBlockId()+"";
		            	mb.LandArea = c.getLandarea()+"";
		            	mb.Lat = c.getLatitude()+"";
		            	mb.Lng = c.getLongitude()+"";
		            	mb.Population = c.getPopulation()+"";
		            	totalLand+= c.getLandarea();
		            	totalPop+= c.getPopulation();
		            	String tmpCountyId = c.getBlockId().substring(0, 5);
		            	if(!countyKey.containsKey(tmpCountyId)){
		            		MapCounty mc = new MapCounty();
		            		County county = countyRef.get(tmpCountyId);
		            		mc.Id = county.getCountyId()+"";
		            		mc.Poopulation = 0;
		            		mc.Name = county.getName()+"";
		            		countyKey.put(tmpCountyId, mc);
		            	}
		            	String tmpTractId = c.getBlockId().substring(0, 11);
		            	MapCounty tmpMC = countyKey.get(tmpCountyId);
		            	mb.County = tmpMC.Name+"";
		            	if(!tractKey.containsKey(tmpTractId)){
		            		MapTract mt = new MapTract();
		            		Tract tract = tractRef.get(tmpTractId);
		            		mt.ID = tract.getTractId()+"";
		            		mt.LandArea = tract.getLandarea()+"";
		            		mt.Lat = tract.getLatitude()+"";
		            		mt.Lng = tract.getLongitude()+"";
		            		mt.Population = tract.getPopulation()+"";
		            		mt.County = tmpMC.Name+"";
		            		tractKey.put(tmpTractId, mt);
		            		tmpMC.MapTracts.add(mt);
		            		totalTract++;
		            	}
		            	tmpMC.MapBlocks.add(mb);
		            	tmpMC.Poopulation+=c.getPopulation(); //this is the only double field 
			        }
		            mg.TotalLandArea = totalLand+"";
		            mg.TotalPopulation = totalPop+"";
		            mg.TotalTracts = totalTract+"";
		            for(Entry<String, MapCounty> entry : countyKey.entrySet()){
						mg.MapCounties.add(entry.getValue());
		            }
		        } catch (FactoryException e) {
		            e.printStackTrace();
		        } catch (TransformException e) {
		            e.printStackTrace();
		        }
		            		     
    		    System.out.println("Thread 2 exiting.");
    		    mapResponse.MapG = mg;
    		}
    	}
    	
    	TransitR T1 = new TransitR(lat, lon, x, date, dates, days);
    	GeoR T2 = new GeoR(lat, lon, x);
        T1.start();
        T2.start();
    	
    	while(T1.isAlive() || T2.isAlive()){continue;}
    	//JOptionPane.showMessageDialog(null, mapResponse.MapTr.TotalRoutes);
    	return mapResponse;
    }
	/**
     * Generates a sorted by agency id list of routes for the LHS menu
     *  
     */
    @GET
    @Path("/menu")
    @Produces({ MediaType.APPLICATION_JSON , MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    public Object getmenu(@QueryParam("dbindex") Integer dbindex) throws JSONException {  
    	if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        }
    	Collection <Agency> allagencies = GtfsHibernateReaderExampleMain.QueryAllAgencies(dbindex);
    	if (menuResponse[dbindex]==null || menuResponse[dbindex].data.size()!=allagencies.size() ){
    		menuResponse[dbindex] = new AgencyRouteList();
	    	for (Agency instance : allagencies){    		
	    		AgencyRoute each = new AgencyRoute();
	    		Attr attribute = new Attr();
	    		attribute.id = instance.getId();
	        	attribute.type = "agency";        	
	        	each.state = "closed";        	
	        	each.attr = attribute;
	    		List <Route> routes = GtfsHibernateReaderExampleMain.QueryRoutesbyAgency(instance, dbindex);	    		
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
	                List<Trip> trips = GtfsHibernateReaderExampleMain.QueryTripsbyRoute(route, dbindex);
	                List<String> shapeids = new ArrayList<String>();
	                List<String> shapes = new ArrayList<String>();
	                int counter = 0;
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
	                        			List<StopTime> st = GtfsHibernateReaderExampleMain.Querystoptimebytrip(trip.getId(), dbindex);
	                        			
	                        			name = "From "+ st.get(0).getStop().getName() + " to "+ st.get(st.size()-1).getStop().getName();
	                        		}
	                        	}
	        	                eachV.data = name;
	        	                eachV.state = "leaf";
	        	                attribute.id = trip.getId().getId();
	        	                attribute.type = "variant";	
	        	                attribute.longest = (counter > 0 )? 0:1;	        	                	
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
	                        			List<StopTime> st = GtfsHibernateReaderExampleMain.Querystoptimebytrip(trip.getId(), dbindex);
	                        			name = "From "+ st.get(0).getStopHeadsign() + " to "+ st.get(st.size()-1).getStopHeadsign();
	                        		}
	                        	}
	                        	eachV.data = name;
	        	                eachV.state = "leaf";
	        	                attribute.id = trip.getId().getId();
	        	                attribute.type = "variant";
	        	                attribute.longest = (counter > 0 )? 0:1;
	        	                eachV.attr = attribute;
	        	            	eachO.children.add(eachV) ; 
	                    	}
	                	}
	                	counter++;
	                }	                
	                each.children.add(eachO);
	    		}
	    		each.data = instance.getName();
	    		menuResponse[dbindex].data.add(each);
	    	}
    	}
    	return menuResponse[dbindex];    	
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
    	//StopList response = new StopList();		
		List<Stop> stops = GtfsHibernateReaderExampleMain.QueryStopsbyAgency(agency, dbindex);		
		for (Stop stop : stops){
			  response.stops.add(new StopType(stop, false));
		  } 
		//response.stops = stops;
		//return response; 
		//response.stops = GtfsHibernateReaderExampleMain.QueryStops(agency);
		
		return response;
    }
    
    /**
     * Get calendar rage for agency
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
    	Connection c = null;
		Statement statement = null;
		String dbURL = Databases.connectionURLs[dbindex];
		String dbUSER = Databases.username[dbindex];
		String dbPASS = Databases.password[dbindex];
		try {
			c = DriverManager.getConnection(dbURL, dbUSER, dbPASS);
			statement = c.createStatement();
			ResultSet rs = statement.executeQuery("SELECT * FROM gtfs_feed_info where defaultid = '"+defaultAgency+"';");
			if(rs.next()){
				seDates.Startdate = rs.getString("startdate");
				seDates.Enddate = rs.getString("enddate");
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} finally {
			if (statement != null) try { statement.close(); } catch (SQLException e) {}
			if (c != null) try { c.close(); } catch (SQLException e) {}
		}
    	
		return seDates;
    }
    
    /**
     * Get overall calendar rage
     */
    @GET
    @Path("/calendarRange")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object calendarRange(@QueryParam("dbindex") Integer dbindex){
    	if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        }
    	StartEndDates seDates = new StartEndDates();
    	Connection c = null;
		Statement statement = null;
		String dbURL = Databases.connectionURLs[dbindex];
		String dbUSER = Databases.username[dbindex];
		String dbPASS = Databases.password[dbindex];
		int start = 100000000;
		int end = 0;
		String s;
		String f;
		try {
			c = DriverManager.getConnection(dbURL, dbUSER, dbPASS);
			statement = c.createStatement();
			ResultSet rs = statement.executeQuery("SELECT * FROM gtfs_feed_info;");
			while(rs.next()){
				s = rs.getString("startdate");
				if(Integer.parseInt(s)<start){
					start = Integer.parseInt(s);
					seDates.Startdateunion = s;
				}
				
				f = rs.getString("enddate");
				if(Integer.parseInt(f)>end){
					end = Integer.parseInt(f);
					seDates.Enddateunion = f;
				}
			}
		} catch (SQLException e) {
			System.out.println(e.getMessage());
		} finally {
			if (statement != null) try { statement.close(); } catch (SQLException e) {}
			if (c != null) try { c.close(); } catch (SQLException e) {}
		}
    	
		return seDates;
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
     * Returns full date for the dates selected on calendar
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
        	response.HoursOfService = new SimpleDateFormat("hh:mm aa").format(new Date(Integer.parseInt(Hos.split("-")[1]) * 1000L))+"-"+new SimpleDateFormat("hh:mm aa").format(new Date(Integer.parseInt(Hos.split("-")[0]) * 1000L));
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
    	x = x * 1609.34;
    	if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        }
    	StopListR response = new StopListR();
    	response.AgencyName = GtfsHibernateReaderExampleMain.QueryAgencybyid(agency, dbindex).getName();
    	int index =0;
    	if (routeid != null){    		
    		AgencyAndId route = new AgencyAndId(agency,routeid);
    		List<Stop> stops = GtfsHibernateReaderExampleMain.QueryStopsbyRoute(route, dbindex);
    		/*List <Coordinate> points = new ArrayList <Coordinate>();
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
    		int k = 0; */  		
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
    			//k++;
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
    	response.AgencyName = "";
    	List<Stop> tmpStops = GtfsHibernateReaderExampleMain.QueryStopsbyAgency(agency, dbindex);
    	String defAgency = tmpStops.get(0).getId().getAgencyId();
    	for (String instance: stopIds){
    		
    		AgencyAndId stopId = new AgencyAndId(defAgency,instance);
    		Stop stop = GtfsHibernateReaderExampleMain.QueryStopbyid(stopId, dbindex);
    		//System.out.println(stop.toString());
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
	public Object getASR(@QueryParam("x") double x, @QueryParam("key") double key, @QueryParam("dbindex") Integer dbindex) throws JSONException {
		if (Double.isNaN(x) || x <= 0) {
	        x = STOP_SEARCH_RADIUS;
	    }
		x = x * 1609.34;
		if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        }
		AgencyList allagencies = new AgencyList();
		allagencies.agencies = GtfsHibernateReaderExampleMain.QueryAllAgencies(dbindex);            
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
	    	////////////////////////////////////////////////////////////////////////////////////////////
			//each.RoutesCount = String.valueOf(GtfsHibernateReaderExampleMain.QueryRoutesbyAgency(instance).size()) ;
	    	List <Stop> stops = GtfsHibernateReaderExampleMain.QueryStopsbyAgency(instance.getId(), dbindex);
	        each.StopsCount = String.valueOf(stops.size());        
	       /* long pop = 0;	        
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
	        each.PopServed = String.valueOf(pop);*/
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
		x = x * 1609.34;
		if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        }
		String[] dates = date.split(",");
		int[][] days = daysOfWeek(dates);
		
		List <Trip> alltrips = GtfsHibernateReaderExampleMain.QueryTripsforAgency_RouteSorted(agency, dbindex);	
		RouteListR response = new RouteListR();
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
						
			//int stops = 0;
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
				//System.out.println("yes");
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
	 * Generates The counties Summary report
	 */
	    
	@GET
	@Path("/GeoCSR")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	public Object getGCSR(@QueryParam("key") double key, @QueryParam("type") String type, @QueryParam("dbindex") Integer dbindex ) throws JSONException {
		if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        }
		List<County> allcounties = new ArrayList<County> ();
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
	    		each.StopsCount = String.valueOf(EventManager.getstopscountbycounty(instance.getCountyId(), dbindex));
			} catch (FactoryException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (TransformException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}	
	    	each.RoutesCount = String.valueOf(0);
	    	try {
	    		each.RoutesCount = String.valueOf(EventManager.getroutescountsbycounty(instance.getCountyId(), dbindex));
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
     * Generates The Counties Extended report
     */ 
    @GET
    @Path("/CountiesXR")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getCXR(@QueryParam("county") String county, @QueryParam("day") String date,@QueryParam("x") double x, @QueryParam("l") Integer L, @QueryParam("key") double key, @QueryParam("dbindex") Integer dbindex) throws JSONException {
    	
       	if (Double.isNaN(x) || x <= 0) {
            x = STOP_SEARCH_RADIUS;
        }
       	x = x * 1609.34;
       	if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        }       	
       	if (L==null || L<0){
       		L = LEVEL_OF_SERVICE;
       	}       
    	String[] dates = date.split(",");
    	int[][] days = daysOfWeek(dates);
    	String[] fulldates = fulldate(dates);
    	    	
    	GeoXR response = new GeoXR();
    	response.AreaId = county;
    	County instance = EventManager.QueryCountybyId(county, dbindex);
    	response.AreaName = instance.getName();
    	long StopsCount = 0;
    	List<GeoStop> stops = new ArrayList<GeoStop>();    	
    	try {
    		stops = EventManager.getstopsbycounty(instance.getCountyId(), dbindex);
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransformException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	StopsCount = stops.size();
    	response.StopsPersqMile = String.valueOf(Math.round((StopsCount*2.58999e8)/instance.getLandarea())/100.0);
    	long pop = 0;	
		List <Coordinate> stopcoords = new ArrayList<Coordinate>();
		for (GeoStop stop: stops){
			stopcoords.add(new Coordinate(stop.getLat(),stop.getLon()));
		}
        try {
			pop =EventManager.getcountyunduppopbatch(x,county, stopcoords, dbindex);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        List<String> connectedCounties = new ArrayList<String>();
        try {
        	connectedCounties =EventManager.getconnectedcounties(county, dbindex);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        String ccs = "";
        for (String str : connectedCounties){
        	if (str.length()>7)
        		ccs+=str.substring(0,str.length()-7)+"; ";
        }
        if (ccs.length()>2)
        	ccs = ccs.substring(0,ccs.length()-2);	
        response.ConnectedCommunities = ccs;        		
		response.PopWithinX = String.valueOf(pop);
		response.PopServed = String.valueOf(Math.round((1E4*pop/instance.getPopulation()))/100.0);
		response.PopUnServed = String.valueOf(Math.round(1E4-((1E4*pop/instance.getPopulation())))/100.0);
		List<CountyTripMap> trips = new ArrayList<CountyTripMap>();
		try {
			trips = EventManager.gettripsbycounty(county, dbindex);
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransformException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	    	
    	int totalLoad = trips.size();
    	double RouteMiles = 0;    	
    	double length = 0;
    	double ServiceMiles = 0; 
    	double ServiceHours = 0;
        double Stopportunity = 0;
        double PopStopportunity = 0;
    	String agencyId = "";
    	String routeId = "";
    	String uid = "";
    	ArrayList<String> ServiceDays = new ArrayList<String>();
    	long trippop = 0;
    	int index = 0;
    	List <ServiceCalendar> agencyServiceCalendar = new ArrayList<ServiceCalendar>();
    	List <ServiceCalendarDate> agencyServiceCalendarDates = new ArrayList<ServiceCalendarDate>();
    	int frequency = 0;
    	int startDate;
        int endDate;
        List<String> routes = new ArrayList<String>();
        ArrayList <String> activeTrips = new ArrayList<String>();
        List <Stop> tripstops = new ArrayList<Stop>();
        List <Coordinate> tripstopcoords = new ArrayList<Coordinate>();
        HashMap<Coordinate, Integer> Stopfrequencies = new HashMap<Coordinate, Integer>();
        List<Coordinate> StopswithLOS = new ArrayList<Coordinate>();
        for (CountyTripMap inst: trips){
        	index++;
        	frequency = 0;
        	int tripstopscount = inst.getStopscount();
        	double TL = inst.getLength();			
    		if (TL > length) 
    			length = TL;
        	if (!agencyId.equals(inst.getagencyId_def())){     		
        		agencyServiceCalendar = GtfsHibernateReaderExampleMain.QueryCalendarforAgency(inst.getagencyId_def(), dbindex);
        		agencyServiceCalendarDates = GtfsHibernateReaderExampleMain.QueryCalendarDatesforAgency(inst.getagencyId_def(), dbindex); 
        		agencyId = inst.getagencyId_def();
        	} 
        	if (!routeId.equals(inst.getRouteId())){
        		if (!routes.contains(inst.getagencyId_def()+","+inst.getRouteId()))
        			routes.add(inst.getagencyId_def()+","+inst.getRouteId());
    			RouteMiles += length;  	        
    			//initialize all again    			
    			routeId = inst.getRouteId();   			
    			length = 0;    						
    		}
        	ServiceCalendar sc = null;
    		if(agencyServiceCalendar!=null){
    			for(ServiceCalendar scs: agencyServiceCalendar){
    				if(scs.getServiceId().getId().equals(inst.getServiceId())){
    					sc = scs;
    					break;
    				}
    			}  
    		}
            List <ServiceCalendarDate> scds = new ArrayList<ServiceCalendarDate>();
    		for(ServiceCalendarDate scdss: agencyServiceCalendarDates){
    			if(scdss.getServiceId().getId().equals(inst.getServiceId())){
    				scds.add(scdss);
    			}			
    		}        	
        	daysLoop:   for (int i=0; i<dates.length; i++){  
				for(ServiceCalendarDate scd: scds){
					if(days[0][i]==Integer.parseInt(scd.getDate().getAsString())){
						if(scd.getExceptionType()==1){
							frequency ++;
							if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);	
							if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
								activeTrips.add(inst.getagencyId()+inst.getTripId());
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
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
						case 2:
							if (sc.getMonday()==1){
								frequency ++;
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
						case 3:
							if (sc.getTuesday()==1){
								frequency ++;
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
						case 4:
							if (sc.getWednesday()==1){
								frequency ++;
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
						case 5:
							if (sc.getThursday()==1){
								frequency ++;
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
						case 6:
							if (sc.getFriday()==1){
								frequency ++;
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
						case 7:
							if (sc.getSaturday()==1){
								frequency ++;
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
					}
				}
			}
    		if (frequency >0 ){    		
    			if (!inst.getUid().equals(uid)){
	    			trippop = 0;
	    			uid = inst.getUid();
	    			AgencyAndId trip = new AgencyAndId(agencyId, inst.getTripId());    			
		    		tripstops = GtfsHibernateReaderExampleMain.QueryStopsbyTripCounty(trip,county, dbindex);
		    		tripstopcoords = new ArrayList<Coordinate>();
		    		if (tripstops.size()>0 && tripstops.get(0)!=null){			    		
			    		for (Stop stop: tripstops){
			    			tripstopcoords.add(new Coordinate(stop.getLat(),stop.getLon()));
			    		}
			            try {
			    			trippop =EventManager.getcountyunduppopbatch(x,county, tripstopcoords, dbindex);
			    		} catch (FactoryException e) {
			    			// TODO Auto-generated catch block
			    			e.printStackTrace();
			    		} catch (TransformException e) {
			    			// TODO Auto-generated catch block
			    			e.printStackTrace();
			    		}
		    		}
    			}
	    		if (tripstops.size()>0 && tripstops.get(0)!=null ){
	    			for (Coordinate stopcoord: tripstopcoords){
	    				if (Stopfrequencies.containsKey(stopcoord)){
	    					Stopfrequencies.put(stopcoord, Stopfrequencies.get(stopcoord)+frequency);
	    				}else{
	    					Stopfrequencies.put(stopcoord,frequency);
	    				}
	    			}
	    		}
    		}  		
    		ServiceMiles += TL * frequency;
    		ServiceHours += frequency * inst.getTlength();
    		Stopportunity += frequency * tripstopscount; 
    		PopStopportunity += frequency * trippop;
    		setprogVal(key, (int) Math.round(index*100/totalLoad));    		
    	}                
        RouteMiles += length;
        response.ServiceStops = String.valueOf(Math.round(Stopportunity));             
        response.ServiceMiles = String.valueOf(Math.round(ServiceMiles*100.0)/100.0);
        response.ServiceHours = String.valueOf(Math.round(ServiceHours/36.0)/100.0);
        response.RouteMiles = String.valueOf(Math.round(RouteMiles*100.0)/100.0);        
        response.PopServedByService = String.valueOf(PopStopportunity);
        response.ServiceMilesPersqMile = (instance.getLandarea()>0.01) ? String.valueOf(Math.round((ServiceMiles*2.58999e6)/instance.getLandarea())/100.0):"NA";
        response.MilesofServicePerCapita = (instance.getPopulation()>0) ? String.valueOf(Math.round((ServiceMiles*100.0)/instance.getPopulation())/100.0): "NA";
        response.StopPerServiceMile = (ServiceMiles>0.01)? String.valueOf(Math.round((StopsCount*100)/ServiceMiles)/100.0): "NA";        
        String svcdays = "";
        for (String str:ServiceDays){
        	svcdays += str+"; ";
        }
        if (activeTrips.size()>0) {
        	String Hos = GtfsHibernateReaderExampleMain.QueryServiceHours(activeTrips, dbindex);
        	response.HoursOfService = new SimpleDateFormat("hh:mm aa").format(new Date(Integer.parseInt(Hos.split("-")[1]) * 1000L))+"-"+new SimpleDateFormat("hh:mm aa").format(new Date(Integer.parseInt(Hos.split("-")[0]) * 1000L));
        }
        if (svcdays.length()>2){
        	svcdays= svcdays.substring(0,svcdays.length()-2);
        }
        response.ServiceDays = svcdays;
        float sumfare =0;        
        List<Float> fprices = new ArrayList<Float>();
        if (routes.size()>0)
        	fprices = GtfsHibernateReaderExampleMain.QueryFarePriceByRoutes(routes, dbindex);
        if (fprices.size()>0){        	        
            for (Float price: fprices){
            	sumfare +=price;
            }
            response.AverageFare = String.valueOf(Math.round(sumfare*100/fprices.size())/100);
    		response.MedianFare = String.valueOf(Math.round(fprices.get((int)Math.floor(fprices.size()/2))*100)/100);
        } else{
        	response.AverageFare = "NA";
        	response.MedianFare =  "NA";
        }        
        for (Map.Entry<Coordinate, Integer> entry: Stopfrequencies.entrySet()){
        	if (entry.getValue()>=L)
        		StopswithLOS.add(entry.getKey());
        }
        double popatLOS = 0;		
        try {
        	popatLOS =EventManager.getcountyunduppopbatch(x,county, StopswithLOS, dbindex);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        response.PopServedAtLoService = String.valueOf(Math.round(10000.0*popatLOS/instance.getPopulation())/100.0);
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
	public Object getGCTSR(@QueryParam("county") String county, @QueryParam("key") double key, @QueryParam("type") String type, @QueryParam("dbindex") Integer dbindex) throws JSONException {
		if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        }
		List<Tract> alltracts = new ArrayList<Tract> ();
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
	    	/*each.BlocksCount = "0";
	    	try {
	    		each.BlocksCount = String.valueOf(EventManager.getblockscountbytract(instance.getTractId(), dbindex));
			} catch (FactoryException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (TransformException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}*/	    		    	
	    	each.AverageFare = "0";
	    	each.MedianFare = "0";	    	
	    	each.StopsCount = String.valueOf(0);
	    	try {
	    		each.StopsCount = String.valueOf(EventManager.getstopscountbytract(instance.getTractId(), dbindex));
			} catch (FactoryException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (TransformException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}	
	    	each.RoutesCount = String.valueOf(0);
	    	try {
	    		each.RoutesCount = String.valueOf(EventManager.getroutescountsbytract(instance.getTractId(), dbindex));
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
     * Generates The Tracts Extended report
     */ 
    @GET
    @Path("/TractsXR")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getTXR(@QueryParam("tract") String tract, @QueryParam("day") String date,@QueryParam("x") double x,@QueryParam("l") Integer L, @QueryParam("key") double key, @QueryParam("dbindex") Integer dbindex) throws JSONException {
    	
       	if (Double.isNaN(x) || x <= 0) {
            x = STOP_SEARCH_RADIUS;
        }
       	if (L==null || L<0){
       		L = LEVEL_OF_SERVICE;
       	}
       	x = x * 1609.34;
       	if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        }
    	String[] dates = date.split(",");
    	int[][] days = daysOfWeek(dates);
    	String[] fulldates = fulldate(dates);    	    	
    	GeoXR response = new GeoXR();    	
    	Tract instance = EventManager.QueryTractbyId(tract, dbindex);    	
    	response.AreaId = instance.getTractId();
    	response.AreaName = instance.getName();
    	response.AreaLongName = instance.getLongname();
    	long StopsCount = 0;
    	List<GeoStop> stops = new ArrayList<GeoStop>();
    	try {
    		stops = EventManager.getstopsbytract(instance.getTractId(), dbindex);
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransformException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	StopsCount = stops.size();
    	response.StopsPersqMile = String.valueOf(Math.round((StopsCount*2.58999e8)/instance.getLandarea())/100.0);
    	long pop = 0;	
		List <Coordinate> stopcoords = new ArrayList<Coordinate>();
		for (GeoStop stop: stops){
			stopcoords.add(new Coordinate(stop.getLat(),stop.getLon()));
		}
        try {
			pop =EventManager.gettractunduppopbatch(x, instance.getTractId(), stopcoords, dbindex);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        List<String> connectedTracts = new ArrayList<String>();
        try {
        	connectedTracts =EventManager.getconnectedtracts(instance.getTractId(), dbindex);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        String ccs = "";
        for (String str : connectedTracts)
        	ccs+=str+"; ";
        if (ccs.length()>2)
        	ccs = ccs.substring(0,ccs.length()-2);
        response.ConnectedCommunities = ccs;       		
		response.PopWithinX = String.valueOf(pop);
		response.PopServed = String.valueOf(Math.round((1E4*pop/instance.getPopulation()))/100.0);
		response.PopUnServed = String.valueOf(Math.round(1E4-((1E4*pop/instance.getPopulation())))/100.0);
		List<TractTripMap> trips = new ArrayList<TractTripMap>();
		try {
			trips = EventManager.gettripsbytract(instance.getTractId(), dbindex);
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransformException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	    	
    	int totalLoad = trips.size();
    	double RouteMiles = 0;    	
    	double length = 0;
    	double ServiceMiles = 0;
    	double ServiceHours = 0;
        double Stopportunity = 0;
        double PopStopportunity = 0;
    	String agencyId = "";
    	String routeId = "";
    	String uid = "";
    	ArrayList<String> ServiceDays = new ArrayList<String>();
    	long trippop = 0;
    	int index = 0;
    	List <ServiceCalendar> agencyServiceCalendar = new ArrayList<ServiceCalendar>();
    	List <ServiceCalendarDate> agencyServiceCalendarDates = new ArrayList<ServiceCalendarDate>();
    	int frequency = 0;
    	int startDate;
        int endDate;
        List<String> routes = new ArrayList<String>();
        ArrayList <String> activeTrips = new ArrayList<String>();
        List <Stop> tripstops = new ArrayList<Stop>();
        List <Coordinate> tripstopcoords = new ArrayList<Coordinate>();
        HashMap<Coordinate, Integer> Stopfrequencies = new HashMap<Coordinate, Integer>();
        List<Coordinate> StopswithLOS = new ArrayList<Coordinate>();
        for (TractTripMap inst: trips){
        	index++;
        	frequency = 0;
        	int tripstopscount = inst.getStopscount();
        	double TL = inst.getLength();			
    		if (TL > length) 
    			length = TL;
        	if (!agencyId.equals(inst.getagencyId_def())){     		
        		agencyServiceCalendar = GtfsHibernateReaderExampleMain.QueryCalendarforAgency(inst.getagencyId_def(), dbindex);
        		agencyServiceCalendarDates = GtfsHibernateReaderExampleMain.QueryCalendarDatesforAgency(inst.getagencyId_def(), dbindex); 
        		agencyId = inst.getagencyId_def();
        	} 
        	if (!routeId.equals(inst.getRouteId())){
        		if (!routes.contains(inst.getagencyId_def()+","+inst.getRouteId()))
        			routes.add(inst.getagencyId_def()+","+inst.getRouteId());
    			RouteMiles += length;  	        
    			//initialize all again    			
    			routeId = inst.getRouteId();   			
    			length = 0;    						
    		}
        	ServiceCalendar sc = null;
    		if(agencyServiceCalendar!=null){
    			for(ServiceCalendar scs: agencyServiceCalendar){
    				if(scs.getServiceId().getId().equals(inst.getServiceId())){
    					sc = scs;
    					break;
    				}
    			}  
    		}
            List <ServiceCalendarDate> scds = new ArrayList<ServiceCalendarDate>();
    		for(ServiceCalendarDate scdss: agencyServiceCalendarDates){
    			if(scdss.getServiceId().getId().equals(inst.getServiceId())){
    				scds.add(scdss);
    			}			
    		}        	
        	daysLoop:   for (int i=0; i<dates.length; i++){  
				for(ServiceCalendarDate scd: scds){
					if(days[0][i]==Integer.parseInt(scd.getDate().getAsString())){
						if(scd.getExceptionType()==1){
							frequency ++;
							if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);	
							if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
								activeTrips.add(inst.getagencyId()+inst.getTripId());
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
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
						case 2:
							if (sc.getMonday()==1){
								frequency ++;
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
						case 3:
							if (sc.getTuesday()==1){
								frequency ++;
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
						case 4:
							if (sc.getWednesday()==1){
								frequency ++;
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
						case 5:
							if (sc.getThursday()==1){
								frequency ++;
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
						case 6:
							if (sc.getFriday()==1){
								frequency ++;
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
						case 7:
							if (sc.getSaturday()==1){
								frequency ++;
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
					}
				}
			}
    		if (frequency >0){ 
    			if (!inst.getUid().equals(uid)){
	    			trippop = 0;
	    			uid = inst.getUid();
	    			AgencyAndId trip = new AgencyAndId(agencyId, inst.getTripId());    			
		    		tripstops = GtfsHibernateReaderExampleMain.QueryStopsbyTripTract(trip,tract, dbindex);
		    		if (tripstops.size()>0 && tripstops.get(0)!=null){
			    		tripstopcoords = new ArrayList<Coordinate>();
			    		for (Stop stop: tripstops){
			    			tripstopcoords.add(new Coordinate(stop.getLat(),stop.getLon()));
			    		}
			            try {
			    			trippop =EventManager.gettractunduppopbatch(x,tract, tripstopcoords, dbindex);
			    		} catch (FactoryException e) {
			    			// TODO Auto-generated catch block
			    			e.printStackTrace();
			    		} catch (TransformException e) {
			    			// TODO Auto-generated catch block
			    			e.printStackTrace();
			    		}
		    		}
    			}
    			if (tripstops.size()>0 && tripstops.get(0)!=null ){
	    			for (Coordinate stopcoord: tripstopcoords){
	    				if (Stopfrequencies.containsKey(stopcoord)){
	    					Stopfrequencies.put(stopcoord, Stopfrequencies.get(stopcoord)+frequency);
	    				}else{
	    					Stopfrequencies.put(stopcoord,frequency);
	    				}
	    			}
	    		}
    		}  		
    		ServiceMiles += TL * frequency;
    		ServiceHours += frequency * inst.getTlength();
    		Stopportunity += frequency * tripstopscount; 
    		PopStopportunity += frequency * trippop;
    		setprogVal(key, (int) Math.round(index*100/totalLoad));    		
    	}                
        RouteMiles += length;
        response.ServiceStops = String.valueOf(Math.round(Stopportunity));             
        response.ServiceMiles = String.valueOf(Math.round(ServiceMiles*100.0)/100.0); 
        response.ServiceHours = String.valueOf(Math.round(ServiceHours/36.0)/100.0);
        response.RouteMiles = String.valueOf(Math.round(RouteMiles*100.0)/100.0);        
        response.PopServedByService = String.valueOf(PopStopportunity);
        response.ServiceMilesPersqMile = (instance.getLandarea()>0.01) ? String.valueOf(Math.round((ServiceMiles*2.58999e6)/instance.getLandarea())/100.0):"NA";
        response.MilesofServicePerCapita = (instance.getPopulation()>0) ? String.valueOf(Math.round((ServiceMiles*100.0)/instance.getPopulation())/100.0): "NA";
        response.StopPerServiceMile = (ServiceMiles>0.01)? String.valueOf(Math.round((StopsCount*100)/ServiceMiles)/100.0): "NA";        
        String svcdays = "";
        for (String str:ServiceDays){
        	svcdays += str+"; ";
        }
        if (activeTrips.size()>0) {
        	String Hos = GtfsHibernateReaderExampleMain.QueryServiceHours(activeTrips, dbindex);
            response.HoursOfService = new SimpleDateFormat("hh:mm aa").format(new Date(Integer.parseInt(Hos.split("-")[1]) * 1000L))+"-"+new SimpleDateFormat("hh:mm aa").format(new Date(Integer.parseInt(Hos.split("-")[0]) * 1000L));            
        }
        if (svcdays.length()>2){
        	svcdays= svcdays.substring(0,svcdays.length()-2);
        }
        response.ServiceDays = svcdays;
        float sumfare =0;        
        List<Float> fprices = new ArrayList<Float>();
        if (routes.size()>0)
        	fprices = GtfsHibernateReaderExampleMain.QueryFarePriceByRoutes(routes, dbindex);
        if (fprices.size()>0){        	        
            for (Float price: fprices){
            	sumfare +=price;
            }
            response.AverageFare = String.valueOf(Math.round(sumfare*100/fprices.size())/100);
    		response.MedianFare = String.valueOf(Math.round(fprices.get((int)Math.floor(fprices.size()/2))*100)/100);
        } else{
        	response.AverageFare = "NA";
        	response.MedianFare =  "NA";
        }
        for (Map.Entry<Coordinate, Integer> entry: Stopfrequencies.entrySet()){
        	if (entry.getValue()>=L)
        		StopswithLOS.add(entry.getKey());
        }
        double popatLOS = 0;		
        try {
        	popatLOS =EventManager.gettractunduppopbatch(x,tract, StopswithLOS, dbindex);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        response.PopServedAtLoService = String.valueOf(Math.round(10000.0*popatLOS/instance.getPopulation())/100.0);
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
	public Object getGCPSR(@QueryParam("key") double key, @QueryParam("type") String type, @QueryParam("dbindex") Integer dbindex) throws JSONException {
		if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        }
		List<Place> allplaces = new ArrayList<Place> ();
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
	    		each.StopsCount = String.valueOf(EventManager.getstopscountbyplace(instance.getPlaceId(), dbindex));
			} catch (FactoryException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (TransformException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}	
	    	each.RoutesCount = String.valueOf(0);
	    	try {
	    		each.RoutesCount = String.valueOf(EventManager.getroutescountsbyplace(instance.getPlaceId(), dbindex));
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
     * Generates The Tracts Extended report
     */ 
    @GET
    @Path("/PlacesXR")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getPXR(@QueryParam("place") String place, @QueryParam("day") String date,@QueryParam("x") double x, @QueryParam("l") Integer L, @QueryParam("key") double key, @QueryParam("dbindex") Integer dbindex) throws JSONException {
    	
       	if (Double.isNaN(x) || x <= 0) {
            x = STOP_SEARCH_RADIUS;
        }
       	if (L==null || L<0){
       		L = LEVEL_OF_SERVICE;
       	}
       	x = x * 1609.34;
       	if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        }
    	String[] dates = date.split(",");
    	int[][] days = daysOfWeek(dates);
    	String[] fulldates = fulldate(dates);
    	    	
    	GeoXR response = new GeoXR();
    	response.AreaId = place;
    	Place instance = EventManager.QueryPlacebyId(place, dbindex);
    	response.AreaName = instance.getName();
    	long StopsCount = 0;
    	List<GeoStop> stops = new ArrayList<GeoStop>();
    	try {
    		stops = EventManager.getstopsbyplace(instance.getPlaceId(), dbindex);
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransformException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	StopsCount = stops.size();
    	response.StopsPersqMile = String.valueOf(Math.round((StopsCount*2.58999e8)/instance.getLandarea())/100.0);
    	long pop = 0;	
		List <Coordinate> stopcoords = new ArrayList<Coordinate>();
		for (GeoStop stop: stops){
			stopcoords.add(new Coordinate(stop.getLat(),stop.getLon()));
		}
        try {
			pop =EventManager.getplaceunduppopbatch(x, instance.getPlaceId(), stopcoords, dbindex);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        List<String> connectedPlaces = new ArrayList<String>();
        try {
        	connectedPlaces =EventManager.getconnectedplaces(instance.getPlaceId(), dbindex);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        String ccs = "";
        for (String str : connectedPlaces){
        	if (str.length()>5)
        		ccs+=str.substring(0,str.length()-5)+"; ";
        } 
        if (ccs.length()>2)
        	ccs = ccs.substring(0,ccs.length()-2);
        response.ConnectedCommunities = ccs;     		
		response.PopWithinX = String.valueOf(pop);
		response.PopServed = String.valueOf(Math.round((1E4*pop/instance.getPopulation()))/100.0);
		response.PopUnServed = String.valueOf(Math.round(1E4-((1E4*pop/instance.getPopulation())))/100.0);
		List<PlaceTripMap> trips = new ArrayList<PlaceTripMap>();
		try {
			trips = EventManager.gettripsbyplace(instance.getPlaceId(), dbindex);
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransformException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}    	    	
    	int totalLoad = trips.size();
    	double RouteMiles = 0;    	
    	double length = 0;
    	double ServiceMiles = 0;
    	double ServiceHours = 0;
        double Stopportunity = 0;
        double PopStopportunity = 0;
    	String agencyId = "";
    	String routeId = "";
    	String uid = "";
    	ArrayList<String> ServiceDays = new ArrayList<String>();
    	long trippop = 0;
    	int index = 0;
    	List <ServiceCalendar> agencyServiceCalendar = new ArrayList<ServiceCalendar>();
    	List <ServiceCalendarDate> agencyServiceCalendarDates = new ArrayList<ServiceCalendarDate>();
    	int frequency = 0;
    	int startDate;
        int endDate;
        List<String> routes = new ArrayList<String>();
        ArrayList <String> activeTrips = new ArrayList<String>();
        List <Stop> tripstops = new ArrayList<Stop>();
        List <Coordinate> tripstopcoords = new ArrayList<Coordinate>();
        HashMap<Coordinate, Integer> Stopfrequencies = new HashMap<Coordinate, Integer>();
        List<Coordinate> StopswithLOS = new ArrayList<Coordinate>();
        for (PlaceTripMap inst: trips){
        	index++;
        	frequency = 0;
        	int tripstopscount = inst.getStopscount();
        	double TL = inst.getLength();			
    		if (TL > length) 
    			length = TL;
        	if (!agencyId.equals(inst.getagencyId_def())){     		
        		agencyServiceCalendar = GtfsHibernateReaderExampleMain.QueryCalendarforAgency(inst.getagencyId_def(), dbindex);
        		agencyServiceCalendarDates = GtfsHibernateReaderExampleMain.QueryCalendarDatesforAgency(inst.getagencyId_def(), dbindex); 
        		agencyId = inst.getagencyId_def();
        	} 
        	if (!routeId.equals(inst.getRouteId())){
        		if (!routes.contains(inst.getagencyId_def()+","+inst.getRouteId()))
        			routes.add(inst.getagencyId_def()+","+inst.getRouteId());
    			RouteMiles += length;  	        
    			//initialize all again    			
    			routeId = inst.getRouteId();   			
    			length = 0;    						
    		}
        	ServiceCalendar sc = null;
    		if(agencyServiceCalendar!=null){
    			for(ServiceCalendar scs: agencyServiceCalendar){
    				if(scs.getServiceId().getId().equals(inst.getServiceId())){
    					sc = scs;
    					break;
    				}
    			}  
    		}
            List <ServiceCalendarDate> scds = new ArrayList<ServiceCalendarDate>();
    		for(ServiceCalendarDate scdss: agencyServiceCalendarDates){
    			if(scdss.getServiceId().getId().equals(inst.getServiceId())){
    				scds.add(scdss);
    			}			
    		}        	
        	daysLoop:   for (int i=0; i<dates.length; i++){  
				for(ServiceCalendarDate scd: scds){
					if(days[0][i]==Integer.parseInt(scd.getDate().getAsString())){
						if(scd.getExceptionType()==1){
							frequency ++;
							if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);	
							if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
								activeTrips.add(inst.getagencyId()+inst.getTripId());
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
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
						case 2:
							if (sc.getMonday()==1){
								frequency ++;
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
						case 3:
							if (sc.getTuesday()==1){
								frequency ++;
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
						case 4:
							if (sc.getWednesday()==1){
								frequency ++;
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
						case 5:
							if (sc.getThursday()==1){
								frequency ++;
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
						case 6:
							if (sc.getFriday()==1){
								frequency ++;
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
						case 7:
							if (sc.getSaturday()==1){
								frequency ++;
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
					}
				}
			}
    		if (frequency >0){
    			if (!inst.getUid().equals(uid)){
	    			trippop = 0;
	    			uid = inst.getUid();
	    			AgencyAndId trip = new AgencyAndId(agencyId, inst.getTripId());    			
		    		tripstops = GtfsHibernateReaderExampleMain.QueryStopsbyTripPlace(trip,place, dbindex);
		    		if (tripstops.size()>0 && tripstops.get(0)!=null){
			    		tripstopcoords = new ArrayList<Coordinate>();
			    		for (Stop stop: tripstops){
			    			tripstopcoords.add(new Coordinate(stop.getLat(),stop.getLon()));
			    		}
			            try {
			    			trippop =EventManager.getplaceunduppopbatch(x,place, tripstopcoords, dbindex);
			    		} catch (FactoryException e) {
			    			// TODO Auto-generated catch block
			    			e.printStackTrace();
			    		} catch (TransformException e) {
			    			// TODO Auto-generated catch block
			    			e.printStackTrace();
			    		}
		    		}
    			}
    			if (tripstops.size()>0 && tripstops.get(0)!=null ){
	    			for (Coordinate stopcoord: tripstopcoords){
	    				if (Stopfrequencies.containsKey(stopcoord)){
	    					Stopfrequencies.put(stopcoord, Stopfrequencies.get(stopcoord)+frequency);
	    				}else{
	    					Stopfrequencies.put(stopcoord,frequency);
	    				}
	    			}
	    		}
    		}  		
    		ServiceMiles += TL * frequency;
    		ServiceHours += frequency * inst.getTlength();
    		Stopportunity += frequency * tripstopscount; 
    		PopStopportunity += frequency * trippop;
    		setprogVal(key, (int) Math.round(index*100/totalLoad));    		
    	}                
        RouteMiles += length;
        response.ServiceStops = String.valueOf(Math.round(Stopportunity));             
        response.ServiceMiles = String.valueOf(Math.round(ServiceMiles*100.0)/100.0); 
        response.ServiceHours = String.valueOf(Math.round(ServiceHours/36.0)/100.0);
        response.RouteMiles = String.valueOf(Math.round(RouteMiles*100.0)/100.0);        
        response.PopServedByService = String.valueOf(PopStopportunity);
        response.ServiceMilesPersqMile = (instance.getLandarea()>0.01) ? String.valueOf(Math.round((ServiceMiles*2.58999e6)/instance.getLandarea())/100.0):"NA";
        response.MilesofServicePerCapita = (instance.getPopulation()>0) ? String.valueOf(Math.round((ServiceMiles*100.0)/instance.getPopulation())/100.0): "NA";
        response.StopPerServiceMile = (ServiceMiles>0.01)? String.valueOf(Math.round((StopsCount*100)/ServiceMiles)/100.0): "NA";        
        String svcdays = "";
        for (String str:ServiceDays){
        	svcdays += str+"; ";
        }
        if (activeTrips.size()>0) {
        	String Hos = GtfsHibernateReaderExampleMain.QueryServiceHours(activeTrips, dbindex);
            response.HoursOfService = new SimpleDateFormat("hh:mm aa").format(new Date(Integer.parseInt(Hos.split("-")[1]) * 1000L))+"-"+new SimpleDateFormat("hh:mm aa").format(new Date(Integer.parseInt(Hos.split("-")[0]) * 1000L));
        }
        if (svcdays.length()>2){
        	svcdays= svcdays.substring(0,svcdays.length()-2);
        }        
        response.ServiceDays = svcdays;
        float sumfare =0;        
        List<Float> fprices = new ArrayList<Float>();
        if (routes.size()>0)
        	fprices = GtfsHibernateReaderExampleMain.QueryFarePriceByRoutes(routes, dbindex);
        if (fprices.size()>0){        	        
            for (Float price: fprices){
            	sumfare +=price;
            }
            response.AverageFare = String.valueOf(Math.round(sumfare*100/fprices.size())/100);
    		response.MedianFare = String.valueOf(Math.round(fprices.get((int)Math.floor(fprices.size()/2))*100)/100);
        } else{
        	response.AverageFare = "NA";
        	response.MedianFare =  "NA";
        }
        for (Map.Entry<Coordinate, Integer> entry: Stopfrequencies.entrySet()){
        	if (entry.getValue()>=L)
        		StopswithLOS.add(entry.getKey());
        }
        double popatLOS = 0;		
        try {
        	popatLOS =EventManager.getplaceunduppopbatch(x,place, StopswithLOS, dbindex);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        response.PopServedAtLoService = String.valueOf(Math.round(10000.0*popatLOS/instance.getPopulation())/100.0);
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
	public Object getGURSR(@QueryParam("pop") Integer upop, @QueryParam("key") double key, @QueryParam("type") String type, @QueryParam("dbindex") Integer dbindex) throws JSONException {
		if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        }
		if (upop==null || upop<=0){
       		upop=50000;
       	}
		List<Urban> allurbanareas = new ArrayList<Urban> ();
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
	    		List<GeoStopRouteMap> routesL = EventManager.getroutesbyurban(instance.getUrbanId(), dbindex);
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
	    		stopscnt = (int)EventManager.getstopscountbyurban(instance.getUrbanId(), dbindex);
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
     * Generates The Aggregated Urban/rural Areas Extended report
     */ 
    @GET
    @Path("/UrbanrXR")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getURXR(@QueryParam("pop") Integer upop,@QueryParam("day") String date,@QueryParam("x") double x, @QueryParam("l") Integer L, @QueryParam("key") double key, @QueryParam("dbindex") Integer dbindex) throws JSONException {
    	
       	if (Double.isNaN(x) || x <= 0) {
            x = STOP_SEARCH_RADIUS;
        }
       	if (L==null || L<0){
       		L = LEVEL_OF_SERVICE;
       	}
       	x = x * 1609.34;
       	if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        }
       	
       	if (upop==null || upop<=0){
       		upop=50000;
       	}
    	String[] dates = date.split(",");
    	int[][] days = daysOfWeek(dates);
    	String[] fulldates = fulldate(dates);
    	    	
    	GeoXR response = new GeoXR();
    	response.AreaId = "00001";
    	response.AreaName = "Oregon Urbanized Areas with "+ String.valueOf(upop)+"+ Population";
    	List<Urban> allurbanareas = new ArrayList<Urban> ();
		try {
			allurbanareas = EventManager.geturbansbypop(upop,dbindex);
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransformException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		};
		long population =0;
		long landarea = 0;
		long waterarea = 0;
		List<String> urbanids = new ArrayList<String>();
		for (Urban inst: allurbanareas){
			population+=inst.getPopulation();
			landarea +=inst.getLandarea();
			waterarea +=inst.getWaterarea();
			urbanids.add(inst.getUrbanId());
		}
		long StopsCount = 0;
    	List<GeoStop> stops = new ArrayList<GeoStop>();
    	try {
    		stops = EventManager.getstopsbyurbanpop(upop,dbindex);
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransformException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	StopsCount = stops.size();
    	response.StopsPersqMile = String.valueOf(Math.round((StopsCount*2.58999e8)/landarea)/100.0);
    	long pop = 0;
    	PgisEventManager.makeConnection(dbindex);
    	pop =PgisEventManager.UrbanCensusbyPop(upop);
		/*List <Coordinate> stopcoords = new ArrayList<Coordinate>();
		for (GeoStop stop: stops){
			stopcoords.add(new Coordinate(stop.getLat(),stop.getLon()));
		}
        try {
			pop =EventManager.geturbanunduppopbatchbypop(x, upop, stopcoords, dbindex);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/    	
        List<String> connectedUrbans = new ArrayList<String>();
        try {
        	connectedUrbans =EventManager.getconnectedurbansbypop(upop, dbindex);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        String ccs = "";
        for (String str : connectedUrbans){
        	ccs+=str+"; ";
        }
        if (ccs.length()>2)
        	ccs = ccs.substring(0,ccs.length()-2);	
        response.ConnectedCommunities = ccs;        		
		response.PopWithinX = String.valueOf(pop);
		response.PopServed = String.valueOf(Math.round((1E4*pop/population))/100.0);
		response.PopUnServed = String.valueOf(Math.round(1E4-((1E4*pop/population)))/100.0);
		List<UrbanTripMap> trips = new ArrayList<UrbanTripMap>();
		try {
			trips = EventManager.gettripsbyurbanpop(upop, dbindex);
			LinkedHashMap<String, UrbanTripMap> tmpHashSet = new LinkedHashMap<String, UrbanTripMap>();
			String tmpKey;
			for(UrbanTripMap t: trips){
				tmpKey = t.getagencyId_def()+t.getTripId();
				tmpHashSet.put(tmpKey, t);
			}
			trips.clear();
			for(Entry<String, UrbanTripMap> entry : tmpHashSet.entrySet()){
				trips.add(entry.getValue());
			}
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransformException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}    	    	
    	int totalLoad = trips.size();
    	double RouteMiles = 0;    	
    	double length = 0;
    	double ServiceMiles = 0;
    	double ServiceHours = 0;
        double Stopportunity = 0;
        double PopStopportunity = 0;
    	String agencyId = "";
    	String routeId = "";
    	String uid = "";
    	ArrayList<String> ServiceDays = new ArrayList<String>();
    	long trippop = 0;
    	int index = 0;
    	List <ServiceCalendar> agencyServiceCalendar = new ArrayList<ServiceCalendar>();
    	List <ServiceCalendarDate> agencyServiceCalendarDates = new ArrayList<ServiceCalendarDate>();
    	int frequency = 0;
    	int startDate;
        int endDate;
        List<String> routes = new ArrayList<String>();
        ArrayList <String> activeTrips = new ArrayList<String>();
        List <Stop> tripstops = new ArrayList<Stop>();
        List <Coordinate> tripstopcoords = new ArrayList<Coordinate>();
        HashMap<Coordinate, Integer> Stopfrequencies = new HashMap<Coordinate, Integer>();
        List<Coordinate> StopswithLOS = new ArrayList<Coordinate>();
        for (UrbanTripMap inst: trips){
        	index++;
        	frequency = 0;
        	int tripstopscount = inst.getStopscount();
        	double TL = inst.getLength();			
    		if (TL > length) 
    			length = TL;
        	if (!agencyId.equals(inst.getagencyId_def())){     		
        		agencyServiceCalendar = GtfsHibernateReaderExampleMain.QueryCalendarforAgency(inst.getagencyId_def(), dbindex);
        		agencyServiceCalendarDates = GtfsHibernateReaderExampleMain.QueryCalendarDatesforAgency(inst.getagencyId_def(), dbindex); 
        		agencyId = inst.getagencyId_def();
        	}
        	if (!agencyId.equals(inst.getagencyId()) || !routeId.equals(inst.getRouteId())){
        		RouteMiles += length; 
        		length = 0;
        	}
        	if (!routeId.equals(inst.getRouteId())){
        		if (!routes.contains(inst.getagencyId_def()+","+inst.getRouteId()))
        			routes.add(inst.getagencyId_def()+","+inst.getRouteId());
    			 	        
    			//initialize all again    			
    			routeId = inst.getRouteId();   						
    		}
        	
        	ServiceCalendar sc = null;
    		if(agencyServiceCalendar!=null){
    			for(ServiceCalendar scs: agencyServiceCalendar){
    				if(scs.getServiceId().getId().equals(inst.getServiceId())){
    					sc = scs;
    					break;
    				}
    			}  
    		}
            List <ServiceCalendarDate> scds = new ArrayList<ServiceCalendarDate>();
    		for(ServiceCalendarDate scdss: agencyServiceCalendarDates){
    			if(scdss.getServiceId().getId().equals(inst.getServiceId())){
    				scds.add(scdss);
    			}			
    		}        	
        	daysLoop:   for (int i=0; i<dates.length; i++){  
				for(ServiceCalendarDate scd: scds){
					if(days[0][i]==Integer.parseInt(scd.getDate().getAsString())){
						if(scd.getExceptionType()==1){
							frequency ++;
							if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);	
							if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
								activeTrips.add(inst.getagencyId()+inst.getTripId());
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
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
						case 2:
							if (sc.getMonday()==1){
								frequency ++;
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
						case 3:
							if (sc.getTuesday()==1){
								frequency ++;
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
						case 4:
							if (sc.getWednesday()==1){
								frequency ++;
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
						case 5:
							if (sc.getThursday()==1){
								frequency ++;
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
						case 6:
							if (sc.getFriday()==1){
								frequency ++;
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
						case 7:
							if (sc.getSaturday()==1){
								frequency ++;
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
					}
				}
			}
    		if (frequency >0){
    			if (!inst.getUid().equals(uid)){
	    			trippop = 0;
	    			uid = inst.getUid();
	    			AgencyAndId trip = new AgencyAndId(agencyId, inst.getTripId());    			
		    		tripstops = GtfsHibernateReaderExampleMain.QueryStopsbyTripUrbans(trip,urbanids, dbindex);
		    		if (tripstops.size()>0 && tripstops.get(0)!=null){
			    		tripstopcoords = new ArrayList<Coordinate>();
			    		for (Stop stop: tripstops){
			    			tripstopcoords.add(new Coordinate(stop.getLat(),stop.getLon()));
			    		}
			            try {
			    			trippop =EventManager.geturbanunduppopbatchbypop(x,upop, tripstopcoords, dbindex);
			    		} catch (FactoryException e) {
			    			// TODO Auto-generated catch block
			    			e.printStackTrace();
			    		} catch (TransformException e) {
			    			// TODO Auto-generated catch block
			    			e.printStackTrace();
			    		}
		    		}
    			}
	    		if (tripstops.size()>0 && tripstops.get(0)!=null ){
	    			for (Coordinate stopcoord: tripstopcoords){
	    				if (Stopfrequencies.containsKey(stopcoord)){
	    					Stopfrequencies.put(stopcoord, Stopfrequencies.get(stopcoord)+frequency);
	    				}else{
	    					Stopfrequencies.put(stopcoord,frequency);
	    				}
	    			}
	    		}
    		}  		
    		ServiceMiles += TL * frequency;  
    		ServiceHours += frequency * inst.getTlength();
    		Stopportunity += frequency * tripstopscount; 
    		PopStopportunity += frequency * trippop;
    		setprogVal(key, (int) Math.round(index*100/totalLoad));    		
    	}                
        RouteMiles += length;
        response.ServiceStops = String.valueOf(Math.round(Stopportunity));             
        response.ServiceMiles = String.valueOf(Math.round(ServiceMiles*100.0)/100.0);
        response.ServiceHours = String.valueOf(Math.round(ServiceHours/36.0)/100.0);
        response.RouteMiles = String.valueOf(Math.round(RouteMiles*100.0)/100.0);        
        response.PopServedByService = String.valueOf(PopStopportunity);
        response.ServiceMilesPersqMile = (landarea>0.01) ? String.valueOf(Math.round((ServiceMiles*2.58999e6)/landarea)/100.0):"NA";
        response.MilesofServicePerCapita = (population>0) ? String.valueOf(Math.round((ServiceMiles*100.0)/population)/100.0): "NA";
        response.StopPerServiceMile = (ServiceMiles>0.01)? String.valueOf(Math.round((StopsCount*100)/ServiceMiles)/100.0): "NA";        
        String svcdays = "";
        for (String str:ServiceDays){
        	svcdays += str+"; ";
        }
        if (activeTrips.size()>0) {
        	String Hos = GtfsHibernateReaderExampleMain.QueryServiceHours(activeTrips, dbindex);
            response.HoursOfService = new SimpleDateFormat("hh:mm aa").format(new Date(Integer.parseInt(Hos.split("-")[1]) * 1000L))+"-"+new SimpleDateFormat("hh:mm aa").format(new Date(Integer.parseInt(Hos.split("-")[0]) * 1000L));
        }
        if (svcdays.length()>2){
        	svcdays= svcdays.substring(0,svcdays.length()-2);
        }        
        response.ServiceDays = svcdays;
        float sumfare =0;        
        List<Float> fprices = new ArrayList<Float>();
        if (routes.size()>0)
        	fprices = GtfsHibernateReaderExampleMain.QueryFarePriceByRoutes(routes, dbindex);
        if (fprices.size()>0){        	        
            for (Float price: fprices){
            	sumfare +=price;
            }
            response.AverageFare = String.valueOf(Math.round(sumfare*100/fprices.size())/100);
    		response.MedianFare = String.valueOf(Math.round(fprices.get((int)Math.floor(fprices.size()/2))*100)/100);
        } else{
        	response.AverageFare = "NA";
        	response.MedianFare =  "NA";
        }
        for (Map.Entry<Coordinate, Integer> entry: Stopfrequencies.entrySet()){
        	if (entry.getValue()>=L)
        		StopswithLOS.add(entry.getKey());
        }
        double popatLOS = 0;		
        try {
        	popatLOS =EventManager.geturbanunduppopbatchbypop(x,upop, StopswithLOS, dbindex);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        response.PopServedAtLoService = String.valueOf(Math.round(10000.0*popatLOS/population)/100.0);
        try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}        
        progVal.remove(key); 
        PgisEventManager.dropConnection();
    	return response;
    }
    
	/**
	 * Generates The urban areas Summary report
	 */
	    
	@GET
	@Path("/GeoUASR")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	public Object getGUASR(@QueryParam("key") double key, @QueryParam("type") String type, @QueryParam("dbindex") Integer dbindex) throws JSONException {
		if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        }
		List<Urban> allurbanareas = new ArrayList<Urban> ();
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
	    		each.RoutesCount = String.valueOf(EventManager.getroutescountbyurban(instance.getUrbanId(), dbindex));
			} catch (FactoryException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (TransformException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	    	each.StopsCount = String.valueOf(0);	    	
	    	try {
	    		each.StopsCount = String.valueOf(EventManager.getstopscountbyurban(instance.getUrbanId(), dbindex));
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
     * Generates The Urban Areas Extended report
     */ 
    @GET
    @Path("/UrbansXR")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getUXR(@QueryParam("urban") String urban, @QueryParam("day") String date,@QueryParam("x") double x, @QueryParam("l") Integer L, @QueryParam("key") double key, @QueryParam("dbindex") Integer dbindex) throws JSONException {
    	
       	if (Double.isNaN(x) || x <= 0) {
            x = STOP_SEARCH_RADIUS;
        }
       	if (L==null || L<0){
       		L = LEVEL_OF_SERVICE;
       	}
       	x = x * 1609.34;
       	if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        }
    	String[] dates = date.split(",");
    	int[][] days = daysOfWeek(dates);
    	String[] fulldates = fulldate(dates);
    	    	
    	GeoXR response = new GeoXR();
    	response.AreaId = urban;
    	Urban instance = EventManager.QueryUrbanbyId(urban, dbindex);
    	response.AreaName = instance.getName();
    	long StopsCount = 0;
    	List<GeoStop> stops = new ArrayList<GeoStop>();
    	try {
    		stops = EventManager.getstopsbyurban(instance.getUrbanId(),dbindex);
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransformException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	StopsCount = stops.size();
    	response.StopsPersqMile = String.valueOf(Math.round((StopsCount*2.58999e8)/instance.getLandarea())/100.0);
    	long pop = 0;	
		List <Coordinate> stopcoords = new ArrayList<Coordinate>();
		for (GeoStop stop: stops){
			stopcoords.add(new Coordinate(stop.getLat(),stop.getLon()));
		}
        try {
			pop =EventManager.geturbanunduppopbatch(x, instance.getUrbanId(), stopcoords, dbindex);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        List<String> connectedUrbans = new ArrayList<String>();
        try {
        	connectedUrbans =EventManager.getconnectedurbans(instance.getUrbanId(), dbindex);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        String ccs = "";
        for (String str : connectedUrbans){
        	ccs+=str+"; ";
        }
        if (ccs.length()>2)
        	ccs = ccs.substring(0,ccs.length()-2);	
        response.ConnectedCommunities = ccs;        		
		response.PopWithinX = String.valueOf(pop);
		response.PopServed = String.valueOf(Math.round((1E4*pop/instance.getPopulation()))/100.0);
		response.PopUnServed = String.valueOf(Math.round(1E4-((1E4*pop/instance.getPopulation())))/100.0);
		List<UrbanTripMap> trips = new ArrayList<UrbanTripMap>();
		try {
			trips = EventManager.gettripsbyurban(instance.getUrbanId(), dbindex);
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransformException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	    	
    	int totalLoad = trips.size();
    	double RouteMiles = 0;    	
    	double length = 0;
    	double ServiceMiles = 0;
    	double ServiceHours = 0;
        double Stopportunity = 0;
        double PopStopportunity = 0;
    	String agencyId = "";
    	String routeId = "";
    	String uid = "";
    	ArrayList<String> ServiceDays = new ArrayList<String>();
    	long trippop = 0;
    	int index = 0;
    	List <ServiceCalendar> agencyServiceCalendar = new ArrayList<ServiceCalendar>();
    	List <ServiceCalendarDate> agencyServiceCalendarDates = new ArrayList<ServiceCalendarDate>();
    	int frequency = 0;
    	int startDate;
        int endDate;
        List<String> routes = new ArrayList<String>();
        ArrayList <String> activeTrips = new ArrayList<String>();
        List <Stop> tripstops = new ArrayList<Stop>();
        List <Coordinate> tripstopcoords = new ArrayList<Coordinate>();
        HashMap<Coordinate, Integer> Stopfrequencies = new HashMap<Coordinate, Integer>();
        List<Coordinate> StopswithLOS = new ArrayList<Coordinate>();
        for (UrbanTripMap inst: trips){
        	index++;
        	frequency = 0;
        	int tripstopscount = inst.getStopscount();
        	double TL = inst.getLength();			
    		if (TL > length) 
    			length = TL;
        	if (!agencyId.equals(inst.getagencyId_def())){     		
        		agencyServiceCalendar = GtfsHibernateReaderExampleMain.QueryCalendarforAgency(inst.getagencyId_def(), dbindex);
        		agencyServiceCalendarDates = GtfsHibernateReaderExampleMain.QueryCalendarDatesforAgency(inst.getagencyId_def(), dbindex); 
        		agencyId = inst.getagencyId_def();
        	} 
        	if (!routeId.equals(inst.getRouteId())){
        		if (!routes.contains(inst.getagencyId_def()+","+inst.getRouteId()))
        			routes.add(inst.getagencyId_def()+","+inst.getRouteId());
    			RouteMiles += length;  	        
    			//initialize all again    			
    			routeId = inst.getRouteId();   			
    			length = 0;    						
    		}
        	ServiceCalendar sc = null;
    		if(agencyServiceCalendar!=null){
    			for(ServiceCalendar scs: agencyServiceCalendar){
    				if(scs.getServiceId().getId().equals(inst.getServiceId())){
    					sc = scs;
    					break;
    				}
    			}  
    		}
            List <ServiceCalendarDate> scds = new ArrayList<ServiceCalendarDate>();
    		for(ServiceCalendarDate scdss: agencyServiceCalendarDates){
    			if(scdss.getServiceId().getId().equals(inst.getServiceId())){
    				scds.add(scdss);
    			}			
    		}        	
        	daysLoop:   for (int i=0; i<dates.length; i++){  
				for(ServiceCalendarDate scd: scds){
					if(days[0][i]==Integer.parseInt(scd.getDate().getAsString())){
						if(scd.getExceptionType()==1){
							frequency ++;
							if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);	
							if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
								activeTrips.add(inst.getagencyId()+inst.getTripId());
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
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
						case 2:
							if (sc.getMonday()==1){
								frequency ++;
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
						case 3:
							if (sc.getTuesday()==1){
								frequency ++;
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
						case 4:
							if (sc.getWednesday()==1){
								frequency ++;
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
						case 5:
							if (sc.getThursday()==1){
								frequency ++;
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
						case 6:
							if (sc.getFriday()==1){
								frequency ++;
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
						case 7:
							if (sc.getSaturday()==1){
								frequency ++;
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
					}
				}
			}
    		if (frequency >0){
    			if (!inst.getUid().equals(uid)){
	    			trippop = 0;
	    			uid = inst.getUid();
	    			AgencyAndId trip = new AgencyAndId(agencyId, inst.getTripId());    			
		    		tripstops = GtfsHibernateReaderExampleMain.QueryStopsbyTripUrban(trip,urban, dbindex);
		    		if (tripstops.size()>0 && tripstops.get(0)!=null){
			    		tripstopcoords = new ArrayList<Coordinate>();
			    		for (Stop stop: tripstops){
			    			tripstopcoords.add(new Coordinate(stop.getLat(),stop.getLon()));
			    		}
			            try {
			    			trippop =EventManager.geturbanunduppopbatch(x,urban, tripstopcoords, dbindex);
			    		} catch (FactoryException e) {
			    			// TODO Auto-generated catch block
			    			e.printStackTrace();
			    		} catch (TransformException e) {
			    			// TODO Auto-generated catch block
			    			e.printStackTrace();
			    		}
		    		}
    			}
	    		if (tripstops.size()>0 && tripstops.get(0)!=null ){
	    			for (Coordinate stopcoord: tripstopcoords){
	    				if (Stopfrequencies.containsKey(stopcoord)){
	    					Stopfrequencies.put(stopcoord, Stopfrequencies.get(stopcoord)+frequency);
	    				}else{
	    					Stopfrequencies.put(stopcoord,frequency);
	    				}
	    			}
	    		}
    		}  		
    		ServiceMiles += TL * frequency;  
    		ServiceHours += frequency * inst.getTlength();
    		Stopportunity += frequency * tripstopscount; 
    		PopStopportunity += frequency * trippop;
    		setprogVal(key, (int) Math.round(index*100/totalLoad));    		
    	}                
        RouteMiles += length;
        response.ServiceStops = String.valueOf(Math.round(Stopportunity));             
        response.ServiceMiles = String.valueOf(Math.round(ServiceMiles*100.0)/100.0);
        response.ServiceHours = String.valueOf(Math.round(ServiceHours/36.0)/100.0);
        response.RouteMiles = String.valueOf(Math.round(RouteMiles*100.0)/100.0);        
        response.PopServedByService = String.valueOf(PopStopportunity);
        response.ServiceMilesPersqMile = (instance.getLandarea()>0.01) ? String.valueOf(Math.round((ServiceMiles*2.58999e6)/instance.getLandarea())/100.0):"NA";
        response.MilesofServicePerCapita = (instance.getPopulation()>0) ? String.valueOf(Math.round((ServiceMiles*100.0)/instance.getPopulation())/100.0): "NA";
        response.StopPerServiceMile = (ServiceMiles>0.01)? String.valueOf(Math.round((StopsCount*100)/ServiceMiles)/100.0): "NA";        
        String svcdays = "";
        for (String str:ServiceDays){
        	svcdays += str+"; ";
        }
        if (activeTrips.size()>0) {
        	String Hos = GtfsHibernateReaderExampleMain.QueryServiceHours(activeTrips, dbindex);
            response.HoursOfService = new SimpleDateFormat("hh:mm aa").format(new Date(Integer.parseInt(Hos.split("-")[1]) * 1000L))+"-"+new SimpleDateFormat("hh:mm aa").format(new Date(Integer.parseInt(Hos.split("-")[0]) * 1000L));
        }
        if (svcdays.length()>2){
        	svcdays= svcdays.substring(0,svcdays.length()-2);
        }        
        response.ServiceDays = svcdays;
        float sumfare =0;        
        List<Float> fprices = new ArrayList<Float>();
        if (routes.size()>0)
        	fprices = GtfsHibernateReaderExampleMain.QueryFarePriceByRoutes(routes, dbindex);
        if (fprices.size()>0){        	        
            for (Float price: fprices){
            	sumfare +=price;
            }
            response.AverageFare = String.valueOf(Math.round(sumfare*100/fprices.size())/100);
    		response.MedianFare = String.valueOf(Math.round(fprices.get((int)Math.floor(fprices.size()/2))*100)/100);
        } else{
        	response.AverageFare = "NA";
        	response.MedianFare =  "NA";
        }
        for (Map.Entry<Coordinate, Integer> entry: Stopfrequencies.entrySet()){
        	if (entry.getValue()>=L)
        		StopswithLOS.add(entry.getKey());
        }
        double popatLOS = 0;		
        try {
        	popatLOS =EventManager.geturbanunduppopbatch(x,urban, StopswithLOS, dbindex);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        response.PopServedAtLoService = String.valueOf(Math.round(10000.0*popatLOS/instance.getPopulation())/100.0);
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
	public Object getGCDSR(@QueryParam("key") double key, @QueryParam("type") String type, @QueryParam("dbindex") Integer dbindex) throws JSONException {
		if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        }
		List<CongDist> allcongdists = new ArrayList<CongDist> ();
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
	    		each.RoutesCount = String.valueOf(EventManager.getroutescountbycongdist(instance.getCongdistId(), dbindex));
			} catch (FactoryException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (TransformException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	    	each.StopsCount = String.valueOf(0);	    	
	    	try {
	    		each.StopsCount = String.valueOf(EventManager.getstopscountbycongdist(instance.getCongdistId(), dbindex));
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
     * Generates The Congressional Districts Extended report
     */ 
    @GET
    @Path("/CongdistsXR")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getCDXR(@QueryParam("congdist") String congdist, @QueryParam("day") String date,@QueryParam("x") double x, @QueryParam("l") Integer L, @QueryParam("key") double key, @QueryParam("dbindex") Integer dbindex) throws JSONException {
    	
       	if (Double.isNaN(x) || x <= 0) {
            x = STOP_SEARCH_RADIUS;
        }
       	if (L==null || L<0){
       		L = LEVEL_OF_SERVICE;
       	}
       	x = x * 1609.34;
       	if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        }
    	String[] dates = date.split(",");
    	int[][] days = daysOfWeek(dates);
    	String[] fulldates = fulldate(dates);
    	    	
    	GeoXR response = new GeoXR();
    	response.AreaId = congdist;
    	CongDist instance = EventManager.QueryCongdistbyId(congdist, dbindex);
    	response.AreaName = instance.getName();
    	long StopsCount = 0;
    	List<GeoStop> stops = new ArrayList<GeoStop>();
    	try {
    		stops = EventManager.getstopsbycongdist(instance.getCongdistId(), dbindex);
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransformException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	StopsCount = stops.size();
    	response.StopsPersqMile = String.valueOf(Math.round((StopsCount*2.58999e8)/instance.getLandarea())/100.0);
    	long pop = 0;	
		List <Coordinate> stopcoords = new ArrayList<Coordinate>();
		for (GeoStop stop: stops){
			stopcoords.add(new Coordinate(stop.getLat(),stop.getLon()));
		}
        try {
			pop =EventManager.getcongdistunduppopbatch(x, instance.getCongdistId(), stopcoords, dbindex);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        List<String> connectedCongdists = new ArrayList<String>();
        try {
        	connectedCongdists =EventManager.getconnectedcongdists(instance.getCongdistId(), dbindex);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        String ccs = "";
        for (String str : connectedCongdists){
        	if (str.length()>1)
        		ccs+=str.substring(str.length()-1,str.length())+"; ";
        }        	
        if (ccs.length()>2)
        	ccs = ccs.substring(0,ccs.length()-2);	
        response.ConnectedCommunities = ccs;        		
		response.PopWithinX = String.valueOf(pop);
		response.PopServed = String.valueOf(Math.round((1E4*pop/instance.getPopulation()))/100.0);
		response.PopUnServed = String.valueOf(Math.round(1E4-((1E4*pop/instance.getPopulation())))/100.0);
		List<CongdistTripMap> trips = new ArrayList<CongdistTripMap>();
		try {
			trips = EventManager.gettripsbycongdist(instance.getCongdistId(), dbindex);
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransformException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	    	
    	int totalLoad = trips.size();
    	double RouteMiles = 0;    	
    	double length = 0;
    	double ServiceMiles = 0;
    	double ServiceHours = 0;
        double Stopportunity = 0;
        double PopStopportunity = 0;
    	String agencyId = "";
    	String routeId = "";
    	String uid = "";
    	ArrayList<String> ServiceDays = new ArrayList<String>();
    	long trippop = 0;
    	int index = 0;
    	List <ServiceCalendar> agencyServiceCalendar = new ArrayList<ServiceCalendar>();
    	List <ServiceCalendarDate> agencyServiceCalendarDates = new ArrayList<ServiceCalendarDate>();
    	int frequency = 0;
    	int startDate;
        int endDate;
        List<String> routes = new ArrayList<String>();
        ArrayList <String> activeTrips = new ArrayList<String>();
        List <Stop> tripstops = new ArrayList<Stop>();
        List <Coordinate> tripstopcoords = new ArrayList<Coordinate>();
        HashMap<Coordinate, Integer> Stopfrequencies = new HashMap<Coordinate, Integer>();
        List<Coordinate> StopswithLOS = new ArrayList<Coordinate>();        
        for (CongdistTripMap inst: trips){
        	index++;
        	frequency = 0;
        	int tripstopscount = inst.getStopscount();
        	double TL = inst.getLength();			
    		if (TL > length) 
    			length = TL;
        	if (!agencyId.equals(inst.getagencyId_def())){     		
        		agencyServiceCalendar = GtfsHibernateReaderExampleMain.QueryCalendarforAgency(inst.getagencyId_def(), dbindex);
        		agencyServiceCalendarDates = GtfsHibernateReaderExampleMain.QueryCalendarDatesforAgency(inst.getagencyId_def(), dbindex); 
        		agencyId = inst.getagencyId_def();
        	} 
        	if (!routeId.equals(inst.getRouteId())){
        		if (!routes.contains(inst.getagencyId_def()+","+inst.getRouteId()))
        			routes.add(inst.getagencyId_def()+","+inst.getRouteId());
    			RouteMiles += length;  	        
    			//initialize all again    			
    			routeId = inst.getRouteId();   			
    			length = 0;    						
    		}
        	ServiceCalendar sc = null;
    		if(agencyServiceCalendar!=null){
    			for(ServiceCalendar scs: agencyServiceCalendar){
    				if(scs.getServiceId().getId().equals(inst.getServiceId())){
    					sc = scs;
    					break;
    				}
    			}  
    		}
            List <ServiceCalendarDate> scds = new ArrayList<ServiceCalendarDate>();
    		for(ServiceCalendarDate scdss: agencyServiceCalendarDates){
    			if(scdss.getServiceId().getId().equals(inst.getServiceId())){
    				scds.add(scdss);
    			}			
    		}        	
        	daysLoop:   for (int i=0; i<dates.length; i++){  
				for(ServiceCalendarDate scd: scds){
					if(days[0][i]==Integer.parseInt(scd.getDate().getAsString())){
						if(scd.getExceptionType()==1){
							frequency ++;
							if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);	
							if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
								activeTrips.add(inst.getagencyId()+inst.getTripId());
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
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
						case 2:
							if (sc.getMonday()==1){
								frequency ++;
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
						case 3:
							if (sc.getTuesday()==1){
								frequency ++;
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
						case 4:
							if (sc.getWednesday()==1){
								frequency ++;
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
						case 5:
							if (sc.getThursday()==1){
								frequency ++;
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
						case 6:
							if (sc.getFriday()==1){
								frequency ++;
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
						case 7:
							if (sc.getSaturday()==1){
								frequency ++;
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
					}
				}
			}
    		if (frequency >0){
    			if (!inst.getUid().equals(uid)){
	    			trippop = 0;
	    			uid = inst.getUid();
	    			AgencyAndId trip = new AgencyAndId(agencyId, inst.getTripId());    			
		    		tripstops = GtfsHibernateReaderExampleMain.QueryStopsbyTripCongdist(trip,congdist, dbindex);
		    		if (tripstops.size()>0 && tripstops.get(0)!=null){
			    		tripstopcoords = new ArrayList<Coordinate>();
			    		for (Stop stop: tripstops){
			    			tripstopcoords.add(new Coordinate(stop.getLat(),stop.getLon()));
			    		}
			            try {
			    			trippop =EventManager.getcongdistunduppopbatch(x,congdist, tripstopcoords, dbindex);
			    		} catch (FactoryException e) {
			    			// TODO Auto-generated catch block
			    			e.printStackTrace();
			    		} catch (TransformException e) {
			    			// TODO Auto-generated catch block
			    			e.printStackTrace();
			    		}
		    		}
    			}
    			if (tripstops.size()>0 && tripstops.get(0)!=null ){
	    			for (Coordinate stopcoord: tripstopcoords){
	    				if (Stopfrequencies.containsKey(stopcoord)){
	    					Stopfrequencies.put(stopcoord, Stopfrequencies.get(stopcoord)+frequency);
	    				}else{
	    					Stopfrequencies.put(stopcoord,frequency);
	    				}
	    			}
	    		}
    		}  		
    		ServiceMiles += TL * frequency;
    		ServiceHours += frequency * inst.getTlength();
    		Stopportunity += frequency * tripstopscount; 
    		PopStopportunity += frequency * trippop;
    		setprogVal(key, (int) Math.round(index*100/totalLoad));    		
    	}                
        RouteMiles += length;
        response.ServiceStops = String.valueOf(Math.round(Stopportunity));             
        response.ServiceMiles = String.valueOf(Math.round(ServiceMiles*100.0)/100.0);
        response.ServiceHours = String.valueOf(Math.round(ServiceHours/36.0)/100.0);
        response.RouteMiles = String.valueOf(Math.round(RouteMiles*100.0)/100.0);        
        response.PopServedByService = String.valueOf(PopStopportunity);
        response.ServiceMilesPersqMile = (instance.getLandarea()>0.01) ? String.valueOf(Math.round((ServiceMiles*2.58999e6)/instance.getLandarea())/100.0):"NA";
        response.MilesofServicePerCapita = (instance.getPopulation()>0) ? String.valueOf(Math.round((ServiceMiles*100.0)/instance.getPopulation())/100.0): "NA";
        response.StopPerServiceMile = (ServiceMiles>0.01)? String.valueOf(Math.round((StopsCount*100)/ServiceMiles)/100.0): "NA";        
        String svcdays = "";
        for (String str:ServiceDays){
        	svcdays += str+"; ";
        }
        if (activeTrips.size()>0) {
        	String Hos = GtfsHibernateReaderExampleMain.QueryServiceHours(activeTrips, dbindex);
            response.HoursOfService = new SimpleDateFormat("hh:mm aa").format(new Date(Integer.parseInt(Hos.split("-")[1]) * 1000L))+"-"+new SimpleDateFormat("hh:mm aa").format(new Date(Integer.parseInt(Hos.split("-")[0]) * 1000L));
        }
        if (svcdays.length()>2){
        	svcdays= svcdays.substring(0,svcdays.length()-2);
        }
        
        response.ServiceDays = svcdays;
        float sumfare =0;        
        List<Float> fprices = new ArrayList<Float>();
        if (routes.size()>0)
        	fprices = GtfsHibernateReaderExampleMain.QueryFarePriceByRoutes(routes, dbindex);
        if (fprices.size()>0){        	        
            for (Float price: fprices){
            	sumfare +=price;
            }
            response.AverageFare = String.valueOf(Math.round(sumfare*100/fprices.size())/100);
    		response.MedianFare = String.valueOf(Math.round(fprices.get((int)Math.floor(fprices.size()/2))*100)/100);
        } else{
        	response.AverageFare = "NA";
        	response.MedianFare =  "NA";
        }
        for (Map.Entry<Coordinate, Integer> entry: Stopfrequencies.entrySet()){
        	if (entry.getValue()>=L)
        		StopswithLOS.add(entry.getKey());
        }
        double popatLOS = 0;		
        try {
        	popatLOS =EventManager.getcongdistunduppopbatch(x,congdist, StopswithLOS, dbindex);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        response.PopServedAtLoService = String.valueOf(Math.round(10000.0*popatLOS/instance.getPopulation())/100.0);
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
	public Object getGORSR(@QueryParam("key") double key, @QueryParam("type") String type, @QueryParam("dbindex") Integer dbindex) throws JSONException {
		if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        }
		List<County> allcounties = new ArrayList<County> ();
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
     * Generates The ODOT Transit Regions Extended report
     */ 
    @GET
    @Path("/OdotregionsXR")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getOTXR(@QueryParam("region") String region, @QueryParam("day") String date,@QueryParam("x") double x, @QueryParam("l") Integer L, @QueryParam("key") double key, @QueryParam("dbindex") Integer dbindex) throws JSONException {
    	
       	if (Double.isNaN(x) || x <= 0) {
            x = STOP_SEARCH_RADIUS;
        }
       	if (L==null || L<0){
       		L = LEVEL_OF_SERVICE;
       	}
       	x = x * 1609.34;
       	if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
        	dbindex = default_dbindex;
        }
    	String[] dates = date.split(",");
    	int[][] days = daysOfWeek(dates);
    	String[] fulldates = fulldate(dates);
    	    	
    	GeoXR response = new GeoXR();
    	response.AreaId = region;
    	List<County> instances = EventManager.QueryOdotregionsbyId(region, dbindex);
    	response.AreaName = instances.get(0).getRegionName();
    	long StopsCount = 0;
    	List<GeoStop> stops = new ArrayList<GeoStop>();    	
    	try {
    		stops.addAll(EventManager.getstopsbyregion(region, dbindex));
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransformException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	StopsCount = stops.size();
    	long LandArea = 0;
    	long RegionPop = 0;
    	for (County instance: instances){
    		LandArea+= instance.getLandarea();
    		RegionPop+=instance.getPopulation();
    	}
    	response.StopsPersqMile = String.valueOf(Math.round((StopsCount*2.58999e8)/LandArea)/100.0);
    	long pop = 0;	
		List <Coordinate> stopcoords = new ArrayList<Coordinate>();
		for (GeoStop stop: stops){
			stopcoords.add(new Coordinate(stop.getLat(),stop.getLon()));
		}
        try {
			pop =EventManager.getregionunduppopbatch(x,region, stopcoords, dbindex);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        List<String> connectedRegions = new ArrayList<String>();
        try {
        	connectedRegions =EventManager.getconnectedregions(region, dbindex);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        String ccs = "";
        for (String str : connectedRegions){        	
        	ccs+=str+"; ";
        }
        if (ccs.length()>2)
        	ccs = ccs.substring(0,ccs.length()-2);	
        response.ConnectedCommunities = ccs;        		
		response.PopWithinX = String.valueOf(pop);		
		response.PopServed = String.valueOf(Math.round((1E4*pop/RegionPop))/100.0);
		response.PopUnServed = String.valueOf(Math.round(1E4-((1E4*pop/RegionPop)))/100.0);
		List<CountyTripMap> trips = new ArrayList<CountyTripMap>();
		try {
			trips = EventManager.gettripsbyregion(region, dbindex);
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransformException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	    	
    	int totalLoad = trips.size();
    	double RouteMiles = 0;    	
    	double length = 0;
    	double ServiceMiles = 0;
    	double ServiceHours = 0;
        double Stopportunity = 0;
        double PopStopportunity = 0;
    	String agencyId = "";
    	String routeId = "";
    	String uid = "";
    	ArrayList<String> ServiceDays = new ArrayList<String>();
    	long trippop = 0;
    	int index = 0;
    	List <ServiceCalendar> agencyServiceCalendar = new ArrayList<ServiceCalendar>();
    	List <ServiceCalendarDate> agencyServiceCalendarDates = new ArrayList<ServiceCalendarDate>();
    	int frequency = 0;
    	int startDate;
        int endDate;
        List<String> routes = new ArrayList<String>();
        ArrayList <String> activeTrips = new ArrayList<String>();
        List <Stop> tripstops = new ArrayList<Stop>();
        List <Coordinate> tripstopcoords = new ArrayList<Coordinate>();
        HashMap<Coordinate, Integer> Stopfrequencies = new HashMap<Coordinate, Integer>();
        List<Coordinate> StopswithLOS = new ArrayList<Coordinate>();        
        for (CountyTripMap inst: trips){
        	index++;
        	frequency = 0;
        	int tripstopscount = inst.getStopscount();
        	double TL = inst.getLength();			
    		if (TL > length) 
    			length = TL;
        	if (!agencyId.equals(inst.getagencyId_def())){     		
        		agencyServiceCalendar = GtfsHibernateReaderExampleMain.QueryCalendarforAgency(inst.getagencyId_def(), dbindex);
        		agencyServiceCalendarDates = GtfsHibernateReaderExampleMain.QueryCalendarDatesforAgency(inst.getagencyId_def(), dbindex); 
        		agencyId = inst.getagencyId_def();
        	} 
        	if (!routeId.equals(inst.getRouteId())){
        		if (!routes.contains(inst.getagencyId_def()+","+inst.getRouteId()))
        			routes.add(inst.getagencyId_def()+","+inst.getRouteId());
    			RouteMiles += length;  	        
    			//initialize all again    			
    			routeId = inst.getRouteId();   			
    			length = 0;    						
    		}
        	ServiceCalendar sc = null;
    		if(agencyServiceCalendar!=null){
    			for(ServiceCalendar scs: agencyServiceCalendar){
    				if(scs.getServiceId().getId().equals(inst.getServiceId())){
    					sc = scs;
    					break;
    				}
    			}  
    		}
            List <ServiceCalendarDate> scds = new ArrayList<ServiceCalendarDate>();
    		for(ServiceCalendarDate scdss: agencyServiceCalendarDates){
    			if(scdss.getServiceId().getId().equals(inst.getServiceId())){
    				scds.add(scdss);
    			}			
    		}        	
        	daysLoop:   for (int i=0; i<dates.length; i++){  
				for(ServiceCalendarDate scd: scds){
					if(days[0][i]==Integer.parseInt(scd.getDate().getAsString())){
						if(scd.getExceptionType()==1){
							frequency ++;
							if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);	
							if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
								activeTrips.add(inst.getagencyId()+inst.getTripId());
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
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
						case 2:
							if (sc.getMonday()==1){
								frequency ++;
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
						case 3:
							if (sc.getTuesday()==1){
								frequency ++;
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
						case 4:
							if (sc.getWednesday()==1){
								frequency ++;
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
						case 5:
							if (sc.getThursday()==1){
								frequency ++;
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
						case 6:
							if (sc.getFriday()==1){
								frequency ++;
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
						case 7:
							if (sc.getSaturday()==1){
								frequency ++;
								if (!ServiceDays.contains(fulldates[i]))
									ServiceDays.add(fulldates[i]);
								if (!activeTrips.contains(inst.getagencyId()+inst.getTripId()))
									activeTrips.add(inst.getagencyId()+inst.getTripId());
							}
							break;
					}
				}
			}
    		if (frequency >0){
    			if (!inst.getUid().equals(uid)){
	    			trippop = 0;
	    			uid = inst.getUid();
	    			AgencyAndId trip = new AgencyAndId(agencyId, inst.getTripId());    			
		    		tripstops = GtfsHibernateReaderExampleMain.QueryStopsbyTripRegion(trip,region, dbindex);
		    		if (tripstops.size()>0 && tripstops.get(0)!=null){
			    		tripstopcoords = new ArrayList<Coordinate>();
			    		for (Stop stop: tripstops){
			    			tripstopcoords.add(new Coordinate(stop.getLat(),stop.getLon()));
			    		}
			            try {
			    			trippop =EventManager.getregionunduppopbatch(x,region, tripstopcoords, dbindex);
			    		} catch (FactoryException e) {
			    			// TODO Auto-generated catch block
			    			e.printStackTrace();
			    		} catch (TransformException e) {
			    			// TODO Auto-generated catch block
			    			e.printStackTrace();
			    		}
		    		}
    			}
    			if (tripstops.size()>0 && tripstops.get(0)!=null ){
	    			for (Coordinate stopcoord: tripstopcoords){
	    				if (Stopfrequencies.containsKey(stopcoord)){
	    					Stopfrequencies.put(stopcoord, Stopfrequencies.get(stopcoord)+frequency);
	    				}else{
	    					Stopfrequencies.put(stopcoord,frequency);
	    				}
	    			}
	    		}
    		}  		
    		ServiceMiles += TL * frequency;
    		ServiceHours += frequency * inst.getTlength();
    		Stopportunity += frequency * tripstopscount; 
    		PopStopportunity += frequency * trippop;
    		setprogVal(key, (int) Math.round(index*100/totalLoad));    		
    	}                
        RouteMiles += length;
        response.ServiceStops = String.valueOf(Math.round(Stopportunity));             
        response.ServiceMiles = String.valueOf(Math.round(ServiceMiles*100.0)/100.0); 
        response.ServiceHours = String.valueOf(Math.round(ServiceHours/36.0)/100.0);
        response.RouteMiles = String.valueOf(Math.round(RouteMiles*100.0)/100.0);        
        response.PopServedByService = String.valueOf(PopStopportunity);
        response.ServiceMilesPersqMile = (LandArea>0.01) ? String.valueOf(Math.round((ServiceMiles*2.58999e6)/LandArea)/100.0):"NA";
        response.MilesofServicePerCapita = (RegionPop>0) ? String.valueOf(Math.round((ServiceMiles*100.0)/RegionPop)/100.0): "NA";
        response.StopPerServiceMile = (ServiceMiles>0.01)? String.valueOf(Math.round((StopsCount*100)/ServiceMiles)/100.0): "NA";
        String svcdays = "";
        for (String str:ServiceDays){
        	svcdays += str+"; ";
        }
        if (activeTrips.size()>0) {
        	String Hos = GtfsHibernateReaderExampleMain.QueryServiceHours(activeTrips, dbindex);
        	response.HoursOfService = new SimpleDateFormat("hh:mm aa").format(new Date(Integer.parseInt(Hos.split("-")[1]) * 1000L))+"-"+new SimpleDateFormat("hh:mm aa").format(new Date(Integer.parseInt(Hos.split("-")[0]) * 1000L));
        }
        if (svcdays.length()>2){
        	svcdays= svcdays.substring(0,svcdays.length()-2);
        }
        response.ServiceDays = svcdays;
        float sumfare =0;        
        List<Float> fprices = new ArrayList<Float>();
        if (routes.size()>0)
        	fprices = GtfsHibernateReaderExampleMain.QueryFarePriceByRoutes(routes, dbindex);
        if (fprices.size()>0){        	        
            for (Float price: fprices){
            	sumfare +=price;
            }
            response.AverageFare = String.valueOf(Math.round(sumfare*100/fprices.size())/100);
    		response.MedianFare = String.valueOf(Math.round(fprices.get((int)Math.floor(fprices.size()/2))*100)/100);
        } else{
        	response.AverageFare = "NA";
        	response.MedianFare =  "NA";
        }
        for (Map.Entry<Coordinate, Integer> entry: Stopfrequencies.entrySet()){
        	if (entry.getValue()>=L)
        		StopswithLOS.add(entry.getKey());
        }
        double popatLOS = 0;		
        try {
        	popatLOS =EventManager.getregionunduppopbatch(x,region, StopswithLOS, dbindex);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        response.PopServedAtLoService = String.valueOf(Math.round(10000.0*popatLOS/RegionPop)/100.0);
        try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}        
        progVal.remove(key);                
    	return response;
    }
    
    /**
	 * Generates The Summary spatial gap report
	 */
	    
	@GET
	@Path("/ConNetSR")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	public Object getGURSRd(@QueryParam("gap") double gap, @QueryParam("key") double key, @QueryParam("dbindex") Integer dbindex) throws JSONException {
		if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
       	dbindex = default_dbindex;
       }
		if (gap<=0){
      		gap=528;
      	}
		gap = gap / 3.28084;
		ClusterRList response = new ClusterRList();
		response.type = "GapReport";
		PgisEventManager.makeConnection(dbindex);
		List<agencyCluster> results= new ArrayList<agencyCluster>();
		results = PgisEventManager.agencyCluster(gap);
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
			instance.distances = StringUtils.roundjoin(acl.getMinGaps(), ";");
			response.ClusterR.add(instance);
			setprogVal(key, (int) Math.round(index*100/totalLoad));
		}
		PgisEventManager.dropConnection();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}        
        progVal.remove(key); 
		return response;
		
    }
	
	/**
	 * Generates The Extended spatial gap report
	 */
	    
	@GET
	@Path("/ConNetXR")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	public Object getGURXRd(@QueryParam("agency") String agencyId, @QueryParam("gap") double gap, @QueryParam("key") double key, @QueryParam("dbindex") Integer dbindex) throws JSONException {
		if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
       	dbindex = default_dbindex;
       }
		if (gap<=0){
      		gap=528;
      	}
		gap = gap / 3.28084;
		ClusterRList response = new ClusterRList();
		response.type = "ExtendedGapReport";
		response.agency = GtfsHibernateReaderExampleMain.QueryAgencybyid(agencyId, dbindex).getName();
		PgisEventManager.makeConnection(dbindex);
		List<agencyCluster> results= new ArrayList<agencyCluster>();
		results = PgisEventManager.agencyClusterDetails(gap, agencyId);
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
			instance.connections = StringUtils.join(acl.getConnections(), " ;");			
			response.ClusterR.add(instance);
			setprogVal(key, (int) Math.round(index*100/totalLoad));
		}
		PgisEventManager.dropConnection();
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
	public Object getStateSR(@QueryParam("key") double key, @QueryParam("dbindex") Integer dbindex) throws JSONException {
		if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
       	dbindex = default_dbindex;
       }
		int totalLoad = 2;
		int index = 0;
		setprogVal(key, (int) Math.round(index*100/totalLoad));
		GeoRList response = new GeoRList();
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
		transcounts = GtfsHibernateReaderExampleMain.QueryCounts(dbindex);
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
	public Object getStateXR(@QueryParam("day") String date,@QueryParam("x") double x, @QueryParam("l") Integer L, @QueryParam("key") double key, @QueryParam("dbindex") Integer dbindex) throws JSONException {
		if (Double.isNaN(x) || x <= 0) {
            x = STOP_SEARCH_RADIUS;
        }
       	x = x * 1609.34;		
		if (dbindex==null || dbindex<0 || dbindex>dbsize-1){
       	dbindex = default_dbindex;
        }		
		if (L==null || L<0){
       		L = LEVEL_OF_SERVICE;
       	}
		/*String[] dates = date.split(",");
    	int[][] days = daysOfWeek(dates);
    	String[] fulldates = fulldate(dates);*/
    	GeoXR response = new GeoXR();
    	int totalLoad = 2;
		int index = 0;
		setprogVal(key, (int) Math.round(index*100/totalLoad));
		
		HashMap<String, Float> FareData = new HashMap<String, Float>();
		FareData = GtfsHibernateReaderExampleMain.QueryFareData(null, dbindex);		
		response.MinFare = String.valueOf(FareData.get("min"));
		response.AverageFare = String.valueOf(FareData.get("avg"));
		response.MaxFare = String.valueOf(FareData.get("max"));
		
		/*GeoR each = new GeoR();
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
		transcounts = GtfsHibernateReaderExampleMain.QueryCounts(dbindex);
		each.StopsCount = String.valueOf(transcounts.get("stop"));
		each.RoutesCount = String.valueOf(transcounts.get("route"));
		each.AgenciesCount = String.valueOf(transcounts.get("agency"));
		response.GeoR.add(each);*/
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
}
