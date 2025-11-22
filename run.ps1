docker cp ./input/assessment.txt namenode:/tmp/assessment.txt
docker exec -it namenode bash -c "hdfs dfs -mkdir -p /input"
docker exec -it namenode bash -c "hdfs dfs -put /tmp/assessment.txt /input/assessment.txt"
docker exec -it namenode bash -c "hdfs dfs -ls /input"

docker cp ./target/wordcount.jar namenode:/tmp/wordcount.jar

docker exec -it namenode bash -c "hadoop jar /tmp/wordcount.jar /input/assessment.txt /output/assessment_wc"