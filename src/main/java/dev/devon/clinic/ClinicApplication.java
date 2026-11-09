package dev.devon.clinic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Profiles;

@SpringBootApplication
public class ClinicApplication {
    public static void main(String[] args) {
        var context = SpringApplication.run(ClinicApplication.class, args);
        if (context.getEnvironment().acceptsProfiles(Profiles.of("seed", "init-db"))) {
            context.close();
        }
    }
}
