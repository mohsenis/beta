package com.library.samples;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement; 
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.onebusaway.gtfs.impl.Databases;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.library.model.*;
import com.library.util.Types;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class PgisEventManager {
	//public static Connection connection;
	
	private static Connection makeConnection(int dbindex){
		String url = "";
		switch (dbindex){
		case 0:
			url = "gtfsdb";
			break;
		case 1:
			url = "gtfsdb1";
			break;
		}
		Connection response = null;
		try {
		Class.forName("org.postgresql.Driver");
		response = DriverManager
           .getConnection(Databases.connectionURLs[dbindex],
           Databases.usernames[dbindex], Databases.passwords[dbindex]);
		}catch ( Exception e ) {
	        System.err.println( e.getClass().getName()+": "+ e.getMessage() );
	        System.exit(0);
	      }
		return response;
	}
	
	private static void dropConnection(Connection connection){
		try {
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/////GEO AREA EXTENDED REPORTS QUERIES

	///// CONNECTED AGENCIES ON-MAP REPORT QUERIES
	/**
	 * Queries stops within a certain distance of a given stop while filtering the agencies.
	 */
	public static CAStopsList getConnectedStops(double lat, double lon, int gap, String agencies, int dbindex){
		CAStopsList results=new CAStopsList();	// This object is declared to hold the stops
		
		Connection connection = makeConnection(dbindex);
	    Statement stmt = null;
	    String query = "WITH main AS (SELECT agencyid, id, name, description, lat, lon "
  						+ "FROM gtfs_stops WHERE agencyid = ANY('{"+agencies+"}'::text[]) "
  						+ "AND ST_Dwithin(ST_transform(ST_setsrid(ST_MakePoint("+lon+", "+lat+"),4326), 2993), location, "+gap+")) "
  						+ "SELECT main.id, main.name stopname, main.description, main.agencyid, gtfs_agencies.name agencyname, main.lat, main.lon "
  						+ "FROM main INNER JOIN gtfs_agencies ON main.agencyid=gtfs_agencies.id";
	    System.out.println(query);

	    try {
			stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			
			while(rs.next()){
				CAStop instance  = new CAStop();
				
				instance.id = rs.getString("id");
				instance.name = rs.getString("stopname");
				instance.agencyName = rs.getString("agencyname");
				instance.agencyId = rs.getString("agencyid");
				instance.lat = rs.getString("lat");
				instance.lon = rs.getString("lon");
				results.stopsList.add(instance);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	      
		return results;
	}
	
	/////GEO AREA EXTENDED REORTS QUERIES
	
	/**
	 *Queries Route miles for a geographic area
	 */
	public static float RouteMiles(int type, String areaId, String username, int dbindex) 
    {	
	  Connection connection = makeConnection(dbindex);
      Statement stmt = null;
      float RouteMiles = 0;
      String query = "with aids as (select distinct agency_id as aid from gtfs_selected_feeds where username='"+username+"'), trips as (select agencyid, routeid, "
      		+ "round(max(length)::numeric,2) as length from "+Types.getTripMapTableName(type)+" map inner join aids on map.agencyid_def=aids.aid where "+
        Types.getIdColumnName(type)+" ='"+areaId +"'  group by agencyid, routeid) select sum(length) as routemiles from trips;";
      try {
        stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(query);        
        while ( rs.next() ) {
        	RouteMiles = rs.getFloat("routemiles");                     
        }
        rs.close();
        stmt.close();        
      } catch ( Exception e ) {
        System.err.println( e.getClass().getName()+": "+ e.getMessage() );
        System.exit(0);
      }
      dropConnection(connection);      
      return RouteMiles;
    }
	
	/**
	 *Queries Fare Information for a geographic area. keys are; minfare, maxfare, medianfare, averagefare
	 */
	public static HashMap<String, Float> FareInfo(int type, String[] date, String[] day, String areaId, String username, int dbindex) 
    {	
	  Connection connection = makeConnection(dbindex);
      Statement stmt = null;
      HashMap<String, Float> response = new HashMap<String, Float>();
      ArrayList<Float> faredata = new ArrayList<Float>();
      String query = "with aids as (select distinct agency_id as aid from gtfs_selected_feeds where username='"+username+"'), svcids as (";
      for (int i=0; i<date.length; i++){
    	  query+= "(select serviceid_agencyid, serviceid_id from gtfs_calendars gc inner join aids on gc.serviceid_agencyid = aids.aid where startdate::int<="+date[i]+
    			  " and enddate::int>="+date[i]+" and "+day[i]+" = 1 and serviceid_agencyid||serviceid_id not in (select serviceid_agencyid||serviceid_id from " +
    			  "gtfs_calendar_dates where date='"+date[i]+"' and exceptiontype=2) union select serviceid_agencyid, "+
    			  "serviceid_id from gtfs_calendar_dates gcd inner join aids on gcd.serviceid_agencyid = aids.aid where date='"+date[i]+"' and exceptiontype=1)";
    	  if (i+1<date.length)
				query+=" union all ";
		}      
    query +="), trips as (select trip.route_agencyid as aid, trip.route_id as routeid from svcids inner join gtfs_trips trip using(serviceid_agencyid, serviceid_id) inner join "+
    		Types.getTripMapTableName(type)+" map on trip.id = map.tripid and trip.agencyid = map.agencyid where map."+Types.getIdColumnName(type)+"='"+areaId+"'), fare as "+
    		"(select frule.route_agencyid as aid, frule.route_id as routeid, ftrb.price as price from gtfs_fare_rules frule inner join gtfs_fare_attributes ftrb on "+
    		"ftrb.agencyid= frule.fare_agencyid and ftrb.id=frule.fare_id inner join aids on aids.aid=ftrb.agencyid) select round(avg(price)::numeric,2) as fare from fare inner "+
    		"join trips using (aid,routeid) group by fare.routeid order by fare";
    //System.out.println(query);
      try {
        stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(query);        
        while ( rs.next() ) {
        	faredata.add(rs.getFloat("fare"));                      
        }
        rs.close();
        stmt.close();        
      } catch ( Exception e ) {
        System.err.println( e.getClass().getName()+": "+ e.getMessage() );
        System.exit(0);
      }
      dropConnection(connection);
      if (faredata.size()>0){
    	  Collections.sort(faredata);     
    	  response.put("minfare", faredata.get(0));
    	  response.put("maxfare", faredata.get(faredata.size()-1));    	  
		  if (faredata.size()%2==0){
				response.put("medianfare", (float)(Math.round((faredata.get(faredata.size()/2)+faredata.get((faredata.size()/2)-1))*100.00/2)/100.00));		
		  } else {
				response.put("medianfare", faredata.get((int)(Math.ceil(faredata.size()/2))));		
		  }
		  float faresum = 0;
	      for (int i=0; i<faredata.size();i++){
	    	  faresum+=faredata.get(i);
	      }
	      response.put("averagefare", (float)(Math.round(faresum*100.00/faredata.size())/100.00));
      } else {
    	  response.put("minfare", null);
    	  response.put("maxfare", null);
    	  response.put("medianfare", null);
    	  response.put("averagefare", null);
      }
      return response;
    }
	
	/** Queries all P&Rs nationwide grouped by county*/
	public static ParknRideCountiesList getCountiesPnrs(int dbindex){
		ParknRideCountiesList results = new ParknRideCountiesList();
		Connection connection = makeConnection(dbindex);
		Statement stmt = null;
		String querytext =	"SELECT pr.countyid, pr.county, count(pr.countyid) count, sum(pr.spaces) spaces, sum(pr.accessiblespaces) accessiblespaces " +
				"FROM parknride pr " + 
				"GROUP BY pr.countyid, pr.county;";
		try {
	        stmt = connection.createStatement();
	        ResultSet rs = stmt.executeQuery(querytext); 
	        List<ParknRideCounties> list=new ArrayList<ParknRideCounties>();
	        while ( rs.next() ) {
	        	ParknRideCounties instance = new ParknRideCounties();
	        	instance.countyId = rs.getString("countyid");
	        	instance.cname = rs.getString("county");
	        	instance.count = rs.getString("count");
	        	instance.spaces = rs.getString("spaces");
	        	instance.accessibleSpaces = rs.getString("accessiblespaces");
	        	list.add(instance);
	        }
//	        results.PnrCountiesList.add(instance);
	        rs.close();
	        stmt.close(); 
	        results.PnrCountiesList=list;
	      } catch ( Exception e ) {
	        System.err.println( e.getClass().getName()+": "+ e.getMessage() );
	        e.getStackTrace();
	        //System.exit(0);
	      }
	      dropConnection(connection);
	      
	      return results;
	}
	
	/** Queries all detailed information on all the PnRs within a given county.
	 * 
	 */
	public static PnrInCountyList  getPnrsInCounty(int countyId, int radius, int dbindex, String username){
		PnrInCountyList results = new PnrInCountyList();
		String id = countyId+"";
		Connection connection = makeConnection(dbindex);
		Statement stmt = null;
		String query =	"WITH aids AS ("
						+ "SELECT agency_id AS aid "
						+ "FROM gtfs_selected_feeds "
						+ "WHERE username='"+username+"'), "
						
						+ "temp1 AS ("
						+ "SELECT parknride.*,"+
							"	gtfs_stops.name stopname, "+
							"	gtfs_stops.id stopid "+
							"FROM parknride CROSS JOIN gtfs_stops "+
							"WHERE ST_dwithin(parknride.geom, gtfs_stops.location, "+radius+") "+
							"ORDER BY pnrid), "+
			
							"temp2 AS (SELECT temp1.*, gtfs_stop_route_map.agencyid , gtfs_stop_route_map.routeid "+
							"FROM temp1 CROSS JOIN gtfs_stop_route_map "+
							"WHERE temp1.stopid=gtfs_stop_route_map.stopid), "+
			
							"temp3 AS (SELECT temp2.*, gtfs_routes.longname routename, gtfs_routes.agencyid agenid "+
							"FROM temp2 CROSS JOIN gtfs_routes "+
							"WHERE temp2.routeid = gtfs_routes.id), "
							+ "temp4 AS ("
							+ "	SELECT temp3.* "
							+ "	FROM temp3 INNER JOIN aids "
							+ "	ON temp3.agenid = aids.aid "
							+ "	)," +
							
							"temp5 AS (SELECT temp4.*, gtfs_agencies.name agencyname "+
							"FROM temp4 CROSS JOIN gtfs_agencies "+
							"WHERE temp4.agenid=gtfs_agencies.id), "+
			
							"temp6 AS (SELECT pnrid,  "+
							"	lotname, "+
							"	location, "+
							"	city, "+
							"	zipcode, "+
							"	spaces,  "+
							"	accessiblespaces, "+
							"	lat, "+
							"	lon, "+
							"	bikerackspaces, "+
							"	bikelockerspaces,  "+
							"	electricvehiclespaces, "+ 
							"	carsharing, "+
							"	transitservice, "+
							"	availability, "+
							"	timelimit, "+
							"	restroom, "+
							"	benches, "+
							"	shelter, "+
							"	indoorwaitingarea, "+ 
							"	trashcan, "+
							"	lighting, "+
							"	securitycameras, "+
							"	sidewalks, "+
							"	pnrsignage, "+
							"	lotsurface, "+
							"	propertyowner, "+
							"	localexpert, "+
							"	county, "+
							"   countyid, "+
							"	array_agg(agencyname) agencies,"
							+ "	array_agg(stopid) stopids,"
							+ "	array_agg(stopname) stopnames,"
							+ "	array_agg(routeid) routeids,"
							+ "	array_agg(routename) routenames,"
							+ "array_agg(stopname) stops, "+
							" count(stopname) count "+
							"FROM temp5 "+
							"GROUP BY pnrid, "+
							"lotname, "+
							"	location, "+
							"	city, "+
							"	zipcode, "+
							"	spaces, "+
							"	accessiblespaces, "+
							"	lat, "+
							"	lon, "+
							"	bikerackspaces, "+
							"	bikelockerspaces, "+
							"	electricvehiclespaces, "+
							"	carsharing, "+
							"	transitservice, "+
							"	availability, "+
							"	timelimit, "+
							"	restroom, "+
							"	benches, "+
							"	shelter, "+
							"	indoorwaitingarea,"+
							"	trashcan, "+
							"	lighting, "+
							"	securitycameras, "+
							"	sidewalks, "+
							"	pnrsignage, "+
							"	lotsurface,"+
							"	propertyowner, "+
							"	localexpert, "+
							"	county, "+
							"   countyid),"
							+ " temp7 AS (SELECT parknride.*, temp6.agencies, temp6.stopids, temp6.stopnames, temp6.routeids, temp6.routenames FROM parknride LEFT OUTER JOIN temp6"
							+ "	ON temp6.pnrid = parknride.pnrid)"
							
							+ " SELECT * FROM temp7"
							+ " WHERE countyid='"+id+"'"
							+ "	ORDER BY pnrid;";
		
		try {
	        stmt = connection.createStatement();
	        ResultSet rs = stmt.executeQuery(query); 
	        List<PnrInCounty> list=new ArrayList<PnrInCounty>();
	        while ( rs.next() ) {
	        	PnrInCounty instance = new PnrInCounty();
	        	instance.pnrid = rs.getString("pnrid");
	        	instance.lotname = rs.getString("lotname");
	        	instance.city = rs.getString("city");
	        	instance.zipcode=rs.getString("zipcode");
	        	instance.location = rs.getString("location");	  
	        	if (rs.getString("spaces").equals("0"))
	        		instance.spaces = "N/A";
	        	else
	        		instance.spaces =instance.spaces = rs.getString("spaces");	
	        	if (rs.getString("accessiblespaces").equals("0"))
	        		instance.accessiblespaces = "N/A";
	        	else 
	        		instance.accessiblespaces = rs.getString("accessiblespaces");	
	        	instance.transitservices = rs.getString("transitservice");
	        	if (rs.getString("lat").length()>8)
	        		instance.lat = rs.getString("lat").substring(0,7);
	        	else
	        		instance.lat = rs.getString("lat");
	        	if (rs.getString("lon").length()>10)
	        		instance.lon = rs.getString("lon").substring(0,9);
	        	else
	        		instance.lon = rs.getString("lon");
	        	if (rs.getString("bikerackspaces").equals("0"))
	        		instance.bikerackspaces = "N/A";
	        	else
	        		instance.bikerackspaces = rs.getString("bikerackspaces");
	        	if (rs.getString("bikelockerspaces").equals("0"))
	        		instance.bikelockerspaces = "N/A";
	        	else
	        		instance.bikelockerspaces = rs.getString("bikelockerspaces");
	        	if (rs.getString("electricvehiclespaces").equals("0"))
	        		instance.electricvehiclespaces = "N/A";
	        	else
	        		instance.electricvehiclespaces = rs.getString("electricvehiclespaces");
	        	instance.carsharing = rs.getString("carsharing");
	        	instance.availability = rs.getString("availability");
	        	instance.timelimit = rs.getString("timelimit");
	        	instance.restroom = rs.getString("restroom");
	        	instance.benches = rs.getString("benches");
	        	instance.shelter = rs.getString("shelter");
	        	instance.indoorwaitingarea = rs.getString("indoorwaitingarea");
	        	instance.trashcan = rs.getString("trashcan");
	        	instance.lighting = rs.getString("lighting");
	        	instance.securitycameras = rs.getString("securitycameras");
	        	instance.sidewalks = rs.getString("sidewalks");
	        	instance.pnrsignage = rs.getString("pnrsignage");
	        	instance.lotsurface = rs.getString("lotsurface");
	        	instance.propertyowner = rs.getString("propertyowner");
	        	instance.localexpert = rs.getString("localexpert");
	        	instance.county = rs.getString("county");
	        	if (rs.getString("routenames")==null){
	        		instance.agencies = "N/A";
	        		instance.stopids = "N/A";
	        		instance.stopnames = "N/A";
	        		instance.routeids = "N/A";
	        		instance.routenames = "N/A";
	        	}else{
	        		instance.agencies = rs.getString("agencies");
		        	instance.stopids = rs.getString("stopids");
		        	instance.stopnames = rs.getString("stopnames");
		        	instance.routeids = rs.getString("routeids");
		        	instance.routenames = rs.getString("routenames");
		        		 
	        	}
	        	list.add(instance);
	        }
	        rs.close();
	        stmt.close(); 
	        results.PnrCountiesList=list;
	      } catch ( Exception e ) {
	        System.err.println( e.getClass().getName()+": "+ e.getMessage() );
	        e.getStackTrace();
	        //System.exit(0);
	      }
	      dropConnection(connection);
		
		return results;
	}
	
	/**
	 * Queries employment data on a given area (based on the reportType)
	 */
	public static EmpDataList getEmpData(String DS, String reportType, int dbindex, String username){
		String dataSet = DS;
		EmpDataList results = new EmpDataList();
		Connection connection = makeConnection(dbindex);
	    Statement stmt = null;
	    String query = "";
	    String criteria1 = "";
	    String criteria2 = "";
	    System.out.println(reportType);
	    
	    if (reportType.equals("Counties")){
	    	criteria1 = "countyid";
	    	criteria2 = "SELECT  census_counties.cname  AS name, temp2.*"
			    		+ " FROM temp2 INNER JOIN census_counties"
			    		+ " ON temp2.id = census_counties.countyid" ;
	    }else if (reportType.equals("Census Places")){
	    	criteria1 = "placeid";
	    	criteria2 = "SELECT  census_places.pname  AS name, temp2.*"
			    		+ " FROM temp2 INNER JOIN census_places"
			    		+ " ON temp2.id = census_places.placeid" ;
	    }
	    else if (reportType.equals("ODOT Transit Regions")){
	    	criteria1 = "regionid";
	    	criteria2 = "SELECT  concat('Region ',temp2.id) AS name, temp2.*"
	    				+ " FROM temp2 ";
	    }
	    else if (reportType.equals("Urban Areas")){
	    	criteria1 = "urbanid";
	    	criteria2 = " SELECT census_urbans.uname AS name, temp2.*"
			    		+ " FROM temp2 INNER JOIN census_urbans"
			    		+ " ON temp2.id = census_urbans.urbanid";
	    }
	    else if (reportType.equals("Congressional Districts")){
	    	criteria1 = "congdistid";
	    	criteria2 = " SELECT census_congdists.cname AS name, temp2.*"
			    		+ " FROM temp2 INNER JOIN census_congdists"
			    		+ " ON concat('410',temp2.id) = census_congdists.congdistid";
	    }
	    
	    query = "WITH temp1 AS (SELECT "
	    		+ "		LEFT(census_blocks.blockid,5) countyid,"
	    		+ "		census_blocks.blockid,"
	    		+ "		census_blocks.placeid,"
	    		+ "		census_blocks.congdistid,"
	    		+ "		census_blocks.regionid,"
	    		+ "		census_blocks.urbanid,"
	    		+ "		census_blocks.population"
	    		+ "	FROM census_blocks"
	    		+ "		),"
	    		+ "temp2 AS ("
	    		+ "		SELECT"
	    		+ "		  temp1." + criteria1 + " id,"
	    		+ "		  COALESCE(SUM(population),0)::int population,"
	    		+ "		  COALESCE(SUM(c000), 0)::int c000,"
	    		+ "		  COALESCE(SUM(ca01), 0)::int ca01,"
	    		+ "		  COALESCE(SUM(ca02), 0)::int ca02,"
	    		+ "		  COALESCE(SUM(ca03), 0)::int ca03,"
	    		+ "		  COALESCE(SUM(ce01), 0)::int ce01,"
	    		+ "		  COALESCE(SUM(ce02), 0)::int ce02,"
	    		+ "		  COALESCE(SUM(ce03), 0)::int ce03,"
	    		+ "		  COALESCE(SUM(cns01), 0)::int cns01,"
	    		+ "		  COALESCE(SUM(cns02), 0)::int cns02,"
	    		+ "		  COALESCE(SUM(cns03), 0)::int cns03,"
	    		+ "		  COALESCE(SUM(cns04), 0)::int cns04,"
	    		+ "		  COALESCE(SUM(cns05), 0)::int cns05,"
	    		+ "		  COALESCE(SUM(cns06), 0)::int cns06,"
	    		+ "		  COALESCE(SUM(cns07), 0)::int cns07,"
	    		+ "		  COALESCE(SUM(cns08), 0)::int cns08,"
	    		+ "		  COALESCE(SUM(cns09), 0)::int cns09,"
	    		+ "		  COALESCE(SUM(cns10), 0)::int cns10,"
	    		+ "		  COALESCE(SUM(cns11), 0)::int cns11,"
	    		+ "		  COALESCE(SUM(cns12), 0)::int cns12,"
	    		+ "		  COALESCE(SUM(cns13), 0)::int cns13,"
	    		+ "		  COALESCE(SUM(cns14), 0)::int cns14,"
	    		+ "		  COALESCE(SUM(cns15), 0)::int cns15,"
	    		+ "		  COALESCE(SUM(cns16), 0)::int cns16,"
	    		+ "		  COALESCE(SUM(cns17), 0)::int cns17,"
	    		+ "		  COALESCE(SUM(cns18), 0)::int cns18,"
	    		+ "		  COALESCE(SUM(cns19), 0)::int cns19,"
	    		+ "		  COALESCE(SUM(cns20), 0)::int cns20,"
	    		+ "		  COALESCE(SUM(cr01), 0)::int cr01,"
	    		+ "		  COALESCE(SUM(cr02), 0)::int cr02,"
	    		+ "		  COALESCE(SUM(cr03), 0)::int cr03,"
	    		+ "		  COALESCE(SUM(cr04), 0)::int cr04,"
	    		+ "		  COALESCE(SUM(cr05), 0)::int cr05,"
	    		+ "		  COALESCE(SUM(cr07), 0)::int cr07,"
	    		+ "		  COALESCE(SUM(ct01), 0)::int ct01,"
	    		+ "		  COALESCE(SUM(ct02), 0)::int ct02,"
	    		+ "		  COALESCE(SUM(cd01), 0)::int cd01,"
	    		+ "		  COALESCE(SUM(cd02), 0)::int cd02,"
	    		+ "		  COALESCE(SUM(cd03), 0)::int cd03,"
	    		+ "		  COALESCE(SUM(cd04), 0)::int cd04,"
	    		+ "		  COALESCE(SUM(cs01), 0)::int cs01,"
	    		+ "		  COALESCE(SUM(cs02), 0)::int cs02"
	    		+ ""
	    		+ "		FROM temp1 INNER JOIN " + dataSet 
	    		+ "		ON temp1.blockid = " + dataSet + ".blockid"
	    		+ ""
	    		+ "		GROUP BY " + criteria1
	    		+ "		ORDER BY " + criteria1
	    		+ "	) "
	    		+ criteria2;
	    System.out.println(query);

	    try {
	    	stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(query); 
			
			while (rs.next()){
				EmpData i = new EmpData();
				i.id = rs.getString("id");
				i.name = rs.getString("name");
				i.population = rs.getInt("population");
				i.c000 = rs.getInt("c000");
				i.ca01 = rs.getInt("ca01");
				i.ca02 = rs.getInt("ca02");
				i.ca03 = rs.getInt("ca03");
				i.ce01 = rs.getInt("ce01");
				i.ce02 = rs.getInt("ce02");
				i.ce03 = rs.getInt("ce03");				
				i.cns01 = rs.getInt("cns01");
				i.cns02 = rs.getInt("cns02");
				i.cns03 = rs.getInt("cns03");
				i.cns04 = rs.getInt("cns04");
				i.cns05 = rs.getInt("cns05");
				i.cns06 = rs.getInt("cns06");
				i.cns07 = rs.getInt("cns07");
				i.cns08 = rs.getInt("cns08");
				i.cns09 = rs.getInt("cns09");
				i.cns10 = rs.getInt("cns10");
				i.cns11 = rs.getInt("cns11");
				i.cns12 = rs.getInt("cns12");
				i.cns13 = rs.getInt("cns13");
				i.cns14 = rs.getInt("cns14");
				i.cns15 = rs.getInt("cns15");
				i.cns16 = rs.getInt("cns16");
				i.cns17 = rs.getInt("cns17");
				i.cns18 = rs.getInt("cns18");
				i.cns19 = rs.getInt("cns19");
				i.cns20 = rs.getInt("cns20");
				i.cr01 = rs.getInt("cr01");
				i.cr02 = rs.getInt("cr02");
				i.cr03 = rs.getInt("ce03");	
				i.cr04 = rs.getInt("cr04");
				i.cr05 = rs.getInt("cr05");
				i.cr07 = rs.getInt("cr07");	
				i.ct01 = rs.getInt("ct01");
				i.ct02 = rs.getInt("ct02");
				i.cd01 = rs.getInt("cd01");
				i.cd02 = rs.getInt("cd02");
				i.cd03 = rs.getInt("cd03");
				i.cd04 = rs.getInt("cd04");
				i.cs01 = rs.getInt("cs01");
				i.cs02 = rs.getInt("cs02");
				
				results.EmpDataList.add(i);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    dropConnection(connection);
		return results;
	}
	
	/**
	 * Queries Title VI data on a given area (based on the reportType)
	 */
	public static TitleVIDataList getTitleVIData(String reportType, int dbindex, String username){
		TitleVIDataList results = new TitleVIDataList();
		Connection connection = makeConnection(dbindex);
	    Statement stmt = null;
	    String query = "";
	    String criteria1 = "";
	    String criteria2 = "";
	    System.out.println(reportType);
	    
	    if (reportType.equals("Counties")){
	    	criteria1 = "countyid";
	    	criteria2 = "SELECT  census_counties.cname  AS name, temp2.*"
			    		+ " FROM temp2 INNER JOIN census_counties"
			    		+ " ON temp2.id = census_counties.countyid" ;
	    }else if (reportType.equals("Census Places")){
	    	criteria1 = "placeid";
	    	criteria2 = "SELECT  census_places.pname  AS name, temp2.*"
			    		+ " FROM temp2 INNER JOIN census_places"
			    		+ " ON temp2.id = census_places.placeid" ;
	    }
	    else if (reportType.equals("ODOT Transit Regions")){
	    	criteria1 = "regionid";
	    	criteria2 = "SELECT  concat('Region ',temp2.id) AS name, temp2.*"
	    				+ " FROM temp2 ";
	    }
	    else if (reportType.equals("Urban Areas")){
	    	criteria1 = "urbanid";
	    	criteria2 = " SELECT census_urbans.uname AS name, temp2.*"
			    		+ " FROM temp2 INNER JOIN census_urbans"
			    		+ " ON temp2.id = census_urbans.urbanid";
	    }
	    else if (reportType.equals("Congressional Districts")){
	    	criteria1 = "congdistid";
	    	criteria2 = " SELECT census_congdists.cname AS name, temp2.*"
			    		+ " FROM temp2 INNER JOIN census_congdists"
			    		+ " ON concat('410',temp2.id) = census_congdists.congdistid";
	    }
	    
	    query = "WITH temp1 AS (SELECT census_counties.countyid,"
	    		+ "		census_blocks.blockid,"
	    		+ "		census_blocks.placeid,"
	    		+ "		census_blocks.congdistid,"
	    		+ "		census_blocks.regionid,"
	    		+ "		census_blocks.urbanid,"
	    		+ "		census_blocks.population,"
	    		+ "		census_blocks.population/census_counties.population::FLOAT AS poppercent"
	    		+ "		FROM census_blocks INNER JOIN census_counties"
	    		+ "		ON LEFT(blockid,5) = census_counties.countyid"
	    		+ "		),"
	    		+ "temp2 AS ("
	    		+ "		SELECT"
	    		+ "		   " + criteria1 + " id,"
	    		+ "		   COALESCE(SUM(Population),0)::bigint population,"
	    		+ "		   COALESCE(SUM(Pop_Disabled*poppercent),0)::bigint Pop_Disabled,"
	    		+ "		   COALESCE(SUM(Tot_Pop_BP*poppercent),0)::bigint Tot_Pop_BP,"
	    		+ "		   COALESCE(SUM(Tot_Pop_AP*poppercent),0)::bigint Tot_Pop_AP,"
	    		+ "		   COALESCE(SUM(ENG_SPK*poppercent),0)::bigint ENG_SPK,"
	    		+ "		   COALESCE(SUM(ESP_SPK*poppercent),0)::bigint ESP_SPK,"
	    		+ "		   COALESCE(SUM(OTHER_INDO_SPK*poppercent),0)::bigint OTHER_INDO_SPK,"
	    		+ "		   COALESCE(SUM(ASIAN_SPK*poppercent),0)::bigint ASIAN_SPK,"
	    		+ "		   COALESCE(SUM(OTHER_LNG_SPK*poppercent),0)::bigint OTHER_LNG_SPK,"
	    		+ "		   COALESCE(SUM(HH_Tot*poppercent),0)::bigint HH_Tot,"
	    		+ "		   COALESCE(SUM(HH_White*poppercent),0)::bigint HH_White,"
	    		+ "		   COALESCE(SUM(HH_Hispanic*poppercent),0)::bigint HH_Hispanic,"
	    		+ "		   COALESCE(SUM(HH_Black*poppercent),0)::bigint HH_Black,"
	    		+ "		   COALESCE(SUM(HH_American_Indian*poppercent),0)::bigint HH_American_Indian,"
	    		+ "		   COALESCE(SUM(HH_Asian*poppercent),0)::bigint HH_Asian,"
	    		+ "		   COALESCE(SUM(HH_Pacific_Islander*poppercent),0)::bigint HH_Pacific_Islander,"
	    		+ "		   COALESCE(SUM(HH_Pacific_Other*poppercent),0)::bigint HH_Pacific_Other,"
	    		+ "		   COALESCE(SUM(HH_White_Not_Hisp*poppercent),0)::bigint HH_White_Not_Hisp,"
	    		+ "		   COALESCE(SUM(HH_Over_65*poppercent),0)::bigint HH_Over_65,"
	    		+ "		   COALESCE(SUM(HH_Under_65*poppercent),0)::bigint HH_Under_65"
	    		+ "		   "
	    		+ "		FROM temp1 INNER JOIN oregon_titlevi"
	    		+ "		ON temp1.countyid = oregon_titlevi.county_id"
	    		+ ""
	    		+ "		GROUP BY " + criteria1
	    		+ "		ORDER BY " + criteria1
	    		+ "	)"
	    		+  criteria2;
	    System.out.println(query);

	    try {
	    	stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(query); 
			
			while (rs.next()){
				TitleVIData i = new TitleVIData();
				i.id = rs.getString("id");
				i.name = rs.getString("name");
				i.population = rs.getInt("population");
				i.popDisabled = rs.getInt("pop_disabled");
				i.popAP = rs.getInt("tot_pop_ap");
				i.popBP = rs.getInt("tot_pop_bp");
				i.engSpk = rs.getInt("eng_spk");
				i.espSpk = rs.getInt("esp_spk");
				i.otherIndoSpk= rs.getInt("other_indo_spk");
				i.asianSpk = rs.getInt("asian_spk");
				i.otherLngSpk = rs.getInt("other_lng_spk");
				i.hhTotal = rs.getInt("hh_tot");
				i.hhWhite= rs.getInt("hh_white");
				i.hhHispanic= rs.getInt("hh_hispanic");
				i.hhBlack= rs.getInt("hh_black");
				i.hhAmericanIndian= rs.getInt("hh_american_indian");
				i.hhAsian= rs.getInt("hh_asian");
				i.hhPacificIslander= rs.getInt("hh_pacific_islander");
				i.hhPacificOther= rs.getInt("hh_pacific_other");
				i.hhWhiteNotHisp= rs.getInt("hh_white_not_hisp");
				i.hhOver65= rs.getInt("hh_over_65");
				i.hhUnder65= rs.getInt("hh_under_65");
				results.TitleVIDataList.add(i);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    dropConnection(connection);
		return results;
	}
	
	/**
	 *Queries Stops count, unduplicated urban pop and rural pop within x meters of all stops within the given geographic area
	 */
	public static long[] stopsPop(int type, String areaId, String username, double x, int dbindex) 
    {	
	  Connection connection = makeConnection(dbindex);
      Statement stmt = null;
      String column = "";
      String criteria1 = "";
      String criteria2 = "";
      if (type==0){//county
    	  criteria1 = "left(blockid,5)";
    	  criteria2 = "left(block.blockid,5)";
    	  column = "blockid";
      } else if (type==1){//census tract
    	  criteria1 = "left(blockid,11)";
    	  criteria2 = "left(block.blockid,11)";
    	  column = "blockid";
      } else {// census place, urban area, ODOT region, or congressional district
    	  column = Types.getIdColumnName(type);
    	  criteria1 = Types.getIdColumnName(type);
    	  criteria2 = "block."+Types.getIdColumnName(type);
      }
      String querytext = "with aids as (select agency_id as aid from gtfs_selected_feeds where username='"+username+"'), stops as (select id, agencyid, "+column+", location from "
      		+ "gtfs_stops stop inner join aids on stop.agencyid = aids.aid where "+criteria1+"='"+areaId+"'), census as (select population, poptype from "
      		+ "census_blocks block inner join stops on st_dwithin(block.location, stops.location, "+String.valueOf(x)+") where "+criteria2 +"='"+areaId+"' group by block.blockid), urbanpop as "
      		+ "(select COALESCE(sum(population),0) upop from census where poptype = 'U'), ruralpop as (select COALESCE(sum(population),0) rpop from census where poptype = 'R'),"
      		+ " stopcount as (select count(stops.id) as stopscount from stops) select COALESCE(stopscount,0) as stopscount, COALESCE(upop,0) as urbanpop, COALESCE(rpop,0) as "
      		+ "ruralpop from stopcount inner join urbanpop on true inner join ruralpop on true";
     // System.out.println(querytext);
      long[] results = new long[3];
      //System.out.println(querytext);
      try {
        stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(querytext);        
        while ( rs.next() ) {
        	results[0] = rs.getLong("stopscount");
        	results[1] = rs.getLong("urbanpop"); 
        	results[2] = rs.getLong("ruralpop"); 
        }
        rs.close();
        stmt.close();        
      } catch ( Exception e ) {
        System.err.println( e.getClass().getName()+": "+ e.getMessage() );
        System.exit(0);
      }
      dropConnection(connection);      
      return results;
    }
	
	/**
	 *Queries Service miles, service hours, service stops, served pop at level of service (urban and rural), served population (urban and rural), service days, hours of service,
	 *and connected communities for a geographic area. keys are: svcmiles, svchours, svcstops, upopatlos, rpopatlos, uspop, rspop, svcdays, fromtime, totime, connections
	 */
	public static HashMap<String, String> ServiceMetrics(int type, String[] date, String[] day, String[] fulldates, String areaId, String username, int LOS, double x, int dbindex) 
    {	
	  Connection connection = makeConnection(dbindex);
      Statement stmt = null;
      HashMap<String, String> response = new HashMap<String, String>();      
      String criteria = "";
      if (type==0){//county
    	  criteria = "left(blockid,5)";    	 
      } else if (type==1){//census tract
    	  criteria = "left(blockid,11)";    	 
      } else {// census place, urban area, ODOT region, or congressional district    	  
    	  criteria = Types.getIdColumnName(type);
      }
      String query = "with aids as (select agency_id as aid from gtfs_selected_feeds where username='"+username+"'), svcids as (";
      for (int i=0; i<date.length; i++){
    	  query+= "(select serviceid_agencyid, serviceid_id, '"+fulldates[i]+"' as day from gtfs_calendars gc inner join aids on gc.serviceid_agencyid = aids.aid where "
    	  		+ "startdate::int<="+date[i]+" and enddate::int>="+date[i]+" and "+day[i]+" = 1 and serviceid_agencyid||serviceid_id not in (select "
    	  		+ "serviceid_agencyid||serviceid_id from gtfs_calendar_dates where date='"+date[i]+"' and exceptiontype=2) union select serviceid_agencyid, serviceid_id, '"
    	  		+fulldates[i]+"' from gtfs_calendar_dates gcd inner join aids on gcd.serviceid_agencyid = aids.aid where date='"+date[i]+"' and exceptiontype=1)";
    	  if (i+1<date.length)
				query+=" union all ";
		}      
    query +="), trips as (select trip.agencyid as aid, trip.id as tripid, trip.route_id as routeid, round((map.length)::numeric,2) as length, map.tlength as tlength, "
    		+ "map.stopscount as stops from svcids inner join gtfs_trips trip using(serviceid_agencyid, serviceid_id) inner join "+Types.getTripMapTableName(type)+ " map on "
    		+"trip.id = map.tripid and trip.agencyid = map.agencyid where map."+Types.getIdColumnName(type)+"='"+areaId+"'),service as (select COALESCE(sum(length),0) as svcmiles,"
    		+ " COALESCE(sum(tlength),0) as svchours, COALESCE(sum(stops),0) as svcstops from trips),stopsatlos as (select stime.stop_agencyid as aid, stime.stop_id as stopid, "
    		+ "stop.location as location, count(trips.aid) as service from gtfs_stops stop inner join gtfs_stop_times stime on stime.stop_agencyid = stop.agencyid and "
    		+ "stime.stop_id = stop.id inner join trips on stime.trip_agencyid =trips.aid and stime.trip_id=trips.tripid where "+criteria+"='"+areaId+"' group by "
    		+ "stime.stop_agencyid, stime.stop_id, stop.location having count(trips.aid)>="+LOS+"),stops as (select stime.stop_agencyid as aid, stime.stop_id as stopid, stop.location "
    		+ "as location, min(stime.arrivaltime) as arrival, max(stime.departuretime) as departure, count(trips.aid) as service from gtfs_stops stop inner join gtfs_stop_times "
    		+ "stime on stime.stop_agencyid = stop.agencyid and stime.stop_id = stop.id inner join trips on stime.trip_agencyid =trips.aid and stime.trip_id=trips.tripid where "
    		+criteria+"='"+areaId+"' and stime.arrivaltime>0 and stime.departuretime>0 group by stime.stop_agencyid, stime.stop_id, stop.location), svchrs as (select "
    		+ "COALESCE(min(arrival),-1) as fromtime, COALESCE(max(departure),-1) as totime from stops), concom as (select distinct map."+Types.getIdColumnName(type)+" from "
    		+Types.getTripMapTableName(type)+" map inner join trips on trips.aid=map.agencyid and trips.tripid=map.tripid),concomnames as (select coalesce(string_agg(distinct "
    		+Types.getNameColumn(type)+",'; ' order by "+Types.getNameColumn(type)+"),'-') as connections from concom inner join "+Types.getTableName(type)+" using("
    		+Types.getIdColumnName(type)+")), popserved as (select (population*sum(service)) as population, poptype from census_blocks block inner join stops on "
    		+ "st_dwithin(block.location, stops.location,"+String.valueOf(x)+") where "+criteria+"='"+areaId+"' group by blockid), popatlos as (select population, poptype from "
    		+ "census_blocks block inner join stopsatlos on st_dwithin(block.location,stopsatlos.location,"+String.valueOf(x)+") where "+criteria+"='"+areaId+"' group by blockid),"
    		+ "upopatlos as (select COALESCE(sum(population),0) as upoplos from popatlos where poptype='U'), rpopatlos as (select COALESCE(sum(population),0) as rpoplos from "
    		+ "popatlos where poptype='R'), upopserved as (select COALESCE(sum(population),0) as uspop from popserved where poptype='U'), rpopserved as (select "
    		+ "COALESCE(sum(population),0) as rspop from popserved where poptype='R'), svcdays as (select COALESCE(array_agg(distinct day)::text,'-') as svdays from svcids) select"
    		+ " svcmiles,svchours,svcstops,upoplos,rpoplos,uspop,rspop,svdays,fromtime,totime,connections from service inner join upopatlos on true inner join rpopatlos on true "
    		+ "inner join upopserved on true inner join rpopserved on true inner join svcdays on true inner join svchrs on true inner join concomnames on true";
    //System.out.println(query);
      try {
        stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(query);        
        while ( rs.next() ) {
        	response.put("svcmiles", String.valueOf(rs.getFloat("svcmiles")));
        	response.put("svchours", String.valueOf(Math.round(rs.getLong("svchours")/36.00)/100.00));
        	response.put("svcstops", String.valueOf(rs.getLong("svcstops")));
        	response.put("upopatlos", String.valueOf(rs.getLong("upoplos")));
        	response.put("rpopatlos", String.valueOf(rs.getLong("rpoplos")));
        	response.put("uspop", String.valueOf(rs.getFloat("uspop")));
        	response.put("rspop", String.valueOf(rs.getFloat("rspop")));
        	response.put("svcdays", String.valueOf(rs.getString("svdays")));
        	response.put("fromtime", String.valueOf(rs.getInt("fromtime")));
        	response.put("totime", String.valueOf(rs.getInt("totime")));
        	response.put("connections", rs.getString("connections"));        	                      
        }
        rs.close();
        stmt.close();        
      } catch ( Exception e ) {
        System.err.println( e.getClass().getName()+": "+ e.getMessage() );
        System.exit(0);
      }
      dropConnection(connection);      
      return response;
    }
	
	/**
	 *Queries geographic allocation of service for transit agency reports
	 *0:county 1:census tracts 2:census places 3:urban areas 4:ODOT region 5:congressional district
	 */
	public static List<GeoR> geoallocation(int type, String agencyId, int dbindex, String username) 
    {	
	  Connection connection = makeConnection(dbindex);
      Statement stmt = null;
      List<GeoR> response = new ArrayList<GeoR>();      
      String criteria = "";
      String stoproutes = "areaid";
      String areaquery = "";
      String selectquery = "";
      String query = "with ";
      String join = "";
      String aidsjoin = "";
      if (agencyId==null){
    	  query += "aids as (select distinct agency_id as aid from gtfs_selected_feeds where username='"+username+"'),";
    	  join = "left";
    	  aidsjoin = "inner join aids on aid=stop.agencyid";
      }else {
    	  join = "inner";
    	  aidsjoin = "where map.agencyid='"+agencyId+"'";
      }
      if (type==0){//county: tracts are queries and summed up to reflect the true number of census tracts in the results
    	  criteria = "left(blockid,11)";      
    	  areaquery = "areas as (select countyid as areaid, cname as areaname, population, landarea, waterarea, odotregionid, regionname from census_counties), "
    	  		+ "tracts as (select count(distinct areaid) as tracts, left(areaid,5) as areaid from stops group by left(areaid,5)) ";    	  
    	  selectquery = "select areaid, areaname, population, landarea, waterarea, coalesce(agencies,0) as agencies, coalesce(routes,0) as routes, coalesce(stops,0) as stops,"
    	  		+ " odotregionid, regionname, tracts from areas "+join+" join stoproutes using(areaid) left join tracts using (areaid)";
    	  stoproutes = "left(areaid,5)";
      } else if (type==1){//census tract
    	  criteria = "left(blockid,11)";      
    	  areaquery = "areas as (select tractid as areaid, tname as areaname, tract.population, tract.landarea, tract.waterarea from census_tracts tract)";
    	  selectquery = " select areas.areaid, areaname, population, landarea, waterarea, coalesce(agencies,0) as agencies, coalesce(routes,0) as routes, "
    	  		+ "coalesce(stops,0) as stops from areas "+join+" join stoproutes using(areaid)";
      } else if (type==4) { //ODOT Regions
    	  criteria = Types.getIdColumnName(type);
    	  areaquery = "areas as (select odotregionid as areaid, regionname as areaname, string_agg(trim(trailing 'County' from cname), ';' order by cname) as counties, "
    	  		+ "sum(population) as population, sum(landarea) as landarea, sum(waterarea) as waterarea from census_counties group by odotregionid, regionname)";
    	  selectquery = " select areas.areaid, areaname, counties, population, landarea, waterarea, coalesce(agencies,0) as agencies, coalesce(routes,0) as routes, "
    	  		+ "coalesce(stops,0) as stops from areas "+join+" join stoproutes using(areaid)";
      } else {// census place, urban area, or congressional district    	  
    	  criteria = Types.getIdColumnName(type);
    	  areaquery = "areas as (select "+ criteria +" as areaid, "+Types.getNameColumn(type)+" as areaname, population, landarea, waterarea from " + Types.getTableName(type)+")";
    	  selectquery = " select areas.areaid, areaname, population, landarea, waterarea,coalesce(agencies,0) as agencies, coalesce(routes,0) as routes, "
    	  		+ "coalesce(stops,0) as stops from areas "+join+" join stoproutes using(areaid)";
      }      
     query +="stops as (select stop.id as stopid,stop.agencyid as aid_def, map.agencyid as aid, "+criteria+" as areaid from gtfs_stops stop inner join "
    		+ "gtfs_stop_service_map map on stop.id=map.stopid and stop.agencyid=map.agencyid_def "+aidsjoin+"), stoproutes as (select "
    		+ "coalesce(count(distinct stops.stopid),0) as stops, coalesce(count(distinct routeid),0) as routes, coalesce(count(distinct aid),0) as agencies,"
    		+stoproutes+" as areaid from stops inner join gtfs_stop_route_map map on stops.aid_def = map.agencyid_def and stops.stopid = map.stopid group by "
    		+stoproutes+"),"+areaquery+ selectquery;
    System.out.println(query);
      try {
        stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(query); 
        GeoR instance;
        while ( rs.next() ) {
        	instance = new GeoR();
        	instance.id = rs.getString("areaid");
        	instance.Name = rs.getString("areaname");        	
        	instance.population = String.valueOf(rs.getLong("population"));
        	instance.landArea = String.valueOf(Math.round(rs.getLong("landarea")/2.58999e4)/100.0);
        	instance.waterArea = String.valueOf(Math.round(rs.getLong("waterarea")/2.58999e4)/100.0);
        	instance.RoutesCount = String.valueOf(rs.getLong("routes"));
        	instance.StopsCount = String.valueOf(rs.getLong("stops"));
        	instance.AgenciesCount = String.valueOf(rs.getLong("agencies"));
        	if (type==0){
        		instance.ODOTRegion = rs.getString("odotregionid");
        		instance.ODOTRegionName = rs.getString("regionname");
        		instance.TractsCount = String.valueOf(rs.getLong("tracts"));
        	}
        	if (type==4){
        		//String[] buffer = (String[]) rs.getArray("names").getArray();
        		instance.CountiesCount = rs.getString("counties");
        	}
        	response.add(instance);
        }
        rs.close();
        stmt.close();        
      } catch ( Exception e ) {
        System.err.println( e.getClass().getName()+": "+ e.getMessage() );
        System.exit(0);
      }
      dropConnection(connection);      
      return response;
    }	
	
	/**
	 *Queries the transit agency summary report for geographic areas : Agency allocation of service for geographic areas
	 */
	public static ArrayList <AgencySR> agencyGeosr(String username, int type, String areaId, int dbindex) {
		ArrayList <AgencySR> response = new ArrayList<AgencySR>();		
		AgencySR instance;
		Connection connection = makeConnection(dbindex);		
		String mainquery ="";
		String criteria = "";
		if (type==0){//county
	    	  criteria = "left(blockid,5)";    	  
	      }else if(type==1){
	    	  criteria = "left(blockid,11)";
	      }	else  { //census place, urban area, ODOT Regions or congressional district	      
	    	  criteria = Types.getIdColumnName(type);
	      }
		mainquery += "with aids as (select distinct agency_id as aid from gtfs_selected_feeds where username='"+username+"'), stops as (select map.agencyid as agencyid, count(id) "
			+ "as stops from gtfs_stops stop inner join gtfs_stop_service_map map on map.agencyid_def=stop.agencyid and map.stopid=stop.id inner join aids on stop.agencyid=aid "
			+ "where "+criteria+"='"+areaId+"' group by map.agencyid), agencies as (select agencies.id as id, name, fareurl, phone, url, feedname, version, startdate, enddate, "
			+ "publishername, publisherurl from gtfs_agencies agencies inner join stops on agencies.id = stops.agencyid inner join gtfs_feed_info info on "
			+ "agencies.defaultid=info.defaultid ), fare as (select frule.route_agencyid as aid, frule.route_id as routeid, (round(avg(ftrb.price)*100))::integer as price "
			+ "from gtfs_fare_rules frule inner join gtfs_fare_attributes ftrb on ftrb.agencyid= frule.fare_agencyid and ftrb.id=frule.fare_id group by "
			+ "frule.route_agencyid,frule.route_id), froutes as (select routes.agencyid as aid, coalesce((count(routes.id))::int,-1) as routes, array_agg(coalesce(price, -1) "
			+ "order by price) as fprices from gtfs_routes routes left join fare on routes.agencyid = fare.aid and routes.id = fare.routeid inner join stops on "
			+ "stops.agencyid=routes.agencyid group by routes.agencyid) select id, name, fareurl, phone, url, feedname, version, startdate, enddate, publishername, publisherurl, "
			+ "fprices, routes, stops from agencies inner join stops on agencies.id=stops.agencyid inner join froutes on stops.agencyid = froutes.aid";		
		//System.out.println(mainquery);
		try{
			PreparedStatement stmt = connection.prepareStatement(mainquery);
			ResultSet rs = stmt.executeQuery();			
			double faresum;
			int farecount;
			double faremin;
			double faremax;
			ArrayList<Integer> fareList;
			while (rs.next()) {
				instance = new AgencySR();
				instance.AgencyId = rs.getString("id");
				instance.AgencyName = rs.getString("name");
				instance.FareURL = rs.getString("fareurl");
				instance.Phone = rs.getString("phone");
				instance.URL = rs.getString("url");
				instance.FeedName = rs.getString("feedname");
				instance.FeedVersion = rs.getString("version");
				instance.FeedStartDate = rs.getString("startdate");
				instance.FeedStartDate = instance.FeedStartDate.substring(0, 4)+"-"+instance.FeedStartDate.substring(4, 6)+"-"+instance.FeedStartDate.substring(6, 8);
				instance.FeedEndDate = rs.getString("enddate");
				instance.FeedEndDate = instance.FeedEndDate.substring(0, 4)+"-"+instance.FeedEndDate.substring(4, 6)+"-"+instance.FeedEndDate.substring(6, 8);
				instance.FeedPublisherName = rs.getString("publishername");
				instance.FeedPublisherUrl = rs.getString("publisherurl");								
				instance.RoutesCount = rs.getLong("routes")>0 ?	String.valueOf(rs.getInt("routes")):"0";
				instance.StopsCount = rs.getLong("stops")>0 ?	String.valueOf(rs.getInt("stops")):"0";
				Integer[] fares = (Integer[])(rs.getArray("fprices").getArray());
				faremin = 0;
				faremax = 0;
				faresum = 0;
				farecount = 0;
				fareList = new ArrayList<Integer>();
				for (int i=0; i<fares.length; i++){
					if (fares[i]>0){
						faresum +=fares[i];
						farecount++;
						fareList.add(fares[i]);
						if (fares[i]<faremin)
							faremin = fares[i];
						if (fares[i]>faremax)
							faremax = fares[i];
					}					
				}
				instance.MinFare = (faremin>=0 && farecount>0)? String.valueOf(faremin/100.00):"NA";
				instance.MaxFare = (faremax>=0 && farecount>0)? String.valueOf(faremax/100.00):"NA";
				if (farecount>0){
					instance.AverageFare = String.valueOf(Math.round(faresum/farecount)/100.00);
					if (farecount == 1){
						instance.MedianFare = String.valueOf(fareList.get(0)/100.00);
					}
					else if (farecount%2!=0){
						instance.MedianFare = String.valueOf((fareList.get((int)Math.floor(farecount/2)))/100.00);
					} else {
						instance.MedianFare = String.valueOf(Math.round((fareList.get(farecount/2)+fareList.get((farecount/2)-1))/200.00));
					}
				} else {
					instance.MedianFare = "NA";
					instance.AverageFare = "NA";
				}
				response.add(instance);
	        	}		
			rs.close();
			stmt.close();
		} catch ( Exception e ) {
	        System.err.println( e.getClass().getName()+": "+ e.getMessage() );
	        System.exit(0);
	      }					
		dropConnection(connection);
		return response;
	}
	
	/**
	 *Queries the stops reports:
	 *stops by agency
	 *stops by agency and route 
	 *stops by geographic area
	 *stops by geographic area and agency
	 *stops by geographic are, agency, and route
	*/
	public static ArrayList <StopR> stopGeosr(String username, int type, String areaId, String agency, String route, double x, int dbindex) {
		ArrayList <StopR> response = new ArrayList<StopR>();		
		StopR instance;
		Connection connection = makeConnection(dbindex);	
		String stopsfilter = "";
		String routesfilter = "";
		String agenciesfilter = "";
		String popsfilter = "";
		String mainquery ="with ";
		String criteria = "";		
		
		if (areaId==null){//stops by agency or stops by agency and route
			stopsfilter = "where map.agencyid='"+agency+"'";			
			agenciesfilter = "where id='"+agency+"'";
			if (route==null){//stops by agency
				routesfilter = " where map.agencyid='"+agency+"'";
			}else {//stops by agency and route
				routesfilter = " where map.agencyid='"+agency+"' and map.routeid='"+route+"'";
			}
		}else {//stops by areaId and ...
			if (type==0){//county
		    	  criteria = "left(blockid,5)";    	  
		      }else if (type==1){//tract
		    	  criteria = "left(blockid,11)"; 
		      }	else  { //census place, urban area, ODOT Regions or congressional district
		    	  criteria = Types.getIdColumnName(type);
		      }
			popsfilter = criteria+"='"+areaId+"' and";
			if (route==null){
				if (agency==null){//stops by areaId
					mainquery +="aids as (select distinct agency_id as aid from gtfs_selected_feeds where username='"+username+"'), ";
					stopsfilter = "inner join aids on stop.agencyid=aid and "+criteria+"='"+areaId+"' ";					
				}else {//stops by areaId and agency
					stopsfilter = "where map.agencyid='"+agency+"' and "+criteria+"='"+areaId+"'";
					agenciesfilter = "where agencies.id='"+agency+"'";
					routesfilter = "where map.agencyid='"+agency+"'";
				}			
			}else {//stops by areaId, agency, and route
				stopsfilter = "where map.agencyid='"+agency+"' and "+criteria+"='"+areaId+"'";
				agenciesfilter = "where agencies.id='"+agency+"'";
				routesfilter = "where map.agencyid='"+agency+"' and map.routeid='"+route+"'";
			}			
		}
		mainquery += "stops as (select map.agencyid as agencyid, stop.name as name, stop.id as id, url, location from gtfs_stops stop inner join gtfs_stop_service_map map on "
				+ "map.agencyid_def=stop.agencyid and map.stopid=stop.id "+stopsfilter+"), agencies as (select agencies.id as agencyid, agencies.name as aname from gtfs_agencies "
				+ "agencies "+agenciesfilter+"), routes as (select map.agencyid, stops.id, coalesce(string_agg(map.routeid,'; '),'-') as routes from gtfs_stop_route_map map inner "
				+ "join stops on stops.agencyid=map.agencyid and stops.id=map.stopid "+routesfilter+" group by map.agencyid, stops.id), upops as (select stops.agencyid, stops.id, "
				+ "coalesce(sum(population),0) as upop from census_blocks block inner join stops on st_dwithin(block.location, stops.location, "+String.valueOf(x)+") where "
				+ popsfilter+" poptype='U' group by agencyid, id), rpops as (select stops.agencyid, stops.id, coalesce(sum(population),0) as rpop from census_blocks block inner "
				+ "join stops on st_dwithin(block.location, stops.location, "+String.valueOf(x)+") where "+popsfilter+" poptype='R' group by agencyid, id) select stops.agencyid, "
				+ "aname, id, name, url, routes, coalesce(upop,0) as upop, coalesce(rpop,0) as rpop from stops inner join agencies using(agencyid) inner join routes "
				+ "using(agencyid,id) left join upops using(agencyid,id) left join rpops using(agencyid,id)";						
		System.out.println(mainquery);
		try{
			PreparedStatement stmt = connection.prepareStatement(mainquery);
			ResultSet rs = stmt.executeQuery();				
			while (rs.next()) {
				instance = new StopR();
				instance.AgencyId = rs.getString("agencyid");
				instance.AgencyName = rs.getString("aname");
				instance.Routes = rs.getString("routes");
				instance.UPopWithinX = String.valueOf(rs.getLong("upop"));
				instance.RPopWithinX = String.valueOf(rs.getLong("rpop"));
				instance.URL = rs.getString("url");
				instance.StopId = rs.getString("id");
				instance.StopName = rs.getString("name");				
				response.add(instance);
	        }		
			rs.close();
			stmt.close();
		} catch ( Exception e ) {
	        System.err.println( e.getClass().getName()+": "+ e.getMessage() );
	        System.exit(0);
	      }					
		dropConnection(connection);
		return response;
	}
	
	/**
	 *Queries the routes reports:
	 *routes by agency	 
	 *routes by geographic area	
	 *routes by agency and geographic area  
	*/
	public static ArrayList <RouteR> RouteGeosr(String username, int type, String areaId, String agency, String[] date, String[] day, double x, int dbindex) {
		ArrayList <RouteR> response = new ArrayList<RouteR>();		
		RouteR instance;
		Connection connection = makeConnection(dbindex);		
		String agenciesfilter = "";
		String routesfilter = "";
		String tripsfilter = "";
		String blocksfilter = "";
		String scriteria = "";
		String bcriteria = "";
		String mainquery ="with ";		
		if (areaId==null){//routes by agency
			agenciesfilter = "agencies as (select id as agencyid, name as aname, defaultid from gtfs_agencies agencies where id='"+agency+"'), ";
			routesfilter = "routes as (select route.agencyid, route.id, shortname, longname, type, url, description, COALESCE(max(length+estlength),0) as rlength, "
					+ "COALESCE(max(stopscount),0) as stops from gtfs_routes route inner join agencies using(agencyid) left join gtfs_trips trip on "
					+ "route.agencyid=trip.route_agencyid and route.id = trip.route_id group by route.agencyid, route.id), ";
			tripsfilter = "trips as (select trip.agencyid as aid, trip.id as tripid, trip.route_id as routeid, round((length+estlength)::numeric,2) as length, tlength, "
					+ "stopscount as stops from svcids inner join gtfs_trips trip using(serviceid_agencyid, serviceid_id)), ";			
		}else {//routes by areaId or agency & areaId
			if (type==0){//county
		    	  bcriteria = "left(block.blockid,5)";
		    	  scriteria = "left(stop.blockid,5)"; 
		      }else if (type==1){//tract
		    	  bcriteria = "left(block.blockid,11)";
		    	  scriteria = "left(stop.blockid,11)";
		      }	else  { //census place, urban area, ODOT Regions or congressional district
		    	  bcriteria = "block."+Types.getIdColumnName(type);
		    	  scriteria = "stop."+Types.getIdColumnName(type);
		      }
			routesfilter = "sroutes as (select agencyid as aid, routeid, COALESCE(max(length),0) as length, COALESCE(max(stopscount),0) as stops from "+Types.getTripMapTableName(type)+" map "
					+ "inner join agencies using(agencyid) where "+Types.getIdColumnName(type)+"='"+areaId+"' group by agencyid,routeid), routes as (select route.agencyid, "
					+ "route.id, shortname, longname, type, url, description, length as rlength, stops from gtfs_routes route inner join sroutes on route.agencyid=aid and "
					+ "route.id = routeid), ";
			tripsfilter = "trips as (select trip.agencyid as aid, tripid, routeid, length, tlength, stopscount as stops from svcids inner join "+ Types.getTripMapTableName(type)+" trip on "
					+ "trip.agencyid_def=serviceid_agencyid and trip.serviceid=serviceid_id where "+Types.getIdColumnName(type)+"='"+areaId+"'), ";
			blocksfilter = "where "+bcriteria+"='"+areaId+"' and "+scriteria+"='"+areaId+"'";
			if (agency==null){//routes by areaId
				agenciesfilter = "aids as (select distinct agency_id as aid from gtfs_selected_feeds where username='"+username+"'), agencies as (select agencies.id as "
						+ "agencyid, agencies.name as aname, defaultid from gtfs_agencies agencies inner join aids on aids.aid=agencies.defaultid), ";
			}else {//routes by areId and agency
				agenciesfilter ="agencies as (select id as agencyid, name as aname, defaultid from gtfs_agencies agencies where id='"+agency+"'), ";
			}
		}
		mainquery += agenciesfilter+"svcids as (";
		for (int i=0; i<date.length; i++){
			mainquery+= "(select serviceid_agencyid, serviceid_id from gtfs_calendars gc inner join agencies on gc.serviceid_agencyid = agencies.defaultid where "
	    	  		+ "startdate::int<="+date[i]+" and enddate::int>="+date[i]+" and "+day[i]+" = 1 and serviceid_agencyid||serviceid_id not in (select "
	    	  		+ "serviceid_agencyid||serviceid_id from gtfs_calendar_dates where date='"+date[i]+"' and exceptiontype=2) union select serviceid_agencyid, serviceid_id "
	    	  		+"from gtfs_calendar_dates gcd inner join agencies on gcd.serviceid_agencyid = agencies.defaultid where date='"+date[i]+"' and exceptiontype=1)";
	    	  if (i+1<date.length)
					mainquery+=" union all ";
			}
		mainquery+= "), "+ routesfilter + tripsfilter			
				+ "freq as (select aid, tripid, coalesce(count(aid),0) as frequency from trips group by aid, tripid), service as (select aid, routeid, COALESCE(sum(length),0) "
				+ "as svcmiles, COALESCE(sum(tlength),0) as svchours, COALESCE(sum(stops),0) as svcstops from trips group by aid,routeid), blocks as (select trips.aid, "
				+ "trips.tripid, population, poptype from gtfs_stops stop inner join gtfs_stop_times stime on stime.stop_agencyid = stop.agencyid and stime.stop_id = stop.id "
				+ "inner join trips on stime.trip_agencyid =trips.aid and stime.trip_id=trips.tripid inner join census_blocks block on "
				+ "st_dwithin(block.location, stop.location, "+String.valueOf(x)+") "+blocksfilter+" group by aid,tripid,block.blockid), upop as (select aid, tripid, "
				+ "coalesce(sum(population),0) as upop, coalesce(sum(population*frequency),0) as svcupop from blocks inner join freq using(aid,tripid) where poptype ='U' "
				+ "group by aid, tripid), upopr as (select aid, routeid, sum(upop) as upop, sum(svcupop) as svcupop from upop inner join trips using(aid,tripid) group by aid, "
				+ "routeid), rpop as (select aid, tripid, coalesce(sum(population),0) as rpop, coalesce(sum(population*frequency),0) as svcrpop from blocks inner join freq "
				+ "using(aid,tripid) where poptype ='R' group by aid, tripid), rpopr as (select aid, routeid, sum(rpop) as rpop, sum(svcrpop) as svcrpop from rpop inner join trips "
				+ "using(aid,tripid) group by aid, routeid) select agencies.agencyid, aname, routes.id, shortname, longname, type, url, description, rlength, stops, "
				+ "coalesce(upop,0) as upop, coalesce(rpop,0) as rpop, COALESCE(svcstops,0) as svcstops, COALESCE(svchours,0) as svchours, COALESCE(svcmiles,0) as svcmiles, "
				+ "COALESCE(svcupop,0) as usvcpop, COALESCE(svcrpop,0) as rsvcpop from routes inner join agencies on routes.agencyid=agencies.agencyid left join service on "
				+ "routes.agencyid=service.aid and routes.id=service.routeid left join upopr on routes.agencyid=upopr.aid and routes.id= upopr.routeid left join rpopr on "
				+ "routes.agencyid=rpopr.aid and routes.id=rpopr.routeid";						
		System.out.println(mainquery);
		try{
			PreparedStatement stmt = connection.prepareStatement(mainquery);
			ResultSet rs = stmt.executeQuery();				
			while (rs.next()) {
				instance = new RouteR();
				instance.AgencyId = rs.getString("agencyid");				
				instance.AgencyName = rs.getString("aname");
				instance.RouteId = rs.getString("id");
				instance.RouteSName = rs.getString("shortname");
				instance.RouteLName = rs.getString("longname");
				instance.RouteType = rs.getString("type");
				instance.RouteURL = rs.getString("url");
				instance.RouteDesc = rs.getString("description");
				instance.RouteLength = String.valueOf(Math.round(rs.getDouble("rlength")*100.00)/100.00);
				instance.StopsCount = String.valueOf(rs.getInt("stops"));
				instance.UPopWithinX = String.valueOf(rs.getLong("upop"));
				instance.RPopWithinX = String.valueOf(rs.getLong("rpop"));
				instance.ServiceStops = String.valueOf(rs.getLong("svcstops"));
				instance.ServiceHours = String.valueOf(Math.round(rs.getLong("svchours")/36.0)/100.0);
				instance.ServiceMiles = String.valueOf(Math.round(rs.getDouble("svcmiles")*100.0)/100.0);
				instance.UServicePop = String.valueOf(rs.getLong("usvcpop"));
				instance.RServicePop = String.valueOf(rs.getLong("rsvcpop"));								
				response.add(instance);
	        }		
			rs.close();
			stmt.close();
		} catch ( Exception e ) {
	        System.err.println( e.getClass().getName()+": "+ e.getMessage() );
	        System.exit(0);
	      }					
		dropConnection(connection);
		return response;
	}
	
	
	/**
	 *Queries the transit agency summary report	  
	 */
	public static ArrayList <AgencySR> agencysr(String username, int dbindex) {
		ArrayList <AgencySR> response = new ArrayList<AgencySR>();		
		AgencySR instance;
		Connection connection = makeConnection(dbindex);		
		String mainquery ="";
		mainquery += "with aids as (select distinct agency_id as aid from gtfs_selected_feeds where username='"+username+"'), agencies as (select id, name, fareurl, phone, url, "
				+ "feedname, version, startdate, enddate, publishername, publisherurl from gtfs_agencies agencies inner join aids on agencies.defaultid = aids.aid inner join "
				+ "gtfs_feed_info info on agencies.defaultid=info.defaultid), stops as (select map.agencyid as aid, coalesce((count(id))::int, -1) as stops, "
				+ "coalesce((count(distinct placeid))::int,-1) as places, coalesce((count(distinct left(blockid, 5)))::int,-1) as counties, "
				+ "coalesce((count(distinct regionid))::int,-1) as odotregions, coalesce((count(distinct urbanid))::int,-1) as urbans, "
				+ "coalesce((count(distinct congdistid))::int,-1) as congdists from gtfs_stops stop inner join gtfs_stop_service_map map on stop.agencyid = map.agencyid_def and "
				+ "stop.id=map.stopid group by map.agencyid), fare as (select frule.route_agencyid as aid, frule.route_id as routeid, (round(avg(ftrb.price)*100))::integer as price from gtfs_fare_rules "
				+ "frule inner join gtfs_fare_attributes ftrb on ftrb.agencyid= frule.fare_agencyid and ftrb.id=frule.fare_id group by frule.route_agencyid,frule.route_id), "
				+ "froutes as (select routes.agencyid as aid, coalesce((count(routes.id))::int,-1) as routes, array_agg(coalesce(price, -1) order by price) as fprices from "
				+ "gtfs_routes routes left join fare on routes.agencyid = fare.aid and routes.id = fare.routeid group by routes.agencyid) select id, name, fareurl, phone, url, "
				+ "feedname, version, startdate, enddate, publishername, publisherurl, fprices, routes, stops, places, counties, odotregions, urbans, congdists from agencies "
				+ "inner join stops on agencies.id=stops.aid inner join froutes on stops.aid = froutes.aid";		
		//System.out.println(mainquery);
		try{
			PreparedStatement stmt = connection.prepareStatement(mainquery);
			ResultSet rs = stmt.executeQuery();			
			double faresum;
			int farecount;
			double faremin;
			double faremax;
			ArrayList<Integer> fareList;
			while (rs.next()) {
				instance = new AgencySR();
				instance.AgencyId = rs.getString("id");
				instance.AgencyName = rs.getString("name");
				instance.FareURL = rs.getString("fareurl");
				instance.Phone = rs.getString("phone");
				instance.URL = rs.getString("url");
				instance.FeedName = rs.getString("feedname");
				instance.FeedVersion = rs.getString("version");
				instance.FeedStartDate = rs.getString("startdate");
				instance.FeedStartDate = instance.FeedStartDate.substring(0, 4)+"-"+instance.FeedStartDate.substring(4, 6)+"-"+instance.FeedStartDate.substring(6, 8);
				instance.FeedEndDate = rs.getString("enddate");
				instance.FeedEndDate = instance.FeedEndDate.substring(0, 4)+"-"+instance.FeedEndDate.substring(4, 6)+"-"+instance.FeedEndDate.substring(6, 8);
				instance.FeedPublisherName = rs.getString("publishername");
				instance.FeedPublisherUrl = rs.getString("publisherurl");				
				instance.CountiesCount = rs.getLong("counties")>0 ?	String.valueOf(rs.getInt("counties")):"0";
				instance.PlacesCount = rs.getLong("places")>0 ?	String.valueOf(rs.getInt("places")):"0";
				instance.OdotRegionsCount = rs.getLong("odotregions")>0 ?	String.valueOf(rs.getLong("odotregions")):"0";
				instance.UrbansCount = rs.getLong("urbans")>0 ?	String.valueOf(rs.getInt("urbans")):"0";
				instance.CongDistsCount = rs.getLong("congdists")>0 ?	String.valueOf(rs.getLong("congdists")):"0";				
				instance.RoutesCount = rs.getLong("routes")>0 ?	String.valueOf(rs.getInt("routes")):"0";
				instance.StopsCount = rs.getLong("stops")>0 ?	String.valueOf(rs.getInt("stops")):"0";
				Integer[] fares = (Integer[])(rs.getArray("fprices").getArray());
				faremin = 0;
				faremax = 0;
				faresum = 0;
				farecount = 0;
				fareList = new ArrayList<Integer>();
				for (int i=0; i<fares.length; i++){
					if (fares[i]>0){
						faresum +=fares[i];
						farecount++;
						fareList.add(fares[i]);
						if (fares[i]<faremin)
							faremin = fares[i];
						if (fares[i]>faremax)
							faremax = fares[i];
					}					
				}
				instance.MinFare = (faremin>=0 && farecount>0)? String.valueOf(faremin/100.00):"NA";
				instance.MaxFare = (faremax>=0 && farecount>0)? String.valueOf(faremax/100.00):"NA";
				if (farecount>0){
					instance.AverageFare = String.valueOf(Math.round(faresum/farecount)/100.00);
					if (farecount == 1){
						instance.MedianFare = String.valueOf(fareList.get(0)/100.00);
					}
					else if (farecount%2!=0){
						instance.MedianFare = String.valueOf((fareList.get((int)Math.floor(farecount/2)))/100.00);
					} else {
						instance.MedianFare = String.valueOf(Math.round((fareList.get(farecount/2)+fareList.get((farecount/2)-1))/200.00));
					}
				} else {
					instance.MedianFare = "NA";
					instance.AverageFare = "NA";
				}
				response.add(instance);
	        	}		
			rs.close();
			stmt.close();
		} catch ( Exception e ) {
	        System.err.println( e.getClass().getName()+": "+ e.getMessage() );
	        System.exit(0);
	      }					
		dropConnection(connection);
		return response;
	}	
	
	
/////AGGREGATED URBAN AREAS EXTENDED REPORTS QUERIES
	
	/**
	 *Queries Route miles for all urban areas with population => pop
	 */
	public static float AUrbansRouteMiles(long pop, String username, int dbindex) 
    {	
	  Connection connection = makeConnection(dbindex);
      Statement stmt = null;
      float RouteMiles = 0;
      String query = "with aids as (select distinct agency_id as aid from gtfs_selected_feeds where username='"+username+"'), areas as (select urbanid from census_urbans where "
      		+ "population>="+String.valueOf(pop)+"), trips as (select agencyid, routeid, round(max(length)::numeric,2) as length from census_urbans_trip_map map inner "
      		+ "join aids on map.agencyid_def=aids.aid inner join areas on areas.urbanid = map.urbanid group by agencyid, routeid) select sum(length) as routemiles from trips";
      try {
        stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(query);        
        while ( rs.next() ) {
        	RouteMiles = rs.getFloat("routemiles");                     
        }
        rs.close();
        stmt.close();        
      } catch ( Exception e ) {
        System.err.println( e.getClass().getName()+": "+ e.getMessage() );
        System.exit(0);
      }
      dropConnection(connection);      
      return RouteMiles;
    }
	
	/**
	 *Queries Fare Information for urban areas with population >= pop. keys are; minfare, maxfare, medianfare, averagefare
	 */
	public static HashMap<String, Float> AUrbansFareInfo(String[] date, String[] day, long pop, String username, int dbindex) 
    {	
	  Connection connection = makeConnection(dbindex);
      Statement stmt = null;
      HashMap<String, Float> response = new HashMap<String, Float>();
      ArrayList<Float> faredata = new ArrayList<Float>();
      String query = "with aids as (select distinct agency_id as aid from gtfs_selected_feeds where username='"+username+"'), areas as (select urbanid from census_urbans where population>="
      +String.valueOf(pop)+"), svcids as (";
      for (int i=0; i<date.length; i++){
    	  query+= "(select serviceid_agencyid, serviceid_id from gtfs_calendars gc inner join aids on gc.serviceid_agencyid = aids.aid where startdate::int<="+date[i]+
    			  " and enddate::int>="+date[i]+" and "+day[i]+" = 1 and serviceid_agencyid||serviceid_id not in (select serviceid_agencyid||serviceid_id from " +
    			  "gtfs_calendar_dates where date='"+date[i]+"' and exceptiontype=2) union select serviceid_agencyid, "+
    			  "serviceid_id from gtfs_calendar_dates gcd inner join aids on gcd.serviceid_agencyid = aids.aid where date='"+date[i]+"' and exceptiontype=1)";
    	  if (i+1<date.length)
				query+=" union all ";
		}      
    query +="), trips as (select trip.route_agencyid as aid, trip.route_id as routeid from svcids inner join gtfs_trips trip using(serviceid_agencyid, serviceid_id) inner join "
    	+ "census_urbans_trip_map map on trip.id = map.tripid and trip.agencyid = map.agencyid inner join areas on areas.urbanid = map.urbanid), fare as (select "
    	+ "frule.route_agencyid as aid, frule.route_id as routeid, ftrb.price as price from gtfs_fare_rules frule inner join gtfs_fare_attributes ftrb on "
    	+ "ftrb.agencyid= frule.fare_agencyid and ftrb.id=frule.fare_id inner join aids on aids.aid=ftrb.agencyid) select round(avg(price)::numeric,2) as fare from fare "
    	+ "inner join trips using (aid,routeid) group by fare.routeid order by fare";
    //System.out.println(query);
      try {
        stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(query);        
        while ( rs.next() ) {
        	faredata.add(rs.getFloat("fare"));                      
        }
        rs.close();
        stmt.close();        
      } catch ( Exception e ) {
        System.err.println( e.getClass().getName()+": "+ e.getMessage() );
        System.exit(0);
      }
      dropConnection(connection);
      if (faredata.size()>0){
    	  Collections.sort(faredata);     
    	  response.put("minfare", faredata.get(0));
    	  response.put("maxfare", faredata.get(faredata.size()-1));    	  
		  if (faredata.size()%2==0){
				response.put("medianfare", (float)(Math.round((faredata.get(faredata.size()/2)+faredata.get((faredata.size()/2)-1))*100.00/2)/100.00));		
		  } else {
				response.put("medianfare", faredata.get((int)(Math.ceil(faredata.size()/2))));		
		  }
		  float faresum = 0;
	      for (int i=0; i<faredata.size();i++){
	    	  faresum+=faredata.get(i);
	      }
	      response.put("averagefare", (float)(Math.round(faresum*100.00/faredata.size())/100.00));
      } else {
    	  response.put("minfare", null);
    	  response.put("maxfare", null);
    	  response.put("medianfare", null);
    	  response.put("averagefare", null);
      }
      return response;
    }
	
	/**
	 *Queries Stops count and unduplicated urban pop within x meters of all stops within urban areas with population >= pop
	 */
	public static long[] AUrbansstopsPop(long pop, String username, double x, int dbindex) 
    {	
	  Connection connection = makeConnection(dbindex);
      Statement stmt = null;
      String querytext = "with aids as (select distinct agency_id as aid from gtfs_selected_feeds where username='"+username+"'), areas as (select urbanid from census_urbans where "
      		+ "population>="+pop+"), stops as (select id, agencyid, blockid, location from gtfs_stops stop inner join aids on stop.agencyid = aids.aid inner join areas on "
      		+ "stop.urbanid = areas.urbanid), census as (select population from census_blocks block inner join stops on st_dwithin(block.location, stops.location,"
      		+ String.valueOf(x)+") inner join areas on block.urbanid = areas.urbanid group by block.blockid), urbanpop as (select COALESCE(sum(population),0) upop from census ),"
      		+ "stopcount as (select count(stops.id) as stopscount from stops) select COALESCE(stopscount,0) as stopscount, COALESCE(upop,0) as urbanpop from stopcount inner join "
      		+ "urbanpop on true";
      long[] results = new long[3];
      //System.out.println(querytext);
      try {
        stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(querytext);        
        while ( rs.next() ) {
        	results[0] = rs.getLong("stopscount");
        	results[1] = rs.getLong("urbanpop"); 
        	//results[2] = rs.getLong("ruralpop"); 
        }
        rs.close();
        stmt.close();        
      } catch ( Exception e ) {
        System.err.println( e.getClass().getName()+": "+ e.getMessage() );
        System.exit(0);
      }
      dropConnection(connection);      
      return results;
    }
	
	/**
	 *Queries Service miles, service hours, service stops, served pop at level of service (urban and rural), served population (urban and rural), service days, hours of service,
	 *and connected communities for urban areas with population >= pop. keys are: svcmiles, svchours, svcstops, upopatlos, uspop, svcdays, fromtime, totime, connections
	 */
	public static HashMap<String, String> UAreasServiceMetrics(String[] date, String[] day, String[] fulldates, long pop, String username, int LOS, double x, int dbindex) 
    {	
	  Connection connection = makeConnection(dbindex);
      Statement stmt = null;
      HashMap<String, String> response = new HashMap<String, String>();      
      String query = "with aids as (select distinct agency_id as aid from gtfs_selected_feeds where username='"+username+"'), areas as (select urbanid from census_urbans where "
      		+ "population>="+pop+"), svcids as (";
      for (int i=0; i<date.length; i++){
    	  query+= "(select serviceid_agencyid, serviceid_id, '"+fulldates[i]+"' as day from gtfs_calendars gc inner join aids on gc.serviceid_agencyid = aids.aid where "
    	  		+ "startdate::int<="+date[i]+" and enddate::int>="+date[i]+" and "+day[i]+" = 1 and serviceid_agencyid||serviceid_id not in (select "
    	  		+ "serviceid_agencyid||serviceid_id from gtfs_calendar_dates where date='"+date[i]+"' and exceptiontype=2) union select serviceid_agencyid, serviceid_id, '"
    	  		+fulldates[i]+"' from gtfs_calendar_dates gcd inner join aids on gcd.serviceid_agencyid = aids.aid where date='"+date[i]+"' and exceptiontype=1)";
    	  if (i+1<date.length)
				query+=" union all ";
		}      
    query +="), trips as (select trip.agencyid as aid, trip.id as tripid, trip.route_id as routeid, round((map.length)::numeric,2) as length, map.tlength as tlength, map.stopscount"
    		+ " as stops from svcids inner join gtfs_trips trip using(serviceid_agencyid, serviceid_id) inner join census_urbans_trip_map map on trip.id = map.tripid and "
    		+ "trip.agencyid = map.agencyid inner join areas on areas.urbanid = map.urbanid), service as (select COALESCE(sum(length),0) as svcmiles, COALESCE(sum(tlength),0) "
    		+ "as svchours, COALESCE(sum(stops),0) as svcstops from trips), stopsatlos as (select stime.stop_agencyid as aid, stime.stop_id as stopid, stop.location as location, "
    		+ "count(trips.aid) as service from gtfs_stops stop inner join gtfs_stop_times stime on stime.stop_agencyid = stop.agencyid and stime.stop_id = stop.id inner join "
    		+ "trips on stime.trip_agencyid =trips.aid and stime.trip_id=trips.tripid inner join areas on stop.urbanid = areas.urbanid group by stime.stop_agencyid, stime.stop_id, "
    		+ "stop.location having count(trips.aid)>="+String.valueOf(LOS)+" ), stops as (select stime.stop_agencyid as aid, stime.stop_id as stopid, stop.location as location, "
    		+ "min(stime.arrivaltime) as arrival, max(stime.departuretime) as departure, count(trips.aid) as service from gtfs_stops stop inner join gtfs_stop_times stime on "
    		+ "stime.stop_agencyid = stop.agencyid and stime.stop_id = stop.id inner join trips on stime.trip_agencyid =trips.aid and stime.trip_id=trips.tripid inner join areas "
    		+ "on stop.urbanid = areas.urbanid where stime.arrivaltime>0 and stime.departuretime>0 group by stime.stop_agencyid, stime.stop_id, stop.location), undupblocks as "
    		+ "(select block.population, max(stops.service) as service from census_blocks block inner join stops on st_dwithin(block.location, stops.location, "+String.valueOf(x)
    		+") inner join areas on block.urbanid = areas.urbanid group by blockid), undupblocksatlos as (select block.population from census_blocks block inner join stopsatlos "
    		+ "on st_dwithin(block.location, stopsatlos.location, "+String.valueOf(x)+") inner join areas on block.urbanid = areas.urbanid group by blockid), svchrs as (select "
    		+ "COALESCE(min(arrival),-1) as fromtime, COALESCE(max(departure),-1) as totime from stops), concom as (select distinct map.urbanid from census_urbans_trip_map map "
    		+ "inner join trips on trips.aid=map.agencyid and trips.tripid=map.tripid), concomnames as (select array_agg(uname order by uname)::text as connections from concom "
    		+ "inner join census_urbans using(urbanid)), upopatlos as (select COALESCE(sum(population),0) as upoplos from undupblocksatlos), upopserved as (select "
    		+ "COALESCE(sum(population*service),0) as uspop from undupblocks), svcdays as (select COALESCE(array_agg(distinct day)::text,'-') as svdays from svcids) select "
    		+ "svcmiles, svchours, svcstops, upoplos, uspop, svdays, fromtime, totime, connections from service inner join upopatlos on true inner join upopserved on true "
    		+ "inner join svcdays on true inner join svchrs on true inner join concomnames on true";
    //System.out.println(query);
      try {
        stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(query);        
        while ( rs.next() ) {
        	response.put("svcmiles", String.valueOf(rs.getFloat("svcmiles")));
        	response.put("svchours", String.valueOf(Math.round(rs.getLong("svchours")/36.00)/100.00));
        	response.put("svcstops", String.valueOf(rs.getLong("svcstops")));
        	response.put("upopatlos", String.valueOf(rs.getLong("upoplos")));
        	//response.put("rpopatlos", String.valueOf(rs.getLong("rpoplos")));
        	response.put("uspop", String.valueOf(rs.getFloat("uspop")));
        	//response.put("rspop", String.valueOf(rs.getFloat("rspop")));
        	response.put("svcdays", String.valueOf(rs.getString("svdays")));
        	response.put("fromtime", String.valueOf(rs.getInt("fromtime")));
        	response.put("totime", String.valueOf(rs.getInt("totime")));
        	response.put("connections", rs.getString("connections"));        	                      
        }
        rs.close();
        stmt.close();        
      } catch ( Exception e ) {
        System.err.println( e.getClass().getName()+": "+ e.getMessage() );
        System.exit(0);
      }
      dropConnection(connection);      
      return response;
    }
	
	/////Agency Extended Report Queries
	
	/**
	 *Queries Stops count, unduplicated urban pop and rural pop within x meters of all stops, and route miles for a givn transit agency
	 */
	public static double[] stopsPopMiles(String agencyId, double x, int dbindex) 
    {	
	  Connection connection = makeConnection(dbindex);
      Statement stmt = null;      
      String querytext = "with census as (select population, poptype from census_blocks block inner join gtfs_stops stop on st_dwithin(block.location, stop.location, "+
      String.valueOf(x)+") inner join gtfs_stop_service_map map on map.stopid=stop.id and map.agencyid_def = stop.agencyid where map.agencyid= '" + agencyId +
      "' group by block.blockid), urbanpop as (select COALESCE(sum(population),0) upop from census where poptype = 'U'), ruralpop as (select COALESCE(sum(population),0) rpop " +
      "from census where poptype = 'R'), stopcount as (select count(stop.id) as stopscount from gtfs_stops stop inner join gtfs_stop_service_map map on map.stopid=stop.id and " +
      "map.agencyid_def = stop.agencyid where map.agencyid= '"+agencyId+"'), routes as (select max(round((trip.length+trip.estlength)::numeric,2)) as length, trip.route_id as" +
      " routeid from gtfs_trips trip where trip.agencyid='"+agencyId+"' group by trip.route_id), rmiles as (select sum(length) as rtmiles from routes) select COALESCE(stopscount,0) as "+
      "stopscount, COALESCE(upop,0) as urbanpop, COALESCE(rpop,0) as ruralpop, COALESCE(rtmiles,0) as rtmiles from stopcount inner join urbanpop on true inner join " +
      "ruralpop on true inner join rmiles on true";
      double[] results = new double[4];
      //System.out.println(querytext);
      try {
        stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(querytext);        
        while ( rs.next() ) {
        	results[0] = rs.getLong("stopscount");
        	results[1] = rs.getLong("urbanpop"); 
        	results[2] = rs.getLong("ruralpop");
        	results[3] = Math.round(rs.getFloat("rtmiles")*100.0)/100.0;        	
        }
        rs.close();
        stmt.close();        
      } catch ( Exception e ) {
        System.err.println( e.getClass().getName()+": "+ e.getMessage() );
        System.exit(0);
      }
      dropConnection(connection);      
      return results;
    }
	
	/**
	 *Queries Service miles, service hours, service stops, served population (urban and rural), service days, and hours of service for a given agency id. 
	 *keys are: svcmiles, svchours, svcstops, uspop, rspop, svcdays, fromtime, and totime
	 */
	public static HashMap<String, String> AgencyServiceMetrics(String[] date, String[] day, String[] fulldates, String agencyId, double x, int dbindex) 
    {	
	  Connection connection = makeConnection(dbindex);
      Statement stmt = null;
      HashMap<String, String> response = new HashMap<String, String>();      
      String query = "with svcids as (";
      for (int i=0; i<date.length; i++){
    	  query+= "(select serviceid_agencyid, serviceid_id, '"+fulldates[i]+"' as day from gtfs_calendars gc where startdate::int<="+date[i]+" and enddate::int>="+date[i]
    			+" and "+day[i]+" = 1 and serviceid_agencyid||serviceid_id not in (select serviceid_agencyid||serviceid_id from gtfs_calendar_dates where date='"+date[i]
    			+"' and exceptiontype=2) union select serviceid_agencyid, serviceid_id, '"+fulldates[i]+"' from gtfs_calendar_dates gcd where date='"+date[i]+"' and exceptiontype=1)";
    	  if (i+1<date.length)
				query+=" union all ";
		}      
    query +="), trips as (select trip.agencyid as aid, trip.id as tripid, trip.route_id as routeid, round((trip.length+trip.estlength)::numeric,2) as length,trip.tlength as tlength,"
    		+ " trip.stopscount as stops from svcids inner join gtfs_trips trip using(serviceid_agencyid, serviceid_id) where trip.agencyid ='"+agencyId+"'), service as (select "
    		+ "COALESCE(sum(length),0) as svcmiles, COALESCE(sum(tlength),0) as svchours, COALESCE(sum(stops),0) as svcstops from trips), stops as (select stime.stop_agencyid as "
    		+ "aid, stime.stop_id as stopid, min(stime.arrivaltime) as arrival, max(stime.departuretime) as departure, stop.location, count(trips.aid) as service from gtfs_stops "
    		+ "stop inner join gtfs_stop_times stime on stime.stop_agencyid = stop.agencyid and stime.stop_id = stop.id inner join trips on stime.trip_agencyid =trips.aid "
    		+ "and stime.trip_id=trips.tripid where stime.arrivaltime>0 and stime.departuretime>0 group by stime.stop_agencyid, stime.stop_id, stop.location), undupblocks as "
    		+ "(select block.population, block.poptype, max(stops.service) as service from census_blocks block inner join stops on st_dwithin(block.location, stops.location, "+
    		String.valueOf(x)+") group by blockid), svchrs as (select COALESCE(min(arrival),-1) as fromtime, COALESCE(max(departure),-1) as totime from stops), upopserved as "
    		+ "(select COALESCE(sum(population*service),0) as uspop from undupblocks where poptype='U'), rpopserved as (select COALESCE(sum(population*service),0) as rspop from "
    		+ "undupblocks where poptype='R'), svcdays as (select COALESCE(array_agg(distinct day)::text,'-') as svdays from svcids) select svcmiles, svchours, svcstops, uspop, "
    		+ "rspop, svdays, fromtime, totime from service inner join upopserved on true inner join rpopserved on true inner join svcdays on true inner join svchrs on true";
    //System.out.println(query);
      try {
        stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(query);        
        while ( rs.next() ) {
        	response.put("svcmiles", String.valueOf(rs.getFloat("svcmiles")));
        	response.put("svchours", String.valueOf(Math.round(rs.getLong("svchours")/36.00)/100.00));
        	response.put("svcstops", String.valueOf(rs.getLong("svcstops")));        	
        	response.put("uspop", String.valueOf(rs.getFloat("uspop")));
        	response.put("rspop", String.valueOf(rs.getFloat("rspop")));
        	response.put("svcdays", String.valueOf(rs.getString("svdays")));
        	response.put("fromtime", String.valueOf(rs.getInt("fromtime")));
        	response.put("totime", String.valueOf(rs.getInt("totime")));        	       	                      
        }
        rs.close();
        stmt.close();        
      } catch ( Exception e ) {
        System.err.println( e.getClass().getName()+": "+ e.getMessage() );
        System.exit(0);
      }
      dropConnection(connection);      
      return response;
    }
	
	//////STATEWIDE EXTENDED REPORT QUERIES
	
	/**
	 *Queries Service miles, service hours, service stops, served pop at level of service (urban and rural), served population (urban and rural), service days, and hours of service
	 * for the whole state. keys are: svcmiles, svchours, svcstops, upopatlos, rpopatlos, uspop, rspop, svcdays, fromtime, totime.
	 */
	public static HashMap<String, String> StatewideServiceMetrics(String[] date, String[] day, String[] fulldates, String username, int LOS, double x, int dbindex) 
    {	
	  Connection connection = makeConnection(dbindex);
      Statement stmt = null;
      HashMap<String, String> response = new HashMap<String, String>();      
      
      String query = "with aids as (select distinct agency_id as aid from gtfs_selected_feeds where username='"+username+"'), svcids as (";
      for (int i=0; i<date.length; i++){
    	  query+= "(select serviceid_agencyid, serviceid_id, '"+fulldates[i]+"' as day from gtfs_calendars gc inner join aids on gc.serviceid_agencyid = aids.aid where "
    	  		+ "startdate::int<="+date[i]+" and enddate::int>="+date[i]+" and "+day[i]+" = 1 and serviceid_agencyid||serviceid_id not in (select "
    	  		+ "serviceid_agencyid||serviceid_id from gtfs_calendar_dates where date='"+date[i]+"' and exceptiontype=2) union select serviceid_agencyid, serviceid_id, '"
    	  		+fulldates[i]+"' from gtfs_calendar_dates gcd inner join aids on gcd.serviceid_agencyid = aids.aid where date='"+date[i]+"' and exceptiontype=1)";
    	  if (i+1<date.length)
				query+=" union all ";
		}      
    query +="), trips as (select agencyid as aid, id as tripid, route_id as routeid, round((estlength+length)::numeric,2) as length, tlength, stopscount as stops "
    		+ "from svcids inner join gtfs_trips trip using(serviceid_agencyid, serviceid_id)), service as (select COALESCE(sum(length),0) as svcmiles, COALESCE(sum(tlength),0) "
    		+ "as svchours, COALESCE(sum(stops),0) as svcstops from trips), stopsatlos as (select stime.stop_agencyid as aid, stime.stop_id as stopid, stop.location as location,"
    		+ " count(trips.aid) as service from gtfs_stops stop inner join gtfs_stop_times stime on stime.stop_agencyid = stop.agencyid and stime.stop_id = stop.id inner join "
    		+ "trips on stime.trip_agencyid =trips.aid and stime.trip_id=trips.tripid group by stime.stop_agencyid, stime.stop_id, stop.location having count(trips.aid)>="+LOS+"), "
    		+ "stops as (select stime.stop_agencyid as aid, stime.stop_id as stopid, stop.location as location, min(stime.arrivaltime) as arrival, max(stime.departuretime) as "
    		+ "departure, count(trips.aid) as service from gtfs_stops stop inner join gtfs_stop_times stime on stime.stop_agencyid = stop.agencyid and stime.stop_id = stop.id "
    		+ "inner join trips on stime.trip_agencyid =trips.aid and stime.trip_id=trips.tripid where stime.arrivaltime>0 and stime.departuretime>0 group by stime.stop_agencyid, "
    		+ "stime.stop_id, stop.location),undupblocks as (select block.population, block.poptype, max(stops.service) as service from census_blocks block inner join stops on "
    		+ "st_dwithin(block.location, stops.location,"+String.valueOf(x)+") group by blockid), undupblocksatlos as (select block.population, block.poptype from census_blocks "
    		+ "block inner join stopsatlos on st_dwithin(block.location, stopsatlos.location,"+String.valueOf(x)+") group by blockid), svchrs as (select COALESCE(min(arrival),-1)"
    		+ " as fromtime, COALESCE(max(departure),-1) as totime from stops), upopatlos as (select COALESCE(sum(population),0) as upoplos from undupblocksatlos where poptype='U')"
    		+ ", rpopatlos as (select COALESCE(sum(population),0) as rpoplos from undupblocksatlos where poptype='R'), upopserved as (select COALESCE(sum(population*service),0) "
    		+ "as uspop from undupblocks where poptype='U'), rpopserved as (select COALESCE(sum(population*service),0) as rspop from undupblocks where poptype='R'), svcdays as "
    		+ "(select COALESCE(array_agg(distinct day)::text,'-') as svdays from svcids) select svcmiles, svchours, svcstops, upoplos, rpoplos, uspop, rspop, svdays, fromtime, "
    		+ "totime from service inner join upopatlos on true inner join rpopatlos on true inner join upopserved on true inner join rpopserved on true inner join svcdays "
    		+ "on true inner join svchrs on true";
   // System.out.println(query);
      try {
        stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(query);        
        while ( rs.next() ) {
        	response.put("svcmiles", String.valueOf(rs.getFloat("svcmiles")));
        	response.put("svchours", String.valueOf(Math.round(rs.getLong("svchours")/36.00)/100.00));
        	response.put("svcstops", String.valueOf(rs.getLong("svcstops")));
        	response.put("upopatlos", String.valueOf(rs.getLong("upoplos")));
        	response.put("rpopatlos", String.valueOf(rs.getLong("rpoplos")));
        	response.put("uspop", String.valueOf(rs.getFloat("uspop")));
        	response.put("rspop", String.valueOf(rs.getFloat("rspop")));
        	response.put("svcdays", String.valueOf(rs.getString("svdays")));
        	response.put("fromtime", String.valueOf(rs.getInt("fromtime")));
        	response.put("totime", String.valueOf(rs.getInt("totime")));        	        	                      
        }
        rs.close();
        stmt.close();        
      } catch ( Exception e ) {
        System.err.println( e.getClass().getName()+": "+ e.getMessage() );
        System.exit(0);
      }
      dropConnection(connection);      
      return response;
    }
	
	/**
	 *returns sum of unduplicated population within x distance of all stops in the state
	 */
	public static long PopWithinX(double x, String username, int dbindex){
		long response = 0;
		Connection connection = makeConnection(dbindex);
		Statement stmt = null;
		try{
			stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery( "with aids as (select distinct agency_id as aid from gtfs_selected_feeds where username='"+username+"'), pops as (select population "
					+ "from census_blocks block inner join gtfs_stops stop on st_dwithin(block.location,stop.location,"+String.valueOf(x)+") inner join aids on "
					+ "stop.agencyid=aids.aid group by block.blockid) select sum(population) as pop from pops");	
			while ( rs.next() ) {
				response = rs.getLong("pop");
			}
		} catch ( Exception e ) {
	        System.err.println( e.getClass().getName()+": "+ e.getMessage() );
	        System.exit(0);
	      }
		dropConnection(connection);
		return response;
	}
	
	/**
	 *returns route Miles for the whole state 
	 */
	public static double StateRouteMiles(String username, int dbindex){
		double response = 0;
		Statement stmt = null;
		Connection connection = makeConnection(dbindex);
		try{
			stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("with aids as (select distinct agency_id as aid from gtfs_selected_feeds where username='"+username+"'), trips as "
					+ "(select max(length+estlength) as rlength from gtfs_trips trip inner join aids on trip.serviceid_agencyid = aids.aid group by trip.route_agencyid, "
					+ "trip.route_id) select coalesce(sum(rlength), 0) as rtmiles from trips");	
			while ( rs.next() ) {
				response = rs.getDouble("rtmiles"); 				
			}
		} catch ( Exception e ) {
	        System.err.println( e.getClass().getName()+": "+ e.getMessage() );
	        System.exit(0);
	      }
		dropConnection(connection);
		return response;
	}
	
	/**
	 *returns sum of rural/urban population served at specified level of service for the whole state : ALREADY IMPLEMENTED IN EARLIER SERVICE QUERY
	 */
	/*public static long[] PopServedatLOS(double x, String date, String day, String username, int l, int dbindex){
		long[] response = new long[2];
		Statement stmt = null;
		Connection connection = makeConnection(dbindex);
		try{
			stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("with aids as (select agency_id as aid from gtfs_selected_feeds where username='"+username+"'), svcids as (select serviceid_agencyid, "
					+ "serviceid_id from gtfs_calendars gc inner join aids on gc.serviceid_agencyid = aids.aid where startdate::int<="+date+" and enddate::int>="+date+" and "+day
					+"=1 and serviceid_agencyid||serviceid_id not in (select serviceid_agencyid||serviceid_id from gtfs_calendar_dates where date='"+date+"' and exceptiontype=2) "
					+ "union select serviceid_agencyid, serviceid_id from gtfs_calendar_dates gcd inner join aids on gcd.serviceid_agencyid = aids.aid where date='"+date
					+"' and exceptiontype=1),trips as (select trip.agencyid as aid, trip.id as tripid from svcids inner join gtfs_trips trip using(serviceid_agencyid, "
					+ "serviceid_id)),stopsatlos as (select stime.stop_agencyid as aid, stime.stop_id as stopid, stop.location as location from gtfs_stops stop inner join "
					+ "gtfs_stop_times stime on stime.stop_agencyid = stop.agencyid and stime.stop_id = stop.id inner join trips on stime.trip_agencyid =trips.aid and "
					+ "stime.trip_id=trips.tripid group by stime.stop_agencyid, stime.stop_id, stop.location having count(trips.aid)>="+l+" ),undupblocksatlos as "
					+ "(select block.population, block.poptype from census_blocks block inner join stopsatlos on st_dwithin(block.location, stopsatlos.location,"
					+String.valueOf(x)+") group by blockid),upopatlos as (select COALESCE(sum(population),0) as upoplos from undupblocksatlos where poptype='U'), rpopatlos as "
					+ "(select COALESCE(sum(population),0) as rpoplos from undupblocksatlos where poptype='R') select upoplos, rpoplos from upopatlos inner join rpopatlos on true");	
			while ( rs.next() ) {
				response[0] = rs.getLong("upoplos");
				response[0] = rs.getLong("rpoplos");
			}
		} catch ( Exception e ) {
	        System.err.println( e.getClass().getName()+": "+ e.getMessage() );
	        System.exit(0);
	      }
		dropConnection(connection);
		return response;
	}*/
	
	/**
	 *returns service miles in miles for the whole state for a single day in YYYYMMDD format and day of week in all lower case : ALREADY IMPLEMENTED IN EARLIER SERVICE QUERY
	 */
	/*public static double ServiceMiles(String date, String day, String username, int dbindex){
		double response = 0;
		Connection connection = makeConnection(dbindex);
		Statement stmt = null;
		try{
			stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery( "with aids as (select agency_id as aid from gtfs_selected_feeds where username='"+username+"'), svcids as (select serviceid_agencyid, "
					+ "serviceid_id from gtfs_calendars gc inner join aids on gc.serviceid_agencyid = aids.aid where startdate::int<="+date+" and enddate::int>="+date+" and "+day
					+"=1 and serviceid_agencyid||serviceid_id not in (select serviceid_agencyid||serviceid_id from gtfs_calendar_dates where date='"+date+"' and exceptiontype=2) "
					+ "union select serviceid_agencyid, serviceid_id from gtfs_calendar_dates gcd inner join aids on gcd.serviceid_agencyid = aids.aid where date='"+date
					+"' and exceptiontype=1) select sum(length+estlength) as svcmiles from svcids inner join gtfs_trips trip using(serviceid_agencyid, serviceid_id)");	
			while ( rs.next() ) {
				response = rs.getDouble("svcmiles");
			}
		} catch ( Exception e ) {
	        System.err.println( e.getClass().getName()+": "+ e.getMessage() );
	        System.exit(0);
	      }
		dropConnection(connection);
		return response;
	}*/
	
	/**
	 *returns service hours in seconds for the whole state for a single day in YYYYMMDD format and day of week in all lower case : ALREADY IMPLEMENTED IN EARLIER SERVICE QUERY
	 */
	/*public static long ServiceHours(String date, String day, String username, int dbindex){
		long response = 0;
		Connection connection = makeConnection(dbindex);
		Statement stmt = null;
		try{
			stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("with aids as (select agency_id as aid from gtfs_selected_feeds where username='"+username+"'), svcids as (select serviceid_agencyid, "
					+ "serviceid_id from gtfs_calendars gc inner join aids on gc.serviceid_agencyid = aids.aid where startdate::int<="+date+" and enddate::int>="+date+" and "+day
					+" = 1 and serviceid_agencyid||serviceid_id not in (select serviceid_agencyid||serviceid_id from gtfs_calendar_dates where date='"+date+"' and exceptiontype=2)"
					+ " union select serviceid_agencyid, serviceid_id from gtfs_calendar_dates gcd inner join aids on gcd.serviceid_agencyid = aids.aid where date='"+date
					+"' and exceptiontype=1) select sum(trip.tlength) as svchours from svcids inner join gtfs_trips trip using(serviceid_agencyid, serviceid_id)");	
			while ( rs.next() ) {
				response = rs.getLong("svchours");
			}
		} catch ( Exception e ) {
	        System.err.println( e.getClass().getName()+": "+ e.getMessage() );
	        System.exit(0);
	      }
		dropConnection(connection);
		return response;
	}*/
	
	/**
	 *returns min and max Hours of service in int time (epoch) fromat for a given date and day of week in an integer array : ALREADY IMPLEMENTED IN EARLIER SERVICE QUERY
	 */
	/*public static int[] HoursofService(String date, String day, int dbindex){
		int[] response = new int[2];
		Statement stmt = null;
		Connection connection = makeConnection(dbindex);
		try{
			stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery( "select min(arrivaltime) as start, max(departuretime) as finish from gtfs_stop_times stimes inner join "
					+ "(select agencyid, id from gtfs_trips where serviceid_agencyid||serviceid_id in "
					+ "(select  serviceid_agencyid||serviceid_id from gtfs_calendars where startdate::int<=" + date	+ " and enddate::int>="	+ date + " and " + day + " = 1 "
					+ "and serviceid_agencyid||serviceid_id not in (select serviceid_agencyid||serviceid_id from gtfs_calendar_dates where date='" + date
					+ "' and exceptiontype=2) union select serviceid_agencyid||serviceid_id from gtfs_calendar_dates where date='" + date + "' and exceptiontype=1))as trips "
					+ "on trips.agencyid = stimes.trip_agencyid and trips.id = stimes.trip_id where arrivaltime>=0 and departuretime>=0");	
			while ( rs.next() ) {
				response[0] = rs.getInt("start"); 
				response[1] = rs.getInt("finish"); 
			}
		} catch ( Exception e ) {
	        System.err.println( e.getClass().getName()+": "+ e.getMessage() );
	        System.exit(0);
	      }
		dropConnection(connection);
		return response;
	}*/
	
	////////AGGREGATED URBAN AREAS EXTENDED REPORT QUERIES

	
	/**
	 *Queries undupliated population within x meters of all stops in urban areas with population larger than pop
	 */
	public static long UrbanCensusbyPop(int pop, int dbindex, String username, double x) 
    {	
	  Connection connection = makeConnection(dbindex);      
      Statement stmt = null;
      long population = 0;
      try {
        stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery( "with aids as (select distinct agency_id as aid from gtfs_selected_feeds where username='"+username+"'),uids as (select urbanid from "
        		+ "census_urbans where population>="+String.valueOf(pop)+"),blocks as (select distinct block.blockid, population from census_blocks block inner join gtfs_stops stop"
        		+ " on st_dwithin(block.location,stop.location,"+String.valueOf(x)+") inner join uids on stop.urbanid=uids.urbanid and uids.urbanid=block.urbanid inner join aids on "
        		+ "stop.agencyid=aids.aid) select sum(population) as pop from blocks");        
        while ( rs.next() ) {
           population = rs.getLong("pop");                     
        }
        rs.close();
        stmt.close();
        //c.close();
      } catch ( Exception e ) {
        System.err.println( e.getClass().getName()+": "+ e.getMessage() );
        System.exit(0);
      }
      dropConnection(connection);      
      return population;
    }
	
	///////OTHER QUERIES FOR CONNECTED AGENCIES, CONNECTED NETWORKS AND TRANSIT HUBS REPORTS
	
	/**
	 *Queries agency clusters (connected agencies) and returns a list of all transit agencies with their connected agencies
	 */
	public static List<agencyCluster> agencyCluster(double dist, String username, int dbindex){
		List<agencyCluster> response = new ArrayList<agencyCluster>();
		Connection connection = makeConnection(dbindex);
		Statement stmt = null;
		try{
			stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery( "with aids as (select distinct agency_id as aid from gtfs_selected_feeds where username='"+username+"'), pairs as (select stp1.agencyid as "
					+ "aid1, stp1.id as sid1, stp2.agencyid as aid2, stp2.id as sid2, st_distance(stp1.location,stp2.location) as dist from gtfs_stops stp1 inner join gtfs_stops "
					+ "stp2 on st_dwithin(stp1.location, stp2.location,"+String.valueOf(dist)+") inner join aids on stp1.agencyid = aids.aid inner join aids aids2 on "
					+ "aids2.aid = stp2.agencyid), agencies as (select agency.id as aid, map.agencyid_def as aid_def, agency.name as aname, map.stopid as sid from gtfs_agencies "
					+ "agency inner join gtfs_stop_service_map map on agency.id=map.agencyid), clusters as (select ag1.aid as aid1, ag2.aid as aid2, ag2.aname, "
					+ "round((3.28084*min(pairs.dist))::numeric, 2) as min_gap from agencies ag1 inner join pairs on ag1.aid_def=pairs.aid1 and ag1.sid= pairs.sid1 inner join "
					+ "agencies ag2 on ag2.aid_def=pairs.aid2 and ag2.sid= pairs.sid2 where ag1.aid !=ag2.aid group by ag1.aid, ag2.aid, ag2.aname order by aid1, aname), results "
					+ "as (select aid1, count(aid2) as size, array_agg(aid2) as aids, array_agg(aname) as names, array_agg(min_gap::text) as min_gaps from clusters group by aid1), "
					+ "selctedagencies as (select agency.name, agency.id from gtfs_agencies agency inner join gtfs_stop_service_map map on agency.id=map.agencyid inner join aids on"
					+ " map.agencyid_def=aids.aid group by agency.id, agency.name) select agency.id as aid, agency.name, size, aids, names, min_gaps from selctedagencies agency left"
					+ " join results on agency.id=results.aid1");					
			while ( rs.next() ) {
				agencyCluster instance = new agencyCluster();
				instance.agencyId = rs.getString("aid");
				instance.agencyName = rs.getString("name");
				if (rs.getString("size")!=null){
					instance.clusterSize = rs.getLong("size");
					String[] buffer = (String[]) rs.getArray("aids").getArray();
					instance.agencyIds= Arrays.asList(buffer);
					buffer = (String[]) rs.getArray("names").getArray();
					instance.agencyNames= Arrays.asList(buffer);
					buffer = (String[]) rs.getArray("min_gaps").getArray();
					instance.minGaps= Arrays.asList(buffer);
				} else {
					instance.clusterSize = 0;
					instance.agencyIds= new ArrayList<String>();
					instance.agencyNames= new ArrayList<String>();
					instance.minGaps= new ArrayList<String>();
				}
				
		        response.add(instance);
		        }
		} catch ( Exception e ) {
	        System.err.println( e.getClass().getName()+": "+ e.getMessage() );
	        System.exit(0);
	      }
		dropConnection(connection);
		return response;
	}
	/**
	 *Queries connected transit agencies and list of connections for a given transit agency (connected agencies extended report)
	 */
	public static List<agencyCluster> agencyClusterDetails(double dist, String agencyId, String username, int dbindex){
		List<agencyCluster> response = new ArrayList<agencyCluster>();
		Connection connection = makeConnection(dbindex);
		Statement stmt = null;
		try{
			stmt = connection.createStatement();
			String query = "with aids as (select distinct agency_id as aid from gtfs_selected_feeds where username='"+username+"'), pairs as (select stp1.agencyid as aid1,"
					+ " stp1.id as sid1, array[stp1.lat, stp1.lon]::text as stp1loc, stp1.name as name1, stp2.agencyid as aid2, stp2.id as sid2, array[stp2.lat, stp2.lon]::text "
					+ "as stp2loc, stp2.name as name2, st_distance(stp1.location,stp2.location) as dist from gtfs_stops stp1 inner join gtfs_stops stp2 on "
					+ "st_dwithin(stp1.location, stp2.location,"+String.valueOf(dist)+") inner join aids on stp1.agencyid=aids.aid inner join aids aids2 on stp2.agencyid=aids2.aid)"
					+ ", agency as (select map.agencyid as aid, map.agencyid_def as aid_def, map.stopid as sid from gtfs_stop_service_map map where map.agencyid='"+agencyId+"'), "
					+ "agencies as (select agency.id as aid, map.agencyid_def as aid_def, agency.name as aname, map.stopid as sid from gtfs_agencies agency inner join "
					+ "gtfs_stop_service_map map on agency.id=map.agencyid where agency.id!='"+agencyId+"') select ag1.aid as aid1, ag2.aid as aid2, ag2.aname, count(pairs.sid2) "
					+ "as size, round((3.28084*min(pairs.dist))::numeric, 2) as min_gap, round((3.28084*max(pairs.dist))::numeric, 2) as max_gap, "
					+ "round((3.28084*avg(pairs.dist))::numeric, 2) as avg_gap, array_agg(name1) as names1, array_agg(stp1loc) as locs1, array_agg(sid2) as sids2, array_agg(name2) as names2, "
					+ "array_agg(stp2loc) as locs2, array_agg(round((3.28084*dist)::numeric, 2)::text) as dists from agency ag1 inner join pairs on ag1.aid_def=pairs.aid1 and "
					+ "ag1.sid= pairs.sid1 inner join agencies ag2 on ag2.aid_def=pairs.aid2 and ag2.sid= pairs.sid2 where ag1.aid!=ag2.aid group by ag1.aid, ag2.aid, ag2.aname";					
			//System.out.println(query);
			ResultSet rs = stmt.executeQuery(query);
			while ( rs.next() ) {
				agencyCluster instance = new agencyCluster();
				instance.agencyId = rs.getString("aid2");
				instance.agencyName = rs.getString("aname");
				instance.clusterSize = rs.getLong("size");
				instance.minGap = rs.getFloat("min_gap");
				instance.maxGap = rs.getFloat("max_gap");
				instance.meanGap = rs.getFloat("avg_gap");
				String[] buffer = (String[]) rs.getArray("names1").getArray();
				instance.sourceStopNames = Arrays.asList(buffer);
				buffer = (String[]) rs.getArray("names2").getArray();
				instance.destStopNames = Arrays.asList(buffer);
				buffer = (String[]) rs.getArray("dists").getArray();
				instance.minGaps = Arrays.asList(buffer);
				buffer = (String[]) rs.getArray("locs1").getArray();
				instance.sourceStopCoords = Arrays.asList(buffer);
				buffer = (String[]) rs.getArray("locs2").getArray();
				instance.destStopCoords = Arrays.asList(buffer);
				buffer = (String[]) rs.getArray("sids2").getArray();
				instance.destStopIds = Arrays.asList(buffer);
		        response.add(instance);
		        }
		} catch ( Exception e ) {
	        System.err.println( e.getClass().getName()+": "+ e.getMessage() );
	        System.exit(0);
	      }
		dropConnection(connection);
		return response;
	}
	
	/**
	 * Queries stops of a given agency.
	 * Used to generate Connecteg Agencies On-map report,
	 */
	public static CAStopsList getAgenStops(String agencyId, int dbindex){
		CAStopsList result = new CAStopsList();
		Connection connection = makeConnection(dbindex);
		Statement stmt = null;
		
		try {
			stmt = connection.createStatement();
			String query = "WITH main AS (SELECT id stopid, name stopname, agencyid, lat, lon, location "
					+ "FROM gtfs_stops WHERE agencyid='" + agencyId + "') "
							+ "SELECT main.stopid, main.stopname, main.agencyid, gtfs_agencies.name agencyname, main.lat, main.lon, gtfs_agencies.url "
							+ "FROM main INNER JOIN gtfs_agencies ON main.agencyid = gtfs_agencies.id";
			
			ResultSet rs = stmt.executeQuery(query);
			while (rs.next()){
				CAStop instance = new CAStop();
				instance.id = rs.getString("stopid");
				instance.name = rs.getString("stopname");
				instance.agencyId = rs.getString("agencyid");
				instance.agencyName = rs.getString("agencyname");
				instance.lat = rs.getString("lat");
				instance.lon = rs.getString("lon");
				result.stopsList.add(instance);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}
	
	/**
	 *Queries frequency of service for all stops in the database for a set of dates and days
	 */
	public static HashMap<String, Integer> stopFrequency(String[] date, String[] day, String username, int dbindex){				
		HashMap<String, Integer> response = new HashMap<String, Integer>();
		Connection connection = makeConnection(dbindex);
		String mainquery ="with aids as (select distinct agency_id as aid from gtfs_selected_feeds where username='"+username+"'), svcids as (";
		Statement stmt = null;
		for (int i=0; i<date.length; i++){
			mainquery+= "(select serviceid_agencyid, serviceid_id from gtfs_calendars gc inner join aids on gc.serviceid_agencyid = aids.aid where startdate::int<="+date[i]
					+" and enddate::int>="+date[i]+" and "+day[i]+"=1 and serviceid_agencyid||serviceid_id not in (select serviceid_agencyid||serviceid_id from "
					+ "gtfs_calendar_dates where date='"+date[i]+"' and exceptiontype=2) union select serviceid_agencyid as aid, serviceid_id as sid from gtfs_calendar_dates gcd "
					+ "inner join aids on gcd.serviceid_agencyid = aids.aid where date='"+date[i]+"' and exceptiontype=1)";
			if (i+1<date.length)
				mainquery+=" union all ";
		}
		mainquery +="), trips as (select agencyid as aid, id as tripid from svcids inner join gtfs_trips trip using(serviceid_agencyid, serviceid_id)) select "
				+ "stime.stop_agencyid||stime.stop_id as stopid, COALESCE(count(trips.aid),0) as service from aids inner join gtfs_stop_times stime on "
				+ "aids.aid=stime.stop_agencyid left join trips on stime.trip_agencyid =trips.aid and stime.trip_id=trips.tripid group by stime.stop_agencyid, stime.stop_id";
		//System.out.println(mainquery);
			try{
				stmt = connection.createStatement();
				ResultSet rs = stmt.executeQuery(mainquery);
				while (rs.next()) {
					response.put(rs.getString("stopid"), rs.getInt("service"));										
			        }
				rs.close();
				stmt.close();
			} catch ( Exception e ) {
		        System.err.println( e.getClass().getName()+": "+ e.getMessage() );
		        System.exit(0);
		      }
						
		dropConnection(connection);
		return response;
	}
	
	/**
	 *Queries stop clusters (hub reports) and returns a list of all transit agencies with their connected agencies
	 */
	public static TreeSet<StopCluster> stopClusters(String[] dates, String[] days, String username, double dist, int dbindex){	
		HashMap<String, Integer> serviceMap = stopFrequency(dates, days, username, dbindex);
		TreeSet<StopCluster> response = new ClusterPriorityQueue();
		Connection connection = makeConnection(dbindex);
		String mainquery = "with aids as (select distinct agency_id as aid from gtfs_selected_feeds where username='"+username+"'), cluster as (select stp1.agencyid||stp1.id as "
				+ "cid, stp2.id as sid, stp2.name as name, stp2.agencyid as aid from gtfs_stops stp1 inner join gtfs_stops stp2 on st_dwithin(stp1.location, stp2.location,"+
				String.valueOf(dist)+")), map as (select agencyid_def as aid, array_agg(distinct map.agencyid) as agencies, array_agg(distinct agency.name) as anames, "
				+ "stopid as sid, max(stop.lat) as lat, max(stop.lon) as lon, coalesce(string_agg(distinct county.cname,','),'') as cname, coalesce(string_agg(distinct county.regionname,','),'') "
				+ "as region, string_agg(distinct county.regionname,',') as rname, coalesce(string_agg(distinct urban.uname,','),'') as uname, coalesce(max(urban.population),0) as upop, "
				+ "array_agg(distinct routeid) as routes from gtfs_stop_route_map map inner join aids on aids.aid=agencyid_def inner join gtfs_stops stop on "
				+ "map.agencyid_def = stop.agencyid and map.stopid = stop.id inner join gtfs_agencies agency on map.agencyid = agency.id left join census_counties county on "
				+ "left(stop.blockid,5)=countyid left join census_urbans urban on stop.urbanid = urban.urbanid group by agencyid_def, stopid) select cluster.cid as cid, "
				+ "cluster.sid as sid, cluster.aid as aid, cluster.name as name, map.lat, map.lon, map.agencies, map.anames, map.routes as routes, map.cname, map.rname, "
				+ "map.region, map.uname, map.upop from cluster inner join map on map.sid = cluster.sid and map.aid = cluster.aid order by cluster.cid, cluster.sid";
		Statement stmt = null;		
		try{
			//System.out.println(mainquery);
			stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery(mainquery);			
			//System.out.println("Query: "+stmt);
			String cid = "";
			int count = 0;
			StopCluster inst = new StopCluster();
			//System.out.println("Number of records in results: "+rs.getFetchSize());
			//System.out.println("Query Done, collecting results");
			while (rs.next()) {
				count++;
				ClusteredStop instance = new ClusteredStop();
				String clid = rs.getString("cid");	
				instance.agencyId = rs.getString("aid");				
				instance.id = rs.getString("sid");				
				instance.name = rs.getString("name");
				instance.visits = serviceMap.get(instance.agencyId+instance.id);
				String[] agencies = (String[]) rs.getArray("agencies").getArray();
				instance.agencies= Arrays.asList(agencies);
				String[] anames = (String[]) rs.getArray("anames").getArray();
				instance.agencyNames= Arrays.asList(anames);
				instance.lat = rs.getDouble("lat");
				instance.lon = rs.getDouble("lon");
				instance.county = rs.getString("cname");
				instance.urban = rs.getString("uname");
				instance.odotregionname = rs.getString("rname");
				instance.odotregion = rs.getString("region");
				instance.urbanPop = rs.getLong("upop");				
				String[] routes = (String[]) rs.getArray("routes").getArray();
				instance.routes= Arrays.asList(routes);								
				if (count ==1){
					cid =clid;
					inst.setClid(clid);
				}
				if (cid.equals(clid)){
					inst.addStop(instance);
				} else {
					cid=clid;					
					inst.syncParams();
					response.add(inst);					
					inst = new StopCluster();
					inst.setClid(clid);
					inst.addStop(instance);					
					}
		        }
			rs.close();
			stmt.close();
		} catch ( Exception e ) {
	        System.err.println( e.getClass().getName()+": "+ e.getMessage() );
	        System.exit(0);
	      }		
		dropConnection(connection);
		//System.out.println("Processing Clusters");
		return response;
	}
	
	/**
	 *Queries the transit part of the on map report	
	 */
	public static MapTransit onMapStops(String[] date, String[] day, String username, double d, double[] lat, double[] lon, int dbindex) {
		CoordinateReferenceSystem sourceCRS = null;
		CoordinateReferenceSystem targetCRS = null;
		MathTransform transform = null;;
		try {
			sourceCRS = CRS.decode("EPSG:4326");
			targetCRS = CRS.decode("EPSG:2993");
			transform = CRS.findMathTransform(sourceCRS, targetCRS);
		} catch (NoSuchAuthorityCodeException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
		//HashMap<String, Integer> response = new HashMap<String, Integer>();
		MapTransit response = new MapTransit();		
		int TotalStops = 0;
		//int TotalRoutes = 0;
		ArrayList<String> RouteIdList = new ArrayList<String>();
		ArrayList<String> AllRouteIds = new ArrayList<String>();
		int ServiceStops = 0;
		double FareSum = 0;
		ArrayList<Double> Fares = new ArrayList<Double>();
		Collection<MapAgency> agencies = new ArrayList<MapAgency>();
		MapAgency instance = new MapAgency();
		ArrayList<MapRoute> Routes = new ArrayList<MapRoute>();
		Collection<MapStop> Stops = new ArrayList<MapStop>();
		Connection connection = makeConnection(dbindex);		
		//((org.postgresql.PGConnection)connection).addDataType("geometry",Class.forName("org.postgis.PGgeometry"));
		String mainquery = "with aids as (select distinct agency_id as aid from gtfs_selected_feeds where username='"+username+"'), svcids as (";
		for (int i=0; i<date.length; i++){
			mainquery+= "(select  serviceid_agencyid, serviceid_id from gtfs_calendars gc inner join aids on gc.serviceid_agencyid = aids.aid where startdate::int<="+date[i]
					+" and enddate::int>="+date[i]+" and "+day[i]+" = 1 and serviceid_agencyid||serviceid_id not in (select serviceid_agencyid||serviceid_id from "
					+ "gtfs_calendar_dates where date='"+date[i]+"' and exceptiontype=2) union select serviceid_agencyid, serviceid_id from gtfs_calendar_dates gcd inner join "
					+ "aids on gcd.serviceid_agencyid = aids.aid where date='"+date[i]+"' and exceptiontype=1)";
			if (i+1<date.length)
				mainquery+=" union all ";
		}
		mainquery+="), trips as (select trip.agencyid as aid, trip.id as tripid, trip.uid as uid, trip.route_id as routeid, round((trip.length+trip.estlength)::numeric,2)::varchar as length, "+
				"trip.routeshortname as rname, trip.epshape as shape, count(svcids.serviceid_id) as service from gtfs_trips trip inner join svcids "+
				"using(serviceid_agencyid, serviceid_id) group by trip.agencyid, trip.id),fare as (select fare.route_agencyid as aid, fare.route_id as routeid ,"
				+ " avg(fareat.price) as price from gtfs_fare_rules fare inner join gtfs_fare_attributes fareat on fare.fare_agencyid = fareat.agencyid and fare.fare_id=id "
				+ "group by fare.route_agencyid, fare.route_id),trip_fare as (select trips.aid, trips.uid, trips.tripid, trips.routeid, trips.rname, trips.length, trips.shape, "
				+ "trips.service, COALESCE(fare.price::text,'-') as price from trips left join fare using (aid, routeid)),stopids as (select trip_fare.aid, "
				+ "array_agg(concat(trip_fare.uid,chr(196),trip_fare.routeid,chr(196),trip_fare.rname,chr(196),trip_fare.length::text,chr(196),trip_fare.service::text,chr(196),trip_fare.price::text,chr(196),trip_fare.shape)) as routes, "
				+ "stimes.stop_id as stopid, stimes.stop_agencyid as aid_def, sum(service) as svc from trip_fare inner join gtfs_stop_times stimes on "
				+ "trip_fare.aid = stimes.trip_agencyid and trip_fare.tripid = stimes.trip_id group by aid_def, aid, stopid),";
		if (lat.length<2){
			Point point = geometryFactory.createPoint(new Coordinate(lat[0], lon[0]));
			Geometry targetGeometry = null;
			try {
				targetGeometry = JTS.transform( point, transform);
			} catch (MismatchedDimensionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			point = targetGeometry.getCentroid();
			point.setSRID(2993);
			mainquery+="stops as (select stopids.aid, stopids.routes, stopids.stopid, stop.name, array[stop.lat, stop.lon] as location, stopids.svc from stopids inner join gtfs_stops stop on "
					+ "st_dwithin(stop.location,ST_GeomFromText('"+point+"',2993), "+String.valueOf(d)+")=true "
					+ "where stop.id = stopids.stopid and stop.agencyid=stopids.aid_def),";			
		} else {
			Coordinate[] coords = new Coordinate[lat.length+1];
			for(int i=0;i<lat.length;i++){
				coords[i]= new Coordinate(lat[i], lon[i]);
			}
			coords[coords.length-1]= new Coordinate(lat[0], lon[0]);
			LinearRing ring = geometryFactory.createLinearRing( coords );
			LinearRing holes[] = null; 
			Polygon polygon = geometryFactory.createPolygon(ring, holes );
			Geometry targetGeometry = null;
			try {
				targetGeometry = JTS.transform( polygon, transform);
			} catch (MismatchedDimensionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			targetGeometry.setSRID(2993);
			mainquery+="stops as (select stopids.aid, stopids.routes, stopids.stopid, stop.name, array[stop.lat, stop.lon] as location, stopids.svc from "
					+ "stopids inner join gtfs_stops stop on st_within(stop.location,ST_GeomFromText('"+targetGeometry+"',2993))=true where stop.id = stopids.stopid and stop.agencyid=stopids.aid_def),";			
		}
		mainquery+="stopsa as (select agency.name as agency, stops.* from stops inner join gtfs_agencies agency on agency.id = stops.aid order by aid) "
				+ "select * from stopsa";
		//System.out.println(mainquery);
		try{
			PreparedStatement stmt = connection.prepareStatement(mainquery);
			ResultSet rs = stmt.executeQuery();	
			String aid = "";
			String cid = "";
			//System.out.println("Query was ran successfully.");
			int i = 0;
			MapStop stp;
			while (rs.next()) {
				cid = rs.getString("aid");
				stp = new MapStop();
				stp.AgencyId = cid;
				stp.Id = rs.getString("stopid");
				stp.Name = rs.getString("name");
				stp.Frequency = rs.getInt("svc");
				Double[] location = (Double[]) rs.getArray("location").getArray();
				stp.Lat = String.valueOf(location[0]);
				stp.Lng = String.valueOf(location[1]);				
				if (!(aid.equals(cid))){
					if (i>0){
						instance.MapRoutes = Routes;
						instance.MapStops = Stops;
						instance.ServiceStop = ServiceStops;
						instance.RoutesCount = RouteIdList.size();
						agencies.add(instance);
						TotalStops +=Stops.size();						
						RouteIdList = new ArrayList<String>();					
						instance = new MapAgency();
						Routes = new ArrayList<MapRoute>();
						Stops = new ArrayList<MapStop>();
						ServiceStops = 0;
					}					
					aid = cid;				
					instance.Id = aid;
					instance.Name = rs.getString("agency");					
				}	
				ServiceStops += stp.Frequency;								
				String[] stoproutes = (String[]) rs.getArray("routes").getArray();
				for (int j = 0; j<stoproutes.length; j++){
					String[] routeinfo = stoproutes[j].split(Character.toString((char)196), 7);
					if (!stp.RouteIds.contains(routeinfo[1]))
							stp.RouteIds.add(routeinfo[1]);
					if (!RouteIdList.contains(cid+routeinfo[1])){
							RouteIdList.add(cid+routeinfo[1]);							
					}
					if (!(AllRouteIds.contains(cid+routeinfo[1]))){
							AllRouteIds.add(cid+routeinfo[1]);
							if (!routeinfo[5].equals("-")){
								FareSum+=Double.parseDouble(routeinfo[5]);
								Fares.add(Double.parseDouble(routeinfo[5]));
							}
					}
					MapRoute rt = new MapRoute();
					rt.AgencyId = cid;
					rt.uid = routeinfo[0];
					rt.Id = routeinfo[1];
					if (!(Routes.contains(rt))){						
						rt.Name = routeinfo[2];
						rt.Length = Float.parseFloat(routeinfo[3]);
						rt.Frequency = Integer.parseInt(routeinfo[4]);
						if (!routeinfo[5].equals("-")){
							rt.Fare = "$"+String.valueOf(Math.round(Double.parseDouble(routeinfo[5])*100.00)/100.00);
						} else {
							rt.Fare = "NA";
						}											
						rt.Shape = routeinfo[6];
						rt.hasDirection = false;
						Routes.add(rt);							
					} else {						
						int index = Routes.indexOf(rt);
						rt = Routes.get(index);
						Routes.remove(index);
						rt.Frequency += Integer.parseInt(routeinfo[4]);
						if (rt.Length < Float.parseFloat(routeinfo[3])){
							rt.Length = Float.parseFloat(routeinfo[3]);
							rt.Shape = routeinfo[6];
						}						
						Routes.add(rt);
					}
				}
				Stops.add(stp);
				i++;
		        }		
			instance.MapRoutes = Routes;
			instance.MapStops = Stops;
			instance.ServiceStop = ServiceStops;
			instance.RoutesCount = RouteIdList.size();
			agencies.add(instance);
			TotalStops +=Stops.size();
			response.MapAgencies = agencies;
			response.TotalStops = String.valueOf(TotalStops);
			response.TotalRoutes = String.valueOf(AllRouteIds.size());
			if (Fares.size()>0){
				response.AverageFare = String.valueOf(Math.round(FareSum*100.00/Fares.size())/100.00);
				Collections.sort(Fares);				
					if (Fares.size()%2==0){
						response.MedianFare = String.valueOf(Math.round((Fares.get(Fares.size()/2)+Fares.get((Fares.size()/2)-1))*100.00/2)/100.00);
					} else {
						response.MedianFare = String.valueOf(Math.round(Fares.get((int)(Math.ceil(Fares.size()/2)))*100.00)/100.00);
					}			
			}else {
				response.AverageFare = "0";
				response.MedianFare = "0";
			}			
			rs.close();
			stmt.close();
		} catch ( Exception e ) {
	        System.err.println( e.getClass().getName()+": "+ e.getMessage() );
	        System.exit(0);
	      }					
		dropConnection(connection);
		return response;
	}
	/**
	 *Queries the census part of the on map report	  
	 */
	public static MapGeo onMapBlocks(double d, double[] lat, double[] lon, int dbindex) {
		CoordinateReferenceSystem sourceCRS = null;
		CoordinateReferenceSystem targetCRS = null;
		MathTransform transform = null;;
		try {
			sourceCRS = CRS.decode("EPSG:4326");
			targetCRS = CRS.decode("EPSG:2993");
			transform = CRS.findMathTransform(sourceCRS, targetCRS);
		} catch (NoSuchAuthorityCodeException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (FactoryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}		
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
		//HashMap<String, Integer> response = new HashMap<String, Integer>();
		MapGeo response = new MapGeo ();		
		ArrayList<MapCounty> Counties = new ArrayList<MapCounty>();
		ArrayList<MapBlock> Blocks = new ArrayList<MapBlock>();
		ArrayList<MapTract> Tracts = new ArrayList<MapTract>();
		long TotalUrbanPop = 0;
		long TotalRuralPop = 0;
		long CountyUrbanPop = 0;
		long CountyRuralPop = 0;
		long TractArea = 0;
		long TotalLandArea = 0;
		int TotalBlocks = 0;
		int TotalTracts = 0;
		MapCounty instance = new MapCounty();
		MapTract tinstance = new MapTract();
		Connection connection = makeConnection(dbindex);		
		String mainquery ="";		
		if (lat.length<2){
			Point point = geometryFactory.createPoint(new Coordinate(lat[0], lon[0]));
			Geometry targetGeometry = null;
			try {
				targetGeometry = JTS.transform( point, transform);
			} catch (MismatchedDimensionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			point = targetGeometry.getCentroid();
			point.setSRID(2993);
			mainquery += "with block as (select blockid, poptype as btype, landarea as barea, population as bpop, array[block.lat, block.lon] as blocation"
					+ " from census_blocks block where "
					+ "st_dwithin(ST_Transform(ST_SetSRID(ST_MakePoint(block.lon, block.lat),4326), 2993),ST_GeomFromText('"+point+"',2993), "+String.valueOf(d)+")=true),"
					+ "county as (select countyid, cname from census_counties), tract as (select tractid, population as tpop, landarea as tarea, array[tract.lat, tract.lon] as tlocation "
					+ "from census_tracts tract where "
					+ "st_dwithin(ST_Transform(ST_SetSRID(ST_MakePoint(tract.lon, tract.lat),4326), 2993),ST_GeomFromText('"+point+"',2993), "+String.valueOf(d)+")=true), ";			
		} else {
			Coordinate[] coords = new Coordinate[lat.length+1];
			for(int i=0;i<lat.length;i++){
				coords[i]= new Coordinate(lat[i], lon[i]);
			}
			coords[coords.length-1]= new Coordinate(lat[0], lon[0]);
			LinearRing ring = geometryFactory.createLinearRing( coords );
			LinearRing holes[] = null; 
			Polygon polygon = geometryFactory.createPolygon(ring, holes );
			Geometry targetGeometry = null;
			try {
				targetGeometry = JTS.transform( polygon, transform);
			} catch (MismatchedDimensionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransformException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			targetGeometry.setSRID(2993);
			mainquery += "with block as (select blockid, poptype as btype, landarea as barea, population as bpop, array[block.lat, block.lon] as blocation"
					+ " from census_blocks block where "
					+ "st_within(ST_Transform(ST_SetSRID(ST_MakePoint(block.lon, block.lat),4326), 2993),ST_GeomFromText('"+targetGeometry+"',2993))=true),"
					+ "county as (select countyid, cname from census_counties), tract as (select tractid, population as tpop, landarea as tarea, array[tract.lat, tract.lon] as tlocation "
					+ "from census_tracts tract where "
					+ "st_within(ST_Transform(ST_SetSRID(ST_MakePoint(tract.lon, tract.lat),4326), 2993),ST_GeomFromText('"+targetGeometry+"',2993))=true), ";						
		}
		mainquery+= "blockcounty as (select blockid, btype, barea, bpop, blocation, cname from block inner join county on left(blockid,5)= countyid), "
				+ "blocktract as (select blockid, btype, barea, bpop, blocation, cname, tpop, tarea, tlocation from blockcounty left join tract on left(blockid,11)= tractid) "
				+ "select * from blocktract order by blockid";
		//System.out.println(mainquery);
		try{
			PreparedStatement stmt = connection.prepareStatement(mainquery);
			ResultSet rs = stmt.executeQuery();	
			String cname = "";
			String ccname = "";
			//System.out.println("Query was ran successfully.");
			int i = 0;
			MapBlock blk;
			while (rs.next()) {
				cname = rs.getString("cname");
				blk = new MapBlock();
				blk.County = cname;
				blk.ID = rs.getString("blockid");
				blk.LandArea = rs.getLong("barea");
				Double[] location = (Double[]) rs.getArray("blocation").getArray();
				blk.Lat = location[0];
				blk.Lng = location[1];
				blk.Population = rs.getLong("bpop");
				blk.Type = rs.getString("btype");
				if (!(ccname.equals(cname))){
					if (i>0){
						instance.MapBlocks = Blocks;
						instance.MapTracts = Tracts;
						instance.UrbanPopulation = CountyUrbanPop;
						instance.RuralPopulation = CountyRuralPop;
						TotalUrbanPop += CountyUrbanPop;
						TotalRuralPop += CountyRuralPop;
						TotalBlocks += Blocks.size();
						TotalTracts += Tracts.size();
						Counties.add(instance);
						instance = new MapCounty();
						Blocks = new ArrayList<MapBlock>();
						Tracts = new ArrayList<MapTract>();
						CountyUrbanPop = 0;
						CountyRuralPop = 0;
					}					
					ccname = cname;				
					instance.Name = cname;
					instance.Id = blk.ID.substring(0,5);
					TotalLandArea+= blk.LandArea;
				}
				if (blk.Type.equals("U")){
					CountyUrbanPop += blk.Population;
				} else {
					CountyRuralPop +=blk.Population;
				}
				TractArea = rs.getLong("tarea");
				tinstance = new MapTract();
				tinstance.ID = blk.ID.substring(0,11);
				if (!(Tracts.contains(tinstance)) && TractArea>0){
					tinstance.County = cname;
					tinstance.LandArea = TractArea;
					tinstance.Population = rs.getLong("tpop");
					Double[] tlocation = (Double[]) rs.getArray("tlocation").getArray();
					tinstance.Lat = tlocation[0];
					tinstance.Lng = tlocation[1];
					Tracts.add(tinstance);
				}				
				Blocks.add(blk);
				i++;
		        }
			instance.MapBlocks = Blocks;
			instance.MapTracts = Tracts;
			instance.UrbanPopulation = CountyUrbanPop;
			instance.RuralPopulation = CountyRuralPop;
			TotalUrbanPop += CountyUrbanPop;
			TotalRuralPop += CountyRuralPop;	
			TotalBlocks += Blocks.size();
			TotalTracts += Tracts.size();
			Counties.add(instance);			
			response.MapCounties = Counties;
			response.RuralPopulation = TotalRuralPop;
			response.UrbanPopulation = TotalUrbanPop;
			response.TotalBlocks = TotalBlocks;
			response.TotalLandArea = TotalLandArea;
			response.TotalTracts = TotalTracts;					
			rs.close();
			stmt.close();
		} catch ( Exception e ) {
	        System.err.println( e.getClass().getName()+": "+ e.getMessage() );
	        System.exit(0);
	      }					
		dropConnection(connection);
		return response;
	}
	
	/**
	 *Queries the transit agency tree menu	  
	 */
	public static AgencyRouteList agencyMenu(String[] date, String[] day, String username, int dbindex) {
		AgencyRouteList response = new AgencyRouteList();		
		AgencyRoute instance = new AgencyRoute();
		RouteListm rinstance = new RouteListm();
		VariantListm tinstance = new VariantListm();
		Attr attribute = new Attr();
		Connection connection = makeConnection(dbindex);		
		String mainquery ="";		
		if (day!=null){
			//the query with dates
			mainquery += "with aids as (select distinct agency_id as aid from gtfs_selected_feeds where username='"+username+"'), svcids as (";
			for (int i=0; i<date.length; i++){
				mainquery+= "(select  serviceid_agencyid, serviceid_id from gtfs_calendars gc inner join aids on gc.serviceid_agencyid = aids.aid where startdate::int<="+date[i]
						+" and enddate::int>="+date[i]+" and "+day[i]+" = 1 and serviceid_agencyid||serviceid_id not in (select serviceid_agencyid||serviceid_id from "
						+ "gtfs_calendar_dates where date='"+date[i]+"' and exceptiontype=2)union select serviceid_agencyid, serviceid_id from gtfs_calendar_dates gcd inner join "
						+ "aids on gcd.serviceid_agencyid = aids.aid where date='"+date[i]+"' and exceptiontype=1)";
				if (i+1<date.length)
					mainquery+=" union all ";
			}
			mainquery += "), trips as (select trip.agencyid as aid, trip.tripshortname as sname, trip.tripheadsign as sign, round((trip.length+trip.estlength)::numeric,2) as length, "
					+ "trip.id as tripid, trip.uid as uid, trip.route_id as routeid, count(svcids.serviceid_id) as service from gtfs_trips trip inner join svcids "
					+ "using(serviceid_agencyid, serviceid_id) group by trip.agencyid, trip.id having count(svcids.serviceid_id)>0),tripagency as (select trips.aid, "
					+ "agency.name as agency, trips.sname, trips.sign, trips.length, trips.tripid, trips.uid, trips.routeid from trips inner join gtfs_agencies agency on "
					+ "trips.aid=agency.id), tripstops as (select trip.aid, trip.agency, trip.tripid, trip.routeid, trip.sname, trip.sign, trip.length, trip.uid, "
					+ "stop.stop_agencyid_origin, stop.stop_id_origin, stop.stop_name_origin, stop.stop_agencyid_destination, stop.stop_id_destination, stop.stop_name_destination "
					+ "from tripagency trip inner join gtfs_trip_stops stop on trip.aid = stop.trip_agencyid and trip.tripid = stop.trip_id), triproute as (select trip.aid, "
					+ "trip.agency, trip.tripid, trip.routeid, trip.sname, trip.sign, trip.length, trip.uid, trip.stop_agencyid_origin, trip.stop_id_origin, trip.stop_name_origin, "
					+ "trip.stop_agencyid_destination, trip.stop_id_destination, trip.stop_name_destination, route.shortname as rsname, route.longname as rlname from tripstops trip "
					+ "inner join gtfs_routes route on trip.aid = route.agencyid and trip.routeid = route.id) select * from triproute order by agency, routeid, length desc, tripid";
		}else {	
			mainquery += "with aids as (select distinct agency_id as aid from gtfs_selected_feeds where username='"+username+"'), trips as (select trip.agencyid as aid, trip.tripshortname "
					+ "as sname, round((trip.length+trip.estlength)::numeric,2) as length, trip.tripheadsign as sign, trip.id as tripid, trip.uid as uid, trip.route_id as routeid "
					+ "from gtfs_trips trip inner join aids on trip.serviceid_agencyid = aids.aid), tripagency as (select trips.aid, agency.name as agency, trips.sname, "
					+ "trips.sign, trips.length, trips.tripid, trips.uid, trips.routeid from trips inner join gtfs_agencies agency on trips.aid=agency.id), tripstops as (select "
					+ "trip.aid, trip.agency, trip.tripid, trip.routeid, trip.sname, trip.sign, trip.length, trip.uid, stop.stop_agencyid_origin, stop.stop_id_origin, "
					+ "stop.stop_name_origin, stop.stop_agencyid_destination, stop.stop_id_destination, stop.stop_name_destination from tripagency trip inner join "
					+ "gtfs_trip_stops stop on trip.aid = stop.trip_agencyid and trip.tripid = stop.trip_id), triproute as (select trip.aid, trip.agency, trip.tripid, trip.routeid, "
					+ "trip.sname, trip.sign, trip.uid, trip.length, trip.stop_agencyid_origin, trip.stop_id_origin, trip.stop_name_origin, trip.stop_agencyid_destination, "
					+ "trip.stop_id_destination, trip.stop_name_destination, route.shortname as rsname, route.longname as rlname from tripstops trip inner join gtfs_routes route on "
					+ "trip.aid = route.agencyid and trip.routeid = route.id) select * from triproute order by agency, routeid, length desc, tripid";							
		}		
		//System.out.println(mainquery);
		try{
			PreparedStatement stmt = connection.prepareStatement(mainquery);
			ResultSet rs = stmt.executeQuery();	
			String aid = "";
			String caid = "";
			String rid = "";
			String crid = "";
			String rsname = "";
			String rlname = "";
			String uid = "";
			String tsname = "";
			String thsign = "";			
			ArrayList<String> tripuids = new ArrayList<String>();			
			//System.out.println("Query was ran successfully.");
			int agencies_cntr = 0;
			int routes_cntr = 0;
			int trips_cntr = 0;
			int c = 1;
			while (rs.next()) {	    			    		
				aid = rs.getString("aid");					        	
	        	if (!(caid.equals(aid))){
					if (agencies_cntr>0){
						///add some children to it and add it to the rfesponse
						instance.children.add(rinstance);
						response.data.add(instance);
						instance = new AgencyRoute();
						rinstance = new RouteListm();
						attribute = new Attr();
						routes_cntr = 0;
						trips_cntr = 0;
						tripuids = new ArrayList<String>();
					}					
					caid  = aid;				
					instance.state = "closed";
					instance.data = formattedIndex(c)+". "+rs.getString("agency");
					c+=1;
					attribute.id = caid;
					attribute.type = "agency";
					instance.attr = attribute;					
					agencies_cntr++;										
	        	}
	        	rid = rs.getString("routeid");					        	
	        	if (!(crid.equals(rid))){
					if (routes_cntr>0){
						///add some children to it and add it to the rfesponse
						instance.children.add(rinstance);
						rinstance = new RouteListm();						
					}
					crid  = rid;				
					rinstance.state = "closed";
					attribute = new Attr();
					attribute.id = rid;
					attribute.type = "route";
					rinstance.attr = attribute;					
					rsname = rs.getString("rsname");
					rlname = rs.getString("rlname");
	                if (rlname!= null && !rlname.equals("")){
	                	if ((rsname!= null) && !rsname.equals("")){
		                	rinstance.data = rlname + "(" + rsname+ ")";		                		
		                	} else {
		                		rinstance.data = rlname;
		                	}
		                } else {
		                	rinstance.data = rsname;
		                }
	                trips_cntr= 0;
	                routes_cntr++;
	                tripuids = new ArrayList<String>();
	                }
	        		uid = rs.getString("uid");
	        		if (!tripuids.contains(uid)){
	        			tripuids.add(uid);
	        			tinstance = new VariantListm();
	        			tinstance.state = "leaf";
	        			attribute = new Attr();
	        			attribute.type = "variant";
	        			attribute.id = rs.getString("tripid");
	        			thsign = rs.getString("sign");	        				        					
                    	if (thsign!=null && !thsign.equals("")) {
                    		tinstance.data = thsign;  
                    		tsname = rs.getString("sname");
                    	}else {
                    		if (tsname!=null && !tsname.equals("")){
                    			tinstance.data = tsname;                    			
                    		}else{
                    			tinstance.data = "From "+ rs.getString("stop_name_origin") + " to "+ rs.getString("stop_name_destination");
                    		}
                    	}
                    	attribute.longest = (trips_cntr > 0 )? 0:1;                    		
    	                tinstance.attr = attribute;       			
	        			rinstance.children.add(tinstance);
	        			trips_cntr++;
	        		}					
	        	}
			instance.children.add(rinstance);
			response.data.add(instance);
			rs.close();
			stmt.close();
		} catch ( Exception e ) {
	        System.err.println( e.getClass().getName()+": "+ e.getMessage() );
	        System.exit(0);
	      }					
		dropConnection(connection);
		return response;
	}
	
	/**
	 *Queries calendar start and end dates by username or username and agency id	  
	 */
	public static StartEndDates getsedates(String username, String agency, int dbindex) {
		StartEndDates response = new StartEndDates();		
		Connection connection = makeConnection(dbindex);		
		//Setting default start and end dates in case no start and end dates exist in the database
		SimpleDateFormat sdfDate = new SimpleDateFormat("yyyyMMdd");
	    Date today = new Date();
	    Calendar cal = new GregorianCalendar();
		cal.setTime(today);
		cal.add(Calendar.DAY_OF_MONTH, -45); //default start date set to 45 days before the current date
		String startDate = sdfDate.format(cal.getTime());
		cal.add(Calendar.DAY_OF_MONTH, +90); //default end dates set to 45 days after the current date
		String endDate = sdfDate.format(cal.getTime());
		//System.out.println("Default start date: "+startDate+" Default end date: "+endDate);
		String mainquery ="";
		if (agency==null){
			mainquery = "with aids as (select distinct agency_id as aid from gtfs_selected_feeds where username='"+username+"') select coalesce(min(startdate::int),"+startDate
					+") as start, coalesce(max(enddate::int),"+endDate+") as end from gtfs_feed_info inner join aids on aid=defaultid";
			} else {
			mainquery = "select coalesce(min(startdate::int),"+startDate+") as start, coalesce(max(enddate::int),"+endDate+") as end from gtfs_feed_info inner join gtfs_agencies "
					+ "using(defaultid) where id='"+agency+"'";
		}				
		//System.out.println(mainquery);
		try{
			PreparedStatement stmt = connection.prepareStatement(mainquery);
			ResultSet rs = stmt.executeQuery();				
			while (rs.next()) {
				startDate = rs.getString("start");
				endDate = rs.getString("end");				
	        	}		
			rs.close();
			stmt.close();
		} catch ( Exception e ) {
	        System.err.println( e.getClass().getName()+": "+ e.getMessage() );
	        System.exit(0);
	      }					
		dropConnection(connection);
		response.Startdate = startDate;
		response.Enddate = endDate;		
		return response;
	}
	
	public static String formattedIndex(int c){
		
		if(c<10){
			return "0"+c;
		}else{
			return ""+c;
		}
	}
}
