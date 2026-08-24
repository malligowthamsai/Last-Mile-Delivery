# 🏗️ System Design: Last-Mile Delivery Tracker

## Overview

The **Last-Mile Delivery Tracker** platform provides enterprise-grade last-mile logistics management featuring dynamic multi-tier rate calculations, zone-based routing, intelligent agent assignments, immutable audit lifecycles, and role-based access control.

```mermaid
graph TD
    A[Customer Portal] --> B[Spring Security JWT Guard]
    C[Admin Dashboard] --> B
    D[Public Tracking Page] --> E[Spring Boot REST API]

    B --> E
    E --> F[Rate Engine Service]
    E --> G[Zone Detector Service]
    E --> H[Agent Assignment Service]
    E --> I[Spring Data JPA / Hibernate]
    I --> J[(PostgreSQL / H2 Database)]

    E --> K[Notification Service]
    K --> L[JavaMailSender Email Dispatch]
    K --> M[Fast2SMS Mobile Alerts]
```

---

## 1. System Architecture & Component Interactions

The system adopts a layered enterprise REST architecture with a stateless API layer and an immutable relational data model.

```mermaid
sequenceDiagram
    autonumber
    actor Customer
    participant API as Spring Boot API
    participant Zone as Zone Detector
    participant Rate as Rate Engine
    participant DB as JPA / PostgreSQL DB
    participant Agent as Agent Assign Service
    participant Notify as Notification Service

    Customer->>API: POST /api/orders/calculate
    API->>Zone: Lookup Zone from Pincode
    Zone->>DB: Query areas table by pincode
    DB-->>Zone: Return Area & Zone entities
    API->>Rate: Calculate Volumetric & Billable Weight
    Rate->>DB: Query rate_cards table
    DB-->>Rate: Return RateCard entity
    Rate-->>API: Calculated Multi-Tier Breakdown (Standard, Express, Economy)
    API-->>Customer: Return Charge Breakdown Options

    Customer->>API: POST /api/orders
    API->>DB: Save Order Entity (Transaction)
    API->>DB: Append TrackingHistory (CREATED)
    API->>Notify: Asynchronously trigger Order Placed Notification
    API-->>Customer: Order Created (#UUID) + Breakdown

    Admin->>API: POST /api/orders/{id}/auto-assign
    API->>Agent: Request Auto-Assignment
    Agent->>DB: Query available agent in pickup zone
    alt Pickup Zone Match Found
        DB-->>Agent: Return Zone-Local Agent
    else Fallback to Global
        DB-->>Agent: Return System-Wide Available Agent
    end
    Agent->>DB: Update Order agent & status = AGENT_ASSIGNED
    Agent->>DB: Update AgentProfile isAvailable = false
    Agent->>DB: Append TrackingHistory (AGENT_ASSIGNED)
    API->>Notify: Notify Customer (Agent Assigned)
    API-->>Admin: Assignment Success Response
```

---

## 2. Dynamic Rate Calculation Engine

The rate engine evaluates shipments through a multi-tier pipeline:

```mermaid
graph LR
    A[Package Dimensions & Weight] --> B[Zone Resolution]
    B --> C[Volumetric & Billable Weight]
    C --> D[Rate Card Matrix Lookup]
    D --> E[Multi-Tier Evaluation]
    E --> F[Standard / Express / Economy]
    F --> G{Payment Type COD?}
    G -->|Yes| H[Apply Flat COD Surcharge]
    G -->|No| I[Base Total]
    H --> J[Final Computed Quote]
    I --> J
```

### Calculation Algorithms
1. **Pincode Resolution**: O(1) indexed lookup via `AreaRepository.findByPincode(pincode)`.
2. **Volumetric Weight**: Formula: `(Length × Breadth × Height) / 5000.0`.
3. **Billable Weight**: `Math.max(actualWeight, volumetricWeight)`.
4. **Base Rate Card**: Query `(zone_from_id, zone_to_id, order_type)` matching B2B or B2C.
5. **Multi-Tier Pricing Strategies**:
   - **Standard Zone Delivery**: Base rate from zone rate card (or distance fallback).
   - **Express Same-Day Dispatch**: Point-to-point priority dispatch (`rate × 1.45`, higher floor).
   - **Economy Surface Transport**: Bulk budget ground transport (`rate × 0.75`).
