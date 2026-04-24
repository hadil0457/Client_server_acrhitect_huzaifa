# Smart Campus Sensor & Room Management API

## Overview

This is a RESTful API built with JAX-RS (Jersey) for the university's Smart Campus initiative. It lets you manage rooms, sensors, and sensor readings across the campus. Everything is stored in memory using ConcurrentHashMaps (no database).

The API follows REST principles with proper resource hierarchy - rooms contain sensors, and sensors have historical readings accessible through sub-resources.

### Tech Stack
- Java 11
- JAX-RS (Jersey 2.41)
- Grizzly HTTP Server (embedded)
- Jackson for JSON serialization
- Maven for build management

## How to Build and Run

### Prerequisites
- Java 11 or higher installed
- Maven installed

### Steps

1. Clone the repository:
```bash
git clone <repo-url>
cd smartcampus-server
```

2. Build the project:
```bash
mvn clean package
```

3. Run the server:
```bash
mvn exec:java
```

Or run the shaded jar directly:
```bash
java -jar target/smartcampus-server-1.0.jar
```

The server will start on `http://localhost:8080/api/v1/`

## API Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | /api/v1/ | Discovery endpoint - API info and available resources |
| GET | /api/v1/rooms | List all rooms |
| POST | /api/v1/rooms | Create a new room |
| GET | /api/v1/rooms/{id} | Get a specific room |
| DELETE | /api/v1/rooms/{id} | Delete a room (must be empty) |
| GET | /api/v1/sensors | List all sensors (optional ?type= filter) |
| POST | /api/v1/sensors | Register a new sensor |
| GET | /api/v1/sensors/{id}/readings | Get all readings for a sensor |
| POST | /api/v1/sensors/{id}/readings | Add a new reading to a sensor |

## Sample curl Commands

### 1. Get API info (Discovery)
```bash
curl http://localhost:8080/api/v1/
```

### 2. Create a room
```bash
curl -X POST http://localhost:8080/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"LIB-301","name":"Library Quiet Study","capacity":50}'
```

### 3. Create a sensor in that room
```bash
curl -X POST http://localhost:8080/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-001","type":"Temperature","status":"ACTIVE","currentValue":22.5,"roomId":"LIB-301"}'
```

### 4. Get all sensors filtered by type
```bash
curl "http://localhost:8080/api/v1/sensors?type=Temperature"
```

### 5. Post a reading to a sensor
```bash
curl -X POST http://localhost:8080/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":24.3}'
```

### 6. Get readings for a sensor
```bash
curl http://localhost:8080/api/v1/sensors/TEMP-001/readings
```

### 7. Try deleting a room with sensors (will get 409)
```bash
curl -X DELETE http://localhost:8080/api/v1/rooms/LIB-301
```

## Report - Answers to Questions

### Part 1: Service Architecture & Setup

**Q: What is the default lifecycle of a JAX-RS Resource class? Is a new instance created for every request, or is it a singleton?**

By default, JAX-RS creates a new instance of each resource class for every single HTTP request that comes in. So if 100 requests hit `/api/v1/rooms` at the same time, theres 100 separate `RoomController` objects created. This is called "per-request" lifecycle.

This means you cant just store your data as instance variables on the resource class because each new request gets a fresh object and all the data from the previous request would be gone. Thats why I used `CampusData` which is a utility class with static fields and ConcurrentHashMaps - the data lives at the class level so its shared across all requests. ConcurrentHashMap is thread-safe by design so even if multiple requests try to read and write at the same time, you wont get weird race conditions or corrupted data.

**Q: Why is "Hypermedia" (HATEOAS) important in RESTful design?**

HATEOAS means that the API responses include links telling the client what they can do next and where to find related resources. My discovery endpoint at `GET /api/v1/` does something like this by providing a map of available resource URLs.

This is useful because client developers dont need to hardcode URLs or constantly refer to documentation. The API basically guides them through whats available. If we ever change a URL structure, clients that follow the links dynamically would still work instead of breaking. Its like the difference between a website where you click links to navigate vs having to memorize every URL.

### Part 2: Room Management

**Q: What are the implications of returning only IDs vs full room objects when listing rooms?**

Returning just IDs means less data transferred over the network which is faster, especially if you have thousands of rooms. But then the client has to make a separate GET request for each room it actually wants details about, which means way more network calls overall.

Returning full objects is more bandwidth heavy upfront but the client gets everything in one shot. For this API I return full objects because the room data isnt that big (just id, name, capacity and a list of sensor IDs) so the payload is still pretty small. If rooms had massive amounts of data attached to them, maybe just returning IDs with a pagination approach would make more sense.

**Q: Is DELETE idempotent in your implementation?**

