package com.via.ems.mapper;

import org.mapstruct.Mapper;

import com.via.ems.dto.EmployeeDTO;
import com.via.ems.model.Employee;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {
    
    EmployeeDTO toDto(Employee employee);

    Employee toEntity(EmployeeDTO request);
    
}
