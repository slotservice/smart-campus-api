# Video Demonstration Script — Smart Campus API

**Target length**: 7–8 minutes  
**Tools on screen**: Postman (full screen)  
**Camera**: On, face visible  
**Microphone**: Clear audio  

---

## Before You Start Recording

1. Open **NetBeans** and make sure the server is running.  
   (Open `Main.java` → right-click → **Run File**, or press **Shift+F6**. Wait until you see "Smart Campus API is running!" in the Output window.)
2. Open **Postman**.
3. Create a new **Collection** and name it `Smart Campus Demo`.
4. Do a quick test: GET `http://localhost:8080/api/v1` — make sure you get a JSON response. If you do, you are ready.
5. Start your screen recording with camera on. Speak naturally.

**Important**: In Postman, for every POST request you must:
- Set the method to **POST**
- Set the **Body** tab to **raw** and pick **JSON** from the dropdown
- Paste the JSON body shown below

---

## DEMO SCRIPT (what to do + what to say)

---

### STEP 1 — Introduction (say this while Postman is open) [~20 seconds]

> "Hello, my name is [YOUR NAME] and this is my video demonstration for the Smart Campus Sensor and Room Management API. This API was built using JAX-RS with Jersey on GlassFish. I will now demonstrate all the endpoints using Postman."

---

### STEP 2 — API Discovery [~40 seconds]

**Do this:**
- Method: `GET`
- URL: `http://localhost:8080/api/v1`
- Click **Send**

**Say this:**
> "First I will call the discovery endpoint. This is a GET request to the root path /api/v1. The response shows the API name, version, a description, the admin contact, and links to the two main resources — rooms and sensors. This is an example of HATEOAS, where the API provides navigation links so a client can discover the available resources without needing external documentation."

**You should see:**
```json
{
  "apiName": "Smart Campus Sensor & Room Management API",
  "version": "1.0",
  "description": "RESTful API for managing campus rooms, sensors, and their readings.",
  "adminContact": "admin@smartcampus.ac.uk",
  "links": {
    "rooms": "/api/v1/rooms",
    "sensors": "/api/v1/sensors"
  }
}
```
Status: **200 OK**

---

### STEP 3 — Create Two Rooms [~1 minute]

**Request 1:**
- Method: `POST`
- URL: `http://localhost:8080/api/v1/rooms`
- Body → raw → JSON:
```json
{
  "id": "LIB-301",
  "name": "Library Quiet Study",
  "capacity": 50
}
```
- Click **Send**

**Say this:**
> "Now I will create a room. I am sending a POST request with a JSON body containing the room ID, name, and capacity. The API returns status 201 Created and the room object that was stored. Notice the sensorIds array is empty because no sensors have been assigned yet."

**You should see:** Status **201 Created**, body includes `"sensorIds": []`

**Request 2** (change the body):
```json
{
  "id": "ENG-102",
  "name": "Engineering Lab",
  "capacity": 30
}
```
- Click **Send**

**Say this:**
> "I am creating a second room called Engineering Lab."

---

### STEP 4 — Get All Rooms + Get One Room [~40 seconds]

**Request 1:**
- Method: `GET`
- URL: `http://localhost:8080/api/v1/rooms`
- Click **Send**

**Say this:**
> "A GET request to /rooms returns all rooms as a JSON array. Both rooms are listed here."

**Request 2:**
- Method: `GET`
- URL: `http://localhost:8080/api/v1/rooms/LIB-301`
- Click **Send**

**Say this:**
> "I can also retrieve a single room by its ID."

---

### STEP 5 — Create Three Sensors [~1 minute 15 seconds]

**Request 1 — Temperature sensor:**
- Method: `POST`
- URL: `http://localhost:8080/api/v1/sensors`
- Body:
```json
{
  "id": "TEMP-001",
  "type": "Temperature",
  "status": "ACTIVE",
  "currentValue": 0.0,
  "roomId": "LIB-301"
}
```

**Say this:**
> "Now I will register a sensor. The sensor has an ID, a type, a status, and a roomId. The API validates that the room ID exists before accepting the sensor. This returns 201 Created."

