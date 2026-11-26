package com.test.jparelationsdemo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "instructor_detail")
public class InstructorDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "youtube_channel")
    private String youtubeChannel;

    @Column(name = "hobby")
    private String hobby;

    // Ex. 5.3 : côté INVERSE de la relation 1-1 bidirectionnelle (aucune colonne créée).
    // REMOVE est volontairement absent de la cascade : supprimer un détail
    // ne doit pas supprimer l'instructeur.
    @OneToOne(mappedBy = "instructorDetail",
            cascade = {CascadeType.DETACH, CascadeType.MERGE,
                    CascadeType.PERSIST, CascadeType.REFRESH})
    private Instructor instructor;

    public InstructorDetail() {
    }

    public InstructorDetail(String youtubeChannel, String hobby) {
        this.youtubeChannel = youtubeChannel;
        this.hobby = hobby;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getYoutubeChannel() {
        return youtubeChannel;
    }

    public void setYoutubeChannel(String youtubeChannel) {
        this.youtubeChannel = youtubeChannel;
    }

    public String getHobby() {
        return hobby;
    }

    public void setHobby(String hobby) {
        this.hobby = hobby;
    }

    public Instructor getInstructor() {
        return instructor;
    }

    public void setInstructor(Instructor instructor) {
        this.instructor = instructor;
    }

    // Le champ "instructor" n'est PAS dans toString() : Instructor.toString() affiche
    // son détail, ce qui bouclerait à l'infini (StackOverflowError) en bidirectionnel.
    @Override
    public String toString() {
        return "InstructorDetail{" +
                "id=" + id +
                ", youtubeChannel='" + youtubeChannel + '\'' +
                ", hobby='" + hobby + '\'' +
                '}';
    }
}