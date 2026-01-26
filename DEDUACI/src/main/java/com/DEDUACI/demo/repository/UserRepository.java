package com.DEDUACI.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.DEDUACI.demo.model.User;



public interface UserRepository extends JpaRepository<User, Integer> {
    User findByUsername(String username); // optional

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
    User findByEmail(String email);
}
