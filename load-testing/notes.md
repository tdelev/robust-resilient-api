1. We start with simulation of a workload model.

We will use the open workload model where we have no control over the number of concurrent users, users keep on arriving even though applications have trouble serving them. Most websites behave this way.

2. Load shedding vs Rate Limiting


Load shedding and rate limiting are both techniques for managing system capacity under stress, but they work differently and serve distinct purposes:

**Rate Limiting**
Rate limiting controls the number of requests a client can make within a specific time window (like 100 requests per minute). It's primarily about enforcing usage policies and preventing abuse. When the limit is exceeded, requests are typically rejected with an error like "429 Too Many Requests." Rate limiting is predictable and uniform - every client gets the same treatment based on predetermined rules.

**Load Shedding**
Load shedding is a more drastic measure that involves deliberately dropping or rejecting requests when the system is under extreme stress or approaching capacity limits. Unlike rate limiting, load shedding is reactive and often kicks in during emergencies or unexpected traffic spikes. The system makes real-time decisions about which requests to process and which to drop based on current load conditions.

**Key Differences:**

- **Trigger**: Rate limiting is based on predetermined quotas, while load shedding responds to actual system stress
- **Purpose**: Rate limiting prevents abuse and ensures fair usage; load shedding prevents system collapse
- **Selectivity**: Rate limiting treats requests uniformly; load shedding might prioritize certain types of requests (like paying customers over free users)
- **Timing**: Rate limiting is proactive and consistent; load shedding is reactive and situational

Think of rate limiting as a bouncer checking IDs at a club entrance, while load shedding is like emergency crowd control when the venue becomes dangerously overcrowded - you start turning people away regardless of whether they have valid tickets.
