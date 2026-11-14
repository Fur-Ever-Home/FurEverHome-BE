package proiect_ispgl.proiect_ispgl.Model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

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

        @Column(name = "tip_locatie")
        private String locationType;

        @Column(name = "id_centru")
        private Integer centerId;

        @Column(name = "nume_centru")
        private String shelterName;

        @Column(name = "data_adaugare")
        private LocalDateTime createdAt;

        @Column(name = "temperament")
        private String temperament;

        @Column(name = "compatibil_apartament")
        private Boolean apartmentFriendly;

        @Column(name = "compatibil_casa")
        private Boolean houseFriendly;

        @Column(name = "prietenos_copii")
        private Boolean childFriendly;

        @Column(name = "prietenos_alte_animale")
        private Boolean petFriendly;

        @Column(name = "culoare")
        private String color;

        @Column(name = "categorie_varsta")
        private String ageGroup;

        @Column(name = "tip_adoptie")
        private String adoptionType;

        @Column(name = "tip_ingrijire")
        private String needType;

        @Column(name = "procent_sustinere_lunara")
        private Integer fundingPercent;

    }
