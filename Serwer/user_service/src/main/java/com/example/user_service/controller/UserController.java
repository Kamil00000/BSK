package com.example.user_service.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;

import com.example.user_service.DTO.ActivationRequest;
import com.example.user_service.model.User;
import com.example.user_service.repository.UserRepository;
import com.example.user_service.service.UserServices;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserServices userService;
    @Autowired
    private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody @Valid User user,  BindingResult bindingResult) {
    	 if (bindingResult.hasErrors()) {
    	        Map<String, String> errors = new HashMap<>();
    	        bindingResult.getFieldErrors().forEach(error ->
    	            errors.put(error.getField(), error.getDefaultMessage())
    	        );
    	        return ResponseEntity.badRequest().body(errors);
    	    }

        try {
            userService.registerUser(user);
            Map<String, String> response = Map.of("message", "Rejestracja użytkownika pomyślna");
            	return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);        
            } catch (IllegalArgumentException e) {
            	return ResponseEntity.badRequest().body(e.getMessage());
            }
       }
    

    @GetMapping("/by-username/{username}")
    public ResponseEntity<Object> getUserByUsername(@PathVariable("username") String username) {
        if (username == null || username.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Nazwa użytkownika nie została podana"));
        }
        try {
            Optional<User> userOptional = userRepository.findByUsername(username);
            if (userOptional.isPresent()) {
                return ResponseEntity.ok(userOptional.get());
            } else {
                Map<String, String> error = Map.of("error", "Użytkownik o nazwie '" + username + "' nie został znaleziony");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = Map.of("error", "Wystąpił błąd podczas wyszukiwania użytkownika");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @GetMapping("/by-id/{id}")
    public ResponseEntity<Object> getUserByID(@PathVariable("id") Long id) {
    	 if (id == null) {
             return ResponseEntity.badRequest().body(Map.of("error", "ID użytkownika nie zostało podane"));
         }
        try {
            Optional<User> userOptional = userRepository.findByid(id);
            if (userOptional.isPresent()) {
                return ResponseEntity.ok(userOptional.get());
            } else {
                Map<String, String> error = Map.of("error", "Użytkownik o nazwie '" + id + "' nie został znaleziony");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }
        } catch (Exception e) {
            Map<String, String> error = Map.of("error", "Wystąpił błąd podczas wyszukiwania użytkownika");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PostMapping("/activate")
    public ResponseEntity<?> activateAccount(@RequestBody ActivationRequest request) {
        try {
            userService.activateUser(request.getUsername(), request.getCode());
            Map<String, String> response = Map.of("message", "Konto pomyślnie aktywowane.");
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);     
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }

   
}