Yes, mostly. The first DELETE request removes the room and returns 204 No Content. If the same DELETE is sent again for the same room ID, the room no longer exists so it returns 404 Not Found. The important thing is that the server state is the same after the second call as it was after the first call - the room is still gone. The different status code doesnt break idempotency because idempotency is about the server state not the response code. Sending the same DELETE 10 times has the same effect as sending it once.

### Part 3: Sensor Operations & Linking

**Q: What happens if a client sends data in a format other than JSON when @Consumes(APPLICATION_JSON) is used?**

JAX-RS will automatically reject the request with a `415 Unsupported Media Type` status code before the method even gets called. This is handled at the framework level by checking the `Content-Type` header of the incoming request against the media types in the `@Consumes` annotation. So if someone sends `text/plain` or `application/xml`, Jersey intercepts it and sends back a 415 without our code needing to do anything. This is nice because it means we dont have to manually validate the content type in every POST method.

**Q: Why is @QueryParam better than using the URL path for filtering (e.g. /sensors?type=CO2 vs /sensors/type/CO2)?**

Query parameters are the standard way to do filtering and searching in REST APIs. The path `/api/v1/sensors` represents the sensors collection - thats the resource. Adding `?type=CO2` is a modifier that says "give me this collection but filtered". The resource identity stays the same.

If you put the filter in the path like `/sensors/type/CO2`, it looks like `type` and `CO2` are their own resources in the hierarchy, which they arent. Also, query params are optional by nature - you can call `/sensors` with or without `?type=`. With path-based filtering, youd need separate route handlers for `/sensors` and `/sensors/type/{value}` which is messier. Query params also let you easily combine multiple filters like `?type=CO2&status=ACTIVE` which would get really complicated as nested path segments.

### Part 4: Deep Nesting with Sub-Resources

**Q: What are the benefits of the Sub-Resource Locator pattern?**

The sub-resource locator lets you split up the code so each class handles its own thing. Instead of having one massive controller that handles `/sensors`, `/sensors/{id}/readings`, and `/sensors/{id}/readings/{rid}` all in one file, you delegate the `/readings` stuff to a separate `ReadingController` class.

This keeps each class focused and smaller, which makes it easier to read and maintain. If the readings logic gets complex (like adding pagination, date range filtering etc), you can change `ReadingController` without touching `SensorController` at all. Its basically the single responsibility principle in action. Also, the sub-resource locator method is a good place to do validation (like checking the sensor exists) before handing off to the sub-resource.

### Part 5: Error Handling & Logging

**Q: Why is HTTP 422 more accurate than 404 for a missing reference in a JSON payload?**

404 means the resource you are trying to access doesnt exist - like if you did `GET /rooms/FAKE-ROOM` and that room wasnt there. But when you POST a new sensor with `"roomId": "FAKE-ROOM"` in the body, youre not trying to access the room endpoint. Youre trying to create a sensor and one of the fields inside the valid JSON references something that doesnt exist.

422 Unprocessable Entity is better because it says "I understood your request and the JSON is syntactically fine, but I cant process it because the content is semantically invalid". The request URL was correct (`/sensors`), the JSON was valid, but the data inside had a problem. 404 would be confusing because the client might think the `/sensors` endpoint itself doesnt exist.

**Q: What are the security risks of exposing Java stack traces to API consumers?**

Exposing stack traces is really bad for security. An attacker could see internal package names and class names which reveals your project structure. They could see which libraries and versions youre using, and then look up known vulnerabilities for those specific versions. File paths in the trace could reveal your operating system and directory structure. Exception messages might leak database schema info, internal IP addresses, or configuration details.

Thats why I have a catch-all `ExceptionMapper<Throwable>` that intercepts any unhandled exception and returns a generic "something went wrong" message. The actual stack trace only gets logged server-side where only developers can see it.

**Q: Why use JAX-RS filters for logging instead of putting Logger.info() in every method?**

Using filters means you write the logging logic once and it automatically applies to every single request and response across the entire API. If you put log statements in each controller method, you have to remember to add them every time you create a new endpoint, and if you want to change the log format you have to update it everywhere.

Filters are also a "cross-cutting concern" - logging isnt really related to the business logic of managing rooms or sensors. Keeping it separate in a filter class means your controller code stays clean and focused on what it actually does. Plus filters run even if the request fails before hitting your controller (like a 415 from a wrong content type), so you get more complete logging.

#   C l i e n t - S e r v e r - A r c h i t e c t u r e - C W 1  
 #   C l i e n t - S e r v e r - A r c h i t e c t  
 #   C l i e n t - S e r v e r - A r c h i t e c t  
 #   C l i e n t - S e r v e r - A r c h i t e c t  
 #   r e p o  
 #   r e p o 1 2  
 