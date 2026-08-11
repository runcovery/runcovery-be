package com.likelion14.runcovery.repository;

import com.likelion14.runcovery.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
