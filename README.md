# Smart Campus - Sensor & Room Management API

A JAX-RS RESTful API for managing campus rooms and IoT sensors.
Built with **Jersey 2.40.0** on **Apache Tomcat 9**.

---

## Base URL

```
http://localhost:8080/Smart-Campus-API/api/v1
```

> The context path `/Smart-Campus-API` is the WAR filename set in `pom.xml` (`<finalName>`).
> If Tomcat runs on a different port, replace `8080` accordingly.

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/` | Discovery — API metadata and resource links |
| GET | `/rooms` | List all rooms |
| POST | `/rooms` | Create a room |
| GET | `/rooms/{id}` | Get room by ID |
| DELETE | `/rooms/{id}` | Delete room (409 if sensors still assigned) |
| GET | `/sensors` | List all sensors (optional `?type=` filter) |
| POST | `/sensors` | Register a sensor (422 if roomId not found) |
| GET | `/sensors/{id}` | Get sensor by ID |
| GET | `/sensors/{id}/readings` | Get all readings for a sensor |
| POST | `/sensors/{id}/readings` | Add a reading (403 if sensor is MAINTENANCE) |

---

## Tech Stack

| Component | Version | Notes |
|-----------|---------|-------|
| Java | 17 | JDK 17 |
| Jersey | 2.40.0 | JAX-RS 2.1, `javax.ws.rs` namespace |
| Jackson | Bundled with Jersey | JSON serialisation |
| Servlet API | 4.0.1 | `javax.servlet` namespace — provided by Tomcat 9 |
| Apache Tomcat | 9.0.117 | **Not** Tomcat 10+ (which requires `jakarta.*`) |
| Maven | 3.8+ | Build tool |

> **Tomcat 9 vs Tomcat 10 note:**
> Tomcat 9 uses the `javax.*` namespace (Java EE 8). Tomcat 10+ migrated to the `jakarta.*` namespace (Jakarta EE 9+).
> This project uses Jersey 2.x which targets `javax.*`, making it compatible with Tomcat 9 only.
> If you upgrade to Tomcat 10+, you must also switch to Jersey 3.x and change all `javax.ws.rs` imports to `jakarta.ws.rs`.

---

## How to Build and Run

### Prerequisites
- JDK 17 installed
- Apache Tomcat 9.0.x installed and configured in NetBeans
- NetBeans 18+ (or any Maven-capable IDE)
- Internet connection (first build downloads Jersey jars ~10 MB)

### Steps in NetBeans

1. **Open project:** File → Open Project → select the `SmartCampus-tomcat9` folder
2. **Add Tomcat 9:** Tools → Servers → Add Server → Apache Tomcat 9 → point to your Tomcat install dir
3. **Set server for project:** Right-click project → Properties → Run → Server → select Apache Tomcat 9
4. **Clean and Build:** Right-click project → Clean and Build
5. **Run:** Right-click project → Run

The API deploys to: `http://localhost:8080/Smart-Campus-API/api/v1/`

### Manual Maven Build

```bash
mvn clean package
copy target\Smart-Campus-API.war C:\path\to\apache-tomcat-9.0.117\webapps\
# Then start Tomcat: bin\startup.bat
```

---

## Sample curl Commands

### 1. Discovery
```bash
curl -X GET http://localhost:8080/Smart-Campus-API/api/v1/
```

### 2. Create a Room
```bash
curl -X POST http://localhost:8080/Smart-Campus-API/api/v1/rooms ^
  -H "Content-Type: application/json" ^
  -d "{\"id\":\"1\",\"name\":\"Lab A\",\"capacity\":40,\"sensorIds\":[]}"
```

### 3. Get All Rooms
```bash
curl -X GET http://localhost:8080/Smart-Campus-API/api/v1/rooms
```

