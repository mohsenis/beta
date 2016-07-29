package com.library.samples;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.library.model.TransitConnection;
import com.library.model.ParknRide;
import com.library.model.agencyCluster;
import com.library.model.congrapph.Agency;
import com.library.model.congrapph.AgencyCentroid;
import com.library.model.congrapph.ConGraphAgencyGraph;
import com.library.model.congrapph.ConGraphCluster;
import com.library.model.congrapph.ConGraphObj;
import com.library.model.congrapph.Coordinate;

public class SpatialEventManager {

	public static List<ParknRide> getPnRs(double[] lat, double[] lon,
			double radius, int dbindex) throws SQLException {
		List<ParknRide> output = new ArrayList<ParknRide>();
		Connection connection = PgisEventManager.makeConnection(dbindex);
		Statement stmt = null;
		String query;
		if (lat.length == 1) // If selected area is a circle
			query = "SELECT * FROM parknride "
					+ " WHERE ST_DWITHIN(parknride.geom,ST_transform(ST_setsrid(ST_MakePoint("
					+ lon[0] + ", " + lat[0] + "),4326), 2993), " + radius
					+ ")";
		else { // If selected area is a polygon or rectangle
			query = "SELECT * FROM parknride "
					+ " WHERE ST_CONTAINS( ST_transform(st_geometryfromtext('POLYGON((";
			for (int i = 0; i < lat.length; i++) {
				query += lon[i] + " " + lat[i] + ",";
			}
			query = query += lon[0] + " " + lat[0]; // Closing the polygon loop
			query += "))', 4326),2993), parknride.geom)";
		}
		System.out.println(query);
		stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery(query);
		while (rs.next()) {
			ParknRide i = new ParknRide();
			i.pnrid = rs.getInt("pnrid");
			i.lat = rs.getDouble("lat");
			i.lon = rs.getDouble("lon");
			i.lotname = rs.getString("lotname");
			i.location = rs.getString("location");
			i.city = rs.getString("city");
			i.zipcode = rs.getInt("zipcode");
			i.countyid = rs.getString("countyid");
			i.county = rs.getString("county");
			i.spaces = rs.getInt("spaces");
			i.accessiblespaces = rs.getInt("accessiblespaces");
			i.bikerackspaces = rs.getInt("bikerackspaces");
			i.bikelockerspaces = rs.getInt("bikelockerspaces");
			i.electricvehiclespaces = rs.getInt("electricvehiclespaces");
			i.carsharing = rs.getString("carsharing");
			i.transitservice = rs.getString("transitservice");
			i.availability = rs.getString("availability");
			i.timelimit = rs.getString("timelimit");
			i.restroom = rs.getString("restroom");
			i.benches = rs.getString("benches");
			i.shelter = rs.getString("shelter");
			i.indoorwaitingarea = rs.getString("indoorwaitingarea");
			i.trashcan = rs.getString("trashcan");
			i.lighting = rs.getString("lighting");
			i.securitycameras = rs.getString("securitycameras");
			i.sidewalks = rs.getString("sidewalks");
			i.pnrsignage = rs.getString("pnrsignage");
			i.lotsurface = rs.getString("lotsurface");
			i.propertyowner = rs.getString("propertyowner");
			i.localexpert = rs.getString("localexpert");
			output.add(i);
		}
		return output;
	}

	public static HashMap<String, Agency> getAllAgencies ( String username, int dbindex ) throws SQLException {
		HashMap<String,Agency> response = new HashMap<String, Agency>();
		String query = "SELECT * FROM gtfs_agencies WHERE gtfs_agencies.defaultid IN (SELECT DISTINCT agency_id AS aid "
				+ "FROM gtfs_selected_feeds WHERE username='" + username + "')  ORDER BY name";
		Connection connection = PgisEventManager.makeConnection(dbindex);
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery(query);
		
		while ( rs.next() ){
			Agency i = new Agency();
			i.name = rs.getString("name");
			i.centralized = rs.getBoolean("centralized");
			i.id = rs.getString("id");
			response.put(rs.getString("id"), i);
		}
		
		connection.close();
		return response;
	}
	
