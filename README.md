# Smart Campus Sensor & Room Management API

## Overview

This project implements a RESTful API for the university's **Smart Campus** initiative using **JAX-RS (Jersey)**. The API manages campus **Rooms**, the **Sensors** deployed within them, and the historical **Sensor Readings** they produce. All data is stored in-memory using thread-safe Java data structures (`ConcurrentHashMap`, `ArrayList`) — no database is used.

The API follows RESTful architectural principles including proper HTTP methods, meaningful status codes, JSON responses, resource nesting via sub-resource locators, custom exception mapping, and request/response logging.

---

## Technology Stack

| Component        | Technology                                  |
|------------------|---------------------------------------------|
| Language         | Java 8+                                     |
| Framework        | JAX-RS 2.1 (Jersey – bundled with GlassFish)|
| Build Tool       | Apache Maven                                |
| Server           | GlassFish 5 / Payara 5 (bundled with NetBeans) |
| Data Storage     | In-memory (`ConcurrentHashMap`, `ArrayList`) |
| IDE              | Apache NetBeans                             |
| JSON Provider    | MOXy (bundled with Jersey / GlassFish)      |

---

## Project Structure

```
smart-campus-api/
├── pom.xml
├── nb-configuration.xml
├── README.md
└── src/main/
    ├── java/com/smartcampus/
    │   ├── config/
    │   │   └── ApplicationConfig.java           # @ApplicationPath("/api/v1")
    │   ├── model/
    │   │   ├── Room.java                        # Room POJO
    │   │   ├── Sensor.java                      # Sensor POJO
    │   │   └── SensorReading.java               # SensorReading POJO
    │   ├── dto/
    │   │   └── ErrorResponse.java               # Standardised error JSON body
    │   ├── store/
    │   │   └── DataStore.java                   # Central in-memory data store
    │   ├── resource/
    │   │   ├── DiscoveryResource.java           # GET /api/v1
    │   │   ├── RoomResource.java                # /api/v1/rooms
    │   │   ├── SensorResource.java              # /api/v1/sensors
    │   │   └── SensorReadingResource.java       # /api/v1/sensors/{id}/readings
    │   ├── exception/
    │   │   ├── RoomNotEmptyException.java       # → 409 Conflict
    │   │   ├── LinkedResourceNotFoundException.java  # → 422 Unprocessable Entity
    │   │   └── SensorUnavailableException.java  # → 403 Forbidden
    │   ├── mapper/
    │   │   ├── RoomNotEmptyExceptionMapper.java
    │   │   ├── LinkedResourceNotFoundExceptionMapper.java
    │   │   ├── SensorUnavailableExceptionMapper.java
    │   │   └── GenericExceptionMapper.java      # Catch-all → 500
    │   └── filter/
    │       └── LoggingFilter.java               # Request/Response logging
    └── webapp/WEB-INF/
        └── web.xml
```

---

## How to Build and Run in NetBeans

### Step 1 — Open the Project
1. Open **Apache NetBeans IDE**.
2. Go to **File → Open Project**.
3. Navigate to the `smart-campus-api` folder (the one containing `pom.xml`) and select it.
4. NetBeans will recognise it as a Maven project automatically.

### Step 2 — Configure the Server
1. Ensure **GlassFish Server 5** is registered: **Tools → Servers → Add Server → GlassFish**.
2. Right-click the project → **Properties → Run** → set the server to **GlassFish Server**.

### Step 3 — Build
1. Right-click the project → **Clean and Build**.
2. Maven will download the `javaee-web-api` dependency and compile all classes.
3. The Output window should show `BUILD SUCCESS`.

### Step 4 — Run
1. Right-click the project → **Run**.
2. GlassFish will start and deploy the WAR file.
3. The API is now live at:

```
http://localhost:8080/smart-campus-api/api/v1
```

### Step 5 — Test
Open **Postman** or a browser and send a GET request to the URL above. You should see the JSON discovery response.

---

## API Endpoints

