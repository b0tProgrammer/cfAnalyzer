package com.mohith.cfanalyzer.Controller;

import com.mohith.cfanalyzer.model.CodeSender;
import com.mohith.cfanalyzer.model.PassWord;
import com.mohith.cfanalyzer.model.People;
import com.mohith.cfanalyzer.model.Student;
import com.mohith.cfanalyzer.serivce.JwtService;
import com.mohith.cfanalyzer.serivce.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;


@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtToken;

    @Autowired
    private AuthenticationManager authManager;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody People user) {
        if (Objects.equals(user.getRole(), "USER") && !userService.isValidMentor(user))
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        if (userService.isThere(user)) {
            return new ResponseEntity<>(HttpStatus.ALREADY_REPORTED);
        }
        People p = userService.add(user);
        return new ResponseEntity<>(p, HttpStatus.OK);
    }

    @GetMapping("/users")
    public ResponseEntity<?> getUsers() {
        return new ResponseEntity<>(userService.getAll(), HttpStatus.OK);
    }

    @PostMapping("/signIn")
    public ResponseEntity<?> verifyUser(@RequestBody People user) {
        boolean valid = userService.isCorrect(user);
//        System.out.println(user);   
        if(!valid) return new  ResponseEntity<>(HttpStatus.BAD_REQUEST);
        Authentication authentication = authManager
                .authenticate(new UsernamePasswordAuthenticationToken(user.getUserName(), user.getPassword()));
        if (authentication.isAuthenticated()) {
            return new ResponseEntity<>(jwtToken.getToken(user.getUserName()), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/{userName}/isAdmin")
    public ResponseEntity<?> getIsAdmin(@PathVariable String userName) {
        return new ResponseEntity<>(userService.getAdminOrUserDetails(userName),HttpStatus.OK);
    }
    
    @PostMapping("/{mentorName}/addStudent")
    public ResponseEntity<?> addStudent(@PathVariable String mentorName,@RequestBody Student user) {
        boolean status =  userService.addStudent(user,mentorName);
        if(!status) return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/{userName}/changeAdmin")
    public ResponseEntity<?> changeAdmin(@PathVariable String userName, @RequestBody People user) {
        Authentication authentication = authManager
                .authenticate(new UsernamePasswordAuthenticationToken(user.getUserName(), user.getPassword()));
        boolean status = false;
        if (authentication.isAuthenticated())
            status = userService.changeAdmin(userName,user.getMentorName());
        if(status) return new ResponseEntity<>(HttpStatus.OK);
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping("/{userName}/delete")
    public ResponseEntity<?> deleteUser(@PathVariable String userName,@RequestBody People mentorName) { 
        boolean status = userService.deleteUser(userName,mentorName.getUserName());
        if(status) return new ResponseEntity<>(HttpStatus.OK);
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/{userName}/changePassword")
    public  ResponseEntity<?> changePassword(@PathVariable String userName,@RequestBody PassWord password) {
        Authentication authentication = authManager
                .authenticate(new UsernamePasswordAuthenticationToken(userName,password.getOldPassWord()));
        if(authentication.isAuthenticated()) {
            boolean status = userService.changePassWord(userName, password);
            if(status) return new ResponseEntity<>(HttpStatus.OK);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @PostMapping("/verifyEmail")
    public ResponseEntity<?> verifyEmail(@RequestBody CodeSender email) {
        if(userService.verifiedEmail(email.getEmail())) {
            userService.sendCode(email);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/verifyCode")
    public ResponseEntity<?> verifyCode(@RequestBody CodeSender email) {
        if(userService.authenticated(email))
            return new ResponseEntity<>(HttpStatus.OK);
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }
}
