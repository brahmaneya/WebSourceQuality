create materialized view sourceCountries as
select sourcedomain, count(distinct actiongeo_countrycode) countries_touched
from gdelt_raw_input
group by sourcedomain;


create materialized view sourceEventTypes as
select sourcedomain, count(distinct eventrootcode) eventTypes
from gdelt_raw_input
group by sourcedomain;

create materialized view sourceEvAvgTone as
select sourcedomain, avg(avgtone) eventAvgTone
from gdelt_raw_input
group by sourcedomain;

create materialized view sourceEventBreakDown as
select sourcedomain, count(CASE WHEN numsources < 50 THEN 1 ELSE null END) unpopEvents, 
count(CASE WHEN numsources >= 50 THEN 1 ELSE null END) popEvents, count(*) totalEvents
from gdelt_raw_input
group by sourcedomain;


copy (
select sourcedomain, 'SourceSize='||round((totalevents::float/37842::float)::numeric,3) as feat from sourceeventbreakdown
where sourcedomain in (select * from srcwithfeats)
UNION
select sourcedomain, 'PopToUnpopEvents='||round((popevents::float/unpopevents::float)::numeric,3) as feat from sourceeventbreakdown
where sourcedomain in (select * from srcwithfeats)
UNION
select sourcedomain, 'EventTypes='||eventtypes as feat from sourceeventtypes
where sourcedomain in (select * from srcwithfeats)
UNION
select sourcedomain, 'Countries='||countries_touched as feat from sourcecountries
where sourcedomain in (select * from srcwithfeats)
) To '/tmp/srcMentionFeats.csv' With CSV;
