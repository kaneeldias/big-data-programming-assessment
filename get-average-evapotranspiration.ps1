$sql = "
DROP VIEW IF EXISTS avg_evapotranspiration_data;
CREATE VIEW IF NOT EXISTS evapotranspiration_with_month AS
SELECT
    location_id,
    SPLIT(``date``, '/')[0] AS month,
    et0_fao_evapotranspiration AS evapotranspiration
FROM weather_data;

DROP VIEW IF EXISTS avg_evapotranspiration_data_sep_to_mar;
CREATE VIEW IF NOT EXISTS avg_evapotranspiration_data_sep_to_mar AS
SELECT
    location_id,
    AVG(evapotranspiration) AS average_evapotranspiration
FROM evapotranspiration_with_month
WHERE month IN ('9', '10', '11', `12`, '1', '2', '3')
GROUP BY location_id;

DROP VIEW IF EXISTS avg_evapotranspiration_data_apr_to_aug;
CREATE VIEW IF NOT EXISTS avg_evapotranspiration_data_apr_to_aug AS
SELECT
    location_id,
    AVG(evapotranspiration) AS average_evapotranspiration
FROM evapotranspiration_with_month
WHERE month IN ('4', '5', '6', '7', '8')
GROUP BY location_id;

SELECT
    ld.city_name AS city,
    sep_mar.average_evapotranspiration AS avg_evapotranspiration_sep_to_mar,
    apr_aug.average_evapotranspiration AS avg_evapotranspiration_apr_to_aug
FROM avg_evapotranspiration_data_sep_to_mar sep_mar
JOIN avg_evapotranspiration_data_apr_to_aug apr_aug
    ON sep_mar.location_id = apr_aug.location_id
JOIN location_data ld
    ON sep_mar.location_id = ld.location_id;
"

docker exec -it hive-server beeline -u jdbc:hive2://localhost:10000 -n hive -e "$sql"
