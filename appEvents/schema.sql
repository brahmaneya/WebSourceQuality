DROP TABLE IF EXISTS eventPosVotes CASCADE;
CREATE TABLE eventPosVotes(
  source_id varchar(200),
  geid int
)
DISTRIBUTED BY (geid);

DROP TABLE IF EXISTS eventNegVotes CASCADE;
CREATE TABLE eventNegVotes(
  source_id varchar(200),
  geid int
)
DISTRIBUTED BY (geid);


DROP TABLE IF EXISTS source_features CASCADE;
CREATE TABLE source_features(
  source_id varchar(200),
  feature text
  )
DISTRIBUTED BY (source_id);

DROP TABLE IF EXISTS event_input CASCADE;
CREATE TABLE event_input(
  geid int,
  is_true boolean
  )
DISTRIBUTED BY (geid);

DROP TABLE IF EXISTS event_truth CASCADE;
CREATE TABLE event_truth(
  geid int,
  is_true boolean, -- whether tuple is true
  id bigint -- reserved for deepdive
  )
DISTRIBUTED BY (geid);


DROP TABLE IF EXISTS source_event_truth CASCADE;
CREATE TABLE source_event_truth(
  source_id varchar(200),
  geid int,
  is_true boolean,
  id bigint
)
DISTRIBUTED BY (geid);
