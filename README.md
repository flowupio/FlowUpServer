# FlowUpServer

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
