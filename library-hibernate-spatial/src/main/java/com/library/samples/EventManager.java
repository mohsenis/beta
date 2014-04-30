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
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Geometry;

import org.geotools.geometry.jts.JTSFactoryFinder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;

import com.library.util.Hutil;
import com.library.model.*;





public class EventManager {		
//private SessionFactory factory;
private	static Session session = Hutil.getSessionFactory().openSession();
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
 * returns population number
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
	public static List<Census> getundupcentbatch(double d, List <Coordinate> points) throws FactoryException, TransformException {		
		CoordinateReferenceSystem sourceCRS = CRS.decode("EPSG:4326");
		CoordinateReferenceSystem targetCRS = CRS.decode("EPSG:2993");
		MathTransform transform = CRS.findMathTransform(sourceCRS, targetCRS);
		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
		session.beginTransaction();
		Type geomType = GeometryUserType.TYPE;		
		StringBuffer queryBuf = new StringBuffer("from Census");
		boolean firstClause = true;
		int i = 1;		
		List<Point> qpoints = new ArrayList<Point>();
		for (Coordinate point: points){
			queryBuf.append(firstClause ? " where " : " or ");
			Point p = geometryFactory.createPoint(point);
			Geometry targetGeometry = JTS.transform( p, transform);
			p = targetGeometry.getCentroid();
			p.setSRID(2993);
			qpoints.add(p);
			queryBuf.append("(distance(:point"+String.valueOf(i)+",location)<:radius)");			
			firstClause = false;
			i++;
		}
		System.out.println("no of points: "+qpoints.size());
		queryBuf.append("group by id");
		String hqlQuery = queryBuf.toString();
		Query query = session.createQuery(hqlQuery);
		query.setParameter("radius",d);
		System.out.println(hqlQuery);		
		i=1;
		for (Point p :qpoints){
			query.setParameter("point"+String.valueOf(i),p,geomType);			
			i++;
		}
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
		StringBuffer queryBuf = new StringBuffer("select sum(population) from Census where id in (select distinct id from Census");
		boolean firstClause = true;
		int i = 1;		
		List<Point> qpoints = new ArrayList<Point>();
		for (Coordinate point: points){
			queryBuf.append(firstClause ? " where " : " or ");
			Point p = geometryFactory.createPoint(point);
			Geometry targetGeometry = JTS.transform( p, transform);
			p = targetGeometry.getCentroid();
			p.setSRID(2993);
			qpoints.add(p);
			queryBuf.append("(distance(:point"+String.valueOf(i)+",location)<:radius)");			
			firstClause = false;
			i++;
		}
		System.out.println("no of points: "+qpoints.size());
		queryBuf.append(") ");
		String hqlQuery = queryBuf.toString();
		Query query = session.createQuery(hqlQuery);
		System.out.println(hqlQuery);		
		i=1;
		query.setParameter("radius",d);
		for (Point p :qpoints){
			query.setParameter("point"+String.valueOf(i),p,geomType);			
			i++;
		}
		//@SuppressWarnings("unchecked")
		//List<Census> results = (List<Census>) query.list();		
        
        List results = query.list();
		long pop = 0;
		if (results.size()>0){ 
		pop = (Long) results.get(0);
		//pop = (Integer) results.get(0);
		}
		Hutil.getSessionFactory().close();
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
