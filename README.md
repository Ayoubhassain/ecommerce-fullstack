# ecommerce-fullstack

A full stack e-commerce application: a Spring Boot REST API and an Angular front end, backed by MySQL. Customers browse and search the catalog, fill a cart and place orders. Registered users log in with a JWT and see their order history, and admins manage products and orders from a dedicated area.

## Background

I first built this application on my own. After losing that code, I rebuilt it in this repository, starting from the public project of the course **[Full Stack: Angular and Spring Boot](https://github.com/darbyluv2code/fullstack-angular-and-springboot)** by Chad Darby (luv2code) for the catalog, search, cart and checkout.

This version adds:

- **Authentication:** sign up and log in with Spring Security and JWT, BCrypt-hashed passwords, USER and ADMIN roles.
- **Admin area:** product management (list, search, create, edit, delete) and order management (list, change status), restricted to the ADMIN role on the backend and hidden by a route guard on the frontend.
- **My orders:** logged-in customers see their own orders.
- **Checkout hardening:** prices and totals are recomputed on the server from the database instead of being trusted from the browser, an existing customer is reused instead of duplicated, and new orders get the NEW status.
- **API safety:** customers, orders and users are no longer exposed by Spring Data REST, validation errors return clear JSON messages.
- **Integration tests:** security rules, authentication, admin CRUD and orders are tested end to end with MockMvc and an in-memory H2 database.
- **Configuration:** database, JWT secret and admin account come from environment variables, and a Docker Compose file starts MySQL with the schema and sample data.

## Architecture

```mermaid
flowchart LR
    subgraph Browser["Angular (port 4200)"]
        C[Components] --> S[Services]
        S --> I[Auth interceptor<br/>adds the JWT]
    end
    I -- "HTTP / JSON" --> F
    subgraph API["Spring Boot (port 8080)"]
        F[JWT filter +<br/>SecurityConfig] --> CT[Controllers /<br/>Spring Data REST]
        CT --> SV[Services]
        SV --> R[Repositories JPA]
    end
    R --> DB[(MySQL)]
```

| Layer | Technology |
|---|---|
| Front end | Angular 14, TypeScript, Bootstrap, ng-bootstrap |
| Back end | Java 17, Spring Boot 2.7, Spring Data JPA/REST, Spring Security |
| Auth | JWT (jjwt), BCrypt |
| Database | MySQL 8 (Docker Compose for local dev, H2 for tests) |

**How a request flows:** an Angular component calls a service, the interceptor adds the JWT, Spring Security checks the token and the role, the controller calls a service, the service uses a JPA repository, and the result goes back as JSON.

## Data model

```mermaid
erDiagram
    PRODUCT_CATEGORY ||--o{ PRODUCT : contains
    CUSTOMER ||--o{ ORDERS : places
    ORDERS ||--|{ ORDER_ITEM : contains
    ORDERS ||--|| ADDRESS : "ships to"
    ORDERS ||--|| ADDRESS : "bills to"
    COUNTRY ||--o{ STATE : has

    PRODUCT_CATEGORY {
        bigint id PK
        string category_name
    }
    PRODUCT {
        bigint id PK
        string sku
        string name
        decimal unit_price
        int units_in_stock
        boolean active
        bigint category_id FK
    }
    CUSTOMER {
        bigint id PK
        string first_name
        string last_name
        string email
    }
    ORDERS {
        bigint id PK
        string order_tracking_number
        int total_quantity
        decimal total_price
        string status
        bigint customer_id FK
    }
    ORDER_ITEM {
        bigint id PK
        bigint product_id
        int quantity
        decimal unit_price
        bigint order_id FK
    }
    ADDRESS {
        bigint id PK
        string street
        string city
        string country
        string zip_code
    }
    USERS {
        bigint id PK
        string email UK
        string password "BCrypt hash"
        string role "USER or ADMIN"
    }
    COUNTRY {
        int id PK
        string code
        string name
    }
    STATE {
        int id PK
        string name
        int country_id FK
    }
```

`USERS` holds the accounts used to log in. A customer's orders are linked to their account by email, so guests can still order without an account.

## How authentication works

```mermaid
sequenceDiagram
    actor U as User
    participant A as Angular
    participant S as Spring Security
    participant API as Controller / Service
    participant DB as MySQL

    U->>A: email + password
    A->>API: POST /api/auth/login
    API->>DB: find user by email
    API->>API: check password against BCrypt hash
    API-->>A: JWT (email, role, expiry)
    A->>A: store the token

    U->>A: open "Admin · Products"
    A->>S: GET /api/admin/products<br/>Authorization: Bearer JWT
    S->>S: verify signature and expiry,<br/>load the user's role
    alt role is ADMIN
        S->>API: forward the request
        API->>DB: read products
        API-->>A: 200 + JSON
    else no token or invalid token
        S-->>A: 401 Unauthorized
    else logged in but not ADMIN
        S-->>A: 403 Forbidden
    end
```

The Angular route guard only hides the admin pages. The real protection is the backend rule on `/api/admin/**`.

## How an order is placed

```mermaid
sequenceDiagram
    participant A as Angular (checkout form)
    participant C as CheckoutController
    participant S as CheckoutService
    participant DB as MySQL

    A->>C: POST /api/checkout/purchase<br/>customer, addresses, items
    C->>C: if logged in, use the account email
    C->>S: placeOrder(purchase)
    S->>DB: load each product
    S->>S: recompute unit prices and total<br/>(the browser's prices are ignored)
    S->>DB: reuse the customer if the email exists,<br/>save order + items + addresses (one transaction)
    S-->>A: order tracking number
```

## Main API routes

| Method | Route | Access |
|---|---|---|
| GET | `/api/products`, `/api/product-category` | Public |
| POST | `/api/auth/register`, `/api/auth/login` | Public |
| POST | `/api/checkout/purchase` | Public (linked to the account when logged in) |
| GET | `/api/auth/me`, `/api/orders/me` | Logged-in user |
| GET, POST, PUT, DELETE | `/api/admin/products` | ADMIN |
| GET, PUT | `/api/admin/orders`, `/api/admin/orders/{id}/status` | ADMIN |

## Project structure

```
backend/
  entity/       # JPA entities: Product, Order, Customer, User...
  dao/          # Spring Data repositories
  dto/          # objects sent and received by the API
  service/      # business logic: checkout, auth, admin
  controller/   # REST endpoints
  security/     # JWT service, JWT filter, security rules
  exception/    # JSON error responses
frontend/src/app/
  components/   # pages: catalog, cart, checkout, login, my orders, admin
  services/     # HTTP calls, cart, auth, interceptor
  guards/       # route protection (logged in, admin)
db-scripts/     # MySQL schema and sample data
docker-compose.yml
```

## Getting started

**Prerequisites:** Java 17, Node.js (16 or later), and Docker (or a local MySQL 8).

```bash
# 1. Database (MySQL in Docker, tables and sample data created automatically)
docker compose up -d

# 2. Back end (http://localhost:8080)
cd backend
./mvnw spring-boot:run

# 3. Front end (http://localhost:4200), in another terminal
cd ../frontend
npm install
npm start
```

At first start, the backend creates an admin account from `ADMIN_EMAIL` / `ADMIN_PASSWORD` (local defaults: `admin@luv2shop.com` / `Admin123!`). Set `JWT_SECRET` (32+ characters) and your own admin password outside local development.

## Tests

```bash
cd backend
./mvnw test
```
