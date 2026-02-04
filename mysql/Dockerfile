FROM mysql:8.0

USER root

# Install unzip (using microdnf because MySQL base is Oracle Linux)
RUN microdnf install -y unzip && microdnf clean all

# Copy initialization scripts into image (optional if using bind mount)
COPY ./init /docker-entrypoint-initdb.d
# COPY ./data /data/*.sql

# Ensure all shell scripts in init folder are executable
RUN chmod +x /docker-entrypoint-initdb.d/*.sh || true

USER mysql
