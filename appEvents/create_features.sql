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