6. **COD Surcharge**: Evaluated per order type from `cod_surcharges` table when `paymentType == COD`.

---

## 3. Intelligent Agent Auto-Assignment Logic

Agent assignment uses a 2-tier fallback model executed within a declarative `@Transactional` boundary:

```mermaid
graph TD
    Start[Order Assignment Triggered] --> QueryPickup[Query Agent with isAvailable=true in Pickup Zone]
    QueryPickup --> FoundPickup{Agent Found?}
    FoundPickup -->|Yes| AssignAgent[Assign Zone-Local Agent]
    FoundPickup -->|No| QueryGlobal[Query Any Agent with isAvailable=true]
    QueryGlobal --> FoundGlobal{Agent Found?}
    FoundGlobal -->|Yes| AssignAgent[Assign Fallback Agent]
    FoundGlobal -->|No| ThrowError[Throw BadRequestException]
    
    AssignAgent --> T1[Update Order.agent & status = AGENT_ASSIGNED]
    T1 --> T2[Set AgentProfile.isAvailable = false]
    T2 --> T3[Append TrackingHistory Audit Entry]
    T3 --> Finish[Trigger Customer Notification]
```

---

## 4. Order Status Lifecycle & Immutable Audit History

All status transitions are strictly recorded in the append-only `tracking_history` table. Existing tracking entries are immutable.

```mermaid
stateDiagram-v2
    [*] --> CREATED: Customer places order
    CREATED --> AGENT_ASSIGNED: Admin triggers auto/manual assign
    AGENT_ASSIGNED --> PICKED_UP: Agent collects package
    PICKED_UP --> IN_TRANSIT: In transit between hubs
    IN_TRANSIT --> OUT_FOR_DELIVERY: Out for final delivery
    OUT_FOR_DELIVERY --> DELIVERED: Successfully delivered (Frees Agent)
    OUT_FOR_DELIVERY --> FAILED: Delivery attempt failed (Frees Agent)
    FAILED --> RESCHEDULED: Customer/Admin reschedules date
    RESCHEDULED --> AGENT_ASSIGNED: Admin reassigns agent
    CREATED --> CANCELLED: Admin cancels order
    AGENT_ASSIGNED --> CANCELLED: Admin cancels order
    DELIVERED --> [*]
    CANCELLED --> [*]
```

### Immutable Tracking Record Schema
- `order_id`: Associated order UUID.
- `status`: New lifecycle milestone state.
- `changed_by_id`: User ID of actor triggering transition.
- `changed_by_role`: Role of actor (`CUSTOMER`, `AGENT`, `ADMIN`).
- `note`: Operational note, failure reason, or milestone description.
- `timestamp`: Server timestamp (`LocalDateTime.now()`).

---

## 5. Technology Stack Summary

| Layer | Component | Description / Rationale |
|---|---|---|
| **Frontend** | React 18 (Vite) + Tailwind CSS | Responsive SPA with custom design system, Lucide icons, and Axios |
| **Backend** | Java 17 + Spring Boot 3.3.3 | Enterprise-grade REST API with dependency injection and clean layered services |
| **Security** | Spring Security 6 + JJWT | Stateless JWT authentication filter with role-based method guards (`@PreAuthorize`) |
| **ORM & Data** | Spring Data JPA + Hibernate 6.5 | Type-safe repository abstraction with automatic schema updates and transactions |
| **Database** | PostgreSQL / H2 Database | PostgreSQL for production; in-memory H2 with PostgreSQL dialect for testing |
| **Email Service** | `spring-boot-starter-mail` | Asynchronous MIME HTML email dispatch using `JavaMailSender` |
| **SMS Service** | Fast2SMS Gateway | Asynchronous mobile notification integration via Spring `RestTemplate` |
| **Auto-Seeding** | `CommandLineRunner` | Idempotent startup seeder populating users, zones, areas, rate cards, and agents |
