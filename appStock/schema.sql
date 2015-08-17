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
CREATE TABLE tuple_truth(
  stock_tuple_id text,
  is_true boolean, -- whether tuple is true
  id bigint -- reserved for deepdive
  )
  DISTRIBUTED BY (stock_tuple_id);

DROP TABLE IF EXISTS source_stock_truth CASCADE;
CREATE TABLE source_stock_truth(
  source_id varchar(100),
  source_tuple_id text, -- unique identifier for source_outputs
  is_true boolean -- whether the source did output the tuple
  )
  DISTRIBUTED BY (source_tuple_id);

