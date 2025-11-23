$sql = "
SELECT
    ld.city_name as city,
    MAX(wd.temperature_2m_max) AS max_temp
FROM
    weather_data wd
JOIN location_data ld ON wd.location_id = ld.location_id
GROUP BY ld.city_name
ORDER BY max_temp DESC
LIMIT 10;
"

docker exec -it hive-server beeline -u jdbc:hive2://localhost:10000 -n hive -e "$sql"
