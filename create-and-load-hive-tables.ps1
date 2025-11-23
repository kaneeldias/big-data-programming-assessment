$weather_data_sql = "
DROP TABLE IF EXISTS weather_data;
CREATE TABLE weather_data (
    location_id INT,
    ``date`` STRING,
    weather_code INT,
    temperature_2m_max FLOAT,
    temperature_2m_min FLOAT,
    temperature_2m_mean FLOAT,
    apparent_temperature_max FLOAT,
    apparent_temperature_min FLOAT,
    apparent_temperature_mean FLOAT,
    daylight_duration FLOAT,
    sunshine_duration FLOAT,
    precipitation_sum FLOAT,
    rain_sum FLOAT,
    precipitation_hours INT,
    wind_speed_10m_max FLOAT,
    wind_gusts_10m_max FLOAT,
    wind_direction_10m_dominant INT,
    shortwave_radiation_sum FLOAT,
    et0_fao_evapotranspiration FLOAT,
    sunrise STRING,
    sunset STRING
)
ROW FORMAT DELIMITED
FIELDS TERMINATED BY ','
STORED AS TEXTFILE
TBLPROPERTIES ('skip.header.line.count'='1');
"

docker exec -it hive-server beeline -u jdbc:hive2://localhost:10000 -n hive -e "$weather_data_sql"

docker cp ./input/weatherData.csv hive-server:/tmp/weather_data.csv
docker exec -it hive-server beeline -u jdbc:hive2://localhost:10000 -n hive -e "LOAD DATA LOCAL INPATH '/tmp/weather_data.csv' OVERWRITE INTO TABLE weather_data;"

$location_data_sql = "
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
)
ROW FORMAT DELIMITED
FIELDS TERMINATED BY ','
STORED AS TEXTFILE
TBLPROPERTIES ('skip.header.line.count'='1');
"

docker exec -it hive-server beeline -u jdbc:hive2://localhost:10000 -n hive -e "$location_data_sql"

docker cp ./input/locationData.csv hive-server:/tmp/location_data.csv
docker exec -it hive-server beeline -u jdbc:hive2://localhost:10000 -n hive -e "LOAD DATA LOCAL INPATH '/tmp/location_data.csv' OVERWRITE INTO TABLE location_data;"
