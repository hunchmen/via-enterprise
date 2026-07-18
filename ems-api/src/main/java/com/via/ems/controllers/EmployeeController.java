package com.via.ems.controllers;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import com.via.ems.dto.EmployeeDTO;
import com.via.ems.service.EmployeeService;

@Tag(name = "Employee", description = "APIs for managing employees")
@RestController
@RequestMapping(path = "/v1/employees")
public class EmployeeController {

    private EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    /**
     * Creates a new employee with the provided details.
     *
     * @param EmployeeDTO the employee details to save.
     * @return the saved employee details.
     */
    @Operation(summary = "Create a new employee", description = "Creates a new employee with the provided details")
    @ApiResponse(responseCode = "201", description = "Employee created successfully")
    @PostMapping
    public ResponseEntity<EmployeeDTO> createEmployee(
            @Valid @RequestBody EmployeeDTO request) {
        EmployeeDTO savedEmployeeResponseDTO = employeeService.createEmployee(request);
        return new ResponseEntity<>(savedEmployeeResponseDTO, HttpStatus.CREATED);
    }

    @Operation(summary = "Get employee by ID", description = "Retrieves an employee's details by their unique identifier")
    @ApiResponse(responseCode = "200", description = "Employee found and returned")
    @ApiResponse(responseCode = "404", description = "Employee not found")
    @GetMapping("{id}")
    public ResponseEntity<EmployeeDTO> getEmployeeById(
            @Parameter(description = "ID of the employee to be retrieved", required = true) @PathVariable("id") Long id) {
        EmployeeDTO EmployeeResponseDTO = employeeService.getEmployeeById(id);
        return new ResponseEntity<>(EmployeeResponseDTO, HttpStatus.OK);
    }

    @Operation(summary = "Get all employees", description = "Retrieves a list of all employees in the system")
    @ApiResponse(responseCode = "200", description = "List of all employees returned successfully")
    @GetMapping()
    public ResponseEntity<List<EmployeeDTO>> getAllEmployee() {
        List<EmployeeDTO> listOfEmployee = employeeService.getAllEmployee();
        return new ResponseEntity<>(listOfEmployee, HttpStatus.OK);
    }

    @Operation(summary = "Update an existing employee", description = "Updates the details of an existing employee identified by their ID")
    @ApiResponse(responseCode = "200", description = "Employee updated successfully")
    @ApiResponse(responseCode = "404", description = "Employee not found")
    @PutMapping("{id}")
    public ResponseEntity<EmployeeDTO> updateEmployee(
            @Parameter(description = "ID of the employee to be updated", required = true) @PathVariable("id") Long id,
            @Valid @RequestBody EmployeeDTO updatedEmployee) {
        EmployeeDTO updatedEmployeeInfo = employeeService.updateEmployee(id, updatedEmployee);
        return ResponseEntity.ok(updatedEmployeeInfo);
    }

    @Operation(summary = "Delete an employee", description = "Deletes an employee from the system by their ID")
    @ApiResponse(responseCode = "200", description = "Employee deleted successfully")
    @ApiResponse(responseCode = "404", description = "Employee not found")
    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteEmployee(
            @Parameter(description = "ID of the employee to be deleted", required = true) @PathVariable("id") Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.ok("Employee deleted successfully.");
    }

}
