drop table if exists census_urbans_trip_map;

CREATE TABLE census_urbans_trip_map
(
  gid serial NOT NULL,
  agencyid character varying(255),
  agencyid_def character varying(255),
  routeid character varying(255),
  urbanid character varying(5),
  tripid character varying(255),
  serviceid character varying(255),
  stopscount integer,  
  length float,
  shape geometry(multilinestring),
  uid varchar(512),
  CONSTRAINT census_urbans_trip_map_pkey PRIMARY KEY (gid),
  CONSTRAINT census_urbans_trip_map_fkey FOREIGN KEY (agencyid, tripid)
      REFERENCES gtfs_trips (agencyid, id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE census_urbans_trip_map
  OWNER TO postgres;

insert into census_urbans_trip_map(tripid, agencyid, agencyid_def, serviceid, routeid,  urbanid, shape, length, uid) 
select trip.id, trip.agencyid, trip.serviceid_agencyid, trip.serviceid_id, trip.route_id, urban.urbanid, st_multi(ST_CollectionExtract(st_union(ST_Intersection(trip.shape,urban.shape)),2)), (ST_Length(st_transform(ST_Intersection(trip.shape,urban.shape),2993))/1609.34), trip.uid
from gtfs_trips trip
inner join census_urbans urban on  st_intersects(urban.shape,trip.shape)=true group by trip.id, trip.agencyid, urban.urbanid;

update census_urbans_trip_map set stopscount = res.cnt+0 from 
(select count(stop.id) as cnt, stop.urbanid as cid, stime.trip_agencyid as aid, stime.trip_id as tid 
from gtfs_stops stop inner join gtfs_stop_times stime 
on stime.stop_agencyid = stop.agencyid and stime.stop_id = stop.id group by stime.trip_agencyid, stime.trip_id, stop.urbanid) as res
where urbanid =  res.cid and agencyid = res.aid and tripid=res.tid;

update census_urbans_trip_map set stopscount=0 where stopscount IS NULL;