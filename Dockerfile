# Étape de build
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Installer Maven
RUN apt-get update && apt-get install -y maven

# Copier les fichiers du projet
COPY . .

# Construire l'application avec Maven
RUN mvn package -DskipTests

# Étape finale : exécuter l'application
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copier le JAR généré depuis l'étape de build
COPY --from=build /app/target/*.jar app.jar

# Exposer le port 8080
EXPOSE 8080

# Lancer l'application
CMD ["java", "-jar", "app.jar"]