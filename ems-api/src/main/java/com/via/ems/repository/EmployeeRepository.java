package com.via.ems.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.via.ems.model.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long>{
    
}