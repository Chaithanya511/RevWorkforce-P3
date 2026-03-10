package com.rev.user_service.config;

import com.rev.user_service.entity.User;
import com.rev.user_service.entity.Role;
import com.rev.user_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            
            // Karthik Nalla - ADMIN
            User karthik = new User();
            karthik.setEmployeeId("EMP001");
            karthik.setEmail("karthik@revworkforce.com");
            karthik.setFirstName("Karthik");
            karthik.setLastName("Nalla");
            karthik.setPassword(passwordEncoder.encode("admin123"));
            karthik.setPhone("9876543210");
            karthik.setDepartment("HR");
            karthik.setDesignation("Admin");
            karthik.setDepartmentId(1L);
            karthik.setDesignationId(1L);
            karthik.setRole(Role.ADMIN);
            karthik.setActive(true);
            karthik.setJoiningDate(LocalDate.now());
            userRepository.save(karthik);

            // Chaithanya Palamani - MANAGER
            User chaithanya = new User();
            chaithanya.setEmployeeId("EMP002");
            chaithanya.setEmail("chaithanya@revworkforce.com");
            chaithanya.setFirstName("Chaithanya");
            chaithanya.setLastName("Palamani");
            chaithanya.setPassword(passwordEncoder.encode("admin123"));
            chaithanya.setPhone("9876543211");
            chaithanya.setDepartment("HR");
            chaithanya.setDesignation("Manager");
            chaithanya.setDepartmentId(1L);
            chaithanya.setDesignationId(1L);
            chaithanya.setRole(Role.MANAGER);
            chaithanya.setActive(true);
            chaithanya.setJoiningDate(LocalDate.now());
            userRepository.save(chaithanya);

            // John Doe - EMPLOYEE
            User john = new User();
            john.setEmployeeId("EMP003");
            john.setEmail("john@revworkforce.com");
            john.setFirstName("John");
            john.setLastName("Doe");
            john.setPassword(passwordEncoder.encode("admin123"));
            john.setPhone("9876543212");
            john.setDepartment("HR");
            john.setDesignation("Software Engineer");
            john.setDepartmentId(1L);
            john.setDesignationId(2L);
            john.setManagerId(2L); // Managed by Chaithanya
            john.setRole(Role.EMPLOYEE);
            john.setActive(true);
            john.setJoiningDate(LocalDate.now());
            userRepository.save(john);
        } else {
            // Ensure live DB data is updated if already seeded
            userRepository.findByEmail("karthik@revworkforce.com").ifPresent(k -> {
                if (!"Admin".equals(k.getDesignation())) {
                    k.setDesignation("Admin");
                    k.setRole(Role.ADMIN);
                    userRepository.save(k);
                }
            });
        }
    }
}