**Request 2 — CO2 sensor:**
```json
{
  "id": "CO2-001",
  "type": "CO2",
  "status": "ACTIVE",
  "currentValue": 0.0,
  "roomId": "LIB-301"
}
```

**Say this:**
> "I will create a second sensor of type CO2, also assigned to LIB-301."

**Request 3 — Maintenance sensor:**
```json
{
  "id": "OCC-001",
  "type": "Occupancy",
  "status": "MAINTENANCE",
  "currentValue": 0.0,
  "roomId": "ENG-102"
}
```

**Say this:**
> "This third sensor is an Occupancy sensor with status set to MAINTENANCE. I will use this later to demonstrate the 403 error."

---

### STEP 6 — Sensor with Invalid Room → 422 Error [~30 seconds]

- Method: `POST`
- URL: `http://localhost:8080/api/v1/sensors`
- Body:
```json
{
  "id": "TEMP-999",
  "type": "Temperature",
  "status": "ACTIVE",
  "currentValue": 0.0,
  "roomId": "FAKE-ROOM"
}
```

**Say this:**
> "Now I will try to create a sensor linked to a room that does not exist. The API returns 422 Unprocessable Entity. This is because the JSON syntax is valid but the data inside it references a non-existent room. Our custom LinkedResourceNotFoundException is caught by its exception mapper and converted to this JSON error response."

**You should see:** Status **422**, body:
```json
{
  "statusCode": 422,
  "error": "Unprocessable Entity",
  "message": "Room with ID 'FAKE-ROOM' does not exist. Cannot register a sensor to a non-existent room."
}
```

---

### STEP 7 — Filter Sensors by Type [~25 seconds]

- Method: `GET`
- URL: `http://localhost:8080/api/v1/sensors?type=Temperature`
- Click **Send**

**Say this:**
> "The GET sensors endpoint supports an optional query parameter called type. Here I filter for only Temperature sensors. This uses the @QueryParam annotation in JAX-RS. If I remove the query parameter, all sensors are returned."

---

### STEP 8 — Post Sensor Readings [~50 seconds]

**Request 1:**
- Method: `POST`
- URL: `http://localhost:8080/api/v1/sensors/TEMP-001/readings`
- Body:
```json
{
  "value": 22.5
}
```

**Say this:**
> "Now I will post a sensor reading using the sub-resource pattern. The path is /sensors/TEMP-001/readings. This is handled by SensorReadingResource, which is a sub-resource class instantiated by SensorResource's sub-resource locator method. The API automatically generates a UUID for the reading ID and sets the timestamp. It also updates the parent sensor's currentValue field to 22.5 as a side effect."

**You should see:** Status **201 Created**, body includes `"id": "some-uuid"`, `"value": 22.5`

**Request 2:**
```json
{
  "value": 23.1
}
```

**Say this:**
> "I will post a second reading."

---

### STEP 9 — Get Reading History [~20 seconds]

- Method: `GET`
- URL: `http://localhost:8080/api/v1/sensors/TEMP-001/readings`

**Say this:**
> "A GET request to the readings path returns the full history. Both readings are listed with their generated IDs and timestamps."

---

### STEP 10 — Maintenance Sensor → 403 Error [~25 seconds]

- Method: `POST`
- URL: `http://localhost:8080/api/v1/sensors/OCC-001/readings`
- Body:
```json
{
  "value": 15
}
```

**Say this:**
> "If I try to post a reading to the OCC-001 sensor which has status MAINTENANCE, the API returns 403 Forbidden. This is our SensorUnavailableException being caught by its mapper. A sensor in maintenance mode cannot accept new readings."

**You should see:** Status **403**, message about MAINTENANCE mode.

---

### STEP 11 — Delete Room with Sensors → 409 Error [~25 seconds]

- Method: `DELETE`
- URL: `http://localhost:8080/api/v1/rooms/LIB-301`

**Say this:**
> "Now I will try to delete room LIB-301 which has two sensors assigned. The API returns 409 Conflict because the business rule prevents deleting a room with active sensors. The RoomNotEmptyException mapper returns a clear error message."

