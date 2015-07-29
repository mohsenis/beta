/**
 * Copyright (C) 2011 Brian Ferris <bdferris@onebusaway.org>
 * Copyright (C) 2011 Google, Inc.
 * Copyright (C) 2011 Laurent Gregoire <laurent.gregoire@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.gtfs.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.SessionFactory;
import org.onebusaway.gtfs.model.Agency;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.FareAttribute;
import org.onebusaway.gtfs.model.FareRule;
import org.onebusaway.gtfs.model.FeedInfo;
import org.onebusaway.gtfs.model.Frequency;
import org.onebusaway.gtfs.model.IdentityBean;
import org.onebusaway.gtfs.model.Pathway;
import org.onebusaway.gtfs.model.Route;
import org.onebusaway.gtfs.model.ServiceCalendar;
import org.onebusaway.gtfs.model.ServiceCalendarDate;
import org.onebusaway.gtfs.model.ShapePoint;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.StopTime;
import org.onebusaway.gtfs.model.Transfer;
import org.onebusaway.gtfs.model.Trip;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs.services.GtfsRelationalDao;
import org.onebusaway.gtfs.services.HibernateOperation;
import org.onebusaway.gtfs.services.HibernateOperations;

public class HibernateGtfsRelationalDaoImpl implements GtfsMutableRelationalDao {

  protected HibernateOperations _ops;

  public HibernateGtfsRelationalDaoImpl() {

  }

  public HibernateGtfsRelationalDaoImpl(SessionFactory sessionFactory) {
    setSessionFactory(sessionFactory);
  }

  public void setSessionFactory(SessionFactory sessionFactory) {
    _ops = new HibernateOperationsImpl(sessionFactory);
  }

  public SessionFactory getSessionFactory() {
    if (_ops == null)
      return null;
    return _ops.getSessionFactory();
  }

  public Object execute(HibernateOperation callback) {
    return _ops.execute(callback);
  }

  @Override
  public <T> Collection<T> getAllEntitiesForType(Class<T> type) {
    return _ops.find("from " + type.getName());
  }

  @Override
  public <T> T getEntityForId(Class<T> type, Serializable id) {
    return (T) _ops.get(type, id);
  }

  @Override
  public List<Agency> getAllAgencies() {
    return _ops.find("from Agency");
  }

  @Override
  public List<ServiceCalendar> getAllCalendars() {
    return _ops.find("FROM ServiceCalendar");
  }

  @Override
  public List<ServiceCalendarDate> getAllCalendarDates() {
    return _ops.find("FROM ServiceCalendarDate");
  }

  @Override
  public Collection<FareAttribute> getAllFareAttributes() {
    return _ops.find("FROM FareAttribute");
  }

  @Override
  public Collection<FareRule> getAllFareRules() {
    return _ops.find("FROM FareRule");
  }

  @Override
  public Collection<FeedInfo> getAllFeedInfos() {
    return _ops.find("FROM FeedInfo");
  }

  @Override
  public Collection<Frequency> getAllFrequencies() {
    return _ops.find("FROM Frequency");
  }

  @Override
  public List<Route> getAllRoutes() {
    return _ops.find("FROM Route route");
  }

  @Override
  public List<Stop> getAllStops() {
    return _ops.find("FROM Stop");
  }
  
  @Override
  public List<Pathway> getAllPathways() {
    return _ops.find("FROM Pathway");
  }

  @Override
  public List<Trip> getAllTrips() {
    return _ops.find("FROM Trip ORDER BY id.agencyId");
  }

  @Override
  public List<StopTime> getAllStopTimes() {
    return _ops.find("FROM StopTime");
  }

  @Override
  public Collection<ShapePoint> getAllShapePoints() {
    return _ops.find("FROM ShapePoint");
  }

  @Override
  public Collection<Transfer> getAllTransfers() {
    return _ops.find("FROM Transfer");
  }

  @Override
  public Agency getAgencyForId(String id) {
    return (Agency) _ops.get(Agency.class, id);
  }
  

  @Override
  public FareAttribute getFareAttributeForId(AgencyAndId id) {
    return (FareAttribute) _ops.get(FareAttribute.class, id);
  }

  @Override
  public FareRule getFareRuleForId(int id) {
    return (FareRule) _ops.get(FareRule.class, id);
  }

  @Override
  public FeedInfo getFeedInfoForId(int id) {
    return (FeedInfo) _ops.get(FeedInfo.class, id);
  }

  @Override
  public Frequency getFrequencyForId(int id) {
    return (Frequency) _ops.get(Frequency.class, id);
  }

  @Override
  public Pathway getPathwayForId(AgencyAndId agencyAndId) {
    return (Pathway) _ops.get(Pathway.class, agencyAndId);
  }

  @Override
  public Route getRouteForId(AgencyAndId id) {
    return (Route) _ops.get(Route.class, id);
  }

  @Override
  public ServiceCalendar getCalendarForId(int id) {
    return (ServiceCalendar) _ops.get(ServiceCalendar.class, id);
  }

  @Override
  public ServiceCalendarDate getCalendarDateForId(int id) {
    return (ServiceCalendarDate) _ops.get(ServiceCalendarDate.class, id);
  }

  @Override
  public ShapePoint getShapePointForId(int id) {
    return (ShapePoint) _ops.get(ShapePoint.class, id);
  }

  @Override
  public Stop getStopForId(AgencyAndId agencyAndId) {
    return (Stop) _ops.get(Stop.class, agencyAndId);
  }

  @Override
  public StopTime getStopTimeForId(int id) {
    return (StopTime) _ops.get(StopTime.class, id);
  }

  @Override
  public Transfer getTransferForId(int id) {
    return (Transfer) _ops.get(Transfer.class, id);
  }

  @Override
  public Trip getTripForId(AgencyAndId id) {
    return (Trip) _ops.get(Trip.class, id);
  }

  /****
   * {@link GtfsRelationalDao} Interface
   ****/
  @Override
  public String getServiceHours(List<String> trips) {
    return _ops.findByNamedQueryAndNamedParam("serviceHoursbyTrip",
            "trips", trips).get(0).toString();
  }
  
  @Override
  public List<String> getTripAgencyIdsReferencingServiceId(AgencyAndId serviceId) {
    return _ops.findByNamedQueryAndNamedParam("agencyIdsReferencingServiceId",
        "serviceId", serviceId);
  }

  @Override
  public List<Route> getRoutesForAgency(Agency agency) {
    return _ops.findByNamedQueryAndNamedParam("routesForAgency", "agency",
        agency);
  }
  
  //@Override
  public List<String> getRouteIdsForStop(Stop stop){
	  return _ops.findByNamedQueryAndNamedParam("routeIdsForStop", "stop", stop);
  }

  @Override
  public List<Stop> getStopsForStation(Stop station) {
    String[] names = { "stationId", "agencyId" };
    Object[] values = { station.getId().getId(), station.getId().getAgencyId() };
    return _ops.findByNamedQueryAndNamedParams("stopsForStation", names, values);
  }
