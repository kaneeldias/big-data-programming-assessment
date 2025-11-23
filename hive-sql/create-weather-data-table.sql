DROP TABLE IF EXISTS weather_data;

CREATE TABLE IF NOT EXISTS weather_data (
    location_id INT,
    `date` STRING,
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
TBLPROPERTIES ("skip.header.line.count"="1");
