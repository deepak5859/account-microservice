package io.omnirio.accountservice.service;

import java.util.ArrayList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import io.omnirio.accountservice.repository.UserRepository;

@Service
public class MyUserDetailsService implements UserDetailsService {

	@Autowired
	UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
		io.omnirio.accountservice.models.User user = userRepository.findByName(userName);
		
		if(user != null) {
			return new User(user.getName(), user.getName(), new ArrayList<>());
		}
		
		return new User("", "", new ArrayList<>());
	}
}