//Added by saeed
 // @Override
  public List<Stop> getStopsForAgency(String agency) {
	  return _ops.findByNamedQueryAndNamedParam("stopsForAgency", "agency",
		        agency);
  }
  
 // @Override
  public List<Stop> getStopsForRoute(AgencyAndId route) {	  
	  String[] names = {"agency", "route"};
	  Object[] values = {route.getAgencyId(), route.getId()};
	  return _ops.findByNamedQueryAndNamedParams("stopsForRoute", names,
		        values);
  }
  
 // @Override
  public List<Stop> getStopsForTrip(AgencyAndId trip) {	  
	  String[] names = {"agency", "trip"};
	  Object[] values = {trip.getAgencyId(), trip.getId()};
	  return _ops.findByNamedQueryAndNamedParams("stopsForTrip", names,
		        values);
  }
  
  public List<Stop> getStopsForTripCounty(AgencyAndId trip, String county) {	  
	  String[] names = {"agency", "trip", "county"};
	  Object[] values = {trip.getAgencyId(), trip.getId(), county};
	  return _ops.findByNamedQueryAndNamedParams("stopsForTripCounty", names,
		        values);
  }
  
  public List<Stop> getStopsForTripRegion(AgencyAndId trip, String region) {	  
	  String[] names = {"agency", "trip", "region"};
	  Object[] values = {trip.getAgencyId(), trip.getId(), region};
	  return _ops.findByNamedQueryAndNamedParams("stopsForTripRegion", names,
		        values);
  }
  
  public List<Stop> getStopsForTripTract(AgencyAndId trip, String tract) {	  
	  String[] names = {"agency", "trip", "tract"};
	  Object[] values = {trip.getAgencyId(), trip.getId(), tract};
	  return _ops.findByNamedQueryAndNamedParams("stopsForTripTract", names,
		        values);
  }
  
  public List<Stop> getStopsForTripPlace(AgencyAndId trip, String place) {	  
	  String[] names = {"agency", "trip", "place"};
	  Object[] values = {trip.getAgencyId(), trip.getId(), place};
	  return _ops.findByNamedQueryAndNamedParams("stopsForTripPlace", names,
		        values);
  }
  
  public List<Stop> getStopsForTripCongdist(AgencyAndId trip, String congdist) {	  
	  String[] names = {"agency", "trip", "congdist"};
	  Object[] values = {trip.getAgencyId(), trip.getId(), congdist};
	  return _ops.findByNamedQueryAndNamedParams("stopsForTripCongdist", names,
		        values);
  }
  
  public List<Stop> getStopsForTripUrban(AgencyAndId trip, String urban) {	  
	  String[] names = {"agency", "trip", "urban"};
	  Object[] values = {trip.getAgencyId(), trip.getId(), urban};
	  return _ops.findByNamedQueryAndNamedParams("stopsForTripUrban", names,
		        values);
  }
  
  public List<Stop> getStopsForTripUrbans(AgencyAndId trip, List<String> urbans) {	  
	  String[] names = {"agency", "trip", "urbans"};
	  Object[] values = {trip.getAgencyId(), trip.getId(), urbans};
	  return _ops.findByNamedQueryAndNamedParams("stopsForTripUrbans", names,
		        values);
  }
  
  @Override
  public List<Trip> getTripsForRoute(Route route) {
    return _ops.findByNamedQueryAndNamedParam("tripsByRoute", "route", route);
  }
  