| Method | Endpoint                                | Description                              |
|--------|-----------------------------------------|------------------------------------------|
| GET    | `/api/v1`                               | API discovery – metadata and links       |
| GET    | `/api/v1/rooms`                         | List all rooms                           |
| POST   | `/api/v1/rooms`                         | Create a new room                        |
| GET    | `/api/v1/rooms/{roomId}`                | Get a specific room by ID                |
| DELETE | `/api/v1/rooms/{roomId}`                | Delete a room (must have no sensors)     |
| GET    | `/api/v1/sensors`                       | List all sensors (optional `?type=` filter) |
| POST   | `/api/v1/sensors`                       | Register a new sensor (roomId must exist)|
| GET    | `/api/v1/sensors/{sensorId}/readings`   | Get reading history for a sensor         |
| POST   | `/api/v1/sensors/{sensorId}/readings`   | Post a new reading (updates currentValue)|

---

## Sample curl Commands

### 1. API Discovery
```bash
curl -X GET http://localhost:8080/smart-campus-api/api/v1
```

### 2. Create a Room
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id": "LIB-301", "name": "Library Quiet Study", "capacity": 50}'
```

### 3. Get All Rooms
```bash
curl -X GET http://localhost:8080/smart-campus-api/api/v1/rooms
```

### 4. Create a Sensor (linked to room LIB-301)
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id": "TEMP-001", "type": "Temperature", "status": "ACTIVE", "currentValue": 0.0, "roomId": "LIB-301"}'
```

### 5. Get Sensors Filtered by Type
```bash
curl -X GET "http://localhost:8080/smart-campus-api/api/v1/sensors?type=Temperature"
```

### 6. Post a Sensor Reading
```bash
curl -X POST http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value": 22.5}'
```

### 7. Get Sensor Reading History
```bash
curl -X GET http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-001/readings
```

### 8. Attempt to Delete a Room That Has Sensors (triggers 409 Conflict)
```bash
curl -X DELETE http://localhost:8080/smart-campus-api/api/v1/rooms/LIB-301
```

---

## Report — Answers to Coursework Questions

---

### Part 1: Service Architecture & Setup

#### Task 1.1 — Question: Default lifecycle of a JAX-RS Resource class

> *Explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronise your in-memory data structures to prevent data loss or race conditions.*

By default, JAX-RS resource classes follow a **per-request lifecycle**. This means the JAX-RS runtime (Jersey, in our case) creates a **new instance** of the resource class for every incoming HTTP request. Once the request is processed and the response is sent, the instance is discarded and eligible for garbage collection. The runtime does **not** treat it as a singleton unless explicitly annotated with `@Singleton`.

This lifecycle decision has a direct impact on in-memory data management. Because each request gets a fresh resource instance, any data stored in **instance fields** would be lost the moment the request finishes. If we stored our rooms or sensors in an instance variable of `RoomResource`, the data would vanish after a single request and the next request would see an empty collection.

To solve this, our implementation uses a **centralised `DataStore` class with `static` fields** backed by `ConcurrentHashMap` and `ArrayList`. Static fields belong to the **class itself**, not to any particular instance, so they persist for the entire lifetime of the deployed application regardless of how many resource instances are created and destroyed.

We chose `ConcurrentHashMap` specifically for **thread safety**. Because the JAX-RS runtime creates separate resource instances on separate threads for concurrent requests, multiple threads can attempt to read and write the shared data simultaneously. `ConcurrentHashMap` provides built-in atomic operations (`put`, `get`, `remove`, `computeIfAbsent`) that prevent race conditions, data corruption, and lost updates without requiring explicit `synchronized` blocks. This makes the code both safe and simple.

---

#### Task 1.2 — Question: Hypermedia and HATEOAS

