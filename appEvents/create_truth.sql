create materialized view trueEvents as
select distinct geid from (
select g.eventid as geid, 
g.actiongeo_lat, 
g.actiongeo_long, 
g.actiongeo_countrycode, 
g.sqldate, g.sourceurl, 
g.sourcedomain, 
ac.eventid as aceid, 
ac.latitude, ac.longitude, 
ac.eventdate, ac.country
from (select distinct eventid, 
	sqldate, 
	actiongeo_lat, 
	actiongeo_long, 
	actiongeo_countrycode, 
	sourceurl, 
	sourcedomain, 
	the_geom, 
	eventrootcode, 
	eventcode from gdelt_africa_raw_input) as g, acled_raw_input as ac
where ST_Distance_Sphere(g.the_geom, ac.the_geom) < 50000
and g.eventrootcode = '14' and g.sqldate = ac.eventdate) tmp;


create materialized view falseEvents as
select distinct eventid from gdelt_africa_raw_input
where eventid not in (select geid from trueEvents);

Copy ( 
select geid, 'true' as is_true 
from trueEvents
union
select eventid as geid, 'false' as is_true
from falseEvents) To '/tmp/groundTruth.csv' With CSV;
