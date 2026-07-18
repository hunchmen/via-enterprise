package com.via.ems.service;

import java.util.List;

import com.via.ems.dto.EmployeeDTO;


public interface EmployeeService {
    
    EmployeeDTO createEmployee(EmployeeDTO request);

    EmployeeDTO getEmployeeById(Long id);

    List<EmployeeDTO> getAllEmployee();

    EmployeeDTO updateEmployee(Long id, EmployeeDTO updatedEmployee);

    void deleteEmployee(Long id);

}