	public static Set<ConGraphObj> getConGraphObj(String agencyID, String agencyName, String fulldate, String day, String username, double radius, Statement stmt) throws SQLException{
		Set<ConGraphObj> response = new HashSet<ConGraphObj>();
		String query = "with aids as (select agency_id as aid from gtfs_selected_feeds where username='" + username + "'),"
				+ "svcids as (select serviceid_agencyid, serviceid_id "
				+ "	from gtfs_calendars gc inner join aids on gc.serviceid_agencyid = '" + agencyID + "'"
				+ " 	where startdate::int<=" + fulldate + " and enddate::int>=" + fulldate + " and " + day + "= 1 and serviceid_agencyid||serviceid_id "
				+ "	not in (select serviceid_agencyid||serviceid_id from gtfs_calendar_dates where date='" + fulldate + "' and exceptiontype=2)"
				+ "	union select serviceid_agencyid, serviceid_id "
				+ "	from gtfs_calendar_dates gcd inner join aids on gcd.serviceid_agencyid = '" + agencyID + "' where date='" + fulldate + "' and exceptiontype=1),"
				+ " trips AS (select trip.agencyid as aid, trip.id as tripid, trip.route_id as routeid"
				+ "	from svcids inner join gtfs_trips trip using(serviceid_agencyid, serviceid_id)),"
				+ " a1stops AS (select stime.trip_agencyid as agencyid, gtfs_agencies.name as agencyname, stime.stop_id as stopid, stop.name as name, stop.lat, stop.lon, stop.location as location"
				+ "	from gtfs_stops stop inner join gtfs_stop_times stime on stime.stop_agencyid = stop.agencyid and stime.stop_id = stop.id"
				+ "	inner join trips on stime.trip_agencyid =trips.aid and stime.trip_id=trips.tripid"
				+ "	inner join gtfs_agencies ON stime.trip_agencyid = gtfs_agencies.id"
				+ "	where stop.agencyid IN (SELECT aid FROM aids)"
				+ "	group by stime.trip_agencyid, stime.stop_agencyid, stime.stop_id, stop.location, gtfs_agencies.name, stop.name, stop.lat, stop.lon),"
				+ " svcids1 as (select serviceid_agencyid, serviceid_id"
				+ "	from gtfs_calendars gc inner join aids on gc.serviceid_agencyid = aids.aid"
				+ "	where startdate::int<=" + fulldate + " and enddate::int>=" + fulldate + " and " + day  + "= 1 and gc.serviceid_agencyid <> '" + agencyID + "'"
				+ "	and serviceid_agencyid||serviceid_id not in (select serviceid_agencyid||serviceid_id from gtfs_calendar_dates where date='" + fulldate + "' and exceptiontype=2)"
				+ " union select serviceid_agencyid, serviceid_id"
				+ "	from gtfs_calendar_dates gcd inner join aids on gcd.serviceid_agencyid = aids.aid where date='" + fulldate + "' and exceptiontype=1 and gcd.serviceid_agencyid <> '" + agencyID + "' ),"
				+ " trips1 AS (select trip.agencyid as aid, trip.id as tripid, trip.route_id as routeid"
				+ " from svcids1 inner join gtfs_trips trip using(serviceid_agencyid, serviceid_id)),"
				+ " a2stops AS (select a1stops.agencyid AS a1id, a1stops.agencyname AS a1name, stime.trip_agencyid as a2id, gtfs_agencies.name as a2name,"
				+ " stime.stop_id as stopid, stop.name as name, stop.lat, stop.lon, stop.location as location, ST_DISTANCE(stop.location, a1stops.location)::NUMERIC AS dist"
				+ " from gtfs_stops stop inner join a1stops on ST_DISTANCE(stop.location, a1stops.location) < " + radius 
				+ "	inner join gtfs_stop_times stime on stime.stop_agencyid = stop.agencyid and stime.stop_id = stop.id"
				+ "	inner join trips1 on stime.trip_agencyid =trips1.aid and stime.trip_id=trips1.tripid"
				+ "	inner join gtfs_agencies ON stime.trip_agencyid = gtfs_agencies.id"
				+ "	where stop.agencyid IN (SELECT aid FROM aids)"
				+ "	group by stime.trip_agencyid, stime.stop_agencyid, stime.stop_id, stop.location, gtfs_agencies.name, stop.name, stop.lat, stop.lon,a1stops.agencyid, a1stops.agencyname,a1stops.location),"
				+ ""
				+ " a1coordinates AS (SELECT a1stops.agencyid AS a1id, AVG(a1stops.lat)::TEXT||','||AVG(a1stops.lon)::TEXT AS a1coordinate FROM a1stops GROUP BY a1id),"
				+ " a2coordinates AS (SELECT a2id, AVG(a2stops.lat)::TEXT||','||AVG(a2stops.lon)::TEXT AS a2coordinate FROM a2stops GROUP BY a2id)"
				+ " select a1id, a1name, ARRAY_AGG(DISTINCT a1coordinate) AS a1coordinate, a2id, a2name, ARRAY_AGG(DISTINCT a2coordinate) AS a2coordinate, COUNT(dist) AS size"
				+ "	FROM a2stops JOIN a1coordinates USING(a1id)"
				+ "	JOIN a2coordinates USING(a2id)"
				+ "	GROUP BY a1id, a1name, a2id, a2name";
//		System.out.println(query);
		
		try{
			ResultSet rs = stmt.executeQuery(query);
			
			// Initialize a ConGraphObj for isolated agencies. 
			if (!rs.next()){
				ConGraphObj instance = new ConGraphObj();
				instance.a1ID = agencyID;
				instance.a1name = agencyName;
				instance.a2ID = "";
				instance.a2name = "";
				instance.connections = new TransitConnection(0);
				response.add(instance);
			}
			
			// Initialize ConGraphObj for the agency connections. 
			while (rs.next()){
				ConGraphObj instance = new ConGraphObj();
				// Getting the agency IDs
				instance.a1ID = rs.getString("a1id");
				instance.a1name = rs.getString("a1name");
				instance.a2ID = rs.getString("a2id");
				instance.a2name = rs.getString("a2name");
				
				// Getting the edge properties.
				instance.connections = new TransitConnection(rs.getInt("size"));
				response.add(instance);		
			}
		}catch(SQLException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}
	
	/**
	 * 
	 * @param agencyID
	 * @param stmt
	 * @return
	 * @throws SQLException 
	 */
	public static ConGraphAgencyGraph getAgencyCentroids(String agencyID, Statement stmt, double RADIUS) throws SQLException{
//		ConGraphAgencyGraph response = new ConGraphAgencyGraph(new HashSet<Coordinate[]>());
		String query = "SELECT name, lat, lon FROM gtfs_stops AS stops INNER JOIN gtfs_stop_service_map AS map "
				+ " ON stops.id = map.stopid AND stops.agencyid = map.agencyid_def "
				+ " WHERE map.agencyid='" + agencyID + "' ORDER BY lat, lon";;
		ResultSet rs = stmt.executeQuery(query);
		List<ConGraphCluster> clusters = new ArrayList<ConGraphCluster>();
		List<Coordinate> points = new ArrayList<Coordinate>();
//		System.out.println("agency:"+agencyID);
		while (rs.next()){
			Coordinate c = new Coordinate(rs.getDouble("lat"), rs.getDouble("lon"));
			points.add(c);
		}
		
		while (!points.isEmpty()){
			Set<Coordinate> clusterPoints = new HashSet<Coordinate>();
			Coordinate currenPoint = points.remove(0);
			clusterPoints.add(currenPoint);
			
			for ( Coordinate p : points ){
				if (ConGraphAgencyGraph.getDistance(currenPoint, p) < RADIUS){
					clusterPoints.add(p);
				}
			}
			
			ConGraphCluster c = new ConGraphCluster( clusterPoints );
			points.removeAll(clusterPoints);
			clusters.add(c); 
		}
		
		ConGraphAgencyGraph response = new ConGraphAgencyGraph(agencyID, clusters);
		return response;
	}
	
	/**
	 * 
	 * @param agencyID
	 * @param stmt
	 * @return
	 * @throws SQLException 
	 */
	public static AgencyCentroid getAgencyCentroid(String agencyID, Statement stmt) throws SQLException{
		String query = "SELECT AVG(lat) AS lat, AVG(lon) AS lng "
				+ " FROM gtfs_stops AS stops INNER JOIN gtfs_stop_service_map AS map "
				+ " ON stops.id = map.stopid AND stops.agencyid = map.agencyid_def WHERE map.agencyid='"+agencyID+"'";;
		ResultSet rs = stmt.executeQuery(query);
		AgencyCentroid response = new AgencyCentroid();
		while (rs.next()){
			response.id = agencyID;
			response.lat = rs.getDouble("lat");
			response.lng = rs.getDouble("lng");
		}		
		return response;
	}	
}
