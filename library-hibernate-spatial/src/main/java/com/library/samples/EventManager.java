package com.library.samples;

import org.hibernate.Session;
import org.hibernate.Query;
import org.hibernate.type.Type;
import org.hibernatespatial.GeometryUserType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;

import com.library.util.Hutil;
import com.library.model.*;




public class EventManager {		
//private SessionFactory factory;
private	static Session session = Hutil.getSessionFactory().openSession();

/**
 * returns trip data and shape
 *//*
	public static Geotrip getTripData(AgencyAndId id) throws FactoryException, TransformException {			
		session.beginTransaction();
		Query q = session.getNamedQuery("SHAPE_BY_TRIP");		
		q.setParameter("id", id);
		@SuppressWarnings("unchecked")
		List<Geotrip> results = (List<Geotrip>) q.list();
        Hutil.getSessionFactory().close();
        return results.get(0);
    }*/

/**
 * returns population centroids
 */
	public static List<Census> getcentroids(double d, double lat, double lon) throws FactoryException, TransformException {
		CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326");
		CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:2993");
		MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();			
		Point point = geometryFactory.createPoint(new Coordinate(lat, lon));
		Geometry targetGeometry = JTS.transform( point, transform);
		//point = geometryFactory.createPoint(targetGeometry.getCoordinate());
		point = targetGeometry.getCentroid();
		point.setSRID(2993);	
		session.beginTransaction();
		Query q = session.getNamedQuery("CENSUS_BY_COORDINATES");
		Type geomType = GeometryUserType.TYPE;
		q.setParameter("point", point, geomType);
		q.setParameter("radius", d);
		@SuppressWarnings("unchecked")
		List<Census> results = (List<Census>) q.list();
        Hutil.getSessionFactory().close();
        return results;
    }
	
/**
 * returns centroids within a rectangle
 */
	public static List<Census> getcentroidswithinrectangle(double[] lat, double[] lon) throws FactoryException, TransformException {			
		CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326");
		CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:2993");
		MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
		Coordinate[] coords = new Coordinate[lat.length+1];
		for(int i=0;i<lat.length;i++){
			coords[i]= new Coordinate(lat[i], lon[i]);
		}
		coords[coords.length-1]= new Coordinate(lat[0], lon[0]);
		LinearRing ring = geometryFactory.createLinearRing( coords );
		LinearRing holes[] = null; 
		Polygon polygon = geometryFactory.createPolygon(ring, holes );
		//Point point = geometryFactory.createPoint(new Coordinate(lat, lon));
		Geometry targetGeometry = JTS.transform( polygon, transform);
		//point = geometryFactory.createPoint(targetGeometry.getCoordinate());
		//point = targetGeometry.getCentroid();
		targetGeometry.setSRID(2993);	
		session.beginTransaction();
		Query q = session.getNamedQuery("CENSUS_WITHIN_RECTANGLE");
		Type geomType = GeometryUserType.TYPE;
		q.setParameter("polygon", targetGeometry, geomType);
		//q.setParameter("radius", d);
		@SuppressWarnings("unchecked")
		List<Census> results = (List<Census>) q.list();
        Hutil.getSessionFactory().close();
        return results;
    }	
	
/**
 * returns stops within a circle
 */
	public static List<GeoStop> getstopswithincircle(double d, double lat, double lon) throws FactoryException, TransformException {			
		CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326");
		CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:2993");
		MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();			
		Point point = geometryFactory.createPoint(new Coordinate(lat, lon));
		Geometry targetGeometry = JTS.transform( point, transform);
		//point = geometryFactory.createPoint(targetGeometry.getCoordinate());
		point = targetGeometry.getCentroid();
		point.setSRID(2993);	
		session.beginTransaction();
		Query q = session.getNamedQuery("STOP_BY_COORDINATES");
		Type geomType = GeometryUserType.TYPE;
		q.setParameter("point", point, geomType);
		q.setParameter("radius", d);
		@SuppressWarnings("unchecked")
		List<GeoStop> results = (List<GeoStop>) q.list();
        Hutil.getSessionFactory().close();
        return results;
    }	
	
