/*
 * This object is used to store the connections for
 * a given agency to all the other agencies. The given
 * agency is referred to as the Anchor Agency. 
 * 06/06/2016. PB.
 *  
 */

import java.util.List;

public class ConGraphObj {

	public String id; // refers to the agency id.
	public List<Coordinate> points; // the points that are transfered to vertices of the connectivity graph to represent the anchor agency.
	public int size; //represents the size of the anchor agency, i.e., number of its stops
	public List<transitConnection> connections;	
	
	
	private class Coordinate {
		public double lat;
		public double lon;
	}
	
	private class transitConnection{
		protected int connectionNo; // number of connection from the anchor agency (source) to the destination agency.
		protected String agencyID; // ID of the destination agency.
	}
	
}
