# syntax=docker/dockerfile:1
#
# Build context esperado: la raiz del repositorio (household-finance-platform).
# Ejemplo: docker build -f household-finance-service/finance-bootstrap/Dockerfile -t household-finance-service .

########################  Etapa de build  ########################
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Copiamos primero los descriptores Maven para aprovechar la cache de capas.
COPY mvnw mvnw.cmd /workspace/
COPY .mvn /workspace/.mvn
COPY pom.xml /workspace/pom.xml
COPY household-finance-service/pom.xml /workspace/household-finance-service/pom.xml
COPY household-finance-service/finance-domain/pom.xml /workspace/household-finance-service/finance-domain/pom.xml
COPY household-finance-service/finance-application/pom.xml /workspace/household-finance-service/finance-application/pom.xml
COPY household-finance-service/finance-adapters/pom.xml /workspace/household-finance-service/finance-adapters/pom.xml
COPY household-finance-service/finance-bootstrap/pom.xml /workspace/household-finance-service/finance-bootstrap/pom.xml
COPY identity-service/pom.xml /workspace/identity-service/pom.xml
COPY identity-service/identity-domain/pom.xml /workspace/identity-service/identity-domain/pom.xml
COPY identity-service/identity-application/pom.xml /workspace/identity-service/identity-application/pom.xml
COPY identity-service/identity-adapters/pom.xml /workspace/identity-service/identity-adapters/pom.xml
COPY identity-service/identity-bootstrap/pom.xml /workspace/identity-service/identity-bootstrap/pom.xml
COPY architecture-tests/pom.xml /workspace/architecture-tests/pom.xml

RUN chmod +x /workspace/mvnw

# Copiamos el codigo fuente y compilamos el artefacto ejecutable, sin tests (se ejecutan en CI).
COPY household-finance-service/finance-domain/src /workspace/household-finance-service/finance-domain/src
COPY household-finance-service/finance-application/src /workspace/household-finance-service/finance-application/src
COPY household-finance-service/finance-adapters/src /workspace/household-finance-service/finance-adapters/src
COPY household-finance-service/finance-bootstrap/src /workspace/household-finance-service/finance-bootstrap/src

RUN /workspace/mvnw -q -f /workspace/pom.xml -pl household-finance-service/finance-bootstrap -am \
    -DskipTests package

########################  Etapa de runtime  ########################
FROM eclipse-temurin:21-jre AS runtime

RUN addgroup --system spring && adduser --system --ingroup spring spring
USER spring:spring

WORKDIR /app
COPY --from=build /workspace/household-finance-service/finance-bootstrap/target/household-finance-service.jar /app/household-finance-service.jar

ENV JAVA_OPTS=""
EXPOSE 8082

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD wget -q -O - http://localhost:8082/actuator/health/liveness || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/household-finance-service.jar"]
