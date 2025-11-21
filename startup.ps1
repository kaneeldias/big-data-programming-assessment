docker-compose down -v
docker volume rm namenode_data datanode_data hadoop_conf 2>$null
docker-compose run --rm --user root namenode bash -lc "mkdir -p /opt/hadoop/data/namenode /opt/hadoop/data/datanode && chown -R hadoop:hadoop /opt/hadoop/data"
docker-compose run --rm namenode bash -lc "ls -la /opt/hadoop/data/namenode || true"
docker-compose run --rm namenode bash -lc "/opt/hadoop/bin/hdfs namenode -format -force -nonInteractive"
docker-compose up -d
docker-compose run --rm --user root namenode bash -lc "chown -R 1000:1000 /tmp/hadoop-hadoop/dfs 2>/dev/null || true; if [ ! -d /tmp/hadoop-hadoop/dfs/name/current ]; then /opt/hadoop/bin/hdfs namenode -format -force -nonInteractive || true; fi"
docker-compose run --rm --user root datanode bash -lc "chown -R 1000:1000 /opt/hadoop/data 2>/dev/null || true"
docker-compose run --rm --user root namenode bash -lc `
  "mkdir -p /tmp/hadoop-hadoop/dfs/name /opt/hadoop/data/datanode && \
   chown -R 1000:1000 /tmp/hadoop-hadoop/dfs /opt/hadoop/data 2>/dev/null || true && \
   if [ ! -d /tmp/hadoop-hadoop/dfs/name/current ]; then /opt/hadoop/bin/hdfs namenode -format -force -nonInteractive || true; fi"
docker-compose logs -f namenode datanode