#!/bin/sh

ALL_CAMERAS_MANIFEST=../../../allCamerasManifest.json

curl -X POST --data @${ALL_CAMERAS_MANIFEST} --header "Content-Type:application/json" http://localhost:8181/api/manifest/cameras