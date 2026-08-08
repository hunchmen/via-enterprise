package com.via.ems.service;

import com.via.ems.dto.EmployeeDTO;
import java.util.List;

public interface EmployeeService {

    EmployeeDTO createEmployee(EmployeeDTO request);

    EmployeeDTO getEmployeeById(Long id);

    List<EmployeeDTO> getAllEmployee();

    EmployeeDTO updateEmployee(Long id, EmployeeDTO updatedEmployee);

    void deleteEmployee(Long id);
}
