CREATE TABLE Events(
  start_at TIMESTAMPTZ NOT NULL CHECK(date_part('minute', start_at) IN (00,30)),
  end_at TIMESTAMPTZ NOT NULL CHECK(date_part('minute', end_at) IN (00,30)),
  duration INT GENERATED ALWAYS AS (EXTRACT(epoch FROM end_at - start_at)/ 60) stored,
  created_date DATE
);

SELECT EXTRACT(YEAR FROM TIMESTAMP '2023-05-15 10:30:45');

--SELECT EXTRACT(MONTH FROM DATE '2023-05-15'); not supported

--SELECT EXTRACT(HOUR FROM TIME '10:30:45'); not supported

SELECT EXTRACT(EPOCH FROM INTERVAL '1 day 2 hours');

SELECT EXTRACT(HOUR FROM created_date) FROM Events;
