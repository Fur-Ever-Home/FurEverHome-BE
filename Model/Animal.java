package proiect_ispgl.proiect_ispgl.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
@Table(name = "animale")
public class Animal {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Column(name = "id_animal")
        private Long Id;

        @Column(name = "nume")
        private String name;

        @Column(name = "specie")
        private String species;

        @Column(name = "rasa")
        private String breed;

        @Column(name = "varsta")
        private String age;

        @Column(name = "sex")
        private String gender;

        @Column(name = "talie")
        private String size;

        @Column(name = "status")
        private String status;

        @Column(name = "id_centru")
        private Integer centerId;

    }
