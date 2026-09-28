package com.example.hospital;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;

    public DepartmentService(DepartmentRepository departmentRepository,
                             DoctorRepository doctorRepository) {
        this.departmentRepository = departmentRepository;
        this.doctorRepository = doctorRepository;
    }

    public List<Department> getAllDepartments() {
        List<Department> departments = departmentRepository.findAll();
        Map<Long, List<Doctor>> byDept = new HashMap<>();
        for (Doctor d : doctorRepository.findAll()) {
            byDept.computeIfAbsent(d.getDepartmentId(), k -> new ArrayList<>()).add(d);
        }
        departments.forEach(d -> d.setDoctors(byDept.getOrDefault(d.getId(), List.of())));
        return departments;
    }

    public Department getDepartment(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Department not found"));
    }

    public String getDepartmentName(Long id) {
        if (id == null) return null;
        return departmentRepository.findById(id).map(Department::getName).orElse(null);
    }

    public Department createDepartment(Department department) {
        departmentRepository.findByNameIgnoreCase(department.getName())
                .ifPresent(d -> {
                    throw new DuplicateResourceException("Department already exists: " + department.getName());
                });
        return departmentRepository.save(department);
    }

    public Department updateDepartment(Long id, Department updates) {
        Department dept = getDepartment(id);
        if (updates.getName() != null && !updates.getName().isBlank()) {
            dept.setName(updates.getName());
        }
        if (updates.getDescription() != null) {
            dept.setDescription(updates.getDescription());
        }
        return departmentRepository.save(dept);
    }

    public void deleteDepartment(Long id) {
        getDepartment(id);
        boolean assigned = doctorRepository.findAll().stream()
                .anyMatch(d -> id.equals(d.getDepartmentId()));
        if (assigned) {
            throw new InvalidOperationException("Cannot delete department with assigned doctors");
        }
        departmentRepository.deleteById(id);
    }
}
