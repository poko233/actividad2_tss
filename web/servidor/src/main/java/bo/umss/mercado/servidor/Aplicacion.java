package bo.umss.mercado.servidor;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// Aplicacion: punto de entrada. Esta clase + application.properties
// es TODO lo necesario para levantar servidor web + base de datos.
// Se corre con:  mvn spring-boot:run   y se abre http://localhost:8080
@SpringBootApplication
public class Aplicacion {

    public static void main(String[] args) {
        SpringApplication.run(Aplicacion.class, args);
    }
}
