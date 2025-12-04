# --- FASE 1: CONSTRUCCIÓN (BUILD) ---
# Usamos una imagen que ya incluye el binario de Java.
FROM eclipse-temurin:21-jdk-alpine as build
WORKDIR /workspace/app

# --- PASO CRÍTICO DE CORRECCIÓN: INSTALAR MAVEN ---
# Las imágenes 'alpine' no tienen 'mvn' por defecto.
# Usamos 'apk add' (el gestor de paquetes de Alpine) para instalar Maven.
RUN apk add --no-cache maven

# Ya no copiamos 'mvnw', 'mvnw.cmd', ni la carpeta '.mvn' porque no los necesitamos.

# Copiamos el archivo de configuración de Maven y el código fuente.
COPY pom.xml .
COPY src src

# CRÍTICO: Ejecutamos la compilación usando el binario 'mvn' (Maven Estándar)
# Ahora 'mvn' ya está instalado en el contenedor.
RUN mvn install -DskipTests
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