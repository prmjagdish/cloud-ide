📂 Project Service – Working Guide

The Project Service is responsible for managing user projects in the Cloud IDE.
It mainly handles project metadata (name, language, build tool, timestamps) and coordinates with the File Service to create or delete files.

Think of it as the "manager" for all projects – while the File Service is the "worker" that actually builds and stores the code files.

🔄 How It Works

User sends request via API Gateway → project-service

project-service validates the request and ensures the userId matches the project owner

If creating a project:

Save project in DB

Call file-service → /files/bootstrap to set up starter files

If file-service fails → rollback DB entry

For reading/updating/deleting:

Validate project ownership

Perform DB operation

For delete → also notify file-service to delete files

📌 APIs & Their Flow
1. Create Project

Flow:
User → ProjectService → DB Save → FileService Bootstrap → Response

POST /projects
Headers: userId: <UUID>
Body:
{
"name": "MyProject",
"language": "JAVA",
"buildTool": "MAVEN"
}


✔️ Saves project → calls File Service → returns ProjectResponse.

2. Get All Projects

Flow:
User → ProjectService → Fetch all projects by ownerId → Response

GET /projects
Headers: userId: <UUID>


✔️ Returns list of all user-owned projects.
Later we can add pagination & filters.

3. Get Single Project

Flow:
User → ProjectService → Find by projectId + ownerId → Response

GET /projects/{projectId}
Headers: userId: <UUID>


✔️ Ensures the project belongs to user before returning.

4. Update Project (Partial)

Flow:
User → ProjectService → Validate ownership → Update allowed fields → Save → Response

PATCH /projects/{projectId}
Headers: userId: <UUID>
Body:
{
"name": "Renamed Project"
}


✔️ Currently supports renaming or updating metadata (not touching files yet).

5. Delete Project

Flow:
User → ProjectService → Validate ownership → Delete from DB → Call File Service → Response

DELETE /projects/{projectId}
Headers: userId: <UUID>


✔️ Safely removes both DB record and associated files.
✔️ Protects against deleting another user’s project.

⚠️ Important Rules

Every request must include userId in headers (comes from Auth Service in real flow).

Project ownership is always validated before reading/updating/deleting.

If File Service fails during create/delete → rollback changes.

API responses are always wrapped in ProjectResponse DTO (clean, no DB entities leaked).




🔗 File Service Client APIs (Used by Project Service)

The Project Service doesn’t manage files directly.
Instead, it calls the File Service via Feign Client.
These APIs explain what is expected from File Service.

1. Bootstrap Files for New Project

Called by: ProjectService after saving new project in DB.

POST /files/bootstrap
Body:
{
"projectId": "<UUID>",
"language": "JAVA",
"buildTool": "MAVEN"
}


✔️ File Service creates a starter project structure (e.g., pom.xml, Main.java)

2. Delete All Files for a Project

Called by: ProjectService when deleting a project.

DELETE /files/{projectId}


✔️ Removes all project files from File Service storage.
✔️ Prevents orphaned data.

3. (Future) Sync / Refresh Project Files

Use case: If project metadata changes (like switching language).

POST /files/sync
Body:
{
"projectId": "<UUID>",
"newLanguage": "KOTLIN",
"newBuildTool": "GRADLE"
}


✔️ File Service updates structure accordingly.
⚠️ Not implemented yet — can be contributed later.

🧑‍💻 Why Use a Client Instead of Direct Code?

Imagine if Project Service also had to manage file creation, deletion, and syncing:

The service would be bloated (too many responsibilities).

Any change in file storage logic would require changes here too.

It breaks the Single Responsibility Principle.

By using a client (Feign):

Project Service just tells File Service what to do

File Service remains the single source of truth for files

Both services stay independent and scalable

✨ Example – How APIs Work Together

Case: User creates project

User calls → POST /projects

Project Service saves project in DB

Project Service calls → POST /files/bootstrap

File Service sets up folder/files

Final response sent back to user

So the user only talks to Project Service,
but behind the scenes, Project Service → File Service communication happens automatically.