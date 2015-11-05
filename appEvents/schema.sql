CREATE EXTENSION postgis;

DROP TABLE IF EXISTS gdelt_raw_input CASCADE;
CREATE TABLE gdelt_raw_input(
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



DROP INDEX IF EXISTS gdelt_the_geom_gist CASCADE;
CREATE INDEX gdelt_the_geom_gist ON gdelt_raw_input
USING gist (the_geom);


DROP TABLE IF EXISTS acled_raw_input CASCADE;
CREATE TABLE acled_raw_input(
  eventid varchar(100),
  eventdate int,
  event_type varchar(100),
  country varchar(100),
  latitude real,
  longitude real,
  the_geom geometry,
  CONSTRAINT enforce_dims_the_geom CHECK (st_ndims(the_geom) = 2),
  CONSTRAINT enforce_geotype_geom CHECK (geometrytype(the_geom) = 'POINT'::text OR the_geom IS NULL),
  CONSTRAINT enforce_srid_the_geom CHECK (st_srid(the_geom) = 4326)
);


DROP INDEX IF EXISTS acled_the_geom_gist CASCADE;
CREATE INDEX aclead_the_geom_gist ON acled_raw_input
USING gist (the_geom);

DROP TABLE IF EXISTS african_countries CASCADE;
CREATE TABLE african_countries(
	fipscode varchar(2),
	othercode varchar(10),
	name text);

