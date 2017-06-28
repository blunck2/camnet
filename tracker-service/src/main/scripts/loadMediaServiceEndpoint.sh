#!/bin/sh -x

CAMERAS_MANIFEST=../resources/config/mediaServiceEndpoint.json

curl -vv -X POST -u admin:admin --data @${CAMERAS_MANIFEST} --header "Content-Type:application/json" http://localhost:8383/api/tracker/server/endpoint
