CREATE TABLE gdelt_africa_raw_input(
  eventid int,
  sqldate int,
  actor1name varchar(100),
  actor2name varchar(100),
  eventcode varchar(10),
  eventbasecode varchar(10),
  eventrootcode varchar(10),
  nummentions int,
  numsources int,
  numarticles int,
  avgtone real,
  actiongeo_countrycode varchar(10),
  actiongeo_lat real,
  actiongeo_long real,
  sourceurl text,
  sourcedomain varchar(100),
  the_geom geometry,
  CONSTRAINT enforce_dims_the_geom CHECK (st_ndims(the_geom) = 2),
  CONSTRAINT enforce_geotype_geom CHECK (geometrytype(the_geom) = 'POINT'::text OR the_geom IS NULL),
  CONSTRAINT enforce_srid_the_geom CHECK (st_srid(the_geom) = 4326)
);

CREATE INDEX gdelt_africa_geom_gist ON gdelt_africa_raw_input
USING gist (the_geom);


insert into gdelt_africa_raw_input
select g.*
from gdelt_raw_input as g
where g.actiongeo_countrycode in (select fipscode from african_countries)
and g.sourcedomain in (select * from srcwithfeats)
and g.eventrootcode = '14';


