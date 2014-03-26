mp4stream
===============

ABSTRACT

This project provides an example implementation of an MP4 file subdivider, enabling MP4 "pseudo-streaming" via an HTTP server.

BUILDING

This project uses Apache Maven as a build tool.  To build the project, run "mvn clean compile assembly:single".
The build results (assuming a successful build and test pass) may be found in target/mp4stream-1.0-jar-with-dependencies.jar



RUNNING

To use the utility, place mp4 files into the desired working directory, and then run "java -jar target/mp4stream-1.0-jar-with-dependencies.jar".
This will spawn an HTTP server on port 8080.  Access the server via an HTTP client, specifying the mp4 filename and the desired start
time in the URL.  

E.g. curl http://localhost:8080/test_data/simpsons.mp4?start=36

The resulting stream will be a subset of the original MP4 movie, starting as near to the specified starting time as possible.


