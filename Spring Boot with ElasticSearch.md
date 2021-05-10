# Spring Boot with ElasticSearch

D00216118

## ElasticSearch

### What is ElasticSearch

Elasticsearch is a **`near real-time distributed`** and open source **`full-text search`** and analytics engine. It is accessible from **`RESTful web service interface`** and uses schema less **`JSON`** (JavaScript Object Notation) documents to store data. It is **`built on Java programming language`** and hence Elasticsearch can run on different platforms. It enables users to explore very large amount of data at very high speed [1] [2].



### Concepts 

#### Near Realtime

Elasticsearch can achieve near real-time search capabilities with low latency(usually one second).



#### Node

It refers to a single running instance of Elasticsearch.



#### Cluster

A cluster is a collection of one or more nodes (servers) that together holds your entire data and provides federated indexing and search capabilities across all nodes. It is a collection of one or more nodes. 



####  Index

An index is a collection of documents that have somewhat similar characteristics. For example, you can have an index for customer data, another index for a product catalog, and yet another index for order data. 



#### Document

A document is a basic unit of information that can be indexed. It is a collection of fields in a specific manner defined in JSON format. For example, you can have a document for a single customer, another document for a single product, and yet another for a single order. 



#### Shard

Indexes are horizontally subdivided into shards. This means each shard contains all the properties of document but contains less number of JSON objects than index. 



#### Replicas

Elasticsearch allows a user to create replicas of their indexes and shards. Replication not only helps in increasing the availability of data in case of failure, but also improves the performance of searching by carrying out a parallel search operation in these replicas.





#### The relationship between indexes, shards , replices and nodes





![1_P03D7YMDnaNdWeDENOhmpg](/Users/yuchen/Documents/1_P03D7YMDnaNdWeDENOhmpg.png)

​																	Figure : Shards and replicas — 5 shards, 1 replica



