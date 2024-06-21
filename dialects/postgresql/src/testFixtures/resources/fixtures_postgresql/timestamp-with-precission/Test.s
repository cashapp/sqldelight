CREATE TABLE test (
  _id INTEGER NOT NULL PRIMARY KEY,
  date1 TEXT NOT NULL DEFAULT CURRENT_TIME(2),
  date2 TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP(3)
);

-- Throws no errors.
CREATE TRIGGER on_update_trigger
AFTER UPDATE
ON test
BEGIN
  UPDATE test SET date1 = CURRENT_TIME(1) WHERE new._id = old._id;
END;

UPDATE test
SET date1 = CURRENT_TIME(6),
    date2 = CURRENT_TIMESTAMP(2);

UPDATE test
SET date1 = CURRENT_TIME(2),
    date2 = CURRENT_TIMESTAMP(3)
WHERE date1 > CURRENT_TIME(1);