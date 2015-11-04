DROP TABLE IF EXISTS gdelt_raw_input CASCADE;
CREATE TABLE gdelt_raw_input(
  eventid int,
  sqldate int,
  actor1name varchar(100),
  actor2name varchar(100),
  eventcode varchar(10),
  eventbasecode varchar(10),
  eventrootcode varchar(10),
  actiongeo_countrycode varchar(10),
  actiongeo_lat real,
  actiongeo_long real,
  sourceurl text,
  sourcedomain varchar(100)
);


