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

	public static HashMap<String, String> getAllAgencies ( String username, int dbindex ) throws SQLException {
		HashMap<String,String> response = new HashMap<String, String>();
		String query = "SELECT * FROM gtfs_agencies WHERE gtfs_agencies.defaultid IN (SELECT DISTINCT agency_id AS aid "
				+ "FROM gtfs_selected_feeds WHERE username='" + username + "')";
		Connection connection = PgisEventManager.makeConnection(dbindex);
		Statement stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery(query);
		
		while ( rs.next() )
			response.put(rs.getString("id"), rs.getString("name"));
		
		connection.close();
		return response;
	}
	
	public static Set<ConGraphObj> getConGraphObj(String agencyID, String agencyName, double radius, Statement stmt) throws SQLException{
		Set<ConGraphObj> response = new HashSet<ConGraphObj>();
		String query = "WITH a1stops AS (SELECT map.agencyid, agencies.name AS agencyname, stops.location, stops.lat, stops.lon, stops.name " +
				"	FROM gtfs_stops AS stops JOIN gtfs_stop_service_map AS map " +
				"	ON map.agencyid_def = stops.agencyid AND map.stopid = stops.id " + 
				"	INNER JOIN gtfs_agencies AS agencies ON map.agencyid = agencies.id " +
				"	WHERE map.agencyid = '" + agencyID + "'), " +
				" " +
				"a2stops AS (SELECT a1stops.agencyid AS a1id, a1stops.agencyname AS a1name, map.agencyid AS a2id, agencies.name AS a2name, stops.location,  " +
				"	stops.lat, stops.lon, stops.name,ST_DISTANCE(stops.location, a1stops.location)::NUMERIC AS dist " +
				"	FROM gtfs_stops AS stops JOIN gtfs_stop_service_map AS map  " +
				"	ON map.agencyid_def = stops.agencyid AND map.stopid = stops.id  " +
				"	INNER JOIN a1stops ON ST_DISTANCE(stops.location, a1stops.location) < " + radius + " " +
				"	INNER JOIN gtfs_agencies AS agencies ON map.agencyid = agencies.id " +
				"	WHERE map.agencyid != '" + agencyID + "'   " +
				"	), " +
				" " +
				"a1coordinates AS (SELECT a1stops.agencyid AS a1id, AVG(a1stops.lat)::TEXT||','||AVG(a1stops.lon)::TEXT AS a1coordinate FROM a1stops GROUP BY a1id), " +
				" " +
				"a2coordinates AS (SELECT a2stops.a2id AS a2id, AVG(a2stops.lat)::TEXT||','||AVG(a2stops.lon)::TEXT AS a2coordinate FROM a2stops GROUP BY a2id) " +
				" " +
				"select a1id, a1name, ARRAY_AGG(DISTINCT a1coordinate) AS a1coordinate, a2id, a2name, ARRAY_AGG(DISTINCT a2coordinate) AS a2coordinate, COUNT(dist) AS size " +
				"	FROM a2stops JOIN a1coordinates USING(a1id) " +
				"	JOIN a2coordinates USING(a2id) " +
				"	GROUP BY a1id, a1name, a2id, a2name";
//		System.out.println(query);
		
		try{
			ResultSet rs = stmt.executeQuery(query);
			
			// Initialize a ConGraphObj for isolated agencies. 
			if (!rs.next()){
				System.out.println("Isolated Agency: " + agencyID);
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
//				System.out.println(instance.a1ID +"-"+instance.a2ID+"-"+instance.connections.size);
			}
		}catch(SQLException e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		System.out.println("response size: " + response.size());
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
		String query = "SELECT name, lat, lon FROM gtfs_stops WHERE agencyid='" + agencyID + "' ORDER BY lat, lon";;
		ResultSet rs = stmt.executeQuery(query);
		List<ConGraphCluster> clusters = new ArrayList<ConGraphCluster>();
		List<Coordinate> points = new ArrayList<Coordinate>();
		System.out.println("agency:"+agencyID);
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
		
		ConGraphAgencyGraph response = new ConGraphAgencyGraph(clusters);
		response.ID = agencyID;
				
		return response;
		
	}
	
	/**
	 * 
	 * @param agencyID
	 * @param stmt
	 * @return
	 * @throws SQLException 
	 */
	public static AgencyCentroid getAgencyCentroids2(String agencyID, Statement stmt) throws SQLException{
//		ConGraphAgencyGraph response = new ConGraphAgencyGraph(new HashSet<Coordinate[]>());
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
