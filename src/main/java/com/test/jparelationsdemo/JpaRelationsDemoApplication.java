package com.test.jparelationsdemo;

import com.test.jparelationsdemo.dao.AppDAO;
import com.test.jparelationsdemo.entity.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

@SpringBootApplication
public class JpaRelationsDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(JpaRelationsDemoApplication.class, args);
    }

    // @Profile("!test") : le fil rouge ne s'exécute pas pendant "mvn test",
    // sinon chaque build insérerait des lignes en base et décalerait les ids.
    @Bean
    @Profile("!test")
    public CommandLineRunner commandLineRunner(AppDAO appDAO) {
        return runner -> {
            // Décommentez UNE SEULE méthode à la fois pour chaque exercice :

            createInstructor(appDAO);                // Ex. 5.1
            // findInstructor(appDAO);               // Ex. 5.2
            // deleteInstructor(appDAO);             // Ex. 5.2
            // findInstructorDetail(appDAO);         // Ex. 5.3
            // deleteInstructorDetail(appDAO);       // Ex. 5.3
            // createInstructorWithCourses(appDAO);  // Ex. 5.4
            // createCourseAndReviews(appDAO);       // Ex. 5.5
            // deleteCourse(appDAO);                 // Ex. 5.5
        };
    }

    // ---------- Ex. 5.1 : création en cascade ----------
    private void createInstructor(AppDAO appDAO) {
        Instructor instructor =
                new Instructor("Madhu", "Patel", "madhu@test.com");
        InstructorDetail detail =
                new InstructorDetail("http://www.youtube.com/@madhu", "Guitar");

        instructor.setInstructorDetail(detail);

        System.out.println("Saving instructor: " + instructor);
        appDAO.save(instructor); // sauve instructor ET detail grâce à CascadeType.ALL
        System.out.println("Done!");
    }

    // ---------- Ex. 5.2 : récupération et suppression en cascade ----------
    private void findInstructor(AppDAO appDAO) {
        int id = 1;
        Instructor instructor = appDAO.findInstructorById(id);
        if (instructor == null) {
            System.out.println("Aucun instructeur avec l'id " + id);
            return;
        }

        System.out.println("Instructor: " + instructor);
        System.out.println("Detail: " + instructor.getInstructorDetail());
    }

    private void deleteInstructor(AppDAO appDAO) {
        int id = 1;
        System.out.println("Deleting instructor id: " + id);
        appDAO.deleteInstructorById(id); // supprime AUSSI l'instructor_detail
        System.out.println("Done!");
    }

    // ---------- Ex. 5.3 : bidirectionnel ----------
    private void findInstructorDetail(AppDAO appDAO) {
        int id = 1;
        InstructorDetail detail = appDAO.findInstructorDetailById(id);
        if (detail == null) {
            System.out.println("Aucun instructor_detail avec l'id " + id);
            return;
        }

        System.out.println("Detail: " + detail);
        System.out.println("Associated instructor: " + detail.getInstructor());
    }

    private void deleteInstructorDetail(AppDAO appDAO) {
        int id = 1;
        System.out.println("Deleting instructor detail id: " + id);
        appDAO.deleteInstructorDetailById(id); // supprime UNIQUEMENT le détail
        System.out.println("Done!");
    }

    // ---------- Ex. 5.4 : instructeur + cours ----------
    private void createInstructorWithCourses(AppDAO appDAO) {
        Instructor instructor =
                new Instructor("Susan", "Public", "susan.public@test.com");
        instructor.setInstructorDetail(
                new InstructorDetail("http://www.youtube.com/@susan", "Video Games"));

        Course c1 = new Course("Air Guitar - The Ultimate Guide");
        Course c2 = new Course("The Pinball Masterclass");
        instructor.add(c1);
        instructor.add(c2);

        System.out.println("Saving instructor: " + instructor);
        appDAO.save(instructor); // cascade PERSIST : instructor + détail + 2 cours
        System.out.println("Done!");
    }

    // ---------- Ex. 5.5 : cours + avis ----------
    private void createCourseAndReviews(AppDAO appDAO) {
        Course course = new Course("Pacman - How To Score One Million Points");

        course.addReview(new Review("Great course... loved it!"));
        course.addReview(new Review("Cool course, job well done."));
        course.addReview(new Review("What a dumb course, you are an idiot!"));

        System.out.println("Saving the course: " + course);
        appDAO.save(course); // cascade ALL : persiste le cours ET ses 3 reviews
        System.out.println("Done! id du cours créé = " + course.getId() + " (à utiliser dans deleteCourse)");
    }

    private void deleteCourse(AppDAO appDAO) {
        // id du cours "Pacman" (affiché par createCourseAndReviews).
        // Sur une base neuve : 1 et 2 = cours de l'Ex. 5.4, 3 = Pacman.
        // ATTENTION : id 1 = "Air Guitar", qui n'a AUCUN avis -> la cascade ne se verrait pas.
        int id = 3;
        System.out.println("Deleting course id: " + id);
        appDAO.deleteCourseById(id); // supprime le cours ET ses reviews
        System.out.println("Done!");
    }
}