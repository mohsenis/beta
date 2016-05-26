package com.library.samples;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.library.model.ParknRide;
import com.vividsolutions.jts.geom.Point;

public class SpatialEventManager {

	public static List<ParknRide> getPnRs(double[] lat, double[] lon, double radius, int dbindex) throws SQLException{
		List<ParknRide> output = new ArrayList<ParknRide>();
		Connection connection = PgisEventManager.makeConnection(dbindex);
		Statement stmt = null;
		String query;
		if (lat.length==1) // If selected area is a circle
			query = "SELECT * FROM parknride "
				+ " WHERE ST_DWITHIN(parknride.geom,ST_transform(ST_setsrid(ST_MakePoint(" + lon[0] + ", " + lat[0] + "),4326), 2993), " + radius + ")";
		else{	// If selected area is a polygon or rectangle
			query = "SELECT * FROM parknride " + 
					" WHERE ST_CONTAINS( ST_transform(st_geometryfromtext('POLYGON((";
			for (int i =0 ; i <lat.length ; i++){
				query += lon[i] + " " + lat[i] + ",";
			}
			query = query += lon[0] + " " + lat[0]; //Closing the polygon loop 
			query += "))', 4326),2993), parknride.geom)";
		}
		System.out.println(query);
		stmt = connection.createStatement();
		ResultSet rs = stmt.executeQuery(query);
		while(rs.next()){
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

}
