# Docker Build & Deploy Guide

This guide explains various ways to deploy your application, including using Docker Hub (Registry) and direct server deployment methods without a registry.

---

## Method 1: Using Docker Hub (Standard & Recommended)

This is the standard approach: `Local Build -> Push to Registry -> Pull on Server`.

### 1. Build and Push Image
On your local machine:
```bash
# Login to Docker Hub
docker login

# Build the image (replace 'your-username' below)
docker build -t your-username/restapi:latest .

# Push to Docker Hub
docker push your-username/restapi:latest
```

### 2. Deploy on Server
On your remote server:
1.  Create a `docker-compose.yml` file (see example below).
2.  Run the application:
    ```bash
    docker-compose up -d
    ```

**Server `docker-compose.yml` Example:**
```yaml
version: '3.8'
services:
  mysqldb:
    image: mysql:5.7
    environment:
      MYSQL_ROOT_PASSWORD: root
      MYSQL_DATABASE: restapi
    volumes:
      - db_data:/var/lib/mysql
    restart: always

  app:
    image: your-username/restapi:latest  # Pulls from Docker Hub
    ports:
      - "80:8080"
    depends_on:
      - mysqldb
    restart: always
volumes:
  db_data:
```

---

## Method 2: Offline / Direct Transfer (No Docker Hub)

If you cannot or do not want to use Docker Hub, use one of these two options.

### Option A: Transfer Source Code & Build on Server
Send your project files to the server and build the Docker image there. This consumes CPU/RAM on the server during build.

1.  **Transfer Files:** Copy your project folder to the server (using SCP, FileZilla, or Git Clone).
2.  **Run with Build:**
    On the server, inside the project folder, run:
    ```bash
    docker-compose up -d --build
    ```
    *Note: Ensure your `docker-compose.yml` on the server has `build: .` instead of `image: ...`.*

### Option B: Transfer Image as File (docker save/load)
Build the image locally, save it significantly as a `.tar` file, upload it, and load it on the server. Best for air-gapped servers (no internet).

1.  **Build & Save (Local Machine):**
    ```bash
    # Build the image
    docker build -t restapi_offline:latest .

    # Save image to a .tar file
    docker save -o restapi_image.tar restapi_offline:latest
    ```

2.  **Transfer File:**
    Upload `restapi_image.tar` and `docker-compose.yml` to your server.

3.  **Load & Run (Server):**
    ```bash
    # Load the image from the file
    docker load -i restapi_image.tar

    # Update docker-compose.yml to use 'image: restapi_offline:latest'
    # Then run:
    docker-compose up -d
    ```
