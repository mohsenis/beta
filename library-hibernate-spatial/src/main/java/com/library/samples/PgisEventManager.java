package com.library.samples;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement; 
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.TreeSet;

import org.geotools.geometry.jts.JTS;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.referencing.CRS;
import org.onebusaway.gtfs.impl.Databases;
import org.onebusaway.gtfs.model.StopTime;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.library.model.*;
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
	
/*	public static void main( String args[] )
    {
      Connection c = null;
      Statement stmt = null;
      try {
      Class.forName("org.postgresql.Driver");
        c = DriverManager
           .getConnection("jdbc:postgresql://localhost:5432/gtfsdb",
           "postgres", "123123");
        c.setAutoCommit(false);
        System.out.println("Opened database successfully");

        stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery( "SELECT * FROM COMPANY;" );
        while ( rs.next() ) {
           int id = rs.getInt("id");
           String  name = rs.getString("name");
           int age  = rs.getInt("age");
           String  address = rs.getString("address");
           float salary = rs.getFloat("salary");
           System.out.println( "ID = " + id );
           System.out.println( "NAME = " + name );
           System.out.println( "AGE = " + age );
           System.out.println( "ADDRESS = " + address );
           System.out.println( "SALARY = " + salary );
           System.out.println();
        }
        rs.close();
        stmt.close();
        c.close();
      } catch ( Exception e ) {
        System.err.println( e.getClass().getName()+": "+ e.getMessage() );
        System.exit(0);
      }
      System.out.println("Operation done successfully");
    } */
	public static long UrbanCensusbyPop(int pop, int dbindex) 
    {	
		Connection connection = makeConnection(dbindex);
      //Connection c = null;
      Statement stmt = null;
      long population = 0;
      try {
        stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery( "select sum(population) as pop from "
        		+ "(select distinct block.blockid, population from census_blocks block join gtfs_stops stop on st_dwithin(block.location, stop.location, 161)=true "
        		+ "where stop.urbanid in (select urbanid from census_urbans where population>=50000) and block.urbanid in (select urbanid from census_urbans where population>=50000))"
        		+ "as pops;" );        
        while ( rs.next() ) {
           population = rs.getLong("pop");           
           //System.out.println( "population = " + population );           
        }
        rs.close();
        stmt.close();
        //c.close();
      } catch ( Exception e ) {
        System.err.println( e.getClass().getName()+": "+ e.getMessage() );
        System.exit(0);
      }
      dropConnection(connection);
      //System.out.println("Operation done successfully");
      return population;
    }
	/**
	 *Queries agency clusters (connected transit networks) and returns a list of all transit agencies with their connected agencies
	 */
	public static List<agencyCluster> agencyCluster(double dist, int dbindex){
		List<agencyCluster> response = new ArrayList<agencyCluster>();
		Connection connection = makeConnection(dbindex);
		Statement stmt = null;
		try{
			stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery( "select agency.name as name, aid as aid, size as size, aids as aids, names as names, min_gaps as min_gaps from gtfs_agencies agency inner join "
					+ "(select aid1 as aid, count(aid2) as size, array_agg(aid2) as aids, array_agg(name) as names, array_agg(dist) as min_gaps from "
					+ "(select stop1.agencyid as aid1, stop2.name as name, stop2.agencyid as aid2, min(st_distance(stop1.location,stop2.location)) as dist from "
					+"(select map.agencyid as agencyid, stop.location as location, stop.id as id from gtfs_stops stop inner join gtfs_stop_service_map map on "
					+ "map.agencyid_def=stop.agencyid and map.stopid=stop.id) as stop1 inner join "
					+ "(select agency.name as name, stp.agencyid as agencyid, stp.location as location, stp.id as id from gtfs_agencies agency inner join "
					+ "(select map.agencyid as agencyid, stop.location as location, stop.id as id from gtfs_stops stop inner join gtfs_stop_service_map map on"
					+ " map.agencyid_def=stop.agencyid and map.stopid=stop.id) as stp on stp.agencyid=agency.id) as stop2 on st_dwithin(stop1.location, stop2.location, "
					+ String.valueOf(dist)
					+ ")=true where stop1.agencyid!=stop2.agencyid group by aid1, aid2, stop2.name) as pairs group by aid1) as clusters on agency.id=clusters.aid order by size desc" );
			while ( rs.next() ) {
				agencyCluster instance = new agencyCluster();
				instance.agencyId = rs.getString("aid");
				instance.agencyName = rs.getString("name");
				instance.clusterSize = rs.getLong("size");
				String[] agencyIds = (String[]) rs.getArray("aids").getArray();
				instance.agencyIds= Arrays.asList(agencyIds);
				String[] agencyNames = (String[]) rs.getArray("names").getArray();
				instance.agencyNames= Arrays.asList(agencyNames);
				Double[] gaps = (Double[]) rs.getArray("min_gaps").getArray();
				instance.minGaps= Arrays.asList(gaps);
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
	 *Queries connected transit agencies and list of connections for a given transit agency
	 */
	public static List<agencyCluster> agencyClusterDetails(double dist, String agencyId, int dbindex){
		List<agencyCluster> response = new ArrayList<agencyCluster>();
		Connection connection = makeConnection(dbindex);
		Statement stmt = null;
		try{
			stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery( "select name as name, aid2 as aid, count(aid2)as size, min(dist) as mingap, max(dist) as maxgap, round(avg(dist),2) as meangap, "
					+ "array_agg(sname1||'-'||sname2||':'||dist::text) as connection from "
					+ "(select stop2.name as name, stop2.agencyid as aid2, stop1.sname as sname1,stop2.sname as sname2, "
					+ "round((3.28084*st_distance(stop1.location,stop2.location))::numeric, 2) as dist from "
					+ "(select map.agencyid as agencyid, stop.location as location, stop.id as id, stop.name as sname from gtfs_stops stop inner join gtfs_stop_service_map map "
					+ "on map.agencyid_def=stop.agencyid and map.stopid=stop.id) as stop1 inner join "
					+ "(select agency.name as name, stp.name as sname, stp.agencyid as agencyid, stp.location as location, stp.id as id from gtfs_agencies agency inner join "
					+ "(select map.agencyid as agencyid, stop.location as location, stop.id as id, stop.name as name from gtfs_stops stop inner join gtfs_stop_service_map map "
					+ "on map.agencyid_def=stop.agencyid and map.stopid=stop.id) as stp on stp.agencyid=agency.id) as stop2 on st_dwithin(stop1.location, stop2.location,"
					+ String.valueOf(dist)
					+ ")=true where stop1.agencyid!=stop2.agencyid and stop1.agencyid='"
					+ agencyId
					+ "' order by stop2.agencyid,dist, stop2.sname) as recs group by name, aid2 order by aid2");
			while ( rs.next() ) {
				agencyCluster instance = new agencyCluster();
				instance.agencyId = rs.getString("aid");
				instance.agencyName = rs.getString("name");
				instance.clusterSize = rs.getLong("size");
				instance.minGap = rs.getFloat("mingap");
				instance.maxGap = rs.getFloat("maxgap");
				instance.meanGap = rs.getFloat("meangap");
				String[] connections = (String[]) rs.getArray("connection").getArray();
				instance.connections= Arrays.asList(connections);				
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
	 *returns service hours in seconds for the whole state for a single day in YYYYMMDD format and day of week in all lower case
	 */
	public static long ServiceHours(String date, String day, int dbindex){
		long response = 0;
		Connection connection = makeConnection(dbindex);
		Statement stmt = null;
		try{
			stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery( "select sum(tlength) as svchours from gtfs_trips where serviceid_agencyid||serviceid_id in ("+
					"select  serviceid_agencyid||serviceid_id from gtfs_calendars where startdate::int<="
					+ date + " and enddate::int>=" + date + " and "	+ day + " = 1 "+
					"and serviceid_agencyid||serviceid_id not in (select serviceid_agencyid||serviceid_id from gtfs_calendar_dates where date='"
					+ date + "' and exceptiontype=2)"+ " union	select serviceid_agencyid||serviceid_id from gtfs_calendar_dates where date='"
					+ date + "' and exceptiontype=1)");	
			while ( rs.next() ) {
				response = rs.getLong("svchours");
			}
		} catch ( Exception e ) {
	        System.err.println( e.getClass().getName()+": "+ e.getMessage() );
	        System.exit(0);
	      }
		dropConnection(connection);
		return response;
	}
	
	/**
	 *returns service miles in miles for the whole state for a single day in YYYYMMDD format and day of week in all lower case
	 */
	public static double ServiceMiles(String date, String day, int dbindex){
		double response = 0;
		Connection connection = makeConnection(dbindex);
		Statement stmt = null;
		try{
			stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery( "select sum(length+estlength) as svcmiles from gtfs_trips where serviceid_agencyid||serviceid_id in ("+
			"select  serviceid_agencyid||serviceid_id from gtfs_calendars where startdate::int<="
			+ date + " and enddate::int>=" + date + " and "	+ day + " = 1"+ 
			"and serviceid_agencyid||serviceid_id not in (select serviceid_agencyid||serviceid_id from gtfs_calendar_dates where date='"
			+ date + "' and exceptiontype=2)"+ "union select serviceid_agencyid||serviceid_id from gtfs_calendar_dates where date='"
			+ date + "' and exceptiontype=1)");	
			while ( rs.next() ) {
				response = rs.getDouble("svcmiles");
			}
		} catch ( Exception e ) {
	        System.err.println( e.getClass().getName()+": "+ e.getMessage() );
	        System.exit(0);
	      }
		dropConnection(connection);
		return response;
	}
	
	/**
	 *returns sum of population within x distance of all stops 
	 */
	public static long PopWithinX(double x, int dbindex){
		long response = 0;
		Connection connection = makeConnection(dbindex);
		Statement stmt = null;
		try{
			stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery( "select sum(population) as pop from (select population from census_blocks block inner join gtfs_stops stop "
					+ "on st_dwithin(block.location,stop.location, "+ String.valueOf(x) + ")=true group by block.blockid) as pop");	
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
	 *returns sum of population served at specified level of service
	 */
	public static long PopServedatLOS(double x, String date, String day, int l, int dbindex){
		long response = 0;
		Statement stmt = null;
		Connection connection = makeConnection(dbindex);
		try{
			stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery( "select sum(population) as pop from (select population from census_blocks block inner join "
					+ "(select stop.location as location, stimes.stop_agencyid as aid, stimes.stop_id as sid, count(stimes.stop_id) as frequency from "
					+ "gtfs_stops stop inner join gtfs_stop_times stimes on stop.id = stimes.stop_id and stop.agencyid = stimes.stop_agencyid inner join "
					+ "(select agencyid, id from gtfs_trips where serviceid_agencyid||serviceid_id in "
					+ "(select  serviceid_agencyid||serviceid_id from gtfs_calendars where startdate::int<="
					+ date + " and enddate::int>=" + date + " and "	+ day + " = 1 "
					+ "and serviceid_agencyid||serviceid_id not in (select serviceid_agencyid||serviceid_id from gtfs_calendar_dates where date='"
					+ date + "' and exceptiontype=2)union select serviceid_agencyid||serviceid_id from gtfs_calendar_dates where date='" + date
					+ "' and exceptiontype=1))as trips "
					+ "on trips.agencyid = stimes.trip_agencyid and trips.id = stimes.trip_id group by stop_agencyid, stop_id, stop.location having count(stimes.stop_id)>="
					+ l	+ ") as svstop on st_dwithin(svstop.location, block.location, " + String.valueOf(x)+ ")=true group by blockid) as pops");	
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
	 *returns population served by service and service stops. Keys are svcstops and svcpop
	 */
	public static HashMap<String, Long> ServiceStopsPop(double x, String date, String day, int dbindex){
		HashMap<String, Long> response = new HashMap<String, Long>();
		Statement stmt = null;
		Connection connection = makeConnection(dbindex);
		try{
			stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery( "select sum(frequency) as svcstops, sum(svcpop) as svcpop from (select frequency, frequency*sum(population) as svcpop from "
					+ "census_blocks block inner join (select stop.location as location, stimes.stop_agencyid as aid, stimes.stop_id as sid, count(stimes.stop_id) as frequency "
					+ "from gtfs_stops stop inner join gtfs_stop_times stimes on stop.id = stimes.stop_id and stop.agencyid = stimes.stop_agencyid inner join "
					+ "(select agencyid, id from gtfs_trips where serviceid_agencyid||serviceid_id in (select  serviceid_agencyid||serviceid_id from gtfs_calendars where "
					+ "startdate::int<="+ date + " and enddate::int>=" + date + " and "	+ day+ " = 1 and serviceid_agencyid||serviceid_id not in "
					+ "(select serviceid_agencyid||serviceid_id from gtfs_calendar_dates where date='" + date + "' and exceptiontype=2) union select "
					+ "serviceid_agencyid||serviceid_id from gtfs_calendar_dates where date='" + date + "' and exceptiontype=1))as trips on trips.agencyid = stimes.trip_agencyid "
					+ "and trips.id = stimes.trip_id group by stop_agencyid, stop_id, stop.location) as svstop on st_dwithin(svstop.location, block.location, "
					+ String.valueOf(x)	+ ")=true group by "+ "svstop.aid, svstop.sid, frequency) as result");	
			while ( rs.next() ) {
				response.put("svcstops", (long)rs.getInt("svcstops")); 
				response.put("svcpop", rs.getLong("svcpop")); 
			}
		} catch ( Exception e ) {
	        System.err.println( e.getClass().getName()+": "+ e.getMessage() );
	        System.exit(0);
	      }
		dropConnection(connection);
		return response;
	}
	
	/**
	 *returns min and max Hours of service in int time (epoch) fromat for a given date and day of week in an integer array
	 */
	public static int[] HoursofService(String date, String day, int dbindex){
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
	}
	/**
	 *Queries stop clusters (connected transit networks) and returns a list of all transit agencies with their connected agencies
	 */
	public static TreeSet<StopCluster> stopClusters(String[] dates, String[] days, double dist, int dbindex){	
		HashMap<String, Integer> serviceMap = stopFrequency(dates, days, dbindex);
		TreeSet<StopCluster> response = new ClusterPriorityQueue();
		Connection connection = makeConnection(dbindex);
		String mainquery = "select cluster.cid as cid, cluster.sid as sid , cluster.aid as aid, cluster.name as name, map.agencies as agencies, map.routes as routes from "
				+ "(select stp1.agencyid||stp1.id as cid, stp2.id as sid, stp2.name as name, stp2.agencyid as aid from gtfs_stops stp1 inner join gtfs_stops stp2 "
				+ "on st_dwithin(stp1.location, stp2.location, ?)) as cluster inner join (select agencyid_def as aid, array_agg(distinct agencyid) as agencies, stopid as sid, "
				+ "array_agg(distinct routeid) as routes from gtfs_stop_route_map group by agencyid_def, stopid ) as map on map.sid = cluster.sid and map.aid = cluster.aid "
				+ "order by cluster.cid, cluster.sid";
		
		try{
			//System.out.println("Starting Query");
			PreparedStatement stmt = connection.prepareStatement(mainquery);
			stmt.setDouble(1, dist);
			ResultSet rs = stmt.executeQuery();
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
	 *Queries frequency of service for all stops in the database for a set of dates and days
	 */
	public static HashMap<String, Integer> stopFrequency(String[] date, String[] day, int dbindex){				
		HashMap<String, Integer> response = new HashMap<String, Integer>();
		Connection connection = makeConnection(dbindex);
		String[] mainquery = new String[date.length];
		String id = "";
		for (int i=0; i<date.length; i++){
			mainquery[i]= "select aid_def||stopid as id, svc from (select stimes.stop_id as stopid, stimes.stop_agencyid as aid_def, sum(service) as svc from "
					+ "(select trip.agencyid as aid, trip.id as tripid, count(svc.sid) as service from gtfs_trips trip left join "
					+ "(select  serviceid_agencyid as aid, serviceid_id as sid from gtfs_calendars where startdate::int<="+date[i]+" and enddate::int>="+date[i]+" and "+day[i]+" = 1 "
					+ "and serviceid_agencyid||serviceid_id not in (select serviceid_agencyid||serviceid_id from gtfs_calendar_dates where date='"+date[i]+"' and exceptiontype=2) "
					+ "union select serviceid_agencyid, serviceid_id from gtfs_calendar_dates where date='"+date[i]+"' and exceptiontype=1)as svc on "
					+ "trip.serviceid_id = svc.sid and trip.serviceid_agencyid= svc.aid group by trip.agencyid, trip.id ) as svcmap inner join gtfs_stop_times stimes on "
					+ "svcmap.aid = stimes.trip_agencyid and svcmap.tripid = stimes.trip_id group by aid_def, stopid) as stopids inner join gtfs_stops stop on "
					+ "stop.agencyid = stopids.aid_def and stop.id= stopids.stopid";
			try{
				PreparedStatement stmt = connection.prepareStatement(mainquery[i]);
				ResultSet rs = stmt.executeQuery();
				while (rs.next()) {
					//count++;
					if (i==0){
						response.put(rs.getString("id"), rs.getInt("svc"));
					} else {
						id = rs.getString("id");
						response.put(id, response.get(id)+rs.getInt("svc"));
					}					
			        }
				rs.close();
				stmt.close();
			} catch ( Exception e ) {
		        System.err.println( e.getClass().getName()+": "+ e.getMessage() );
		        System.exit(0);
		      }
		}				
		dropConnection(connection);
		return response;
	}
	/**
	 *Queries the transit part of the on map report
	 * @throws TransformException 
	 * @throws MismatchedDimensionException 
	 * @throws FactoryException 
	 * @throws NoSuchAuthorityCodeException 
	 */
	public static MapTransit onMapStops(String[] date, String[] day, double d, double[] lat, double[] lon, int dbindex) {
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
		String mainquery = "with svcids as (";
		for (int i=0; i<date.length; i++){
			mainquery+= "(select  serviceid_agencyid, serviceid_id from gtfs_calendars where startdate::int<="+date[i]+" and enddate::int>="+date[i]+
					" and "+day[i]+" = 1 and serviceid_agencyid||serviceid_id not in (select serviceid_agencyid||serviceid_id from gtfs_calendar_dates where date='"+date[i]+
					"' and exceptiontype=2)union select serviceid_agencyid, serviceid_id from gtfs_calendar_dates where date='"+date[i]+"' and exceptiontype=1)";
			if (i+1<date.length)
				mainquery+=" union all ";
		}
		mainquery+="), trips as (select trip.agencyid as aid, trip.id as tripid, trip.uid as uid, trip.route_id as routeid, round((trip.length+trip.estlength)::numeric,2)::varchar as length, "+
				"trip.routeshortname as rname, trip.epshape as shape, count(svcids.serviceid_id) as service from gtfs_trips trip inner join svcids "+
				"using(serviceid_agencyid, serviceid_id) group by trip.agencyid, trip.id),fare as (select fare.route_agencyid as aid, fare.route_id as routeid ,"
				+ " avg(fareat.price) as price from gtfs_fare_rules fare inner join gtfs_fare_attributes fareat on fare.fare_agencyid = fareat.agencyid and fare.fare_id=id "
				+ "group by fare.route_agencyid, fare.route_id),trip_fare as (select trips.aid, trips.uid, trips.tripid, trips.routeid, trips.rname, trips.length, trips.shape, "
				+ "trips.service, fare.price from trips inner join fare using (aid, routeid)),stopids as (select trip_fare.aid, "
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
							FareSum+=Double.parseDouble(routeinfo[5]);
							Fares.add(Double.parseDouble(routeinfo[5]));
					}
					MapRoute rt = new MapRoute();
					rt.AgencyId = cid;
					rt.uid = routeinfo[0];
					rt.Id = routeinfo[1];
					if (!(Routes.contains(rt))){						
						rt.Name = routeinfo[2];
						rt.Length = Float.parseFloat(routeinfo[3]);
						rt.Frequency = Integer.parseInt(routeinfo[4]);
						rt.Fare = "$"+String.valueOf(Math.round(Double.parseDouble(routeinfo[5])*100.00)/100.00);					
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
	 * @throws TransformException 
	 * @throws MismatchedDimensionException 
	 * @throws FactoryException 
	 * @throws NoSuchAuthorityCodeException 
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
			mainquery += "with aids as (select agency_id as aid from gtfs_selected_feeds where username='"+username+"'), svcids as (";
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
					+ "inner join gtfs_routes route on trip.aid = route.agencyid and trip.routeid = route.id) select * from triproute order by aid, routeid, length desc, tripid";
		}else {	
			mainquery += "with aids as (select agency_id as aid from gtfs_selected_feeds where username='"+username+"'), trips as (select trip.agencyid as aid, trip.tripshortname "
					+ "as sname, round((trip.length+trip.estlength)::numeric,2) as length, trip.tripheadsign as sign, trip.id as tripid, trip.uid as uid, trip.route_id as routeid "
					+ "from gtfs_trips trip inner join aids on trip.serviceid_agencyid = aids.aid), tripagency as (select trips.aid, agency.name as agency, trips.sname, "
					+ "trips.sign, trips.length, trips.tripid, trips.uid, trips.routeid from trips inner join gtfs_agencies agency on trips.aid=agency.id), tripstops as (select "
					+ "trip.aid, trip.agency, trip.tripid, trip.routeid, trip.sname, trip.sign, trip.length, trip.uid, stop.stop_agencyid_origin, stop.stop_id_origin, "
					+ "stop.stop_name_origin, stop.stop_agencyid_destination, stop.stop_id_destination, stop.stop_name_destination from tripagency trip inner join "
					+ "gtfs_trip_stops stop on trip.aid = stop.trip_agencyid and trip.tripid = stop.trip_id), triproute as (select trip.aid, trip.agency, trip.tripid, trip.routeid, "
					+ "trip.sname, trip.sign, trip.uid, trip.length, trip.stop_agencyid_origin, trip.stop_id_origin, trip.stop_name_origin, trip.stop_agencyid_destination, "
					+ "trip.stop_id_destination, trip.stop_name_destination, route.shortname as rsname, route.longname as rlname from tripstops trip inner join gtfs_routes route on "
					+ "trip.aid = route.agencyid and trip.routeid = route.id) select * from triproute order by aid, routeid, length desc, tripid";							
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
					instance.data = rs.getString("agency");
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
}