	/**
	 * returns stops within a rectangle
	 */
		public static List<GeoStop> getstopswithinrectangle(double[] lat, double[] lon) throws FactoryException, TransformException {			
			CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326");
			CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:2993");
			MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);
			GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
			Coordinate[] coords = new Coordinate[lat.length+1];
			for(int i=0;i<lat.length;i++){
				coords[i]= new Coordinate(lat[i], lon[i]);
			}
			coords[coords.length-1]= new Coordinate(lat[0], lon[0]);
			LinearRing ring = geometryFactory.createLinearRing( coords );
			LinearRing holes[] = null; 
			Polygon polygon = geometryFactory.createPolygon(ring, holes );
			//Point point = geometryFactory.createPoint(new Coordinate(lat, lon));
			Geometry targetGeometry = JTS.transform( polygon, transform);
			//point = geometryFactory.createPoint(targetGeometry.getCoordinate());
			//point = targetGeometry.getCentroid();
			targetGeometry.setSRID(2993);	
			session.beginTransaction();
			Query q = session.getNamedQuery("STOP_WITHIN_RECTANGLE");
			Type geomType = GeometryUserType.TYPE;
			q.setParameter("polygon", targetGeometry, geomType);
			//q.setParameter("radius", d);
			@SuppressWarnings("unchecked")
			List<GeoStop> results = (List<GeoStop>) q.list();
	        Hutil.getSessionFactory().close();
	        return results;
	    }	
	
/**
 * returns route for a given stop
 */
	public static List<GeoStopRouteMap> getroutebystop(String id, String agency) throws FactoryException, TransformException {			
		session.beginTransaction();
		Query q = session.getNamedQuery("ROUTE_BY_STOP");
		q.setParameter("id", id).setParameter("agency", agency);
		@SuppressWarnings("unchecked")
		List<GeoStopRouteMap> result = (List<GeoStopRouteMap>) q.list();
        Hutil.getSessionFactory().close();
        return result;
	}

/**
 * returns list of stop_route_map
 */
	public static List<GeoStopRouteMap> getstoproutemaps() throws FactoryException, TransformException {			
		session.beginTransaction();
		Query q = session.getNamedQuery("All_STOP_ROUTE_MAPS");
		@SuppressWarnings("unchecked")
		List<GeoStopRouteMap> results = q.list();
        Hutil.getSessionFactory().close();
        return results;
    }
			
	
/**
 * returns list of counties
 */
	public static List<County> getcounties() throws FactoryException, TransformException {			
		session.beginTransaction();
		Query q = session.getNamedQuery("All_COUNTIES");
		@SuppressWarnings("unchecked")
		List<County> results = q.list();
        Hutil.getSessionFactory().close();
        return results;
    }
	
/**
 * returns list of tracts
 */
	public static List<Tract> gettracts() throws FactoryException, TransformException {			
		session.beginTransaction();
		Query q = session.getNamedQuery("All_TRACTS");
		@SuppressWarnings("unchecked")
		List<Tract> results = q.list();
        Hutil.getSessionFactory().close();
        return results;
    }
	
/**
 * returns list of census places
 */
	public static List<Place> getplaces() throws FactoryException, TransformException {			
		session.beginTransaction();
		Query q = session.getNamedQuery("All_PLACES");
		@SuppressWarnings("unchecked")
		List<Place> results = (List<Place>) q.list();
        Hutil.getSessionFactory().close();
        return results;
    }
	
/**
 * returns list of urban areas
 */
	public static List<Urban> geturban() throws FactoryException, TransformException {			
		session.beginTransaction();
		Query q = session.getNamedQuery("All_URBANS");
		@SuppressWarnings("unchecked")
		List<Urban> results = (List<Urban>) q.list();
        Hutil.getSessionFactory().close();
        return results;
    }

/**
 * returns list of urban areas
 */
	public static List<CongDist> getcongdist() throws FactoryException, TransformException {			
		session.beginTransaction();
		Query q = session.getNamedQuery("All_CONGDISTS");
		@SuppressWarnings("unchecked")
		List<CongDist> results = (List<CongDist>) q.list();
        Hutil.getSessionFactory().close();
        return results;
    }
	
/**
 * returns number of tracts for a given county
 */
	public static long gettractscountbycounty(String countyId) throws FactoryException, TransformException {			
		session.beginTransaction();
		Query q = session.getNamedQuery("TRACTSNO_BY_COUNTY");
		q.setParameter("id", countyId);
		//@SuppressWarnings("unchecked")
		long result = (Long) q.list().get(0);
        Hutil.getSessionFactory().close();
        return result;
	    }

/**
 * returns number of census blocks for a given county
 */
	public static long getblockscountbytract(String tractId) throws FactoryException, TransformException {			
		session.beginTransaction();
		Query q = session.getNamedQuery("BLOCKSNO_BY_TRACT");
		q.setParameter("id", tractId);
		//@SuppressWarnings("unchecked")
		long result = (Long) q.list().get(0);
        Hutil.getSessionFactory().close();
        return result;
	    }
	
