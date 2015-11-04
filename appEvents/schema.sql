CREATE EXTENSION postgis;

DROP TABLE IF EXISTS gdelt_raw_input CASCADE;
CREATE TABLE gdelt_raw_input(
  sqldate int,
  actor1name varchar(100),
  actor2name varchar(100),
  eventcode varchar(10),
  eventbasecode varchar(10),
  eventrootcode varchar(10),
  actiongeo_countrycode varchar(10),
  actiongeo_lat real,
  actiongeo_long real,
  sourceurl varchar(200),
  sourcedomain varchar(100),
  the_geom geometry,
  CONSTRAINT enforce_dims_the_geom CHECK (st_ndims(the_geom) = 2),
  CONSTRAINT enforce_geotype_geom CHECK (geometrytype(the_geom) = 'POINT'::text OR the_geom IS NULL),
  CONSTRAINT enforce_srid_the_geom CHECK (st_srid(the_geom) = 4326)
  )
  DISTRIBUTED BY (sqldate);


DROP INDEX IF EXISTS gdelt_the_geom_gist CASCADE;
CREATE INDEX gdelt_the_geom_gist ON gdelt_raw_input
USING gist (the_geom);


DROP TABLE IF EXISTS acled_raw_input CASCADE;
CREATE TABLE acled_raw_input(
  eventdate int,
  event_type varchar(100),
  country varchar(20),
  location varchar(100),
  latitude real,
  longitude real,
  the_geom geometry,
  CONSTRAINT enforce_dims_the_geom CHECK (st_ndims(the_geom) = 2),
  CONSTRAINT enforce_geotype_geom CHECK (geometrytype(the_geom) = 'POINT'::text OR the_geom IS NULL),
  CONSTRAINT enforce_srid_the_geom CHECK (st_srid(the_geom) = 4326)
  )
  DISTRIBUTED BY (eventdate);


DROP INDEX IF EXISTS acled_the_geom_gist CASCADE;
CREATE INDEX aclead_the_geom_gist ON acled_raw_input
USING gist (the_geom);


