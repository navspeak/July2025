- 🧠 **1. Core Anchors**: Memorize: `2^10 = 10^3 (1024 ≈  1000)`
- 🔢 Powers of 2 ↔ Powers of 10
- | Unit   | Power of 10 | Power of 2 (approx) |
    | ------ | ----------- | ------------------- |
  | KB     | 10³         | 2¹⁰                 |
  | MB     | 10⁶         | 2²⁰                 |
  | GB     | 10⁹         | 2³⁰                 |
  | TB     | 10¹²        | 2⁴⁰                 |
  | PB     | 10¹⁵        | 2⁵⁰                 |
  | **EB** | **10¹⁸**    | **2⁶⁰**             |


- 🧠 **2. Integer & Data Type Ranges**
- | Type | Bits | Range (exact, 2-power) | Range (approx) | Storage analogy |
  | ---- | ---- | ---------------------- | -------------- | --------------- |
  | int  | 32   | −2³¹ to 2³¹−1          | ±2 × 10⁹       | ≈ 2 GB          |
  | long | 64   | −2⁶³ to 2⁶³−1          | ±9 × 10¹⁸      | ≈ 9 EB          |

- 🧠 **3. Time Conversions (VERY useful)**
 
- | Unit     | Approx      |
  |----------|-------------|
  | 1 second | 10⁰         |
  | 1 minute | 60 ≈ 10²    |
  | 1 hour   | 3600 ≈ 10⁴  |
  | 1 day    | 86400 ≈ 10⁵ |
  | 1 year   | ≈ 3 × 10⁷   |

- 🧠 **4. Throughput / Scale Estimation**
- **Requests per second:**
- | Scale     | Meaning |
  | --------- | ------- |
  | 10³ / sec | 1K rps  |
  | 10⁶ / sec | 1M rps  |
  | 10⁹ / sec | 1B rps  |
- If a system handles `10,000 req/sec  = 10^4` -> `10^4 * 10^5 r/day` -> `1 Billion r/day`
- 🧠 **5. Storage Estimation**
  - If 1 record = 1 KB, 10^3 records = 10^3 * 10^3 bytes = 10 ^6 bytes = 1MB

- 🧠 **6. Network / Bandwidth**
- | Bandwidth | Bytes/sec                  |
  | --------- | -------------------------- |
  | 1 Mbps    | ≈ 10⁶ bits/s ≈ 10⁵ bytes/s |
  | 1 Gbps    | ≈ 10⁹ bits/s ≈ 10⁸ bytes/s |
- Example: If each response = 10 KB = 10⁴ bytes and traffic = 10⁴ req/sec
  - 10^4 × 10^4 = 10^8 bytes/sec ≈ 100 MB/sec
  
- 🧠 **7. Latency Cheat Sheet**
- - | Operation         | Latency    |
    | ----------------- | ---------- |
    | L1 cache          | ~1 ns      |
    | RAM               | ~100 ns    |
    | SSD               | ~100 µs    |
    | Network (same DC) | ~0.5–1 ms  |
    | Cross region      | ~50–100 ms |

- 🧠 **8. CPU & Memory Thinking**
- Example: If each request uses 1 ms CPU time (The CPU is actively executing instructions for that request for a total of 1 millisecond.)
  - CPU per second: 1 core = 1000 ms/sec
  - So one core handles: 1000 req/sec 
  - 👉 Need 10,000 rps?:10,000 / 1000 = 10 cores
🧠 **9. Log Volume Estimation**

If each request logs 1 KB

and you have 1M req/sec

10^6 × 10^3 bytes = 10^9 bytes/sec = 1 GB/sec

👉 per day:

1 GB × 10^5 sec ≈ 100 TB/day

🔥 huge!

🧠 10. Quick Mental Math Patterns
Pattern 1: multiply powers
10^a × 10^b = 10^(a+b)
Pattern 2: divide powers
10^a / 10^b = 10^(a-b)
Pattern 3: combine numbers
3 × 10^5 × 2 × 10^3
= 6 × 10^8
🎯 Interview Example

Q: You have 5M users, each uploads 2 photos per day, each photo 3 MB.
How much storage per year?

5 × 10^6 users
× 2 photos/day
× 3 MB
× 365 ≈ 3 × 10^2

= 5 × 2 × 3 × 3 × 10^6 × 10^2
= 90 × 10^8 MB
= 9 × 10^9 MB
≈ 9 PB
🚀 Your “Architect Brain” Summary

When you see any system, immediately estimate:

QPS → traffic

Payload size → bandwidth

Record size × count → storage

CPU per request → cores needed

Logs/events → pipeline size