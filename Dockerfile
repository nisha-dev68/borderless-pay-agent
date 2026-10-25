FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY src ./src
RUN mkdir out && javac -d out src/borderless_pay_agent/*.java
CMD ["java", "-cp", "out", "borderless_pay_agent.WebServer"]