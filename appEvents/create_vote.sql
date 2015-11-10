create materialized view srcGeidToAceid as
select geid, aceid,eventdate, sqldate, sourcedomain from (
select g.eventid as geid, g.actiongeo_lat, g.actiongeo_long, g.actiongeo_countrycode, g.sqldate, g.sourceurl, g.sourcedomain, ac.eventid as aceid, ac.latitude, ac.longitude, ac.eventdate, ac.country
from (select distinct eventid, sqldate, actiongeo_lat, actiongeo_long, actiongeo_countrycode, sourceurl, sourcedomain, the_geom, eventrootcode, eventcode from gdelt_africa_raw_input) as g, acled_raw_input as ac
where ST_Distance_Sphere(g.the_geom, ac.the_geom) < 100000
and g.eventrootcode = '14' and g.sqldate - ac.eventdate >=0 and g.sqldate - ac.eventdate < 2) tmp;

create materialized view negativeVotes as
select s1.sourcedomain as s1srcdomain, s1.geid as s1geid, s2.sourcedomain as s2srcdomain, s2.geid as s2geid 
from srcGeidToAceid as s1, srcGeidToAceid as s2
where s1.sourcedomain <> s2.sourcedomain
and s1.aceid = s2.aceid
and s1.sqldate <> s2.sqldate
order by s1.sourcedomain, s2.sourcedomain;

create materialized view positiveVotesAcross as
select s1.sourcedomain as s1srcdomain, s1.geid as s1geid, s2.sourcedomain as s2srcdomain, s2.geid as s2geid
from srcGeidToAceid as s1, srcGeidToAceid as s2
where s1.sourcedomain <> s2.sourcedomain
and s1.aceid = s2.aceid
and s1.sqldate = s2.sqldate
order by s1.sourcedomain, s2.sourcedomain;

create materialized view positiveVotes as
select distinct sourcedomain as srcdomain, eventid as geid
from gdelt_africa_raw_input;
