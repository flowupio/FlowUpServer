#!/usr/bin/env bash

curl -XPUT "${ES_HOST:-localhost}:${ES_PORT:-9200}/_template/flowup-template" -d @es_template.json