To run server:
  Compile and Run:
    mvn clean install
    java -jar target/camsink-0.0.1.jar
  Load Configuration
    cp src/main/resources/config/allCamerasManifest.json .
    vi allCamerasManifest.json
    // add usernames/passwords where appropriate
    cd src/main/scripts
    ./loadCameraManifest.sh


To post:
  curl  -F "file=@/Users/chris/command.txt" -F "id=cam1" http://localhost:8080/api/ingest

To get camera manifest:
  curl http://localhost:8080/api/manifest/cameras/

To update a camera manifest edit a file named camera.json to look like this:
{"id":"wells_driveway","fileName":"wells-driveway.jpg","houseName":"Wells","cameraName":"Wells Driveway","sleepTimeInSeconds":9000}

and then:
  curl -X POST -H "Content-Type: application/json"  -d @camera.json http://localhost:8080/api/manifest/cameras

  To post a new image for a camera:
  curl  -X POST -F "file=@camera.jpg" http://localhost:8080/api/image/ingest/{camera_id}
