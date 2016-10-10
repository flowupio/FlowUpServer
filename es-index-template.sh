#!/usr/bin/env bash

curl -XDELETE "${ES_HOST:-localhost}:${ES_PORT:-9200}/_template/statsd-template"
curl -XDELETE "${ES_HOST:-localhost}:${ES_PORT:-9200}/statsd-network_data"

curl -XPUT "${ES_HOST:-localhost}:${ES_PORT:-9200}/_template/statsd-template" -d '
{
    "template" : "statsd-*",
    "settings" : {
        "number_of_shards" : 1
    },
    "mappings" : {
        "counter" : {
            "_source" : { "enabled" : true },
            "properties": {
                "@timestamp": {
                    "type": "date"
                },
                "AndroidOSVersion": {
                    "type": "string",
                    "index": "not_analyzed"
                },
                "AppPackage": {
                    "type": "string",
                    "index": "not_analyzed"
                },
                "BatterySaverOn": {
                    "type": "string",
                    "index": "not_analyzed"
                },
                "DeviceModel": {
                    "type": "string",
                    "index": "not_analyzed"
                },
                "InstallationUUID": {
                    "type": "string",
                    "index": "not_analyzed"
                },
                "NumberOfCores": {
                    "type": "string",
                    "index": "not_analyzed"
                },
                "ScreenDensity": {
                    "type": "string",
                    "index": "not_analyzed"
                },
                "ScreenSize": {
                    "type": "string",
                    "index": "not_analyzed"
                }
            }
        }
    }
}'