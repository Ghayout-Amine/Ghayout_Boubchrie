package ma.ump.fso.datamining;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Point d'entree de l'application web Spring Boot.
 * Binome : Ghayout Amine & Boubechrie Rida — Mini-projet Data Mining M2I.
 * Lancer via IntelliJ ou {@code mvn spring-boot:run}, puis ouvrir http://localhost:8080
 */
@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
