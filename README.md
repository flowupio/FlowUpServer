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

## How to setup MySQL, elasticsearch, grafana and redis locally

`docker-compose up`

## How to execute the stress tests

`sbt gatling:test`

## How to execute just one test

`sbt "test-only *AnyPatterRelatedToTheClassName"`

An example based on this project could be:

`sbt "test-only *ElasticsearchClientTest"`

## How to work with Elasticsearch

Full documentation [here](./ELASTICSEARCH_TIPS.md).


## How to work with email templates

Full documentation [here](./MANDRILL_TIPS.md).

## Testing production

To run the FlowUp server using a configuration similar to the production one where the assets are going to be versioned and the javascript minified you can run the server using this command ``sbt testProd``. This will build the server artifact and will start the service using a production like build you can run locally.