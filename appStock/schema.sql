DROP TABLE IF EXISTS stock_input CASCADE;
CREATE TABLE stock_input(
  stock_symbol varchar(10),
  volume int,
  is_true text
  )
  DISTRIBUTED BY (stock_symbol);

DROP TABLE IF EXISTS source_stock_input CASCADE;
CREATE TABLE source_stock_input(
  source_id varchar(100),
  stock_symbol varchar(10),
  stock_volume int,
  is_true boolean
  )
  DISTRIBUTED BY (source_id);

DROP TABLE IF EXISTS source_features CASCADE;
CREATE TABLE source_features(
  source_id varchar(100),
  feature text
  )
  DISTRIBUTED BY (source_id);

DROP TABLE IF EXISTS stock_truth CASCADE;
CREATE TABLE stock_truth(
  stock_symbol varchar(10),
  stock_tuple_id text,
  is_true boolean, -- whether tuple is true
  id bigint -- reserved for deepdive
  )
  DISTRIBUTED BY (stock_tuple_id);

DROP TABLE IF EXISTS source_stock_truth CASCADE;
CREATE TABLE source_stock_truth(
  source_id varchar(100),
  stock_tuple_id text, -- unique identifier for source_outputs
  is_true boolean -- whether the source did output the tuple
  )
  DISTRIBUTED BY (stock_tuple_id);


DROP TABLE IF EXISTS source_train CASCADE;
CREATE TABLE stock_train(
  stock_symbol varchar(10)
  ) 
  DISTRIBUTED BY (stock_symbol);

DROP TABLE IF EXISTS feature_source_counts CASCADE;
CREATE TABLE feature_source_counts(
  feature text,
  count int
  ) 
  DISTRIBUTED BY (feature);

DROP TABLE IF EXISTS feature_metadata CASCADE;
CREATE TABLE feature_metadata(
  feature text,
  metadata text
  )
  DISTRIBUTED BY (feature);
  
DROP TABLE IF EXISTS feature_counts CASCADE;
CREATE TABLE feature_counts(
  feature text,
  count int
  )
  DISTRIBUTED BY (feature);

DROP TABLE IF EXISTS feature_pair_sources CASCADE;
CREATE TABLE feature_pair_sources(
  feature1 text,
  feature2 text,
  source_id varchar(100)
  )
  DISTRIBUTED BY (feature1, feature2);

DROP TABLE IF EXISTS feature_pair_counts CASCADE;
CREATE TABLE feature_pair_counts(
  feature1 text,
  feature2 text,
  count1 int,
  count2 int,
  count12 int
  )
  DISTRIBUTED BY (feature1, feature2);

DROP TABLE IF EXISTS feature_feature_correlations CASCADE;
CREATE TABLE feature_feature_correlations(
  feature1 text,
  feature2 text,
  correlation real
  )
  DISTRIBUTED BY (feature1, feature2);

DROP TABLE IF EXISTS source_features2 CASCADE;
CREATE TABLE source_features2(
  source_id varchar(100),
  feature text
  )
  DISTRIBUTED BY (source_id);