/**
 * returns number of stops for a given county
 */
	public static long getstopscountbycounty(String countyId) throws FactoryException, TransformException {			
		session.beginTransaction();
		Query q = session.getNamedQuery("STOPS_BY_COUNTY");
		q.setParameter("id", countyId);
		//@SuppressWarnings("unchecked")
		long result = (Long) q.list().get(0);
        Hutil.getSessionFactory().close();
        return result;
	    }
/**
 * returns number of stops for a given ODOT Region
 */
	public static long getstopscountbyregion(String regionId) throws FactoryException, TransformException {			
		session.beginTransaction();
		Query q = session.getNamedQuery("STOPS_BY_REGION");
		q.setParameter("id", regionId);
		//@SuppressWarnings("unchecked")
		long result = (Long) q.list().get(0);
        Hutil.getSessionFactory().close();
        return result;
	    }
/**
 * returns number of stops for a given census tract
 */
	public static long getstopscountbytract(String tractId) throws FactoryException, TransformException {			
		session.beginTransaction();
		Query q = session.getNamedQuery("STOPS_BY_TRACT");
		q.setParameter("id", tractId);
		//@SuppressWarnings("unchecked")
		long result = (Long) q.list().get(0);
        Hutil.getSessionFactory().close();
        return result;
	    }
		
/**
 * returns number of stops for a given census place
 */
	public static long getstopscountbyplace(String placeId) throws FactoryException, TransformException {			
		session.beginTransaction();
		Query q = session.getNamedQuery("STOPS_BY_PLACE");
		q.setParameter("id", placeId);
		//@SuppressWarnings("unchecked")
		long result = (Long) q.list().get(0);
        Hutil.getSessionFactory().close();
        return result;
	    }
	
/**
 * returns number of stops for a given urban area
 */
	public static long getstopscountbyurban(String urbanId) throws FactoryException, TransformException {			
		session.beginTransaction();
		Query q = session.getNamedQuery("STOPS_BY_URBAN");
		q.setParameter("id", urbanId);
		//@SuppressWarnings("unchecked")
		long result = (Long) q.list().get(0);
        Hutil.getSessionFactory().close();
        return result;
	    }
	
/**
 * returns number of stops for a given congressional district
 */
	public static long getstopscountbycongdist(String congdistId) throws FactoryException, TransformException {			
		session.beginTransaction();
		Query q = session.getNamedQuery("STOPS_BY_CONGDIST");
		q.setParameter("id", congdistId);
		//@SuppressWarnings("unchecked")
		long result = (Long) q.list().get(0);
        Hutil.getSessionFactory().close();
        return result;
	    }
/**
 * returns list of routes for a given ODOT region
 */
	public static int getroutescountsbyregion(String regionId) throws FactoryException, TransformException {			
		session.beginTransaction();
		Query q = session.getNamedQuery("ROUTES_BY_REGION");
		q.setParameter("id", regionId);
		@SuppressWarnings("unchecked")
		List<GeoStopRouteMap> result = q.list();
        Hutil.getSessionFactory().close();
        return result.size();
	    }
/**
 * returns list of routes for a given county
 */
	public static int getroutescountsbycounty(String countyId) throws FactoryException, TransformException {			
		session.beginTransaction();
		Query q = session.getNamedQuery("ROUTES_BY_COUNTY");
		q.setParameter("id", countyId);
		@SuppressWarnings("unchecked")
		List<GeoStopRouteMap> result = q.list();
        Hutil.getSessionFactory().close();
        return result.size();
	    }
/**
 * returns list of routes for a given census place
 */
	public static int getroutescountsbyplace(String placeId) throws FactoryException, TransformException {			
		session.beginTransaction();
		Query q = session.getNamedQuery("ROUTES_BY_PLACE");
		q.setParameter("id", placeId);
		@SuppressWarnings("unchecked")
		List<GeoStopRouteMap> result = q.list();
        Hutil.getSessionFactory().close();
        return result.size();
	    }
/**
 * returns list of routes for a given census tract
 */
	public static int getroutescountsbytract(String tractId) throws FactoryException, TransformException {			
		session.beginTransaction();
		Query q = session.getNamedQuery("ROUTES_BY_TRACT");
		q.setParameter("id", tractId);
		@SuppressWarnings("unchecked")
		List<GeoStopRouteMap> result = q.list();
        Hutil.getSessionFactory().close();
        return result.size();
	    }
