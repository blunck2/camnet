#!/bin/sh -x

CONFIG_FILE=../../../../allCamerasManifest.json

curl -vv -X POST -u admin:admin --data @${CONFIG_FILE} --header "Content-Type:application/json" http://localhost:8383/api/tracker/manifest/cameras
