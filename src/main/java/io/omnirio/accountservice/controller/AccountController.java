package io.omnirio.accountservice.controller;

import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.transaction.Transactional;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

import io.omnirio.accountservice.models.Account;
import io.omnirio.accountservice.models.AuthenticationRequest;
import io.omnirio.accountservice.models.AuthenticationResponse;
import io.omnirio.accountservice.models.Role;
import io.omnirio.accountservice.models.User;
import io.omnirio.accountservice.repository.AccountRepository;
import io.omnirio.accountservice.service.MyUserDetailsService;
import io.omnirio.accountservice.util.JwtUtil;
import reactor.core.publisher.Mono;

@RestController
public class AccountController {
	@Autowired
	private AccountRepository accountRepository;

    @Autowired
    WebClient webClient;
    
    @Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtUtil jwtTokenUtil;

	@Autowired
	private MyUserDetailsService userDetailsService;
	
	@PostMapping("/authenticate")
	public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) throws Exception {
		try {
			authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
					authenticationRequest.getUsername(), authenticationRequest.getPassword()));
		} catch (BadCredentialsException e) {
			throw new Exception("Incorrect username or password", e);
		}

		final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
		final String jwt = jwtTokenUtil.generateToken(userDetails);

		return ResponseEntity.ok(new AuthenticationResponse(jwt));
	}

    @GetMapping("/account")
	List<Account> getAllAccounts() {
		return accountRepository.findAll();
	}
    
    @PostMapping("/account")
    @Transactional
	public void addAccount(@RequestBody String accountData) throws ParseException, java.text.ParseException {
    	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    	JSONObject accJsonObject = (JSONObject) new JSONParser().parse(accountData);
    	
    	if(accJsonObject != null && !accJsonObject.isEmpty()) {
    		User createdUser = null;
    		
    		if(accJsonObject.containsKey("user")) {
    			JSONObject userJsobObj = (JSONObject) accJsonObject.get("user");
    			
    			User user = new User();
    			user.setName(userJsobObj.get("name").toString());
    			user.setPhone(userJsobObj.get("phone").toString());
    			user.setGender(userJsobObj.get("gender").toString());
    			user.setBirthDate(dateFormat.parse(userJsobObj.get("birthDate").toString())); // Date.parse(userJsobObj.get("birthDate").toString()));
    			
    			Set<Role> roles = new HashSet<>();
    			
    			if(userJsobObj.containsKey("userRole")) {
    				JSONArray jsonArrayRoles = (JSONArray) userJsobObj.get("userRole");
    				
    				for(int i = 0; i < jsonArrayRoles.size(); i++) {
    					JSONObject roleJson = (JSONObject) jsonArrayRoles.get(i);
    					
    					Role role = new Role();
    					role.setId(((Long)roleJson.get("id")).intValue());
    					role.setName(roleJson.get("name").toString());
    					role.setCode(roleJson.get("code").toString());
    					
    					roles.add(role);
    				}
    			}
    			
    			user.setUserRole(roles);
    			
    			Mono<User> createdUserMono = webClient.post()
    			        .uri("http://customer-microservice/customer/")
    			        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
    			        .body(Mono.just(user), User.class)
    			        .retrieve()
    			        .bodyToMono(User.class);
    			
    			createdUser = createdUserMono.block();
    			
    			System.out.println(createdUser.toString());
    		}
    		
    		Account newAccount = new Account();
    		newAccount.setName(accJsonObject.get("name").toString());
    		newAccount.setType(accJsonObject.get("type").toString());
    		newAccount.setOpenDate(dateFormat.parse(accJsonObject.get("openDate").toString()));
    		newAccount.setBranch(accJsonObject.get("branch").toString());
    		newAccount.setMinor(Boolean.valueOf(accJsonObject.get("isMinor").toString()));
    		
    		if(createdUser != null) {
    			newAccount.setUser(createdUser);
    		}
    		
    		accountRepository.save(newAccount);
    		
    		System.out.println("User Account created successfully!!!");
    	}
    	
	}
}

/*
Alternative WebClient way
Movie movie = webClientBuilder.build().get().uri("http://localhost:8082/movies/"+ rating.getMovieId())
.retrieve().bodyToMono(Movie.class).block();
*/