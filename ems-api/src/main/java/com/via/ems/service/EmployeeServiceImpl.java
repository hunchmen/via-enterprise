package com.via.ems.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.via.ems.dto.EmployeeDTO;
import com.via.ems.exception.ResourceNotFoundException;
import com.via.ems.mapper.EmployeeMapper;
import com.via.ems.model.Employee;
import com.via.ems.repository.EmployeeRepository;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private EmployeeRepository employeeRepository;

    private EmployeeMapper employeeMapper;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, EmployeeMapper employeeMapper) {
        this.employeeRepository = employeeRepository;
        this.employeeMapper = employeeMapper;
    }

    @Override
    public EmployeeDTO createEmployee(EmployeeDTO request) {
        Employee savedEmployee = employeeMapper.toEntity(request);
        employeeRepository.save(savedEmployee);
        return employeeMapper.toDto(savedEmployee);
    }

    @Override
    public EmployeeDTO getEmployeeById(Long id) {
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> 
                    new ResourceNotFoundException("Employee is not exist with the given id=" + id));

        return employeeMapper.toDto(employee);
    }

    @Override
    public List<EmployeeDTO> getAllEmployee() {
        List<Employee> employeeList = employeeRepository.findAll();
        return employeeList.stream().map(employeeMapper::toDto).collect(Collectors.toList());
    }

    @Override
    public EmployeeDTO updateEmployee(Long id, EmployeeDTO updatedEmployee) {
        
        //find the employee by id
        Employee employee = employeeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id=" + id));
        
        //set all the property based on new employee information
        employee.setFirstName(updatedEmployee.firstName());
        employee.setLastName(updatedEmployee.lastName());
        employee.setEmail(updatedEmployee.email());

        Employee newlyUpdatedEmployee = employeeRepository.save(employee);

        return employeeMapper.toDto(newlyUpdatedEmployee);
    }

    @Override
    public void deleteEmployee(Long id) {

        Employee employeeToDelete = employeeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Employee not found with id=" + id));
        
        employeeRepository.delete(employeeToDelete);
    }
    
}
