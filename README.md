# Redis Virality Engine

A distributed backend system built using Spring Boot, PostgreSQL, and Redis that simulates a social media virality engine with realtime scoring, concurrency-safe bot limits, and batched notifications.
A robust, high-performance Spring Boot microservice that acts as the central
API gateway and guardrail system 
handles concurrent requests, manage distributed state using Redis, and implement event-driven
scheduling.

---

# Tech Stack

- Java 17
- Spring Boot 3
- PostgreSQL
- Redis
- Spring Data JPA
- Spring Data Redis
- Docker Compose
- JMeter

---

## Features

### Phase 1 — Content APIs

Implemented APIs for:

- Create Post
- Add Comment
- Like Post

PostgreSQL stores all persistent data:

- Posts
- Comments
- Users
- Bots

---

### Phase 2 — Redis Virality Engine

#### Virality Score

Realtime virality score updates using Redis.

| Interaction | Score |
|---|---|
| Bot Reply | +1 |
| Human Like | +20 |
| Human Comment | +50 |

---

## Atomic Locks (Thread Safety)

### Horizontal Cap

A post cannot have more than 100 bot replies.

Redis key:

```text
post:{id}:bot_comment_count
```

Implementation:

- Redis `INCR` atomically reserves a bot slot
- If count exceeds 100:
  - Redis `DECR` rolls back reservation
  - Request is rejected with HTTP 429

This guarantees thread safety under concurrent requests.

---

### Vertical Cap

Comment thread depth cannot exceed 20 levels.

Implementation:

- Each comment stores `parentCommentId`
- Depth is calculated server-side
- Requests exceeding depth 20 are rejected

---

### Cooldown Cap

A bot cannot interact with the same human more than once every 10 minutes.

Redis key:

```text
cooldown:bot_{botId}:human_{humanId}
```

Implementation uses Redis atomic `SETNX` with TTL.

---

## Phase 3 — Notification Engine

### Notification Throttling

If a user already received a notification within 15 minutes:

- notifications are queued in Redis Lists
- instead of sending immediate push notifications

Redis key:

```text
user:{id}:pending_notifs
```

---

### Scheduled Notification Sweeper

A Spring `@Scheduled` task runs every 5 minutes and:

- aggregates pending notifications
- creates summarized notification logs
- clears Redis queues

---

## Stateless Architecture

The application remains completely stateless.

All distributed state is stored in Redis:

- counters
- cooldowns
- notification queues
- pending notification users

No in-memory synchronization or HashMaps are used.

---

## Concurrency Testing

Concurrency was tested using JMeter.

Test Scenario:

- 200 concurrent bot requests
- same post
- simultaneous execution

Expected Results:

| Metric | Expected |
|---|---|
| Successful bot comments | 100 |
| Rejected requests | 100 |
| Final DB bot comments | 100 |
| Redis bot counter | 100 |

This validates thread-safe concurrency handling using Redis atomic operations.

---

## Running the Project

### Start Docker Services

```bash
docker compose up -d
```

---

### Run Spring Boot Application

Run the application from IntelliJ or using:

```bash
./mvnw spring-boot:run
```

---
## Redis Keys Used

```text
post:{id}:virality_score
post:{id}:bot_comment_count
cooldown:bot_{id}:human_{id}
notif:userId:{id} -throttle key   
user:{id}:pending_notifs -list of pending notification for all users
pending_notif_users- set of users
```
