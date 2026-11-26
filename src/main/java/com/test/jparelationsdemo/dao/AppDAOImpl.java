package com.test.jparelationsdemo.dao;

import com.test.jparelationsdemo.entity.*;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public class AppDAOImpl implements AppDAO {

    private final EntityManager entityManager;

    @Autowired
    public AppDAOImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    // ---------- Ex. 5.1 / 5.2 / 5.4 ----------

    @Override
    @Transactional
    public void save(Instructor instructor) {
        // cascade PERSIST : persiste aussi le détail et les cours rattachés
        entityManager.persist(instructor);
    }

    @Override
    public Instructor findInstructorById(int id) {
        return entityManager.find(Instructor.class, id);
    }

    @Override
    @Transactional
    public void deleteInstructorById(int id) {
        Instructor instructor = entityManager.find(Instructor.class, id);
        if (instructor == null) {
            return; // id inexistant : rien à supprimer (évite une IllegalArgumentException)
        }

        // Les cours ne sont pas supprimés en cascade (pas de REMOVE) : on les détache
        // de l'instructeur, sinon la FK course.instructor_id empêcherait le DELETE.
        List<Course> courses = instructor.getCourses();
        if (courses != null) {
            for (Course course : courses) {
                course.setInstructor(null);
            }
        }

        entityManager.remove(instructor); // supprime AUSSI l'instructor_detail (cascade ALL)
    }

    // ---------- Ex. 5.3 ----------

    @Override
    public InstructorDetail findInstructorDetailById(int id) {
        return entityManager.find(InstructorDetail.class, id);
    }

    @Override
    @Transactional
    public void deleteInstructorDetailById(int id) {
        InstructorDetail detail = entityManager.find(InstructorDetail.class, id);
        if (detail == null) {
            return;
        }

        // rompre la liaison bidirectionnelle : instructor.instructor_detail_id -> NULL
        if (detail.getInstructor() != null) {
            detail.getInstructor().setInstructorDetail(null);
        }

        entityManager.remove(detail); // supprime UNIQUEMENT le détail
    }

    // ---------- Ex. 5.5 ----------

    @Override
    @Transactional
    public void save(Course course) {
        // cascade ALL : persiste le cours ET ses reviews
        entityManager.persist(course);
    }

    @Override
    @Transactional
    public void deleteCourseById(int id) {
        Course course = entityManager.find(Course.class, id);
        if (course == null) {
            return;
        }
        entityManager.remove(course); // supprime aussi les reviews (cascade ALL), pas l'instructeur
    }
}