//  @Override
  public List<ServiceCalendar> getServiceCalendarsForRoute(Route route) {	
    return _ops.findByNamedQueryAndNamedParam("serviceCalendarsForRoute", "route", route);
  }
  
//  @Override
  public List<Trip> getTripsForAgency(String agencyId){
	  return _ops.findByNamedQueryAndNamedParam("tripsByAgency_uidsorted", "agencyId", agencyId);
  }
  
//  @Override
  public List<Double> getMaxTripLengthsForAgency(String agencyId){
	  return _ops.findByNamedQueryAndNamedParam("serviceMilesbyAgency", "agencyId", agencyId);
  }
  
//  @Override
  public List<Trip> getTripsForAgency_RouteSorted(String agencyId){
	  return _ops.findByNamedQueryAndNamedParam("tripsByAgency_routesorted", "agencyId", agencyId);
  }

  @Override
  public List<Trip> getTripsForShapeId(AgencyAndId shapeId) {
    return _ops.findByNamedQueryAndNamedParam("tripsByShapeId", "shapeId", shapeId);
  }

  @Override
  public List<Trip> getTripsForServiceId(AgencyAndId serviceId) {
    return _ops.findByNamedQueryAndNamedParam("tripsByServiceId", "serviceId", serviceId);
  }

  @Override
  public List<Trip> getTripsForBlockId(AgencyAndId blockId) {
    String[] names = {"agencyId", "blockId"};
    Object[] values = {blockId.getAgencyId(), blockId.getId()};
    return _ops.findByNamedQueryAndNamedParams("tripsByBlockId", names, values);
  }
  
