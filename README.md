
# UrlShortener

A simple and lightweight URL Shortener built with **Spring Boot**. Converts long URLs into short, shareable links and resolves them back to their original form. Includes rate limiting and Redis caching for performance.

---

## Features

- Shortens long URLs into short links.
- Resolves short links to the original URL.
- Rate limiting to prevent API abuse.
- Redis caching for faster URL lookups.
- MongoDB for persistent storage.

---

## How to Use

### 1. Shorten a URL
**POST** `/UrlShortener/shorten`  

**Request**:  
```json
{ "longUrl": "https://example.com" }
```
**Response**
```json
{ "shortUrl": "http://localhost:8080/{shortCode}" }
```

### 2. Redirect to Original URL
**GET /UrlShortener/{shortUrlEncoding}**
Redirects to the original URL.

### 3. Redis Operations
List all keys: **GET /redis/getAllKeys**

Cleanup invalid keys: **POST /redis/cleanupKeys**

Flush all keys: **POST /redis/flushAllKeys**

## Configuration
```
spring.data.mongodb.uri=mongodb://localhost:27017/urlshortener
spring.redis.host=localhost
spring.redis.port=6379
rate.limit.max.requests=10
rate.limit.time.window.ms=60000
```

## How to Run (locally)

### 1. Clone the repository:
```
git clone <repository-url>
cd url-shortener
```

### 2. Install Dependencies:
```
./mvnw clean install
```
### 3. Start Mongo and Redis

### 4. Start the SpringBootApplication
```
./mvnw spring-boot:run
```
## How to Run (using Docker)

### 1. Build the Docker Image
Navigate to the project directory containing your Dockerfile and run:
```
docker build -t urlshortener .
```

### 2. Ensure Mongo and Redis are Running
```
docker run -d -p 8080:8080 --name url-shortener url-shortener
```

## Testing the API
```
Use Postman or similar tools to test the endpoints. Sample requests are provided above.
```