### 4. Create a Sensor (valid roomId)
```bash
curl -X POST http://localhost:8080/Smart-Campus-API/api/v1/sensors ^
  -H "Content-Type: application/json" ^
  -d "{\"id\":\"S1\",\"type\":\"temperature\",\"status\":\"ACTIVE\",\"currentValue\":0,\"roomId\":\"1\"}"
```

### 5. Get Sensors Filtered by Type
```bash
curl -X GET "http://localhost:8080/Smart-Campus-API/api/v1/sensors?type=temperature"
```

### 6. Post a Sensor Reading
```bash
curl -X POST http://localhost:8080/Smart-Campus-API/api/v1/sensors/S1/readings ^
  -H "Content-Type: application/json" ^
  -d "{\"id\":\"R1\",\"timestamp\":1713780000,\"value\":26.5}"
```

### 7. Get All Readings for a Sensor
```bash
curl -X GET http://localhost:8080/Smart-Campus-API/api/v1/sensors/S1/readings
```

### 8. Try Sensor with Invalid Room (expect 422)
```bash
curl -X POST http://localhost:8080/Smart-Campus-API/api/v1/sensors ^
  -H "Content-Type: application/json" ^
  -d "{\"id\":\"S2\",\"type\":\"humidity\",\"status\":\"ACTIVE\",\"currentValue\":0,\"roomId\":\"999\"}"
```

### 9. Create Maintenance Sensor then Try Reading (expect 403)
```bash
curl -X POST http://localhost:8080/Smart-Campus-API/api/v1/sensors ^
  -H "Content-Type: application/json" ^
  -d "{\"id\":\"S3\",\"type\":\"temperature\",\"status\":\"MAINTENANCE\",\"currentValue\":0,\"roomId\":\"1\"}"

curl -X POST http://localhost:8080/Smart-Campus-API/api/v1/sensors/S3/readings ^
  -H "Content-Type: application/json" ^
  -d "{\"id\":\"R2\",\"timestamp\":1713780000,\"value\":30.2}"
```

### 10. Delete Room with Sensors (expect 409 Conflict)
```bash
curl -X DELETE http://localhost:8080/Smart-Campus-API/api/v1/rooms/1
```

---

## Postman Testing Order

| Step | Method | URL | Body |
|------|--------|-----|------|
| 1 | POST | `/rooms` | `{"id":"1","name":"Lab A","capacity":40,"sensorIds":[]}` |
| 2 | GET | `/rooms` | — |
| 3 | GET | `/rooms/1` | — |
| 4 | POST | `/sensors` | `{"id":"S1","type":"temperature","status":"ACTIVE","currentValue":0,"roomId":"1"}` |
| 5 | GET | `/sensors` | — |
| 6 | GET | `/sensors?type=temperature` | — |
| 7 | POST | `/sensors/S1/readings` | `{"id":"R1","timestamp":1713780000,"value":26.5}` |
| 8 | GET | `/sensors/S1/readings` | — |
| 9 | POST | `/sensors` | `{"id":"S2","type":"humidity","status":"ACTIVE","currentValue":0,"roomId":"999"}` — expect **422** |
| 10 | POST | `/sensors` | `{"id":"S3","type":"temperature","status":"MAINTENANCE","currentValue":0,"roomId":"1"}` |
| 11 | POST | `/sensors/S3/readings` | `{"id":"R2","timestamp":1713780000,"value":30.2}` — expect **403** |
| 12 | DELETE | `/rooms/1` | — expect **409** (sensors still assigned) |

---

## Report — Answers to Coursework Questions

### Part 1.1 — JAX-RS Resource Lifecycle

By default, JAX-RS creates a **new instance of each resource class per HTTP request** (per-request scope). This is not a singleton. Because a fresh object is created for every request, instance variables on the resource class itself are not shared between requests and would be lost immediately after the response is sent.

This directly impacts in-memory data management: data must not be stored as instance fields on the resource class. Instead, a separate **singleton data store** (`DataStore.java` using `ConcurrentHashMap`) is used. The singleton is shared across all request instances. `ConcurrentHashMap` is thread-safe, preventing race conditions when multiple requests read and write simultaneously, avoiding data corruption or loss.

