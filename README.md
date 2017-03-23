# FlowUpServer [![Build Status](https://travis-ci.com/Karumi/FlowUpServer.svg?token=Kb2RqPaWxFZ8XPxpqvqz&branch=master)](https://travis-ci.com/Karumi/FlowUpServer)

## How to deploy AWS ?

### tl;dr
If your tests are passing and branch merge to master your changes will be deploy automatically to AWS.

http://flowupapp-env.eu-west-1.elasticbeanstalk.com/

### Process

Travis is configured to automatically deploy the project to our AWS instances of any commits merge to master that are passing tests. 

Travis compiles and generates a zip using `sbt dist`.

When the zip is ready, travis upload it to a versioned bucket named `flowupserver-builds`

AWS Codepipeline is watching the bucket and will deploy to elastic beanstalk `flowup-app > flowupApp-env`

`flowupApp-env` will build the Dockerfile and will run binary generated by `sbt dist`.

Available at http://flowupapp-env.eu-west-1.elasticbeanstalk.com/

## How to setup elasticsearch locally

`docker run -p 127.0.0.1:9200:9200 -d elasticsearch:2.3`
Test your setup http://asquera.de/blog/2013-07-10/an-elasticsearch-workflow/

## How to setup MYSQL locally

`docker run -e MYSQL_ROOT_PASSWORD=my-secret-pw -e MYSQL_DATABASE=flowupdb -e MYSQL_USER=flowupUser -e MYSQL_PASSWORD=flowupPassword -p 127.0.0.1:3306:3306 -d mysql:5.6`

## How to setup grafana locally

`docker run -p 3000:3000 -d grafana/grafana`

##  How to setup redis locally
`docker run -p 127.0.0.1:6379:6379 -d redis:3.2`

## How to execute the stress tests

`sbt gatling:test`

## How to execute just one test

`sbt "test-only *AnyPatterRelatedToTheClassName"`

An example based on this project could be:

`sbt "test-only *ElasticsearchClientTest"`

## How to work with Elasticsearch

Full documentation [here](./ELASTICSEARCH_TIPS.md).


## How to work with email templates

Full documentation [here](./MAILCHIMP_TIPS.md).