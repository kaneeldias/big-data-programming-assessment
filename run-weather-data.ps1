$timestamp = Get-Date -Format "yyyyMMdd-HH_mm_ss"
Write-Host "Script started at $timestamp"

#docker cp ./input/weatherData.csv "namenode:/tmp/weatherData.csv"
#docker cp ./input/locationData.csv namenode:/tmp/locationData.csv
#
#docker exec -it namenode bash -c "hdfs dfs -mkdir -p /input"
#
#docker exec -it namenode bash -c "hdfs dfs -rm -r /input/weatherData.csv"
#docker exec -it namenode bash -c "hdfs dfs -rm -r /input/locationData.csv"
#
#docker exec -it namenode bash -c "hdfs dfs -put /tmp/weatherData.csv /input/weatherData.csv"
#docker exec -it namenode bash -c "hdfs dfs -put /tmp/locationData.csv /input/locationData.csv"
#
#docker exec -it namenode bash -c "hdfs dfs -ls /input"

mvn clean package
docker cp ./target/weatherdata.jar namenode:/tmp/weatherdata.jar

#docker exec -it namenode bash -c "hdfs dfs -rm -r /output/weatherdata"
#docker exec -it namenode bash -c "hdfs dfs -rm -r /tmp/total_precipitation_output"
#docker exec -it namenode bash -c "hdfs dfs -rm -r /tmp/mean_temperature_output"
#docker exec -it namenode bash -c "hdfs dfs -rm -r /tmp/joined_output"

docker exec -it namenode bash -c "hdfs dfs -mkdir -p /output/$timestamp"
docker exec -it namenode bash -c "hadoop jar /tmp/weatherdata.jar /input/weatherData.csv /input/locationData.csv /output/$timestamp"

docker exec -it namenode bash -c "hdfs dfs -getmerge /output/$timestamp/final/* /tmp/weather_data.csv"
docker exec -it namenode bash -c "hdfs dfs -getmerge /output/$timestamp/maximum_precipitation_by_month_output/* /tmp/maximum_data.csv"

docker cp namenode:/tmp/weather_data.csv ./output/weatherdata_$timestamp.csv
docker cp namenode:/tmp/maximum_data.csv ./output/maximumdata_$timestamp.csv
