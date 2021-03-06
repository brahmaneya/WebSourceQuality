DROP TABLE IF EXISTS source_outputs_input CASCADE;
CREATE TABLE source_outputs_input(
  source_id int,
  tuple_id int,
  is_true boolean -- whether the source did output the tuple
  )
  DISTRIBUTED BY (tuple_id);

DROP TABLE IF EXISTS group_tuple_belief_input CASCADE;
CREATE TABLE group_tuple_belief_input(
  group_id int,
  tuple_id int,
  is_true boolean -- whether the group believes the tuple
  )
  DISTRIBUTED BY (tuple_id);

DROP TABLE IF EXISTS source_outputs CASCADE;
CREATE TABLE source_outputs(
  source_id int,
  tuple_id int,
  source_tuple_id text, -- unique identifier for source_outputs
  is_true boolean, -- whether the source did output the tuple
  id bigint  -- reserved for DeepDive
  )
  DISTRIBUTED BY (source_tuple_id);

DROP TABLE IF EXISTS source_groups CASCADE;
CREATE TABLE source_groups(
  source_id int,
  group_id int
  )
  DISTRIBUTED BY (source_id);

DROP TABLE IF EXISTS group_tuple_belief CASCADE;
CREATE TABLE group_tuple_belief(
  group_id int,
  tuple_id int,
  group_tuple_id text, -- unique identifier for group_tuple_belief 
  is_true boolean,
  id bigint   -- reserved for DeepDive
  )
  DISTRIBUTED BY (group_tuple_id);

DROP TABLE IF EXISTS source_group_tuple_belief CASCADE;
CREATE TABLE source_group_tuple_belief(
  source_id int,
  group_id int,
  tuple_id int,
  source_group_tuple_id text, -- unique identifier for source_group_tuple_belief 
  is_true boolean,
  id bigint   -- reserved for DeepDive
  )
  DISTRIBUTED BY (source_group_tuple_id);

DROP TABLE IF EXISTS tuple_truth CASCADE;
CREATE TABLE tuple_truth(
  tuple_id int,
  is_true boolean,
  id bigint   -- reserved for DeepDive
  )
  DISTRIBUTED BY (tuple_id);

DROP TABLE IF EXISTS sentences CASCADE;
CREATE TABLE sentences(
  document_id text,
  sentence text, 
  words text[],
  lemma text[],
  pos_tags text[],
  dependencies text[],
  ner_tags text[],
  sentence_offset bigint,
  sentence_id text -- unique identifier for sentences
  )
  DISTRIBUTED BY (sentence_id);