> *Why is the provision of "Hypermedia" (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?*

HATEOAS (Hypermedia as the Engine of Application State) represents Level 3 of the **Richardson Maturity Model**, the highest level of REST maturity. It means that API responses include **navigational links** that tell the client what actions or resources are available next, without the client needing prior knowledge of URL structures.

Our `/api/v1` discovery endpoint demonstrates this by returning a `"links"` object that points to `/api/v1/rooms` and `/api/v1/sensors`. A client can start at the root and discover the entire API surface by following these links.

The benefits compared to static documentation are:

1. **Discoverability**: A client can explore the API by following links from the root endpoint, similar to how a human browses a website by clicking hyperlinks. No prior URL knowledge is needed.

2. **Reduced Coupling**: Clients do not hard-code URL paths. If the server restructures its URLs (e.g., migrating from `/api/v1/rooms` to `/api/v2/rooms`), clients that follow links dynamically will continue working without any code changes.

3. **Self-Documentation**: The API is partially self-documenting at runtime. Developers exploring the API can examine the links in each response to understand what operations are available, reducing dependence on external PDF or Swagger documentation that may become outdated.

4. **Contextual Navigation**: Links can be conditional — they reflect the current state of the resource. For example, a future enhancement could only show a "delete" link if the room has no sensors. This guides clients toward valid operations and reduces errors.

---

### Part 2: Room Management

#### Task 2.1 — Question: Returning IDs versus full objects

> *When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client-side processing.*

**Returning only IDs** has the advantage of a small response payload, which conserves network bandwidth. This matters when the collection is very large (thousands of rooms). However, it forces the client into the **N+1 problem**: after receiving the list of IDs, the client must make a separate HTTP request for each ID to fetch the actual room data. For 100 rooms, that means 100 additional round-trips, each with its own network latency, TCP overhead, and server processing cost. The total time and resource consumption are typically far worse than a single larger response.

**Returning full objects** (as our implementation does) provides the client with all necessary information in a **single request**. The client can immediately render or process the data without additional network calls. The trade-off is a larger payload, but for a campus with a manageable number of rooms (hundreds, not millions), this is the optimal approach.

In a production system at scale, a common middle ground is to support:
- **Pagination**: `GET /rooms?page=1&size=20` to limit response size.
- **Field selection**: `GET /rooms?fields=id,name` so the client specifies which fields it needs.
- **Sparse fieldsets**: Returning a summary view for collection endpoints and full details only for individual resource endpoints.

Our implementation returns full objects because it is appropriate for the campus scenario and provides the simplest, most useful client experience.

---

#### Task 2.2 — Question: DELETE idempotency

> *Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.*

Yes, the DELETE operation is **idempotent** in our implementation, consistent with the HTTP specification (RFC 7231, Section 4.2.2).

**Idempotency** means that performing the same operation multiple times produces the same **server-side effect** as performing it once. It does **not** require that the response be identical each time — only that the server state converges to the same result.

Here is what happens in our implementation when a client sends the same DELETE request multiple times:

1. **First request** — `DELETE /api/v1/rooms/LIB-301`: The room exists and has no sensors assigned. The server removes the room from the `DataStore` and returns **204 No Content**. The room is now deleted.

2. **Second request** — `DELETE /api/v1/rooms/LIB-301` (identical): The room no longer exists in the `DataStore`. The server returns **404 Not Found** with an error message.

3. **Third and all subsequent requests**: Same result as the second — **404 Not Found**.

The critical observation is that the **server state is identical** after the first, second, and every subsequent call: the room `LIB-301` does not exist. The server is not modified further by repeated calls. The differing response codes (204 then 404) simply reflect the resource's current state, not a different side-effect. This aligns with the definition of idempotency — the operation's effect on the server is the same no matter how many times it is applied.

If the room has sensors assigned, the DELETE is blocked with a **409 Conflict** every time, which is also idempotent — the room is never deleted regardless of how many attempts are made.

---

### Part 3: Sensor Operations & Linking

#### Task 3.1 — Question: @Consumes content type mismatch

> *We explicitly use the @Consumes(MediaType.APPLICATION_JSON) annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?*

When a resource method (or its class) is annotated with `@Consumes(MediaType.APPLICATION_JSON)`, the JAX-RS runtime enforces that the `Content-Type` header of any incoming request must be `application/json`. This check happens **automatically at the framework level**, before the request body is deserialised and before the resource method is invoked.

If a client sends a request with a different content type (such as `text/plain` or `application/xml`), the following occurs:

1. The JAX-RS runtime inspects the `Content-Type` header of the incoming request.
2. It finds no resource method whose `@Consumes` annotation matches the client's content type.
3. The runtime **rejects the request immediately** — the resource method is never called.
4. The server returns HTTP **415 Unsupported Media Type**.

This mechanism is valuable for several reasons:

- **Automatic input validation**: The framework enforces content type constraints without any manual checking code in the resource method. This eliminates boilerplate and ensures consistency.
- **Type safety**: The JSON deserialiser (MOXy in our case) is guaranteed to receive only valid `application/json` content. It will never be asked to parse XML or plain text, which would cause unpredictable parsing failures.
- **Clear error communication**: The 415 status code is a standard, well-understood HTTP response. It tells the client precisely what went wrong (wrong format) and implicitly what to fix (send `application/json`).
- **Security**: By restricting accepted content types, the server reduces its attack surface. It cannot be tricked into processing unexpected data formats that might exploit parser vulnerabilities.

---

#### Task 3.2 — Question: @QueryParam vs path-based filtering

> *You implemented this filtering using @QueryParam. Contrast this with an alternative design where the type is part of the URL path (e.g., /api/v1/sensors/type/CO2). Why is the query parameter approach generally considered superior for filtering and searching collections?*

**Path-based approach** (`/api/v1/sensors/type/CO2`):
- Makes the filter value look like a distinct **resource**, which is semantically incorrect. "CO2" is not a sub-resource of "sensors" — it is a filter criterion applied to the sensors collection.
- Creates an explosion of URL patterns if multiple filters are needed. Filtering by type and status would require `/sensors/type/CO2/status/ACTIVE`, and adding a third filter makes the URL structure even more unwieldy.
- Breaks the REST principle that each URL should identify a **unique resource**. `/sensors/type/CO2` and `/sensors?type=CO2` return the same data, but the path-based version implies they are separate resources.
- Makes filters **mandatory** in the URL structure. With path segments, omitting a filter requires a different URL pattern entirely.

**Query parameter approach** (`/api/v1/sensors?type=CO2`):
- Correctly treats the filter as a **modifier** on the collection resource `/api/v1/sensors`. The base URL consistently represents the full sensors collection.
- **Optional by design**: Omitting the query parameter returns the complete unfiltered collection, which is intuitive and consistent.
- **Composable**: Multiple filters combine naturally: `?type=CO2&status=ACTIVE&roomId=LIB-301`. No URL restructuring needed.
- **Cacheable**: Proxy servers and CDNs understand that the same path with different query strings represents filtered views of the same resource.
- **Convention-aligned**: This approach matches established conventions used by major APIs (GitHub, Twitter, Google Maps) and aligns with RFC 3986, which defines the query string as the component for non-hierarchical data.

Our implementation uses `@QueryParam("type")` in the `SensorResource.getAllSensors()` method. When the parameter is absent or empty, the full collection is returned. When present, the list is filtered using a case-insensitive match via Java Streams.

---

### Part 4: Deep Nesting with Sub-Resources

#### Task 4.1 — Question: Sub-Resource Locator pattern benefits

> *Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path in one massive controller class?*

The **Sub-Resource Locator** pattern is a JAX-RS mechanism where a resource method does not return a `Response` directly, but instead returns **an instance of another resource class** to which the runtime delegates further processing. In our implementation, `SensorResource` contains a method annotated with `@Path("/{sensorId}/readings")` that returns a `new SensorReadingResource(sensorId)`.

Key characteristics of a sub-resource locator method:
- It has a `@Path` annotation but **no HTTP method annotation** (`@GET`, `@POST`, etc.).
- It returns a **resource class instance**, not a `Response`.
- The returned class contains the actual `@GET` / `@POST` methods that handle the sub-resource.

The architectural benefits are:

1. **Separation of Concerns**: Each resource class owns exactly one logical entity. `SensorResource` handles sensor CRUD. `SensorReadingResource` handles reading-specific logic (history retrieval, posting new readings, updating `currentValue`). Neither class needs to know the other's internal implementation details.

2. **Avoids the "God Class" Anti-Pattern**: Without sub-resource locators, all paths — `/sensors`, `/sensors/{id}`, `/sensors/{id}/readings`, and potentially `/sensors/{id}/readings/{readingId}` — would be defined in a single class. As the API grows, this class becomes unmanageably large, difficult to navigate, and prone to merge conflicts in team environments.

3. **Modularity and Maintainability**: When a developer needs to change reading-related logic, they open `SensorReadingResource.java`. There is no ambiguity about where the code lives. New team members can understand the codebase faster because each class has a clear, limited scope.

4. **Validation Chain**: The parent resource (SensorResource) can validate that the sensor exists **before** delegating to the sub-resource. If the sensor does not exist, the locator throws a `NotFoundException` immediately. This creates a natural hierarchical validation that mirrors the resource structure.

5. **Reusability and Testability**: `SensorReadingResource` is a standalone class that can be unit-tested independently with a mock sensor ID. It could also be reused from a different parent resource if the API design evolves.

6. **Cleaner URL Mapping**: The `@Path` annotation on the locator method (`"/{sensorId}/readings"`) combined with the sub-resource's method annotations (`@GET`, `@POST`) produces the full path `/api/v1/sensors/{sensorId}/readings` automatically. The URL structure is distributed across classes in a way that mirrors the resource hierarchy.

---

### Part 5: Advanced Error Handling, Exception Mapping & Logging

#### Task 5.2 — Question: HTTP 422 vs 404 for missing references

> *Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?*

HTTP **404 Not Found** means the **target URL** does not identify an existing resource. It describes a problem with the **request URI** — the client asked for a resource at a path that does not exist on the server.

HTTP **422 Unprocessable Entity** means the server understands the request, the syntax is valid, and the content type is correct, but the server **cannot process the contained instructions** because of a semantic error in the payload.

When a client sends `POST /api/v1/sensors` with a JSON body containing `"roomId": "FAKE-ROOM"`, the situation is:
- The **URL** `/api/v1/sensors` is valid and exists — it is the sensors collection endpoint.
- The **JSON syntax** is valid — it parses correctly.
- The **Content-Type** is `application/json` — it matches `@Consumes`.
- But the **data inside the payload** references a room that does not exist.

Using **404** here would be misleading because the URL the client called (`/api/v1/sensors`) absolutely exists. The client did not request `/api/v1/rooms/FAKE-ROOM` — they posted to the sensors endpoint with a reference to a non-existent room embedded in the JSON body. A 404 would confuse the client into thinking the sensors endpoint itself is missing.

Using **422** is more accurate because:
1. It correctly communicates that the **request was well-formed** (valid URL, valid JSON, valid content type) but **semantically invalid** (the referenced room does not exist).
2. It distinguishes between "you called the wrong URL" (404) and "your data contains an invalid reference" (422).
3. It helps client developers debug the issue faster — they know the problem is in their request **body**, not in the URL they called.
4. It aligns with the original HTTP/WebDAV specification (RFC 4918), which defined 422 specifically for cases where the server understands the content but cannot process it due to semantic errors.

Our implementation throws a `LinkedResourceNotFoundException` which is mapped to **422 Unprocessable Entity** by the `LinkedResourceNotFoundExceptionMapper`. The error response body explicitly states which room ID was not found, giving the client all the information needed to correct the request.

---

#### Task 5.4 — Question: Security risks of exposing stack traces

> *From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?*

Exposing raw Java stack traces to external consumers is a significant security vulnerability classified under **CWE-209 (Generation of Error Message Containing Sensitive Information)** and falls within the OWASP Top 10 category of **Security Misconfiguration**. The specific risks are:

1. **Technology Fingerprinting**: Stack traces reveal the exact frameworks, libraries, and their versions (e.g., `org.glassfish.jersey.server` version 2.34, `java.base` version 1.8.0_351). Attackers can search public vulnerability databases (CVE/NVD) for known exploits targeting those specific versions.

2. **Internal Architecture Exposure**: Package names and class names (e.g., `com.smartcampus.store.DataStore.addSensor()`) reveal the internal structure of the application. An attacker can map out the codebase, understand the data flow, and identify classes that handle sensitive operations.

3. **File Path Disclosure**: Stack traces often include full file system paths (e.g., `/opt/glassfish5/glassfish/domains/domain1/applications/smart-campus-api/...`). This reveals the operating system, deployment directory structure, server software, and sometimes the username of the deployment account.

4. **Logic Flow Revelation**: The sequence of method calls in a trace shows the application's execution path for a given request. An attacker can understand which validation steps occur (and in what order), identify points where input is processed, and craft payloads that exploit gaps in the validation chain.

5. **Sensitive Data Leakage**: Stack traces may include fragments of data being processed when the error occurred — partial SQL queries, configuration values, API keys, session tokens, or user data that appeared in method parameters or local variables.

Our implementation addresses this through a **`GenericExceptionMapper`** that catches all unhandled `Throwable` exceptions. It returns a generic, safe JSON response to the client:
```json
{
  "statusCode": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred. Please contact the system administrator."
}
```
The actual exception with its full stack trace is logged **server-side only** using `java.util.logging.Logger` at `SEVERE` level, where system administrators can review it through the GlassFish server logs without exposing it to external consumers.

---

#### Task 5.5 — Question: JAX-RS filters vs manual logging

> *Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting Logger.info() statements inside every single resource method?*

Using JAX-RS `ContainerRequestFilter` and `ContainerResponseFilter` for logging (as our `LoggingFilter` class does) is superior to manual logging for several architectural reasons:

1. **DRY Principle (Don't Repeat Yourself)**: A single filter class automatically logs **every** request and response across all endpoints. Without filters, identical logging statements would need to be copy-pasted into every resource method — `getAllRooms()`, `createRoom()`, `getRoom()`, `deleteRoom()`, `getAllSensors()`, `createSensor()`, `getReadings()`, `addReading()`, and every future method. This duplication violates the DRY principle.

2. **Separation of Concerns**: Logging is a **cross-cutting concern** — it applies uniformly across all endpoints and has nothing to do with the business logic of managing rooms or sensors. Filters cleanly separate this infrastructure concern from domain logic. Each resource method can focus exclusively on its primary responsibility.

3. **Guaranteed Consistency**: Filters log every request in exactly the same format (method + URI for requests, status code for responses). Manual logging is error-prone: a developer might forget to add logging to a new endpoint, use a different format, log at a different level, or accidentally include sensitive data.

4. **Easy Maintenance**: To change the log format, add a new field (e.g., client IP address, request duration, correlation ID), or change the log level, only **one file** needs to be modified. With manual logging, the same change would need to be applied to every resource method across multiple classes.

5. **Non-Invasive Registration**: Filters are registered via the `@Provider` annotation and intercepted by the JAX-RS runtime transparently. Adding, modifying, or removing the logging filter requires **zero changes** to any resource class. The resource classes are completely unaware that logging is happening.

6. **Completeness**: Filters can log requests that **fail before reaching a resource method** — for example, requests rejected with 404 (no matching path) or 415 (wrong content type). Manual logging inside resource methods would miss these cases entirely because the method is never invoked.

---

## Design Decisions

1. **Static DataStore**: We use a utility class with `static ConcurrentHashMap` fields rather than singleton beans or dependency injection. This keeps the project simple and avoids introducing additional frameworks, as required by the coursework brief.

2. **javax.ws.rs (not Jakarta)**: We use the `javax.ws.rs` package because GlassFish 5 (bundled with NetBeans) uses the **Java EE 8** namespace. The Jakarta EE 9+ namespace (`jakarta.ws.rs`) is used by GlassFish 6+ and would cause `ClassNotFoundException` on GlassFish 5.

3. **WAR Packaging**: The project is packaged as a WAR file for deployment on GlassFish, which is the standard NetBeans workflow for Java EE web applications.

4. **Single Maven Dependency**: We use only `javaee-web-api:8.0.1` with `provided` scope. GlassFish supplies all runtime libraries (Jersey, MOXy, Servlet API), so zero additional dependencies are needed. This eliminates version conflicts.

5. **MOXy for JSON**: GlassFish's bundled Jersey uses MOXy as its default JSON provider. It automatically serialises/deserialises POJOs with no configuration required — only getters, setters, and a no-arg constructor are needed.

6. **Thread-Safe Collections**: `ConcurrentHashMap` handles concurrent read/write operations safely without explicit `synchronized` blocks, which is important because JAX-RS processes requests on multiple threads simultaneously.

7. **ErrorResponse DTO**: All error responses (400, 403, 404, 409, 422, 500) use a consistent `ErrorResponse` object with `statusCode`, `error`, and `message` fields. This ensures clients can parse errors programmatically using a single structure.

---

## License

This project was developed for the **5COSC022W Client-Server Architectures** coursework at the **University of Westminster**.
