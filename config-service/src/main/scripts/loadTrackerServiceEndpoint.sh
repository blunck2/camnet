#!/bin/sh -x

CONFIG_FILE=../resources/config/trackerServiceEndpoint.json

curl -vv -X POST -u admin:admin --data @${CONFIG_FILE} --header "Content-Type:application/json" http://localhost:8181/api/config/tracker/endpoint
