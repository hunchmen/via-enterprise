package com.via.ems.mapper;

import com.via.ems.dto.EmployeeDTO;
import com.via.ems.model.Employee;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    EmployeeDTO toDto(Employee employee);

    Employee toEntity(EmployeeDTO request);
}
