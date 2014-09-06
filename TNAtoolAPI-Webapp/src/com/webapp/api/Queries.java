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
import java.util.Map.Entry;

import javax.swing.JOptionPane;
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
	
	static MapDisplay mapResponse;
	//static Map<String, TmpMapRoute> tripKey;
	/**
     * Generates a sorted by agency id list of routes for the LHS menu
     *  
     */
    @GET
    @Path("/onmapreport")
    @Produces({ MediaType.APPLICATION_JSON , MediaType.APPLICATION_XML, MediaType.TEXT_XML})
    public Object getSecondmenu(@QueryParam("lat") String lats,@QueryParam("lon") String lons, @QueryParam("day") String date, @QueryParam("x") double x) throws JSONException { 
    	/*if (Double.isNaN(x) || x <= 0) {
            x = STOP_SEARCH_RADIUS;
        }
    	x = x * 1609.34;*/
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
    			Collection<Agency> agencies = GtfsHibernateReaderExampleMain.QueryAllAgencies();
    			Map<String, Agency> agencyRef = new HashMap<String, Agency>();
    			for(Agency agency: agencies){
    				agencyRef.put(agency.getId(), agency);
    			}
    			Collection<Route> routes = GtfsHibernateReaderExampleMain.QueryAllRoutes();
  		        Map<String, Route> routeRef= new HashMap<String, Route>();
  		        for(Route route: routes){
  		        	routeRef.put(route.getAgency().getId()+route.getId().getId(), route);
  		        }
  		        Map<String, TmpMapRoute> tripKey = new HashMap<String, TmpMapRoute>();
  		        List<Trip> trips = (List<Trip>) GtfsHibernateReaderExampleMain.QueryAllTrips();
				String currentAgency  = trips.get(0).getId().getAgencyId();
				List <ServiceCalendar> agencyServiceCalendar = GtfsHibernateReaderExampleMain.QueryCalendarforAgency(currentAgency);
				List <ServiceCalendarDate> agencyServiceCalendarDates = GtfsHibernateReaderExampleMain.QueryCalendarDatesforAgency(currentAgency);
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
				        agencyServiceCalendar = GtfsHibernateReaderExampleMain.QueryCalendarforAgency(trip.getServiceId().getAgencyId());
				        agencyServiceCalendarDates = GtfsHibernateReaderExampleMain.QueryCalendarDatesforAgency(trip.getServiceId().getAgencyId());
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
    				List <GeoStopRouteMap> asrms = EventManager.getstoproutemaps();
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
						stops = EventManager.getstopswithincircle(x, lat[0], lon[0]);
					}else{
						stops = EventManager.getstopswithinrectangle(lat, lon);
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
    		    				List <FareRule> fareRules = GtfsHibernateReaderExampleMain.QueryFareRuleByRoute(rou);
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
		        		centroids =EventManager.getcentroids(x, lat[0], lon[0]);
		        	}else{
		        		centroids =EventManager.getcentroidswithinrectangle(lat, lon);
		        		//centroids =EventManager.getcentroids(x, lat[0], lon[0]);
		        	}
		            
		            mg.TotalBlocks = centroids.size()+"";
		            tracts = EventManager.gettracts();
		            counties = EventManager.getcounties();
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
	                        			List<StopTime> st = GtfsHibernateReaderExampleMain.Querystoptimebytrip(trip.getId());
	                        			
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
	                        			List<StopTime> st = GtfsHibernateReaderExampleMain.Querystoptimebytrip(trip.getId());
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
       	x = x * 1609.34;    	
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
        long PopStopportunity = 0;
        
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
    		long trippop = 0;
    		if (frequency>0){
	    		List <Stop> tripstops = GtfsHibernateReaderExampleMain.QueryStopsbyTrip(instance.getId());
	    		List <Coordinate> tripstopcoords = new ArrayList<Coordinate>();
	    		for (Stop stop: tripstops){
	    			tripstopcoords.add(new Coordinate(stop.getLat(),stop.getLon()));
	    		}
	            try {
	    			trippop =EventManager.getunduppopbatch(x, tripstopcoords);
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
    		PopStopportunity += frequency * trippop;
    		setprogVal(key, (int) Math.round(index*100/totalLoad));
    	}
        RouteMiles += length;
        response.ServiceStops = String.valueOf(Math.round(Stopportunity));
        response.ServiceMiles = String.valueOf(Math.round(ServiceMiles*100.0)/100.0); 
        response.RouteMiles = String.valueOf(Math.round(RouteMiles*100.0)/100.0);
        long pop = 0;
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
    public Object getAXRS(@QueryParam("agency") String agency,@QueryParam("x") double x){
    	if (Double.isNaN(x) || x <= 0) {
            x = STOP_SEARCH_RADIUS;
        }
       	x = x * 1609.34;
    	AgencyXR response = new AgencyXR();
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
        response.PopServed = String.valueOf(pop);
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
    	List<Stop> tmpStops = GtfsHibernateReaderExampleMain.QueryStopsbyAgency(agency);
    	String defAgency = tmpStops.get(0).getId().getAgencyId();
    	for (String instance: stopIds){
    		
    		AgencyAndId stopId = new AgencyAndId(defAgency,instance);
    		Stop stop = GtfsHibernateReaderExampleMain.QueryStopbyid(stopId);
    		//System.out.println(stop.toString());
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
	    	List <Stop> stops = GtfsHibernateReaderExampleMain.QueryStopsbyAgency(instance.getId());
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
	    double PopStopportunity = 0;
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
			long trippop = 0;
			
			//int stops = 0;
			if (!routeId.equals(instance.getRoute().getId().getId())){		
				each.RouteLength = String.valueOf(Math.round(length*100.0)/100.0);                
		        each.ServiceMiles = String.valueOf(Math.round(ServiceMiles*100.0)/100.0); 
		        each.Stopportunity = String.valueOf(Math.round(Stopportunity));
		        each.PopStopportunity = String.valueOf(Math.round(Stopportunity));
		        long pop = 0;
		        List <Stop> Rstops = GtfsHibernateReaderExampleMain.QueryStopsbyRoute(instance.getRoute().getId());
				List <Coordinate> Rstopcoords = new ArrayList<Coordinate>();
				for (Stop stop: Rstops){
					Rstopcoords.add(new Coordinate(stop.getLat(),stop.getLon()));
				}
		        try {
					pop =EventManager.getunduppopbatch(x, Rstopcoords);
				} catch (FactoryException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TransformException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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
			List <Stop> tripstops = GtfsHibernateReaderExampleMain.QueryStopsbyTrip(instance.getId());
    		List <Coordinate> tripstopcoords = new ArrayList<Coordinate>();
    		for (Stop stop: tripstops){
    			tripstopcoords.add(new Coordinate(stop.getLat(),stop.getLon()));
    		}
            try {
    			trippop =EventManager.getunduppopbatch(x, tripstopcoords);
    		} catch (FactoryException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} catch (TransformException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
            PopStopportunity += frequency * trippop;
			ServiceMiles += TL * frequency;  
			Stopportunity += frequency * stops;
			
			setprogVal(key, (int) Math.round(index*100/totalLoad));
		}
		each.RouteLength = String.valueOf(Math.round(length*100.0)/100.0)+"";                
	    each.ServiceMiles = String.valueOf(Math.round(ServiceMiles*100.0)/100.0)+""; 
	    each.Stopportunity = String.valueOf(Math.round(Stopportunity))+"";
	    each.PopStopportunity = String.valueOf(Math.round(PopStopportunity))+"";	    
	    long pop = 0;
        List <Stop> Rstops = GtfsHibernateReaderExampleMain.QueryStopsbyRoute(alltrips.get(alltrips.size()-1).getRoute().getId());
		List <Coordinate> Rstopcoords = new ArrayList<Coordinate>();
		for (Stop stop: Rstops){
			Rstopcoords.add(new Coordinate(stop.getLat(),stop.getLon()));
		}
        try {
			pop =EventManager.getunduppopbatch(x, Rstopcoords);
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
     * Generates The Counties Extended report
     */ 
    @GET
    @Path("/CountiesXR")
    @Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
    public Object getCXR(@QueryParam("county") String county, @QueryParam("day") String date,@QueryParam("x") double x, @QueryParam("key") double key) throws JSONException {
    	
       	if (Double.isNaN(x) || x <= 0) {
            x = STOP_SEARCH_RADIUS;
        }
       	x = x * 1609.34;    	
    	String[] dates = date.split(",");
    	int[][] days = daysOfWeek(dates);
    	    	
    	GeoXR response = new GeoXR();
    	response.AreaId = county;
    	County instance = EventManager.QueryCountybyId(county);
    	response.AreaName = instance.getName();
    	long StopsCount = 0;
    	List<GeoStop> stops = new ArrayList<GeoStop>();
    	try {
    		stops = EventManager.getstopsbycounty(instance.getCountyId());
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
			pop =EventManager.getunduppopbatch(x, stopcoords);
		} catch (FactoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		response.PopWithinX = String.valueOf(pop);
		response.PopServed = String.valueOf(Math.round((1E4*pop/instance.getPopulation()))/100.0);
		response.PopUnServed = String.valueOf(Math.round(1E4-((1E4*pop/instance.getPopulation())))/100.0);
		List<CountyTripMap> trips = new ArrayList<CountyTripMap>();
		try {
			trips = EventManager.gettripsbycounty(county);
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
        double Stopportunity = 0;
        double PopStopportunity = 0;
    	String agencyId = "";
    	String routeId = "";
    	int index = 0;
    	List <ServiceCalendar> agencyServiceCalendar = new ArrayList<ServiceCalendar>();
    	List <ServiceCalendarDate> agencyServiceCalendarDates = new ArrayList<ServiceCalendarDate>();
    	int frequency = 0;
    	int startDate;
        int endDate;
        
        for (CountyTripMap inst: trips){
        	index++;
        	frequency = 0;
        	int tripstopscount = inst.getStopscount();
        	double TL = inst.getLength();			
    		if (TL > length) 
    			length = TL;
        	if (!agencyId.equals(inst.getagencyId_def())){     		
        		agencyServiceCalendar = GtfsHibernateReaderExampleMain.QueryCalendarforAgency(inst.getagencyId_def());
        		agencyServiceCalendarDates = GtfsHibernateReaderExampleMain.QueryCalendarDatesforAgency(inst.getagencyId_def()); 
        		agencyId = inst.getagencyId_def();
        	} 
        	if (!routeId.equals(inst.getRouteId())){		
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
    		
    		long trippop = 0;
    		if (frequency >0){
    			AgencyAndId trip = new AgencyAndId(agencyId, inst.getTripId());    			
	    		List <Stop> tripstops = GtfsHibernateReaderExampleMain.QueryStopsbyTripCounty(trip,county);
	    		if (tripstops.size()>0 && tripstops.get(0)!=null){
		    		List <Coordinate> tripstopcoords = new ArrayList<Coordinate>();
		    		for (Stop stop: tripstops){
		    			tripstopcoords.add(new Coordinate(stop.getLat(),stop.getLon()));
		    		}
		            try {
		    			trippop =EventManager.getunduppopbatch(x, tripstopcoords);
		    		} catch (FactoryException e) {
		    			// TODO Auto-generated catch block
		    			e.printStackTrace();
		    		} catch (TransformException e) {
		    			// TODO Auto-generated catch block
		    			e.printStackTrace();
		    		}
	    		}	            
    		}  		
    		ServiceMiles += TL * frequency;  
    		Stopportunity += frequency * tripstopscount; 
    		PopStopportunity += frequency * trippop;
    		setprogVal(key, (int) Math.round(index*100/totalLoad));    		
    	}                
        RouteMiles += length;
        response.ServiceStops = String.valueOf(Math.round(Stopportunity));             
        response.ServiceMiles = String.valueOf(Math.round(ServiceMiles*100.0)/100.0); 
        response.RouteMiles = String.valueOf(Math.round(RouteMiles*100.0)/100.0);        
        response.PopServedByService = String.valueOf(PopStopportunity);
        response.ServiceMilesPersqMile = String.valueOf(Math.round((ServiceMiles*2.58999e6)/instance.getLandarea())/100.0);
        response.MilesofServicePerCapita = String.valueOf(Math.round((ServiceMiles*100.0)/instance.getPopulation())/100.0);
        response.StopPerServiceMile = String.valueOf(Math.round((StopsCount*2.58999e6)/instance.getLandarea())/100.0);    	        
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
	    	each.Name = instance.getLongname();
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
	
	/**
	 * Generates The urban areas Summary report
	 */
	    
	@GET
	@Path("/GeoUASR")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	public Object getGUASR(@QueryParam("key") double key, @QueryParam("type") String type ) throws JSONException {
		List<Urban> allurbanareas = new ArrayList<Urban> ();
		try {
			allurbanareas = EventManager.geturban();
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
	    		each.RoutesCount = String.valueOf(EventManager.getroutescountbyurban(instance.getUrbanId()));
			} catch (FactoryException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (TransformException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	    	each.StopsCount = String.valueOf(0);	    	
	    	try {
	    		each.StopsCount = String.valueOf(EventManager.getstopscountbyurban(instance.getUrbanId()));
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
	 * Generates The Congressional Districts Summary report
	 */
	
	@GET
	@Path("/GeoCDSR")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	public Object getGCDSR(@QueryParam("key") double key, @QueryParam("type") String type ) throws JSONException {
		List<CongDist> allcongdists = new ArrayList<CongDist> ();
		try {
			allcongdists = EventManager.getcongdist();
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
	    		each.RoutesCount = String.valueOf(EventManager.getroutescountbycongdist(instance.getCongdistId()));
			} catch (FactoryException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (TransformException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
	    	each.StopsCount = String.valueOf(0);	    	
	    	try {
	    		each.StopsCount = String.valueOf(EventManager.getstopscountbycongdist(instance.getCongdistId()));
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
	 * Generates ODOT Regions Summary report
	 */
	    
	@GET
	@Path("/GeoORSR")
	@Produces({ MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML, MediaType.TEXT_XML })
	public Object getGORSR(@QueryParam("key") double key, @QueryParam("type") String type ) throws JSONException {
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
			    		each.StopsCount = String.valueOf(EventManager.getstopscountbyregion(regionId));
					} catch (FactoryException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					} catch (TransformException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
			    	each.RoutesCount = String.valueOf(0);
			    	try {
			    		each.RoutesCount = String.valueOf(EventManager.getroutescountsbyregion(regionId));
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
    		each.StopsCount = String.valueOf(EventManager.getstopscountbyregion(regionId));
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransformException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    	each.RoutesCount = String.valueOf(0);
    	try {
    		each.RoutesCount = String.valueOf(EventManager.getroutescountsbyregion(regionId));
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
}
