drop table if exists census_congdists_trip_map;

CREATE TABLE census_congdists_trip_map
(
  gid serial NOT NULL,
  agencyid character varying(255),
  agencyid_def character varying(255),
  routeid character varying(255),
  congdistid character varying(4),
  tripid character varying(255),
  serviceid character varying(255),
  stopscount integer,  
  length float,
  shape geometry(multilinestring),
  uid varchar(512),
  CONSTRAINT census_congdists_trip_map_pkey PRIMARY KEY (gid),
  CONSTRAINT census_congdists_trip_map_fkey FOREIGN KEY (agencyid, tripid)
      REFERENCES gtfs_trips (agencyid, id) MATCH SIMPLE
      ON UPDATE NO ACTION ON DELETE NO ACTION
)
WITH (
  OIDS=FALSE
);
ALTER TABLE census_congdists_trip_map
  OWNER TO postgres;

insert into census_congdists_trip_map(tripid, agencyid, agencyid_def, serviceid, routeid,  congdistid, shape, length, uid) 
select trip.id, trip.agencyid, trip.serviceid_agencyid, trip.serviceid_id, trip.route_id, congdist.congdistid, st_multi(ST_CollectionExtract(st_union(ST_Intersection(trip.shape,congdist.shape)),2)), (ST_Length(st_transform(ST_Intersection(trip.shape,congdist.shape),2993))/1609.34), trip.uid
from gtfs_trips trip
inner join census_congdists congdist on  st_intersects(congdist.shape,trip.shape)=true group by trip.id, trip.agencyid, congdist.congdistid;

update census_congdists_trip_map set stopscount = res.cnt+0 from 
(select count(stop.id) as cnt, stop.congdistid as cid, stime.trip_agencyid as aid, stime.trip_id as tid 
from gtfs_stops stop inner join gtfs_stop_times stime 
on stime.stop_agencyid = stop.agencyid and stime.stop_id = stop.id group by stime.trip_agencyid, stime.trip_id, stop.congdistid) as res
where congdistid =  res.cid and agencyid = res.aid and tripid=res.tid;

update census_congdists_trip_map set stopscount=0 where stopscount IS NULL;