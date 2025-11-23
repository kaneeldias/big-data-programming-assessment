DROP TABLE IF EXISTS location_data;

CREATE TABLE IF NOT EXISTS location_data (
    location_id INT,
    latitude FLOAT,
    longitude FLOAT,
    elevation FLOAT,
    utf_offset_seconds INT,
    timezone STRING,
    timezone_abbreviation INT,
    city_name STRING
    city_name STRING
)
ROW FORMAT DELIMITED
FIELDS TERMINATED BY ','
STORED AS TEXTFILE
TBLPROPERTIES ("skip.header.line.count"="1");
