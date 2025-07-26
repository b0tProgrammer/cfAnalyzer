package com.mohith.cfanalyzer.serivce;

import com.mohith.cfanalyzer.dao.PeopleRepo;
import com.mohith.cfanalyzer.model.CodeSender;
import com.mohith.cfanalyzer.model.PassWord;
import com.mohith.cfanalyzer.model.People;
import com.mohith.cfanalyzer.model.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserService {

    @Autowired
    private PeopleRepo peopleRepo;
    private BCryptPasswordEncoder encoder = new BCryptPasswordEncoder(12);

    @Autowired
    private EmailService emailService;
    private static String code;

    public People add(People user) {
        user.setPassword(encoder.encode(user.getPassword()));
        peopleRepo.save(user);
        return user;
    }

    public boolean isThere(People user) {
        return peopleRepo.findByUserName(user.getUserName()) != null;
    }

    public List<People> getAll() {
        return peopleRepo.findAll();
    }

    public boolean isValidMentor(People user) {
        return peopleRepo.findByUserName(user.getMentorName()) != null;
    }

    public People getAdminOrUserDetails(String userName) {
        People user = peopleRepo.findByUserName(userName);
        if(Objects.equals(user.getRole(), "ADMIN")) {
            return user;
        }
        return peopleRepo.findByUserName(user.getMentorName());
    }

    public boolean addStudent(Student student, String mentorName) {
        People mentor  = peopleRepo.findByUserName(mentorName);
        Set<String> currStudents = mentor.getSet();
        if(currStudents != null && currStudents.contains(student.getName())) return false;
        else {
            if(currStudents == null) currStudents = new HashSet<>();
            currStudents.add(student.getName());
            mentor.setSet(currStudents);
        }
        List<String> handles = mentor.getHandles();
        if(handles == null) handles = new ArrayList<>();
        String studentName = student.getName();
        String handle = student.getHandle();
        String studentDetails = studentName + "*" + handle;
        handles.add(studentDetails);
        mentor.setHandles(handles);
        peopleRepo.save(mentor);
        return true;
    }

    public boolean isCorrect(People thisPerson) {
        People thatPerson = peopleRepo.findByUserName(thisPerson.getUserName());
        return Objects.equals(thatPerson.getRole(), thisPerson.getRole()) && Objects.equals(thatPerson.getEmail(), thisPerson.getEmail());
    }

    public boolean changeAdmin(String userName,String mentorName) {
        People user =  peopleRepo.findByUserName(userName);
        user.setMentorName(mentorName);
        peopleRepo.save(user);
        return true;
    }

    public boolean deleteUser(String userName,String mentorName) {
        People mentor =  peopleRepo.findByUserName(mentorName);
        Set<String> currentHandles = mentor.getSet();
        List<String> handles = mentor.getHandles();
        o:for(String handle : handles) {
            for(int i = 0; i < handles.size(); i++) {
                if(handle.startsWith(userName)) {
                    StringBuilder sb = new StringBuilder();
                    for(int j = 0; j < handle.length(); j++) {
                        if(handle.charAt(j) == '*') break;
                        sb.append(handle.charAt(j));
                    }
                    if(sb.toString().equals(userName)) {
                        handles.remove(handle);
                        break o;
                    }
                }
            }
        }
        currentHandles.remove(userName);
        mentor.setSet(currentHandles);
        mentor.setHandles(handles);
        peopleRepo.save(mentor);
        return true;
    }

    public boolean changePassWord(String userName, PassWord password) {
        People user =  peopleRepo.findByUserName(userName);
        if(user == null)  return false;
        user.setPassword(encoder.encode(password.getNewPassWord()));
        peopleRepo.save(user);
        return true;
    }

    public boolean verifiedEmail(String email) {
        return peopleRepo.findByEmail(email) != null;
    }

    public void sendCode(CodeSender email) {
        String to = email.getEmail();
        int random = (int) (Math.random() * 1000000);
        String code = Integer.toString(random);
        setCode(code);
        String subject = code + " Your CF analyzer reset code";
        String body = "Enter this code to change your password "+code + " Your CF analyzer reset code";
        emailService.sendSimpleEmail(to, subject, body);
    }

    private static void setCode(String random) {code = random;}


    public boolean authenticated(CodeSender email) {
        People user = peopleRepo.findByEmail(email.getEmail());
        if(user == null) return false;
        if(!email.getCode().equals(code)) return false;
        user.setPassword(encoder.encode(email.getPassword()));
        peopleRepo.save(user);
        return true;
    }
}