//  @Override
  public void updateTrip(Trip trip) {
    String[] names = {"length", "estlength", "shape", "tripid"};
    Object[] values = {trip.getLength(), trip.getEstlength(), trip.getEpshape(), trip.getId()};
    _ops.updateByNamedQueryAndNamedParams("updatedTripShpaeLength", names, values);
  }

  @Override
  public List<StopTime> getStopTimesForTrip(Trip trip) {
    return _ops.findByNamedQueryAndNamedParam("stopTimesByTrip", "trip", trip);
  }

  @Override
  public List<StopTime> getStopTimesForStop(Stop stop) {
    return _ops.findByNamedQueryAndNamedParam("stopTimesByStop", "stop", stop);
  }
  
  @Override
  public List<FareRule> getFareRuleForRoute(Route route) {
    return _ops.findByNamedQueryAndNamedParam("fareRuleForRoute", "route", route);
  }
  @Override
  public HashMap<String, Float> getFareDataForAgency(String agencyId) {
	  HashMap<String, Float> response = new HashMap<String, Float>();	  
	  List results=  _ops.findByNamedQueryAndNamedParam("fareDataForAgency", "agency", agencyId);
	  Object[] data = (Object[]) results.get(0);
	  Object test = new Object();	  
	  response.put("avg", (float)(Math.round(((Double)data[0])*100)/100.0));
	  response.put("min", (float)(Math.round(((Float)data[1])*100)/100.0));
	  response.put("max", (float)(Math.round(((Float)data[2])*100)/100.0));
	  response.put("count", (float)((Long)data[3]));
	  return response;
	  }
  
  @Override
  public HashMap<String, Float> getFareDataForState(List<String> selectedAgencies) {
	  HashMap<String, Float> response = new HashMap<String, Float>();	  
	  List results =  _ops.findByNamedQueryAndNamedParam("fareDataForState", "sa", selectedAgencies);
	  
	  Object[] data = (Object[]) results.get(0);
	  response.put("avg", (float)(Math.round(((Double)data[0])*100)/100.0));
	  response.put("min", (float)(Math.round(((Float)data[1])*100)/100.0));
	  response.put("max", (float)(Math.round(((Float)data[2])*100)/100.0));
	  response.put("count", (float)((Long)data[3]));
	  return response;
	  }
  
  @Override
  public List<Float> getFarePriceForRoutes(List<String> routes){
	  return _ops.findByNamedQueryAndNamedParam("farePriceForRoutes", "routes", routes);	 
    }
  
  @Override
  public Float getFareMedianForAgency(String agency, int farecount){
	  if (farecount%2==0){		  
		  List<Float> results= _ops.findByNamedQueryAndNamedParamLimited("fareMedianForAgency", "agency", agency,2 ,farecount/2);		  
		  return (float)(Math.round((results.get(0)+results.get(1))*50.0)/100.0);
	  }else {
		  List<Float> results= _ops.findByNamedQueryAndNamedParamLimited("fareMedianForAgency", "agency", agency,1 ,(farecount/2)+1);		  
		  return results.get(0);
	  }	  
    }
  
  @Override
  public Float getFareMedianForState(List<String> selectedAgencies,int farecount){
	  if (farecount%2==0){		  
		  List<Float> results= _ops.findByNamedQueryAndNamedParamLimited("fareMedianForState", "sa", selectedAgencies, 2 ,farecount/2);		  
		  return (float)(Math.round((results.get(0)+results.get(1))*50.0)/100.0);
	  }else {
		  List<Float> results= _ops.findByNamedQueryAndNamedParamLimited("fareMedianForState", "sa", selectedAgencies, 1 ,(farecount/2)+1);		  
		  return results.get(0);
	  }	  
    }
  
  @Override
  public List<AgencyAndId> getAllShapeIds() {
    return _ops.findByNamedQuery("allShapeIds");
  }
  
  @Override
  public Long getStopsCount() {
     List<Long> results = _ops.findByNamedQuery("allStopsCount");
     return results.get(0);
  }
  
  @Override
  public Long getStopsCount(List<String> selectedAgencies) {
     List<Long> results = _ops.findByNamedQueryAndNamedParam("allStopsCount", "sa", selectedAgencies);
     return results.get(0);
  }
  
  @Override
  public Double getRouteMiles() {
    Double response = (Double)_ops.findByNamedQuery("RouteMilesForState").get(0);
    response = Math.round(response*100.00)/100.00;
    return response;
  }
  
  @Override
  public Double getRouteMiles(List<String> selectedAgencies) {
    Double response = (Double)_ops.findByNamedQueryAndNamedParam("RouteMilesForState", "sa", selectedAgencies).get(0);
    response = Math.round(response*100.00)/100.00;
    return response;
  }
  
  
  @Override
  public HashMap<String, Integer> getCounts(List<String> selectedAgencies) {
	  HashMap<String, Integer> response = new HashMap<String, Integer>();
	  List results = _ops.findByNamedQueryAndNamedParam("counts", "sa", selectedAgencies);
	  Object[] counts = (Object[]) results.get(0);
	  response.put("agency", ((Long)counts[0]).intValue());
	  response.put("stop", ((Long)counts[1]).intValue());
	  response.put("route", ((Long)counts[2]).intValue());
	  
    return response;
  }

  @Override
  public List<ShapePoint> getShapePointsForShapeId(AgencyAndId shapeId) {
    return _ops.findByNamedQueryAndNamedParam("shapePointsForShapeId",
        "shapeId", shapeId);
  }

  @Override
  public List<Frequency> getFrequenciesForTrip(Trip trip) {
    return _ops.findByNamedQueryAndNamedParam("frequenciesForTrip", "trip",
        trip);
  }
  
  @Override
  public List<AgencyAndId> getAllServiceIds() {
    List<AgencyAndId> calendarIds = _ops.findByNamedQuery("calendarServiceIds");
    List<AgencyAndId> calendarDateIds = _ops.findByNamedQuery("calendarDateServiceIds");
    Set<AgencyAndId> allIds = new HashSet<AgencyAndId>();
    allIds.addAll(calendarIds);
    allIds.addAll(calendarDateIds);
    return new ArrayList<AgencyAndId>(allIds);
  }

  @Override
  public List<ServiceCalendarDate> getCalendarDatesForServiceId(
      AgencyAndId serviceId) {
    return _ops.findByNamedQueryAndNamedParam("calendarDatesForServiceId",
        "serviceId", serviceId);
  }

  @Override
  public ServiceCalendar getCalendarForServiceId(AgencyAndId serviceId) {

    List<ServiceCalendar> calendars = _ops.findByNamedQueryAndNamedParam(
        "calendarsForServiceId", "serviceId", serviceId);

    switch (calendars.size()) {
      case 0:
        return null;
      case 1:
        return calendars.get(0);
    }

    throw new MultipleCalendarsForServiceIdException(serviceId);
  }
  
