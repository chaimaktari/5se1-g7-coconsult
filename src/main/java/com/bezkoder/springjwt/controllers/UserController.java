package com.bezkoder.springjwt.controllers;

import com.bezkoder.springjwt.Service.UserService;
import com.bezkoder.springjwt.exceptions.UserNotFoundException;
import com.bezkoder.springjwt.models.ERole;
import com.bezkoder.springjwt.models.User;
import com.bezkoder.springjwt.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

//@CrossOrigin(origins = "*", maxAge = 3600)
//@CrossOrigin(origins = "*")

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private JavaMailSender emailSender;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private UserRepository userRepository;
    private String resetCode;

    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long userId) {
        userService.deleteUser(userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/forgotPassword")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        User user = userRepository.findByEmail(email).orElse(null);
        if (user != null) {
            // Generate random code
            resetCode = getRandomNumberString();

            // Send email with code
            try {
                sendEmail(email, resetCode);
                return ResponseEntity.ok("Reset code sent successfully");
            } catch (MailException e) {
                return ResponseEntity.badRequest().body("Failed to send reset code");
            }
        } else {
            return ResponseEntity.badRequest().body("User with provided email not found");
        }
    }

    @PostMapping("/resetPassword")
    public ResponseEntity<String> resetPassword(@RequestParam String email,
                                                @RequestParam String code,
                                                @RequestParam String newPassword) {
        // Verify code
        if (code.equals(resetCode)) {
            // Update password
            User user = userRepository.findByEmail(email).orElse(null);
            if (user != null) {
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
                return ResponseEntity.ok("Password reset successfully");
            } else {
                return ResponseEntity.badRequest().body("User with provided email not found");
            }
        } else {
            return ResponseEntity.badRequest().body("Invalid reset code");
        }
    }

    private void sendEmail(String email, String code) throws MailException {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Password Reset Code");
        message.setText("Your password reset code is: " + code);
        emailSender.send(message);
    }

    private String getRandomNumberString() {
        Random rnd = new Random();
        int number = rnd.nextInt(999999);
        return String.format("%06d", number);
    }
    @GetMapping("/count-by-role")
    public ResponseEntity<List<Object[]>> countUsersByRole() {
        List<Object[]> counts = userService.countUsersByRole();
        return ResponseEntity.ok(counts);
    }

    @GetMapping("/advanced-stats")
    public ResponseEntity<Map<String, Long>> getAdvancedUserStats() {
        long totalUsers = userRepository.count();
        long adminUsers = userRepository.countByRolesName(ERole.ROLE_ADMIN);
        long employeeUsers = userRepository.countByRolesName(ERole.ROLE_EMPLOYEE);
        // Ajoutez d'autres statistiques avancées selon vos besoins

        Map<String, Long> stats = new HashMap<>();
        stats.put("totalUsers", totalUsers);
        stats.put("adminUsers", adminUsers);
        stats.put("employeeUsers", employeeUsers);
        // Ajoutez d'autres statistiques avancées selon vos besoins

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/getbyusername/{username}")
    public ResponseEntity<User> getUserByUserName(@PathVariable String username) throws IOException {
        try{
            return new ResponseEntity<User>(userService.getUserByUserName(username), HttpStatus.OK);
        }catch (UserNotFoundException e){
            return new ResponseEntity("User not Found", HttpStatus.NOT_FOUND);
        }
    }
}