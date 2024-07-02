CREATE TABLE Schedules(
  slot TSTZRANGE NOT NULL CHECK(
      date_part('minute', LOWER(slot)) IN (00, 30)
      AND
      date_part('minute', UPPER(slot)) IN (00, 30)),
    duration INT GENERATED ALWAYS AS (
      EXTRACT (epoch FROM UPPER(slot) - LOWER(slot))/60
  ) STORED CHECK(duration IN (30, 60, 90, 120)),
  EXCLUDE USING GIST(slot WITH &&)
);

CREATE TABLE Reservations (
    id INTEGER,
    start_time TSTZRANGE,
    finish_time TSTZRANGE,
    CONSTRAINT no_screening_time_overlap EXCLUDE USING GIST (finish_time WITH =, start_time WITH &&)
);

SELECT *
FROM Reservations
WHERE finish_time <@ start_time;

SELECT finish_time @> start_time
FROM Reservations;

SELECT start_time && finish_time, start_time << finish_time, start_time >> finish_time,
 start_time &> finish_time, start_time &< finish_time, start_time -|- finish_time,
 start_time * finish_time, start_time + finish_time, start_time - finish_time
FROM Reservations;

--error[col 7]: expression must be JSONB, TSVECTOR, TSRANGE, TSTZRANGE.
SELECT id @> start_time
FROM Reservations;

SELECT tstzmultirange(tstzrange('2010-01-01 14:30:00', '2010-01-01 15:30:00', '[]')) - range_agg(start_time)
FROM Reservations
WHERE start_time && tstzrange('2010-01-01 14:30:00', '2010-01-01 15:30:00', '[]');
