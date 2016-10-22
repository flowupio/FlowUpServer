#!/usr/bin/env bash

curl -XPUT "${ES_HOST:-localhost}:${ES_PORT:-9200}/_template/flowup-template" -d '
{
    "template" : "flowup-*",
    "mappings" : {
        "network_data" : {
            "_source" : { "enabled" : true },
            "properties": {
                "@timestamp": {
                    "type": "date"
                },
                "VersionName": {
                    "type": "string",
                    "index": "not_analyzed"
                },
                "AndroidOSVersion": {
                    "type": "string",
                    "index": "not_analyzed"
                },
                "BatterySaverOn": {
                    "type": "string",
                    "index": "not_analyzed"
                },
                "AppPackage": {
                    "type": "string",
                    "index": "not_analyzed"
                },
                "DeviceModel": {
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
                },
                "InstallationUUID": {
                    "type": "string",
                    "index": "not_analyzed"
                },
                "NumberOfCores": {
                    "type": "string",
                    "index": "not_analyzed"
                }
            }
        },
        "ui_data" : {
            "_source" : { "enabled" : true },
            "properties": {
                "@timestamp": {
                    "type": "date"
                },
                "VersionName": {
                    "type": "string",
                    "index": "not_analyzed"
                },
                "AndroidOSVersion": {
                    "type": "string",
                    "index": "not_analyzed"
                },
                "BatterySaverOn": {
                    "type": "string",
                    "index": "not_analyzed"
                },
                "AppPackage": {
                    "type": "string",
                    "index": "not_analyzed"
                },
                "DeviceModel": {
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
                },
                "InstallationUUID": {
                    "type": "string",
                    "index": "not_analyzed"
                },
                "NumberOfCores": {
                    "type": "string",
                    "index": "not_analyzed"
                }
            }
        },
        "cpu_data" : {
            "_source" : { "enabled" : true },
            "properties": {
                "@timestamp": {
                    "type": "date"
                },
                "VersionName": {
                    "type": "string",
                    "index": "not_analyzed"
                },
                "AndroidOSVersion": {
                    "type": "string",
                    "index": "not_analyzed"
                },
                "BatterySaverOn": {
                    "type": "string",
                    "index": "not_analyzed"
                },
                "AppPackage": {
                    "type": "string",
                    "index": "not_analyzed"
                },
                "DeviceModel": {
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
                },
                "InstallationUUID": {
                    "type": "string",
                    "index": "not_analyzed"
                },
                "NumberOfCores": {
                    "type": "string",
                    "index": "not_analyzed"
                }
            }
        },
        "gpu_data" : {
            "_source" : { "enabled" : true },
            "properties": {
                "@timestamp": {
                    "type": "date"
                },
                "VersionName": {
                    "type": "string",
                    "index": "not_analyzed"
                },
                "AndroidOSVersion": {
                    "type": "string",
                    "index": "not_analyzed"
                },
                "BatterySaverOn": {
                    "type": "string",
                    "index": "not_analyzed"
                },
                "AppPackage": {
                    "type": "string",
                    "index": "not_analyzed"
                },
                "DeviceModel": {
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
                },
                "InstallationUUID": {
                    "type": "string",
                    "index": "not_analyzed"
                },
                "NumberOfCores": {
                    "type": "string",
                    "index": "not_analyzed"
                }
            }
        },
        "memory_data" : {
            "_source" : { "enabled" : true },
            "properties": {
                "@timestamp": {
                    "type": "date"
                },
                "VersionName": {
                    "type": "string",
                    "index": "not_analyzed"
                },
                "AndroidOSVersion": {
                    "type": "string",
                    "index": "not_analyzed"
                },
                "BatterySaverOn": {
                    "type": "string",
                    "index": "not_analyzed"
                },
                "AppPackage": {
                    "type": "string",
                    "index": "not_analyzed"
                },
                "DeviceModel": {
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
                },
                "InstallationUUID": {
                    "type": "string",
                    "index": "not_analyzed"
                },
                "NumberOfCores": {
                    "type": "string",
                    "index": "not_analyzed"
                }
            }
        },
        "disk_data" : {
            "_source" : { "enabled" : true },
            "properties": {
                "@timestamp": {
                    "type": "date"
                },
                "VersionName": {
                    "type": "string",
                    "index": "not_analyzed"
                },
                "AndroidOSVersion": {
                    "type": "string",
                    "index": "not_analyzed"
                },
                "BatterySaverOn": {
                    "type": "string",
                    "index": "not_analyzed"
                },
                "AppPackage": {
                    "type": "string",
                    "index": "not_analyzed"
                },
                "DeviceModel": {
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
                },
                "InstallationUUID": {
                    "type": "string",
                    "index": "not_analyzed"
                },
                "NumberOfCores": {
                    "type": "string",
                    "index": "not_analyzed"
                }
            }
        }
    }
}'