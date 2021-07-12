**OPENING-HOURS API**

*How to run:*

* sbt run
* ```curl -X POST -H "Content-type: application/json"  http://localhost:9000/opening-hours -d '{"sunday":[{"type":"open","value":3600},{"type":"close","value":64800}],"monday":[{"type":"open","value":3600},{"type":"close","value":64800}],"tuesday":[{"type":"open","value":3600},{"type":"close","value":64800}],"wednesday":[{"type":"open","value":3600},{"type":"close","value":64800}],"thursday":[{"type":"open","value":3600},{"type":"close","value":64800}],"friday":[{"type":"open","value":3600},{"type":"close","value":64800},{"type":"open","value":68400}],"saturday":[{"type":"close","value":7200},{"type":"open","value":32400},{"type":"close","value":39600},{"type":"open","value":57600},{"type":"close","value":82800}]}'```



*Testing:*
* sbt test

**Part 2**

I found the initial model is pretty in days validation, for instance, if you make a typo in a day name, the request 
will be rejected. The same is related to state object ``{"type":"open","value":3600}`` as well. 
However, I found that approach to have an Open and Close time in different days is a slightly annoying,
especially from development perspective, requires extra work. The structure requires handling 
special cases. Even, if it is similar to a trivial balanced brackets problem. Although UNIX time is a good approach from 
parsing perspective, but a bit badly
readable if you need to look quickly through input json.  

I can propose a bit different structure
```json
  [
    {"day": 0, "times": [{"open": "1200", "close": "1830"}, {"open": "1900", "close": "2330"}]},
    {"day": 1, "times": [{"open": "1000", "close": "2030"}]},
    {"day": 2, "times": [{"open": "0900", "close": "2130"}]},
    {"day": 3, "times": [{"open": "0700", "close": "1530"}, {"open": "1900", "close": "2330"}]},
    {"day": 4, "times": [{"open": "0830", "close": "1215"}]},
    {"day": 5, "times": [{"open": "1200", "close": "1830"}]},
    {"day": 6, "times": [{"open": "1915", "close": "0100"}]}
  ]
```
Days: 
  * I would use numbers instead of strings. I think it is more generic, then we can map the numbers
  to days names in different languages.
    
Time periods:
  * From my point of view, it would be nice to keep open/close periods in the same object even if the restaurant opens 
    and closes not in the same days. The approach should simplify validation and coding efforts.
    
  * the object ```{"open": "1915", "close": "0100"}``` can be validated during decoding from json. 
    In case of initial model to make validation of open/close state during decoding  is much harder.
    
  * time format could be: 0000-2359. Perhaps it requires more efforts to handle the format, but in general the readability
    of the format is better for a human eye.
    


    



 