package com.bankofgeorgia.customer.config;

import com.bankofgeorgia.customer.model.Employee;
import com.bankofgeorgia.customer.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class EmployeeSeeder {

    private static final Logger log = LoggerFactory.getLogger(EmployeeSeeder.class);

    @Bean
    CommandLineRunner seedEmployees(EmployeeRepository repo, PasswordEncoder encoder) {
        return args -> {
            if (repo.count() > 0) {
                return;
            }
            repo.save(new Employee("Admin",  "admin@bankofga.com",  encoder.encode("admin123")));
            repo.save(new Employee("Quincy", "quincy@bankofga.com", encoder.encode("demo123")));
            repo.save(new Employee("Ben",    "ben@bankofga.com",    encoder.encode("demo123")));
            log.info("Seeded 3 employees");
        };
    }
}