**You should see:** Status **409**, message about 2 sensors assigned.

---

### STEP 12 — Delete an Empty Room Successfully [~30 seconds]

**First create an empty room:**
- Method: `POST`
- URL: `http://localhost:8080/api/v1/rooms`
- Body:
```json
{
  "id": "TEMP-ROOM",
  "name": "Temporary Room",
  "capacity": 10
}
```

**Then delete it:**
- Method: `DELETE`
- URL: `http://localhost:8080/api/v1/rooms/TEMP-ROOM`

**Say this:**
> "To show that delete works when a room is empty, I create a temporary room with no sensors and then delete it. The response is 204 No Content, which means the deletion was successful. If I send the same delete request again, I get 404 because the room no longer exists, which demonstrates that DELETE is idempotent."

**(Optional: send the DELETE again to show the 404)**

---

### STEP 13 — Closing [~15 seconds]

**Say this:**
> "This concludes my demonstration of the Smart Campus API. The project uses JAX-RS with in-memory data structures, custom exception mappers for error handling, sub-resource locators for the readings endpoint, and a logging filter for request and response observability. Thank you for watching."

---

## Quick Reference — All Postman Requests in Order

| #  | Method | URL | Body | Expected |
|----|--------|-----|------|----------|
| 1  | GET    | `/api/v1` | — | 200 |
| 2  | POST   | `/api/v1/rooms` | `{"id":"LIB-301","name":"Library Quiet Study","capacity":50}` | 201 |
| 3  | POST   | `/api/v1/rooms` | `{"id":"ENG-102","name":"Engineering Lab","capacity":30}` | 201 |
| 4  | GET    | `/api/v1/rooms` | — | 200 |
| 5  | GET    | `/api/v1/rooms/LIB-301` | — | 200 |
| 6  | POST   | `/api/v1/sensors` | `{"id":"TEMP-001","type":"Temperature","status":"ACTIVE","currentValue":0.0,"roomId":"LIB-301"}` | 201 |
| 7  | POST   | `/api/v1/sensors` | `{"id":"CO2-001","type":"CO2","status":"ACTIVE","currentValue":0.0,"roomId":"LIB-301"}` | 201 |
| 8  | POST   | `/api/v1/sensors` | `{"id":"OCC-001","type":"Occupancy","status":"MAINTENANCE","currentValue":0.0,"roomId":"ENG-102"}` | 201 |
| 9  | POST   | `/api/v1/sensors` | `{"id":"TEMP-999","type":"Temperature","status":"ACTIVE","currentValue":0.0,"roomId":"FAKE-ROOM"}` | 422 |
| 10 | GET    | `/api/v1/sensors?type=Temperature` | — | 200 |
| 11 | POST   | `/api/v1/sensors/TEMP-001/readings` | `{"value":22.5}` | 201 |
| 12 | POST   | `/api/v1/sensors/TEMP-001/readings` | `{"value":23.1}` | 201 |
| 13 | GET    | `/api/v1/sensors/TEMP-001/readings` | — | 200 |
| 14 | POST   | `/api/v1/sensors/OCC-001/readings` | `{"value":15}` | 403 |
| 15 | DELETE | `/api/v1/rooms/LIB-301` | — | 409 |
| 16 | POST   | `/api/v1/rooms` | `{"id":"TEMP-ROOM","name":"Temporary Room","capacity":10}` | 201 |
| 17 | DELETE | `/api/v1/rooms/TEMP-ROOM` | — | 204 |
| 18 | DELETE | `/api/v1/rooms/TEMP-ROOM` | — | 404 |

**Base URL for all**: `http://localhost:8080`

---

## Tips for Recording

- **Pace yourself.** Speak slowly and clearly. It is better to be calm than fast.
- **Pause briefly** after each response so the viewer can see the status code and body.
- **Point out the status code** in Postman's response area (top-right of the response panel) each time.
- **Do not rush.** If you make a mistake, just repeat the request. It is a demonstration, not a live exam.
- **Check your audio** before submitting. Re-record if the microphone is muffled.
- The video must be **under 10 minutes** and you must be **visible on camera**.