### Part 1.2 — HATEOAS

HATEOAS (Hypermedia As The Engine Of Application State) means API responses include links to related actions and resources, not just data. For example, a GET /rooms response might include a `_links.sensors` field pointing to `/api/v1/sensors?roomId=X`.

This benefits client developers by making the API **self-discoverable** — clients do not need to hardcode URLs or memorise documentation. They navigate the API dynamically by following links in responses, similar to how a browser follows HTML hyperlinks. This decouples the client from server URL structure, making it easier to evolve the API without breaking clients.

### Part 2.1 — Returning IDs vs Full Objects

Returning **only IDs** reduces bandwidth (small payload) but forces the client to make N additional requests to fetch each room's details — the "N+1 problem". This increases latency and server load. Returning **full objects** increases response size but eliminates follow-up requests. Best practice is to return full objects for moderate-sized collections (as done here), and provide pagination or sparse fieldsets for very large datasets.

### Part 2.2 — DELETE Idempotency

The first DELETE on an existing room removes it and returns `200 OK`. A second DELETE on the same ID returns `404 Not Found`. REST idempotency means repeated calls produce the same **server state** — and they do (room is gone either way). The status code difference (200 vs 404) is acceptable and more informative to the client.

### Part 3.1 — @Consumes(APPLICATION_JSON)

If a client sends `Content-Type: text/plain` or `application/xml` to a method annotated `@Consumes(MediaType.APPLICATION_JSON)`, JAX-RS automatically returns **HTTP 415 Unsupported Media Type** without invoking the resource method. This protects the method from unexpected data formats without any manual content-type checking in business logic.

### Part 3.2 — @QueryParam vs Path Segment for Filtering

Using `?type=CO2` is semantically correct because filtering is not identifying a unique resource — it is narrowing a collection. Path segments (e.g., `/sensors/type/CO2`) imply a distinct addressable resource at that URI, which is incorrect for a filter. Query parameters are optional by design, composable (e.g., `?type=CO2&status=ACTIVE`), and do not pollute the resource hierarchy.

### Part 4.1 — Sub-Resource Locator Pattern

The sub-resource locator pattern delegates a URL subtree to a separate class. `SensorResource` handles `/sensors` CRUD while `SensorReadingResource` handles all logic for `/sensors/{id}/readings`. This avoids a "god class" with dozens of methods. Each resource class has a single responsibility, making it easier to test, maintain, and extend independently.

### Part 5.2 — HTTP 422 vs 404 for Missing roomId Reference

When a client POSTs a sensor with a `roomId` that does not exist, the request is **syntactically valid JSON** and the URI is correct — so 404 is misleading. The problem is a **semantic validation failure** inside the payload. HTTP 422 Unprocessable Entity signals that the server understood the request format but could not process it due to a logical error in the data, giving API consumers a clearer signal to fix their payload, not their URL.

### Part 5.4 — Security Risk of Exposing Stack Traces

Exposing stack traces to external API consumers is a significant security risk: (1) **Package and class names** reveal internal architecture, (2) **File paths and line numbers** expose source code structure for targeted exploits, (3) **Library versions** in stack frames allow attackers to look up CVEs for exact versions, (4) **Exception messages** often contain table names, query fragments, or internal IDs that reveal backend data models. The `GlobalExceptionMapper` in this project intercepts all unhandled exceptions, logs the full trace server-side only, and returns a generic `500` message to the client.

### Part 5.5 — JAX-RS Filters for Cross-Cutting Concerns

Using a `ContainerRequestFilter` / `ContainerResponseFilter` for logging is superior to `Logger.info()` inside every resource method because: (1) **DRY principle** — logging logic defined once, (2) **Separation of concerns** — resource methods focus on business logic, (3) **Consistency** — every request/response is logged uniformly, (4) **Maintainability** — changing log format requires editing one class, (5) Filters can be registered/deregistered globally without touching business code.
