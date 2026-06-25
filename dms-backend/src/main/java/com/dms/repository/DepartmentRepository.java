package com.dms.repository;

import com.dms.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Optional<Department> findByName(String name);

    Optional<Department> findByCode(String code);

    Boolean existsByName(String name);

    Boolean existsByCode(String code);

    Boolean existsByNameAndIdNot(String name, Long id);

    Boolean existsByCodeAndIdNot(String code, Long id);

    List<Department> findAllByOrderByNameAsc();

    List<Department> findAllByIsActiveTrue();

    Page<Department> findAllByIsActiveTrue(Pageable pageable);

    @Query("SELECT d, COUNT(u) FROM Department d LEFT JOIN d.users u WHERE d.isActive = true GROUP BY d")
    List<Object[]> findAllWithUserCount();

    @Query("SELECT COUNT(u) FROM Department d JOIN d.users u WHERE d.id = :departmentId AND u.isActive = true")
    Long countActiveUsersByDepartmentId(Long departmentId);
}