User (Frontend)
│
│ 1. Login request (username/password)
▼
Auth Service
│ Validates credentials
│ Issues JWT (contains userId, roles)
▼
User (Frontend)
│
│ 2. POST /projects (with JWT in Authorization header)
▼
API Gateway
│ Validates JWT (via Auth Service/public key)
│ Extracts userId
│ Forwards request → Project Service (with userId)
▼
Project Service
│ Saves project metadata (DB)
│ Calls File Service → to create starter files
▼
File Service
│ Creates project folder + main.java (hello world)
│ Returns success
▼
Project Service
│ Returns ProjectResponse
▼
API Gateway
│ Forwards response
▼
User (Frontend)
