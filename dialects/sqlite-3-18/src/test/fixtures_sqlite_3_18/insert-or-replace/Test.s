CREATE TABLE test (
  value TEXT
);

INSERT OR REPLACE INTO test
DEFAULT VALUES;

INSERT OR ABORT INTO test
DEFAULT VALUES;