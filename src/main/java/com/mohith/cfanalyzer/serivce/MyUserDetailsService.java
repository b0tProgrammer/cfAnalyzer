package com.mohith.cfanalyzer.serivce;

import com.mohith.cfanalyzer.dao.PeopleRepo;
import com.mohith.cfanalyzer.model.People;
import com.mohith.cfanalyzer.model.UserPrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private PeopleRepo peopleRepo;

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        People p = peopleRepo.findByUserName(userName);
        if(p==null) {
            throw new UsernameNotFoundException(userName);
        }
        return new UserPrincipal(p);
    }
}
