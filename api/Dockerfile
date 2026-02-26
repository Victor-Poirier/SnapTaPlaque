# ==============================================================================
# Dockerfile for the SnapTaPlaque API
# ==============================================================================
# Builds a lightweight Python 3.10 container that serves the FastAPI-based
# license plate recognition API (YOLO + EasyOCR) via Uvicorn.
#
# Key components installed:
#   - System libraries required by OpenCV (libgl1, libglib2.0-0)
#   - Python dependencies from requirements.txt (FastAPI, SQLAlchemy, Alembic,
#     Ultralytics, EasyOCR, etc.)
#
# Build & run:
#   docker-compose down && docker-compose up --build
#
# Exposed port: 8000
# ==============================================================================

# Base image: Python 3.10 slim variant to minimise image size
FROM python:3.10-slim

# Set the working directory inside the container
WORKDIR /app

# Install system-level dependencies required by OpenCV (cv2):
#   - libgl1        : OpenGL library needed by cv2.imread / image processing
#   - libglib2.0-0  : GLib library dependency for OpenCV's GTK backend
# The apt cache is removed afterwards to keep the image layer small.
RUN apt-get update && apt-get install -y --no-install-recommends \
    libgl1 \
    libglib2.0-0 \
    && rm -rf /var/lib/apt/lists/*

# Copy and install Python dependencies first (leverages Docker layer caching;
# dependencies are only re-installed when requirements.txt changes)
COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Configure Ultralytics (YOLO) to store its config/cache in /tmp so that
# the directory is writable at runtime without extra permissions
ENV YOLO_CONFIG_DIR=/tmp/Ultralytics

# Copy the entire API source code into the container
COPY . .

# Start the FastAPI application using Uvicorn on all interfaces (0.0.0.0:8000)
CMD ["uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]
