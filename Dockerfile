# --- FASE 1: CONSTRUCCIÓN (BUILD) ---
# Usa una imagen base de JDK (Java Development Kit) para compilar.
FROM eclipse-temurin:21-jdk-alpine as build
WORKDIR /workspace/app

# Copiamos los scripts de Maven Wrapper necesarios para la compilación.
# Los scripts 'mvnw' y 'mvnw.cmd' son necesarios para ejecutar Maven.
COPY mvnw .
COPY mvnw.cmd .

# CRÍTICO: Creamos la estructura de carpetas y copiamos SÓLO el archivo de propiedades
# necesario de forma explícita. Esto soluciona el error 'not found' al copiar el directorio (.mvn) completo.
RUN mkdir -p .mvn/wrapper/
COPY .mvn/wrapper/maven-wrapper.properties .mvn/wrapper/

# Copiamos el archivo de configuración de Maven y el código fuente.
COPY pom.xml .
COPY src src

# Ejecutamos la compilación. Esto descarga dependencias y crea el JAR.
RUN ./mvnw install -DskipTests
# Extraemos las capas del JAR de Spring Boot para un cacheo más eficiente
RUN mkdir -p target/dependency && (cd target/dependency; jar -xf ../*.jar)

# ----------------- FASE 2: EJECUCIÓN (RUNTIME) -----------------
# Usamos una imagen base más ligera (solo JRE - Java Runtime Environment) para la ejecución.
FROM eclipse-temurin:21-jre-alpine
VOLUME /tmp

# El puerto 8080 es el estándar de Spring Boot. Render necesita el puerto 8080.
ENV PORT 8080
EXPOSE ${PORT}

# Definimos el argumento para la ruta de dependencias
ARG DEPENDENCY=/workspace/app/target/dependency

# Copiamos las capas del JAR ya separadas (código, librerías, metadata)
COPY --from=build ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY --from=build ${DEPENDENCY}/META-INF /app/META-INF
COPY --from=build ${DEPENDENCY}/BOOT-INF/classes /app

# Comando de inicio de la aplicación Spring Boot.
# Esto arranca tu aplicación en el puerto 8080.
ENTRYPOINT ["java","-cp","app:app/lib/*","com.lunchconnect.LunchconnectBackendApplication"]