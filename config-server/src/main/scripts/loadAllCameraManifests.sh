#!/bin/sh -x

CAMERAS_MANIFEST=../../../allCamerasManifest.json

curl -vv -X POST -u admin:admin --data @${CAMERAS_MANIFEST} --header "Content-Type:application/json" http://localhost:8181/api/config/manifest/cameras