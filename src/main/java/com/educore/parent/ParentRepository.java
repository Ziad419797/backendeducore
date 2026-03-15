package com.educore.parent;

import com.educore.student.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ParentRepository extends JpaRepository<Parent, Long> {

//    Optional<Parent> findByPhone(String phone);
Optional<Parent> findByStudent(Student student);
//    @Query("SELECT p FROM Parent p WHERE p.student.phone = :phone")
    Optional<Parent> findByPhone(@Param("phone") String phone);
    boolean existsByPhone(String phone);


}
