 package com.DEDUACI.demo.service;

	import java.time.LocalDate;
	import java.time.format.DateTimeParseException;
	
	import org.springframework.beans.factory.annotation.Autowired;
	import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
	import org.springframework.stereotype.Service;

	import com.DEDUACI.demo.dto.UserRegistrationDto;
	import com.DEDUACI.demo.model.User;
	import com.DEDUACI.demo.repository.UserRepository;
	

	
	@Service
	public class UserService {

    @Autowired
    private UserRepository repository;

    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder(12);

    public User register(UserRegistrationDto dto) {
        if (repository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (repository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        LocalDate dob = null;
        if (dto.getDateOfBirth() != null && !dto.getDateOfBirth().isEmpty()) {
            try {
                dob = LocalDate.parse(dto.getDateOfBirth());
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException("Invalid date of birth format");
            }
        }

        User.Gender genderEnum = null;
        if (dto.getGender() != null && !dto.getGender().isEmpty()) {
            try {
                genderEnum = User.Gender.valueOf(dto.getGender().toUpperCase());
            } catch (IllegalArgumentException ignored) {
                genderEnum = User.Gender.OTHER;
            }
        }

        User user = new User();
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setTelephone(dto.getTelephone());
        user.setPosition(dto.getPosition());
        user.setGender(genderEnum);
        user.setDateOfBirth(dob);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setActive(true);
        user.setRegistrationDate(LocalDate.now());

        return repository.save(user);
    }

    // New method to update user
    public User updateUser(Integer id, UserRegistrationDto dto) {
        User user = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Update user fields from dto
        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setTelephone(dto.getTelephone());
        user.setPosition(dto.getPosition());

        // Parse gender
        if (dto.getGender() != null && !dto.getGender().isEmpty()) {
            try {
                user.setGender(User.Gender.valueOf(dto.getGender().toUpperCase()));
            } catch (IllegalArgumentException e) {
                user.setGender(User.Gender.OTHER);
            }
        } else {
            user.setGender(null);
        }

        // Parse date of birth
        if (dto.getDateOfBirth() != null && !dto.getDateOfBirth().isEmpty()) {
            try {
                user.setDateOfBirth(LocalDate.parse(dto.getDateOfBirth()));
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid date of birth format");
            }
        } else {
            user.setDateOfBirth(null);
        }

        // You might want to update the password conditionally, if present and matches confirmation
        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            if (!dto.getPassword().equals(dto.getConfirmPassword())) {
                throw new IllegalArgumentException("Passwords do not match");
            }
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        return repository.save(user);
    }
}