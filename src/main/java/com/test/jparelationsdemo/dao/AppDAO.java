package com.test.jparelationsdemo.dao;

import com.test.jparelationsdemo.entity.*;

public interface AppDAO {

    // Ex. 5.1 / 5.2 : OneToOne unidirectionnel
    void save(Instructor instructor);

    Instructor findInstructorById(int id);

    void deleteInstructorById(int id);

    // Ex. 5.3 : OneToOne bidirectionnel
    InstructorDetail findInstructorDetailById(int id);

    void deleteInstructorDetailById(int id);

    // Ex. 5.5 : OneToMany unidirectionnel (Course -> Review)
    void save(Course course);

    void deleteCourseById(int id);
}