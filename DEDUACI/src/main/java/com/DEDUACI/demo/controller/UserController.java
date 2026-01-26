package com.DEDUACI.demo.controller;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.DEDUACI.demo.dto.UserRegistrationDto;
import com.DEDUACI.demo.model.User;
import com.DEDUACI.demo.repository.UserRepository;
import com.DEDUACI.demo.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class UserController {

    @Autowired
    private UserService service;

    @Autowired
    private UserRepository userRepository;

    // Show user management page with list + empty form DTO for add
    @GetMapping("/user-management")
    public String showUserManagement(Model model, HttpServletRequest request, Authentication authentication,
                                     @RequestParam(value = "success", required = false) String success,
                                     @RequestParam(value = "error", required = false) String error) {

        // Get currently logged in user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findByUsername(username);
        String position = (user != null && user.getPosition() != null) ? user.getPosition().toUpperCase() : "";

        List<User> users;

        // Only ADMIN can view all users
        if ("ADMIN".equals(position)) {
            users = userRepository.findAll();
        } else {
            // Non-admin: only see their own user info (or restrict further as needed)
            users = Collections.singletonList(user);
        }

        model.addAttribute("users", users);
        model.addAttribute("user", new UserRegistrationDto());  // empty form for add

        String token = (String) request.getSession().getAttribute("jwtToken");
        model.addAttribute("token", token);
        model.addAttribute("sessionId", request.getSession().getId());
        model.addAttribute("position", position);

        if (authentication != null && authentication.isAuthenticated()) {
            model.addAttribute("username", authentication.getName());
        }

        if (success != null) {
            model.addAttribute("success", success);
        }
        if (error != null) {
            model.addAttribute("error", error);
        }

        return "user-management";
    }


    // Add new user (register)
    @PostMapping("/user-management/add")
    public String addUser(@ModelAttribute("user") UserRegistrationDto dto, Model model) {
        try {
            service.register(dto);
            return "redirect:/user-management?success=UserAdded";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", dto);
            model.addAttribute("users", userRepository.findAll());
            return "user-management";
        }
    }

    // Show edit form (same page, load user data into DTO)
    @GetMapping("/users/edit/{id}")
    public String showEditForm(@PathVariable Integer id, Model model) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            return "redirect:/user-management?error=UserNotFound";
        }
        User user = optionalUser.get();

        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setId(user.getId());  // make sure your DTO has setId(Integer)
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setTelephone(user.getTelephone());
        dto.setPosition(user.getPosition());
        dto.setGender(user.getGender() != null ? user.getGender().name() : null);
        dto.setDateOfBirth(user.getDateOfBirth() != null ? user.getDateOfBirth().toString() : null);
        // Password fields left empty for security

        model.addAttribute("user", dto);
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("editMode", true);  // to distinguish add/edit in your Thymeleaf template

        return "user-management";
    }

    // Update user - calls service method that handles validation & update
    @PostMapping("/users/edit/{id}")
    public String updateUser(@PathVariable Integer id, @ModelAttribute("user") UserRegistrationDto dto, Model model) {
        try {
            service.updateUser(id, dto);
            return "redirect:/user-management?success=UserUpdated";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", dto);
            model.addAttribute("users", userRepository.findAll());
            model.addAttribute("editMode", true);
            return "user-management";
        }
    }

    // Delete user
    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Integer id) {
        userRepository.deleteById(id);
        return "redirect:/user-management?success=UserDeleted";
    }
}