/**
 * returns list of routes for a given urban area
 */
	public static int getroutescountbyurban(String urbanId) throws FactoryException, TransformException {			
		session.beginTransaction();
		Query q = session.getNamedQuery("ROUTES_BY_URBAN");
		q.setParameter("id", urbanId);
		@SuppressWarnings("unchecked")
		List<GeoStopRouteMap> result = q.list();
        Hutil.getSessionFactory().close();
        return result.size();
	    }	
	
/**
 * returns list of routes for a given congressional district
 */
	public static int getroutescountbycongdist(String congdistId) throws FactoryException, TransformException {			
		session.beginTransaction();
		Query q = session.getNamedQuery("ROUTES_BY_CONGDIST");
		q.setParameter("id", congdistId);
		@SuppressWarnings("unchecked")
		List<GeoStopRouteMap> result = q.list();
        Hutil.getSessionFactory().close();
        return result.size();
	    }
	
/**
 * returns list of tracts for a given county
 */
	public static List<Tract> gettractsbycounty(String countyId) throws FactoryException, TransformException {			
		session.beginTransaction();
		Query q = session.getNamedQuery("TRACTS_BY_COUNTY");
		q.setParameter("id", countyId);
		@SuppressWarnings("unchecked")
		List<Tract> result = q.list();
        Hutil.getSessionFactory().close();
        return result;
	    }
/**
 * returns population within the d distance of a point
 */
	public static long getpop(double d, double lat, double lon) throws FactoryException, TransformException {			
		CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326");
		CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:2993");
		MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();			
		Point point = geometryFactory.createPoint(new Coordinate(lat, lon));
		Geometry targetGeometry = JTS.transform( point, transform);
		//point = geometryFactory.createPoint(targetGeometry.getCoordinate());
		point = targetGeometry.getCentroid();
		point.setSRID(2993);	
		session.beginTransaction();
		Query q = session.getNamedQuery("POP_BY_COORDINATES");
		Type geomType = GeometryUserType.TYPE;
		q.setParameter("point", point, geomType);
		q.setParameter("radius", d);
		//@SuppressWarnings("unchecked")
		List results = q.list();
		long pop = 0;
		if (results.get(0)!=null){ 
		pop = (Long) results.get(0);
		}
        Hutil.getSessionFactory().close();
        return pop;
    }
	
	/**
	 * returns census block internal points within the d distance of a point
	 */
	public static List<Long> getpopbatch(double d, List <Coordinate> points) throws FactoryException, TransformException {			
		List<Long> response = new ArrayList<Long> ();
		CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326");
		CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:2993");
		MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
		session.beginTransaction();
		Type geomType = GeometryUserType.TYPE;
		Query q = session.getNamedQuery("POP_BY_COORDINATES");
		q.setParameter("radius", d);
		for (Coordinate point: points){
			Point p = geometryFactory.createPoint(point);
			Geometry targetGeometry = JTS.transform( p, transform);
			p = targetGeometry.getCentroid();
			p.setSRID(2993);		
			q.setParameter("point", p, geomType);
			List results = q.list();
			long pop = 0;
			if (results.get(0)!=null){ 
			pop = (Long) results.get(0);
			}
			response.add(pop);
		}		
        Hutil.getSessionFactory().close();
        return response;
    }
	/**
	 * returns unduplicated population within the d distance of a list of points
	 */
	
	public static List<Census> getundupcentbatch(double d, List <Coordinate> points) throws FactoryException, TransformException {		
		CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326");
		CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:2993");
		MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
		session.beginTransaction();
		Type geomType = GeometryUserType.TYPE;		
		StringBuffer queryBuf = new StringBuffer("from Census");
		//boolean firstClause = true;
		int i = 0;		
		//List<Point> qpoints = new ArrayList<Point>();
		Point[] plist = new Point[points.size()];
		for (Coordinate point: points){
			//queryBuf.append(firstClause ? " where " : " or ");
			Point p = geometryFactory.createPoint(point);			
			Geometry targetGeometry = JTS.transform( p, transform);
			p = targetGeometry.getCentroid();
			p.setSRID(2993);
			plist[i]=p;
			//queryBuf.append("(distance(:point"+String.valueOf(i)+",location)<:radius)");			
			//firstClause = false;
			i++;
		}
		MultiPoint allpoints = geometryFactory.createMultiPoint(plist);
		allpoints.setSRID(2993);		
		System.out.println("no of points: "+plist.length);
		queryBuf.append(" where distance(:allpoints, location)<:radius ");
		queryBuf.append("group by blockId");
		String hqlQuery = queryBuf.toString();
		Query query = session.createQuery(hqlQuery);
		query.setParameter("radius",d);
		query.setParameter("allpoints",allpoints,geomType);
		System.out.println(hqlQuery);		
		//i=1;
		/*for (Point p :qpoints){
			query.setParameter("point"+String.valueOf(i),p,geomType);			
			i++;
		}*/
		@SuppressWarnings("unchecked")
		List<Census> results = (List<Census>) query.list();		
        Hutil.getSessionFactory().close();
        //List results = query.list();
		//long pop = 0;
		//if (results.get(0)!=null){ 
		//pop = (Long) results.get(0);
		//}
        return results;		
    }
	public static long getunduppopbatch(double d, List <Coordinate> points) throws FactoryException, TransformException {		
		CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326");
		CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:2993");
		MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
		session.beginTransaction();
		Type geomType = GeometryUserType.TYPE;		
		//StringBuffer queryBuf = new StringBuffer("select sum(population) from Census where id in (select distinct id from Census");
		//boolean firstClause = true;
		int i = 0;		
		//List<Point> qpoints = new ArrayList<Point>();
		Point[] plist = new Point[points.size()];
		for (Coordinate point: points){
			//queryBuf.append(firstClause ? " where " : " or ");
			Point p = geometryFactory.createPoint(point);
			Geometry targetGeometry = JTS.transform( p, transform);
			p = targetGeometry.getCentroid();
			p.setSRID(2993);
			plist[i]=p;
			//qpoints.add(p);
			//queryBuf.append("(distance(:point"+String.valueOf(i)+",location)<:radius)");			
			//firstClause = false;
			i++;
		}
		MultiPoint allpoints = geometryFactory.createMultiPoint(plist);
		allpoints.setSRID(2993);
		//queryBuf.append(" where dwithin(location, :allpoints, :radius) = true ) ");
		System.out.println("no of points: "+plist.length);
		//queryBuf.append(") ");
		Query q = session.getNamedQuery("POP_UNDUP_BATCH");
		//String hqlQuery = queryBuf.toString();
		//Query query = session.createQuery(hqlQuery);				
		//i=1;
		q.setParameter("radius",d);
		q.setParameter("allpoints",allpoints,geomType);
		System.out.println(q.toString());
		/*for (Point p :qpoints){
			query.setParameter("point"+String.valueOf(i),p,geomType);			
			i++;
		}*/
		//@SuppressWarnings("unchecked")
		//List<Census> results = (List<Census>) query.list();		
        
        List results = q.list();
		long pop = 0;
		if (results.size()>0 && results.get(0)!=null){ 
		pop = (Long) results.get(0);
		//pop = (Integer) results.get(0);
		}
		Hutil.getSessionFactory().close();
		System.out.println("Query returned: "+pop);
        return pop;		
    }
