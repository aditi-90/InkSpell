# ==============================================================================
# Multi-stage Docker build for InkSpell (Java 21 + Jakarta Servlet 6 + Tomcat 10)
# Production-ready for Railway, Render, Fly.io, AWS App Runner, and Docker
# ==============================================================================

# Stage 1: Build the Application WAR
FROM maven:3.9.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
COPY db ./db
RUN mvn clean package -DskipTests

# Stage 2: Production Tomcat 10.1 Runtime
FROM tomcat:10.1-jdk21-temurin
WORKDIR /usr/local/tomcat

# Remove default Tomcat webapps for security hardening
RUN rm -rf webapps/*

# Deploy compiled ROOT.war to root context
COPY --from=builder /app/target/ROOT.war webapps/ROOT.war
COPY db ./db

# Environment defaults
ENV PORT=8080
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

EXPOSE 8080

# Dynamically bind Tomcat to $PORT (required by cloud hosts like Railway/Render)
CMD ["sh", "-c", "sed -i \"s/port=\\\"8080\\\"/port=\\\"${PORT}\\\"/g\" conf/server.xml && catalina.sh run"]
