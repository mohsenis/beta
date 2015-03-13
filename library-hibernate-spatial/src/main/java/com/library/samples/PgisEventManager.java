package com.library.samples;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement; 
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.library.model.*;

public class PgisEventManager {
	public static Connection connection;
	
	public static void makeConnection(int dbindex){
		String url = "";
		switch (dbindex){
		case 0:
			url = "gtfsdb";
			break;
		case 1:
			url = "gtfsdb1";
			break;
		}
		try {
		Class.forName("org.postgresql.Driver");
		connection = DriverManager
           .getConnection("jdbc:postgresql://localhost:5432/"+url,
           "postgres", "123123");
		}catch ( Exception e ) {
	        System.err.println( e.getClass().getName()+": "+ e.getMessage() );
	        System.exit(0);
	      }
	}
	
	public static void dropConnection(){
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
	public static long UrbanCensusbyPop(int pop) 
    {
      //Connection c = null;
      Statement stmt = null;
      long population = 0;
      try {
      /*Class.forName("org.postgresql.Driver");
        c = DriverManager
           .getConnection("jdbc:postgresql://localhost:5432/gtfsdb",
           "postgres", "123123");
        c.setAutoCommit(false);
        System.out.println("Opened database successfully");*/

        stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery( "select sum(population) as pop from "
        		+ "(select distinct block.blockid, population from census_blocks block join gtfs_stops stop on st_dwithin(block.location, stop.location, 161)=true "
        		+ "where stop.urbanid in (select urbanid from census_urbans where population>=50000) and block.urbanid in (select urbanid from census_urbans where population>=50000))"
        		+ "as pops;" );        
        while ( rs.next() ) {
           population = rs.getLong("pop");           
           System.out.println( "population = " + population );           
        }
        rs.close();
        stmt.close();
        //c.close();
      } catch ( Exception e ) {
        System.err.println( e.getClass().getName()+": "+ e.getMessage() );
        System.exit(0);
      }
      System.out.println("Operation done successfully");
      return population;
    }
	/*
	 *Queries agency clusters (connected transit networks) and returns a list of all transit agencies with their connected agencies
	 */
	public static List<agencyCluster> agencyCluster(double dist){
		List<agencyCluster> response = new ArrayList<agencyCluster>();
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
		return response;
	}
	/*
	 *Queries connected transit agencies and list of connections for a given transit agency
	 */
	public static List<agencyCluster> agencyClusterDetails(double dist, String agencyId){
		List<agencyCluster> response = new ArrayList<agencyCluster>();
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
		return response;
	}
	/*
	 *returns average, min and Max of fare for the whole state
	 */
	/*public static HashMap<String, Float> FareInfo(double dist, String agencyId){
		HashMap<String, Float> response = new HashMap<String, Float>();
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
		return response;
	}*/

}
