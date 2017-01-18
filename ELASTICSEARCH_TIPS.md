# Elasticsearch Tips

Do you need to work with Elasticsearch and you have no idea about how to use this database? Remember that Elasticsearch can be used through a REST API using simple curl commands. Here you have some tips:

**You can replace ``localhost`` with ``https://search-flowup-ui67d3e2wfxfwzmycdfxxkelfy.eu-west-1.es.amazonaws.com`` to access our production environment but you will need to launch an instance from Amazon EC2.**

### How to send a report to local instance of play:

`curl -XPOST http://localhost:9000/report -H 'Content-Type: application/json;charset=UTF-8' -H 'X-Api-Key: YOUR_API_KEY' -H 'X-UUID: 9c2a2994-b2b9-4297-81eb-231984ad056e' -d @test/resources/reportRequest.json`

You can start a FlowUp instance, register a user and get an API key before to send this request.

### List every index in the database:

`curl 'localhost:9200/_cat/indices?v'`

### List old documents:

`curl -v -XGET -d '{
  "fields" : ["_id"],
  "query": {
    "range": {
      "@timestamp": {
        "lte": "now-90d"
      }
    }
  }
}' 'localhost:9200/_search?size=1&pretty=true'`

### List old documents filtered by index or document type:

`curl -v -XGET -d '{
  "fields" : ["_id"],
  "query": {
    "range": {
      "@timestamp": {
        "lte": "now-90d"
      }
    }
  }
}' 'localhost:9200/INDEX_NAME/DOCUMENT_NAME/_search?size=1&pretty=true'`

### Delete documents using a bulk query:

`curl -v -XPOST -d '
{"delete": { "_index": "INDEX_NAME", "_type": "DOCUMENT", "_id": "ID"}}
{"delete": { "_index": "INDEX_NAME_2", "_type": "DOCUMENT_2", "_id": "ID_2"}}'
'localhost:9200/_bulk'`

### Bulk query using a json file as body:

`curl -v -XPOST --data-binary "@bulkJson" 'localhost:9200/_bulk'`

### Delete an index:

`curl -XDELETE 'localhost:9200/INDEX_NAME'`