/*
    private void createAndStoreEvent(String id, String pop, String lat, String lon) {

        //First interpret the WKT string to a point
        WKTReader fromText = new WKTReader();
        Geometry geom = null;
        try {
            geom = fromText.read(wktPoint);
        } catch (ParseException e) {
            throw new RuntimeException("Not a WKT string:" + wktPoint);
        }
        if (!geom.getGeometryType().equals("Point")) {
            throw new RuntimeException("Geometry must be a point. Got a " + geom.getGeometryType());
        }

        Session session = Hutil.getSessionFactory().getCurrentSession();

        session.beginTransaction();

        Census theEvent = new Census();
        theEvent.setTitle(title);
        theEvent.setDate(theDate);
        theEvent.setLocation((Point) geom);
        session.save(theEvent);

        session.getTransaction().commit();
    }
*/
    /**
    * Utility method to assemble all arguments save the first into a String
    */
	/*
    private static String assemble(String[] args){
            StringBuilder builder = new StringBuilder();
            for(int i = 1; i<args.length;i++){
                    builder.append(args[i]).append(" ");
            }
            return builder.toString();
    }
    private List find(String wktFilter){
        WKTReader fromText = new WKTReader();
        Geometry filter = null;
        try{
                filter = fromText.read(wktFilter);
        } catch(ParseException e){
                throw new RuntimeException("Not a WKT String:" + wktFilter);
        }
        Session session = Hutil.getSessionFactory().getCurrentSession();
        session.beginTransaction();
        System.out.println("Filter is : " + filter);
        Criteria testCriteria = session.createCriteria(Census.class);
        testCriteria.add(SpatialRestrictions.within("location", filter));
        List results = testCriteria.list();
        session.getTransaction().commit();
        return results;
    }*/
}
