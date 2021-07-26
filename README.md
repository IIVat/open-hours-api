**OPENING-HOURS API**

*How to run:*

* sbt run
* ```curl -X POST -H "Content-type: application/json"  http://localhost:9000/opening-hours -d '{"sunday":[{"type":"open","value":3600},{"type":"close","value":64800}],"monday":[{"type":"open","value":3600},{"type":"close","value":64800}],"tuesday":[{"type":"open","value":3600},{"type":"close","value":64800}],"wednesday":[{"type":"open","value":3600},{"type":"close","value":64800}],"thursday":[{"type":"open","value":3600},{"type":"close","value":64800}],"friday":[{"type":"open","value":3600},{"type":"close","value":64800},{"type":"open","value":68400}],"saturday":[{"type":"close","value":7200},{"type":"open","value":32400},{"type":"close","value":39600},{"type":"open","value":57600},{"type":"close","value":82800}]}'```



*Testing:*
* sbt test
 