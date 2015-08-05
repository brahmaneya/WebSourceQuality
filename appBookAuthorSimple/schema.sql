DROP TABLE IF EXISTS tuples_input CASCADE;
CREATE TABLE tuples_input(
  book_id int,
  author_id int
  )
  DISTRIBUTED BY (book_id);

DROP TABLE IF EXISTS labeled_tuples_input CASCADE;
CREATE TABLE labeled_tuples_input(
  book_id int,
  author_id int,
  is_true boolean
  )
  DISTRIBUTED BY (book_id);

DROP TABLE IF EXISTS source_outputs_input CASCADE;
CREATE TABLE source_outputs_input(
  source_id int,
  book_id int,
  author_id int,
  is_true boolean
  )
  DISTRIBUTED BY (book_id);

DROP TABLE IF EXISTS source_features CASCADE;
CREATE TABLE tuples_input(
  source_id int,
  feature text
  )
  DISTRIBUTED BY (source_id);

DROP TABLE IF EXISTS source_books CASCADE;
CREATE TABLE source_books(
  source_id int,
  book_id int
  )
  DISTRIBUTED BY (book_id);

DROP TABLE IF EXISTS source_book_authors CASCADE;
CREATE TABLE source_book_authors(
  source_id int,
  book_id int,
  author_id int
  )
  DISTRIBUTED BY (book_id);

DROP TABLE IF EXISTS tuple_truth CASCADE;
CREATE TABLE tuple_truth(
  tuple_id text,
  book_id int,
  author_id int,
  is_true boolean, -- whether tuple is true
  id bigint -- reserved for deepdive
  )
  DISTRIBUTED BY (tuple_id);

DROP TABLE IF EXISTS source_outputs CASCADE;
CREATE TABLE source_outputs(
  source_id int,
  tuple_id text,
  source_tuple_id text, -- unique identifier for source_outputs
  is_true boolean -- whether the source did output the tuple
  )
  DISTRIBUTED BY (source_tuple_id);

