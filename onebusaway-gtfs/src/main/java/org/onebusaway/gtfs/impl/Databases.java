package org.onebusaway.gtfs.impl;


public class Databases {
	public final static int dbsize =2;
	public final static int defaultDBIndex = 0;
	public static String[] spatialConfigPaths = new String[dbsize];
	public static String[] ConfigPaths = new String[dbsize];
	public static String[] dbnames = new String[dbsize];
	
	static{
		//list of configuration file names used in library-hibernate-spatial used in Hutil.java
		spatialConfigPaths[0]= "hibernate.cfg.xml";
		spatialConfigPaths[1]= "hibernate1.cfg.xml";
		
		//list of configuration file names used in onebusaway-gtfs-hibernate used in GTFSHibernateReaderExampleMain.java
		ConfigPaths[0]= "classpath:org/onebusaway/gtfs/examples/hibernate-configuration-examples.xml";
		ConfigPaths[1]= "classpath:org/onebusaway/gtfs/examples/hibernate-configuration-examples1.xml";
		
		//list of database names used in the GUI
		dbnames[0] = "Database 1";
		dbnames[1] = "Database 2";
	
	}
}
