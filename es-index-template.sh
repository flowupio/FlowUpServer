#!/usr/bin/env bash

curl -XPUT "${ES_HOST:-localhost}:${ES_PORT:-9200}/_template/flowup-template" -d @es_template.json

curl -XPUT "${ES_HOST:-localhost}:${ES_PORT:-9200}/_template/installations-counter-template" -d @es_installations_counter_template.json