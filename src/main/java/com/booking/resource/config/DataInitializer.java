package com.booking.resource.config;

import com.booking.resource.entity.*;
import com.booking.resource.repository.ReservationRepository;
import com.booking.resource.repository.ResourceRepository;
import com.booking.resource.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ResourceRepository resourceRepository;
    private final ReservationRepository reservationRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(
            UserRepository userRepository,
            ResourceRepository resourceRepository,
            ReservationRepository reservationRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.resourceRepository = resourceRepository;
        this.reservationRepository = reservationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        // Seed Users
        User adminUser = userRepository.findByUsername("admin").orElseGet(() -> {
            User admin = new User(
                    "admin",
                    passwordEncoder.encode("admin123"),
                    "admin@booking.com",
                    Role.ROLE_ADMIN
            );
            return userRepository.save(admin);
        });

        User standardUser = userRepository.findByUsername("user").orElseGet(() -> {
            User user = new User(
                    "user",
                    passwordEncoder.encode("user123"),
                    "user@booking.com",
                    Role.ROLE_USER
            );
            return userRepository.save(user);
        });

        // Seed Resources
        if (resourceRepository.count() == 0) {
            Resource r1 = resourceRepository.save(new Resource(
                    "Executive Conference Room A",
                    "Room",
                    "Spacious room equipped with video conferencing and smart board",
                    12,
                    new BigDecimal("50.00"),
                    true
            ));

            Resource r2 = resourceRepository.save(new Resource(
                    "Tesla Model 3 - EV-01",
                    "Vehicle",
                    "Electric vehicle for client transport and business trips",
                    5,
                    new BigDecimal("75.00"),
                    true
            ));

            Resource r3 = resourceRepository.save(new Resource(
                    "Epson 4K Pro Cinema Projector",
                    "Equipment",
                    "High-definition portable projector for presentations",
                    1,
                    new BigDecimal("25.00"),
                    true
            ));

            // Seed Sample Reservations
            if (reservationRepository.count() == 0) {
                LocalDateTime now = LocalDateTime.now();

                reservationRepository.save(new Reservation(
                        standardUser,
                        r1,
                        now.plusDays(1).withHour(9).withMinute(0),
                        now.plusDays(1).withHour(11).withMinute(0),
                        ReservationStatus.CONFIRMED,
                        new BigDecimal("100.00")
                ));

                reservationRepository.save(new Reservation(
                        standardUser,
                        r2,
                        now.plusDays(2).withHour(10).withMinute(0),
                        now.plusDays(2).withHour(14).withMinute(0),
                        ReservationStatus.PENDING,
                        new BigDecimal("300.00")
                ));

                reservationRepository.save(new Reservation(
                        adminUser,
                        r3,
                        now.plusDays(3).withHour(14).withMinute(0),
                        now.plusDays(3).withHour(16).withMinute(0),
                        ReservationStatus.CANCELLED,
                        new BigDecimal("50.00")
                ));
            }
        }
    }
}
