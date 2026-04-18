FROM eclipse-temurin:21-jdk-jammy as build
COPY . .
# Esta linha abaixo dá permissão de execução para o Maven Wrapper
RUN chmod +x mvnw
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:21-jre-jammy
# Ajuste o caminho do JAR se o nome do seu projeto for diferente
COPY --from=build /target/*.jar app.jar
EXPOSE 8080
# Limitando a memória para o plano free do Render não travar
ENTRYPOINT ["java","-Xmx384m","-Xms384m","-jar","/app.jar"]