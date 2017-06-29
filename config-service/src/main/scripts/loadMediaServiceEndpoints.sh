#!/bin/sh -x

CONFIG_FILE=../resources/config/mediaServiceEndpoints.json

curl -vv -X POST -u admin:admin --data @${CONFIG_FILE} --header "Content-Type:application/json" http://localhost:8181/api/config/media/endpoints
