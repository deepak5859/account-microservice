package io.omnirio.accountservice.controller;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
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
import io.omnirio.accountservice.repository.UserRepository;
import io.omnirio.accountservice.service.MyUserDetailsService;
import io.omnirio.accountservice.util.DateUtil;
import io.omnirio.accountservice.util.JwtUtil;
import reactor.core.publisher.Mono;

@RestController
public class AccountController {
	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private UserRepository userRepository;
	
    @Autowired
    WebClient webClient;
    
    @Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private JwtUtil jwtTokenUtil;

	@Autowired
	private MyUserDetailsService userDetailsService;
	
	@GetMapping("/")
	public String getWelcomeMessage() {
		return "Account Microservice";
	}
	
	private UserDetails userDetails = null;
	
	@PostMapping("/authenticate")
	public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) throws Exception {
		try {
			authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(
					authenticationRequest.getUsername(), authenticationRequest.getPassword()));
		} catch (BadCredentialsException e) {
			throw new Exception("Incorrect username or password", e);
		}

		this.userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
		final String jwt = jwtTokenUtil.generateToken(userDetails);

		return ResponseEntity.ok(new AuthenticationResponse(jwt));
	}

    @GetMapping("/account")
	List<Account> getAllAccounts() {
    	String userName = userDetails.getUsername();
    	
    	User user = userRepository.findByName(userName);
    	for(Role role : user.getUserRole()) {
    		if(role.getCode().equalsIgnoreCase("MGR")) {
    			return accountRepository.findAll();
    		}
    	}
    	
    	return Collections.singletonList(accountRepository.findByUser_id(user.getId()));
	}
    
    @PostMapping("/account")
    @Transactional
	public String addAccount(@RequestBody String accountData) throws ParseException, java.text.ParseException {
    	try {
    		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        	JSONObject accJsonObject = (JSONObject) new JSONParser().parse(accountData);
        	Date birthDate = null;
        	
        	if(accJsonObject != null && !accJsonObject.isEmpty()) {
        		User createdUser = null;
        		
        		if(accJsonObject.containsKey("user")) {
        			JSONObject userJsobObj = (JSONObject) accJsonObject.get("user");
        			birthDate = dateFormat.parse(userJsobObj.get("birthDate").toString());
        			
        			User user = new User();
        			user.setName(userJsobObj.get("name").toString());
        			user.setPhone(userJsobObj.get("phone").toString());
        			user.setGender(userJsobObj.get("gender").toString());
        			user.setBirthDate(birthDate);
        			
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
        		newAccount.setMinor(DateUtil.isMinor(birthDate));
        		
        		if(createdUser != null) {
        			newAccount.setUser(createdUser);
        		}
        		
        		accountRepository.save(newAccount);
        		
        		return "User Account created successfully!!!";
        	}	
    	} catch (Exception e) {
    		e.printStackTrace();
		}
    	
    	return "Unable to Create User Account";
	}
}