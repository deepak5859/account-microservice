package io.omnirio.accountservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.omnirio.accountservice.models.Account;

public interface AccountRepository extends JpaRepository<Account, Long> {
	
}
