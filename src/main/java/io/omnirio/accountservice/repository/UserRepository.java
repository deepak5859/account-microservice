package io.omnirio.accountservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.omnirio.accountservice.models.User;

public interface UserRepository extends JpaRepository<User, Long> {
	public User findByName(String userName);
}