The `index` is divided into multiple `shards` which are stored in different `nodes`, and the `replicas` shards are stored as backup data. As shown in the figure, the same shard and shard backup cannot coexist under the same node in a cluster environment. More infomation see here  [[5]](https://medium.com/hipages-engineering/scaling-elasticsearch-b63fa400ee9e)    [[6]](https://www.elastic.co/cn/blog/every-shard-deserves-a-home).







<img src="/Users/yuchen/Documents/1_U39NfbVwkht1kLex8scVIw.png" alt="1_U39NfbVwkht1kLex8scVIw" style="zoom: 50%;" />



​																			Figure : How Distributed search works



In this distributed environment, searches are done in two phases:

- [Query Phase](https://www.elastic.co/guide/en/elasticsearch/guide/current/_query_phase.html): A new search is received, and it’s transformed into a set of searches (one on each shard). Each shard returns its matching documents, the lists are merged, rank, and sorted
  → The result of this phase is the list of documents ids that will be returned to the user
- [Fetch Phase](https://www.elastic.co/guide/en/elasticsearch/guide/current/_fetch_phase.html): Get the documents by id from their owning shards and return to the client



### Comparison between Elasticsearch and RDBMS

Mapping concepts across SQL and Elasticsearch[4].

|   SQL    |   Elasticearch   |
| :------: | :--------------: |
| cluster  |     cluster      |
| database | cluster instance |
|  table   |      index       |
|   row    |     Document     |
|  Column  |      Field       |





"size" : 0,
    "aggs" : { 
        "categorysize" : { 
            "terms" : { 
              "field" : "category"
            }
        },

### API Basic Data operation 

| Basic Operation                                              | Desception                                                   |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| DELETE /_all                                                 | Delete all index                                             |
| GET /_cat/indices?v                                          | list all indices                                             |
| GET _cluster/health                                          | Monitoring delayed unassigned shards                         |
| PUT _all/ _settings<br /> {   "settings": {    "index.unassigned.node_left.delayed_timeout": "0"   } } | Removing a node permanently                                  |
| PUT /customer?pretty <br />PUT /customer/doc/1?pretty <br />{   "name": "John Doe" } | create or modify Index                                       |
| GET /customer/doc/1?pretty                                   | retrieve that document                                       |
| DELETE /customer?pretty                                      | Delete the index of customer                                 |
| POST /customer/doc/1/_update?pretty <br />{   "doc": { "name": "Jane Doe", "age": 20 } }<br /><br />POST /customer/doc/1/_update?pretty <br />{   "script" : "ctx._source.age += 5" } | Updating documents<br /><br />Updates can also be performed by using simple scripts. |
| DELETE /customer/doc/2?pretty                                | delete documents                                             |
| POST /customer/doc/_bulk?pretty <br />{"index":{"_id":"1"}}{"name": "John Doe" } <br />{"index":{"_id":"2"}} {"name": "Jane Doe" } | batch processing                                             |
| curl -H "Content-Type: application/json" -XPOST 'localhost:9200/bank/account/_bulk?pretty&refresh' --data-binary "@accounts.json" <br />curl 'localhost:9200/_cat/indices?v' | Download the dataset                                         |
| analyze/?pretty<br/>{<br/>  "analyzer": "standard", "text": "this is a new car" <br/>} | Analyze keyword                                              |





### search API

#### Request URI

```console
GET /bank/_search?q=*&sort=account_number:asc&pretty
```

We are searching (`_search` endpoint) in the bank index, and the `q=*` parameter instructs Elasticsearch to match all documents in the index. The `sort=account_number:asc` parameter indicates to sort the results using the `account_number` field of each document in an ascending order. The `pretty` parameter, again, just tells Elasticsearch to return pretty-printed JSON results.



#### Request Body

```console
GET /bank/_search
{
  "query": { "match_all": {} },
  "sort": [
    { "account_number": "asc" }
  ]
}
```



#### Query DSL

Elasticsearch provides a full Query [DSL](https://www.elastic.co/guide/en/elasticsearch/reference/7.12/query-dsl.html) (Domain Specific Language) based on JSON to define queries.

```console
GET /bank/_search
{
  "query": { "match_phrase": { "address": "mill lane" } }
}
```



with filter

```console
GET /_search
{
  "query": { 
    "bool": { 
      "must": [
        { "match": { "title":   "Search"        }},
        { "match": { "content": "Elasticsearch" }}
      ],
      "filter": [ 
        { "term":  { "status": "published" }},
        { "range": { "publish_date": { "gte": "2015-01-01" }}}
      ]
    }
  }
}
```



Group 

```console
GET /bank/_search
{
  "size": 0,
  "aggs": {
    "group_by_state": {
      "terms": {
        "field": "state.keyword"
      }
    }
  }
}

same with: 
SELECT state, COUNT(*) FROM bank GROUP BY state ORDER BY COUNT(*) DESC
```



#### SQL API

[SQL REST API](https://www.elastic.co/guide/en/elasticsearch/reference/7.12/sql-rest.html)

```console
POST /_sql?format=txt
{
  "query": "SELECT * FROM library ORDER BY page_count DESC",
  "filter": {
    "range": {
      "page_count": {
        "gte" : 100,
        "lte" : 200
      }
    }
  },
  "fetch_size": 5
}
```



[SQL Translate API](https://www.elastic.co/guide/en/elasticsearch/reference/7.12/sql-translate.html)

```console
POST /_sql/translate
{
  "query": "SELECT * FROM library ORDER BY page_count DESC",
  "fetch_size": 10
}
```



## Spring data ElasticSearch

Spring Data Elasticsearch projects are intended to bring in the concepts of Spring Data repositories, enabling easy development of Elasticsearch repositories. They provide an abstraction layer on top of Elasticsearch to successfully store, retrieve, and modify documents available in the Elasticsearch transparently.

Spring Data Elasticsearch eases CRUD operations by allowing the ElasticsearchRepository interface, which extends from `ElasticsearchCrudRepository.` This hides the complexities of plain Elasticsearch implementations, which need to be implemented and tested by developers. 

### High Leavel REST Client

The spring recommand to use th [High Level REST Client](https://docs.spring.io/spring-data/elasticsearch/docs/4.2.0/reference/html/#elasticsearch.clients.rest)  instead of th TransportClient.

The Java High Level REST Client is the default client of Elasticsearch, which can support asychronous call  are operated upon a client managed thread pool and require a callback to be notified when the request is done.





## reference

[1]. https://www.elastic.co/guide/en/elasticsearch/reference/6.0/getting-started.html

[2]. https://www.tutorialspoint.com/elasticsearch/elasticsearch_basic_concepts.htm

[3]. https://www.elastic.co/guide/en/elasticsearch/reference/6.0/_basic_concepts.html

[4].https://www.elastic.co/guide/en/elasticsearch/reference/current/_mapping_concepts_across_sql_and_elasticsearch.html

[5].https://medium.com/hipages-engineering/scaling-elasticsearch-b63fa400ee9e

