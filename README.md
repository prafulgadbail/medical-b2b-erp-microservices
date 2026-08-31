# Medical B2B ERP — Microservices Platform

A production-grade **B2B ERP system for the medical/healthcare domain** — connecting hospitals, distributors, and administrators on a single platform for catalog, inventory, and order management. Built as three independent Spring Boot microservices behind a React SPA, deployed on AWS EKS.

---

### 🔧 Tech Stack

| Layer | Technology |
|---|---|
| **Frontend** | React 18 + Vite + TailwindCSS + TanStack Query |
| **Backend** | Spring Boot 3.x (3 independent microservices) |
| **Database** | MongoDB Atlas |
| **Auth** | JWT (HS256), shared secret across services |
| **Cloud** | AWS — EKS, S3, CloudFront, Route 53 |
| **IaC** | Terraform (modular) |
| **CI/CD** | Jenkins |
| **Containers** | Docker + Kubernetes manifests |
| **API Docs** | Swagger / OpenAPI 3.0 per service |

![React](https://img.shields.io/badge/React-61DAFB?style=flat&logo=react&logoColor=black)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=flat&logo=springboot&logoColor=white)
![MongoDB](https://img.shields.io/badge/MongoDB%20Atlas-47A248?style=flat&logo=mongodb&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-2496ED?style=flat&logo=docker&logoColor=white)
![Kubernetes](https://img.shields.io/badge/Kubernetes-326CE5?style=flat&logo=kubernetes&logoColor=white)
![AWS](https://img.shields.io/badge/AWS-232F3E?style=flat&logo=amazon-aws&logoColor=white)
![Terraform](https://img.shields.io/badge/Terraform-7B42BC?style=flat&logo=terraform&logoColor=white)
![Jenkins](https://img.shields.io/badge/Jenkins-D24939?style=flat&logo=jenkins&logoColor=white)

---

### 🏗️ Architecture

```
                          CloudFront CDN
                    (React Frontend via S3)
                            │
              AWS ALB Ingress Controller (EKS)
        ┌───────────────────┼───────────────────┐
        │                   │                   │
 ┌──────▼──────┐   ┌────────▼────────┐   ┌──────▼──────┐
 │ user-service│   │ product-service │   │order-service│
 │  Port 8081  │   │   Port 8082     │   │  Port 8083  │
 │ Auth / JWT  │   │ Catalog / Stock │   │Order lifecycle│
 │ Roles/Orgs  │   │ Batches/Reserve │   │(+ product API)│
 └──────┬──────┘   └────────┬────────┘   └──────┬──────┘
        │                   │                   │
        └───────────────────┼───────────────────┘
                             │
                  MongoDB Atlas (per-service DB)
             users_db · products_db · orders_db
```

Each microservice owns its own database (**database-per-service** pattern) — no service reaches into another's data directly; `order-service` calls `product-service` over HTTP with the forwarded JWT to check/reserve stock.

---

### ⚡ Live CI/CD Pipeline Flow

<img src="assets/cicd-flow.svg" alt="CI/CD pipeline animated flow" width="100%"/>

These are **3 independent microservices**, each with its own codebase, Dockerfile, and Jenkinsfile — hosted together in a single GitHub repository for convenience, but built, tested, and deployed completely independently. A push only triggers the pipeline for the service whose folder actually changed; the other two are untouched. Each pipeline runs the same stages on its own: **Path-filtered Trigger → Maven Build & Test → Docker Build → Push to ECR → `kubectl apply` on EKS** — so `user-service`, `product-service`, and `order-service` can be built, tested, and deployed on completely separate schedules without blocking each other.

---

### 🌐 Live Request Flow — How a Hospital User Accesses the ERP

<img src="assets/user-flow.svg" alt="Real user request flow" width="100%"/>

1. **Frontend load** — DNS resolves via Route 53 → CloudFront serves the cached React SPA from S3 (fast, edge-cached)
2. **API call** — e.g. a distributor approving an order → hits ALB Ingress → routed to `order-service` on EKS → which calls `product-service` (reserve stock) and validates the JWT via `user-service` → result persisted to MongoDB Atlas

---

### 💼 Services

| Service | Port | Responsibilities |
|---|---|---|
| **user-service** | 8081 | Authentication, JWT issuance, users, organizations, audit hooks |
| **product-service** | 8082 | Product catalog, inventory batches, stock reserve/release APIs |
| **order-service** | 8083 | Order lifecycle; calls product-service to validate & reserve stock |

---

### 👥 Roles & Access

| Role | Access |
|---|---|
| **ADMIN** | Organizations, all products/inventory (scoped), all orders |
| **DISTRIBUTOR** | Own catalog & stock batches, incoming orders for own organization |
| **HOSPITAL** | Browse catalog, create and track own organization's orders |

---

### 📦 Domain Highlights

- **Catalog** — Only active products are visible to hospitals/distributors; soft-deleted products free up their SKU for reuse
- **Inventory** — Stock tracked per product + warehouse + batch; *available* stock = stored quantity − reserved quantity
- **Orders** — Hospitals place orders → distributor/admin approves only if enough sellable stock exists → approval triggers a reserve call to `product-service` (multi-batch allocation) → distributors act only on orders assigned to their organization

---

### 📁 Project Structure

```
├── frontend/          # React + Vite SPA (HashRouter for static hosting)
├── user-service/      # Spring Boot — auth, users, organizations
│   └── Jenkinsfile    # Own pipeline, triggered only on changes here
├── product-service/   # Spring Boot — catalog, inventory
│   └── Jenkinsfile    # Own pipeline, triggered only on changes here
├── order-service/     # Spring Boot — order lifecycle
│   └── Jenkinsfile    # Own pipeline, triggered only on changes here
├── docker/            # Dockerfiles per service
├── k8s/               # Kubernetes manifests (Deployments, Services, Ingress)
├── terraform/          # Modular AWS infrastructure (EKS, VPC, S3, Route 53)
└── docs/               # Deployment & architecture guides
```

---

### ⚙️ CI/CD

Each service is built and deployed independently through Jenkins:

**Push → Build & Test (Maven) → Docker Build → Push to ECR → `kubectl apply` on EKS**

Infrastructure changes go through Terraform, applied per environment with remote state locked via S3 + DynamoDB.

---

### 🚀 Getting Started

> _Add local setup instructions here — env variables, `docker-compose up`, or per-service run commands._

---

### 📄 API Documentation

Each service exposes Swagger/OpenAPI docs at `/swagger-ui.html` when running locally.
