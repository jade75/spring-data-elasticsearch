# Docker ELK (ElasticSearch logstash Kibana) 

D00216118



## Docker ElasticSearch

#### pulling the imge of ElasticSearch 

```
docker pull elasticsearch:6.5.3
```



#### starting a single nodle cluster with docker 

Here is mainly to simplify the development environment, a node is configured. If you need more environment, please refer to [here](https://www.elastic.co/guide/en/elasticsearch/reference/current/docker.html) .

```
docker run -d --name es -p 9200:9200 -p 9300:9300 -e "discovery.type=single-node" elasticsearch:6.5.3 
```

or small memory(512m) starting

```
docker run -d --name es -p 9200:9200 -p 9300:9300 -e "ES_JAVA_OPTS=-Xms512m -Xmx512m" -e "discovery.type=single-node" elasticsearch:6.5.3                    
```

It may stop automatically after running for a period of time, because the virtual memory is too small, please refer to [here](https://www.elastic.co/guide/en/elasticsearch/reference/current/vm-max-map-count.html)  or [here2](https://stackoverflow.com/questions/62037965/about-docker-containers-exit-when-docker-compose-up) for the solution.



#### check 

```
docker ps -a
```

```
docker logs -f --tail 20 es 
```

http://127.0.0.1:9200



## Docker Logstash



#### pulling the imge of Logstash

Here choose logstash with the same version number as ElasticSearch in order to reduce errors.

```
docker pull logstash:6.5.3
```



#### Running  logstash

```
docker run -d --name=logstash logstash:6.8.12
```



#### Host load configuration file

Here choose the external data, config, pipeline directory, of course, you can also just plug-in confIg and ignore the others, it depends on youself. Local directory is “/Users/yuchen/logstash6”.

```
docker cp logstash:/usr/share/logstash/config /Users/yuchen/logstash6 
docker cp logstash:/usr/share/logstash/data /Users/yuchen/logstash6
docker cp logstash:/usr/share/logstash/pipeline /Users/yuchen/logstash6 
```



#### modify local config file "logstash.yml" 

Here we monitor the use of IP address by elasticsearch to reduce errors caused by using localhost.

```
http.host: "0.0.0.0"
xpack.monitoring.elasticsearch.url: http://192.168.0.101:9200
path.config: "/usr/share/logstash/config/mysql.conf"
#path.logs: /usr/share/logstash/logs


chmod -R 777 logstash6/
```



#### Stop and delete the current container

```
docker stop logstash

docker rm logstash
```



#### Run a new container with a specific configuration

```
docker run \
--name logstash \
--restart=always \
-p 5044:5044 \
-p 9600:9600 \
-v /Users/yuchen/logstash6/config:/usr/share/logstash/config \
-v /Users/yuchen/logstash6/data:/usr/share/logstash/data \
-v /Users/yuchen/logstash6/pipeline:/usr/share/logstash/pipeline \
-d logstash:6.5.3
```



#### Update logstash's dependency driver

```
docker exec -it logstash bash

./bin/logstash-plugin install logstash-input-jdbc

./bin/logstash-plugin install logstash-output-elasticsearch

```



#### Set MySQL synchronization configuration

here the config file name is mysql.conf. you need to put the mysql-connector-java-5.1.47.jar in the local config file. 

```
input {
 stdin { }
    jdbc {
        #mysqliplocalhost
        jdbc_connection_string => "jdbc:mysql://192.168.0.101:3306/test"
        jdbc_user => "root"
        jdbc_password => "root123"
        #jar
        jdbc_driver_library => "/usr/share/logstash/config/mysql-connector-java-5.1.47.jar"
        jdbc_driver_class => "com.mysql.jdbc.Driver"
        jdbc_paging_enabled => "true"
        jdbc_page_size => "50000"
        tracking_column => "unix_ts_in_secs"
        tracking_column_type => "numeric"
        statement => "SELECT *, UNIX_TIMESTAMP(modification_time) AS unix_ts_in_secs FROM test WHERE (UNIX_TIMESTAMP(modification_time) > :sql_last_value AND modification_time < NOW()) ORDER BY modification_time ASC"
        schedule => "*/5 * * * * *"
    }
 }

 output {
     stdout {
        codec => json_lines
    }
    elasticsearch {
        #mysqliplocalhost
        hosts => "192.168.0.101:9200"
        index => "test"
        #document_type => "_doc"
        document_id => "%{id}"
    }
}
```



```sql
CREATE TABLE test ( 
  id BIGINT(20) UNSIGNED NOT NULL,  PRIMARY KEY (id),  
  UNIQUE KEY unique_id (id),  
  client_name VARCHAR(32) NOT NULL,  
  modification_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,  
  insertion_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP 
);
```



#### check

```bash
docker stop logstash

docker start logstash

docker ps -a

docker logs -f --tail 200 logstash 
```



result

```json
http://192.168.0.101:9200/test

{
test: {
aliases: { },
mappings: {
doc: {
properties: {
@timestamp: {
type: "date"
},
...............
```



## Kibana

#### pulling the imge of Logstash

```
docker pull kibana/kibana:6.5.3
```



#### Running  Kibana

```
docker run -it -d --name kibana -p 5601:5601 kibana:6.5.3
```



#### modify config

```
docker exec -it kibana /bin/bash/

cd /usr/share/kibana/config

vim kibana.yml
```



```
server.name: kibana 

server.host: "0" 

elasticsearch.url: "http://192.168.0.101:9200"
```



#### restart Kibana and access

```
docker restart kibana


http://localhost:5601/
```



![截屏2021-04-23 下午4.20.42](/Users/yuchen/Documents/截屏2021-04-23 下午4.20.42.png)



## reference



Elasticsearch

https://hub.docker.com/_/elasticsearch

https://www.elastic.co/guide/en/elasticsearch/reference/current/docker.html

https://www.elastic.co/guide/en/elastic-stack-get-started/current/get-started-docker.html

https://elk-docker.readthedocs.io/

https://blog.csdn.net/u010622525/article/details/100066743



logstash

https://www.elastic.co/cn/blog/how-to-keep-elasticsearch-synchronized-with-a-relational-database-using-logstash

https://blog.csdn.net/u010622525/article/details/100066743





filebeat

https://www.cnblogs.com/mhl1003/p/13036495.html



https://www.cnblogs.com/fbtop/p/11005469.html