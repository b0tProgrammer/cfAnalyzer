package com.mohith.cfanalyzer.dao;

import com.mohith.cfanalyzer.model.People;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PeopleRepo extends JpaRepository<People, Integer> {
    People findByUserName(String userName);
    People findByEmail(String email);
}