//  @Override
  public List<ServiceCalendarDate> getCalendarDatesForAgency(
      String agency) {
    return _ops.findByNamedQueryAndNamedParam("calendarDatesForAgency",
        "agency", agency);
  }
  
 // @Override
  public List<ServiceCalendar> getCalendarForAgency(String agency) {
	    List<ServiceCalendar> calendars = _ops.findByNamedQueryAndNamedParam(
	        "calendarForAgency", "agency", agency);

	    switch (calendars.size()) {
	      case 0:
	        return null;
	      default:
	        return calendars;
	    }
  }

  @Override
  public List<FareRule> getFareRulesForFareAttribute(FareAttribute fareAttribute) {
    return _ops.findByNamedQueryAndNamedParam("fareRulesForFareAttribute",
        "fareAttribute", fareAttribute);
  }
  
  @Override
	public Collection<Agency> getSelectedAgencies(List<String> selectedAgencies) {
		return _ops.findByNamedQueryAndNamedParam("selectedAgenies", "sa", selectedAgencies);
	}
  
  @Override
	public List<FeedInfo> getFeedInfoByDefAgencyId(String defaultAgency) {
		return _ops.findByNamedQueryAndNamedParam("feedInfoByDefAgency", "defaultAgencyId", defaultAgency);
	}

  /****
   * Mutable Methods
   ****/

  @Override
  public void open() {
    _ops.open();
  }

  @Override
  public void close() {
    _ops.close();
  }

  @Override
  public void flush() {
    _ops.flush();
  }

  @Override
  public <K extends Serializable, T extends IdentityBean<K>> void removeEntity(
      T entity) {
    _ops.removeEntity(entity);
  }

  @Override
  public void updateEntity(Object entity) {
    _ops.update(entity);
  }

  @Override
  public void saveEntity(Object entity) {
    _ops.save(entity);
  }

  @Override
  public void saveOrUpdateEntity(Object entity) {
    _ops.saveOrUpdate(entity);
  }

  @Override
  public <T> void clearAllEntitiesForType(Class<T> type) {
    _ops.clearAllEntitiesForType(type);
  }

/*//@Override
public List<CensusData> getAllCensusData() {
	// TODO Auto-generated method stub
	return null;
}*/
}