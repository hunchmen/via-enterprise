package com.via.ems.mapper;

import org.springframework.stereotype.Component;

import com.via.ems.dto.EmployeeDTO;
import com.via.ems.model.Employee;

@Component
public class EmployeeMapperImpl implements EmployeeMapper{

    @Override
    public EmployeeDTO toDto(Employee employee) {
        return new EmployeeDTO(employee.getId(), employee.getFirstName(), employee.getLastName(), employee.getEmail());
    }

    @Override
    public Employee toEntity(EmployeeDTO request) {
       return new Employee(request.firstName(), request.lastName(), request.email());
    }
    
}
