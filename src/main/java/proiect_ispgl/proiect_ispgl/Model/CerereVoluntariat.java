package proiect_ispgl.proiect_ispgl.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "cereri_voluntariat")
public class CerereVoluntariat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cerere")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long idCerere;

    @Column(name = "nume_complet")
    private String numeComplet;

    private String telefon;
    private String email;

    @Column(name = "activitate_dorita")
    private String activitateDorita;

    private String disponibilitate;

    @Column(name = "despre_tine")
    private String despreTine;

    @Column(name = "acord_gdpr")
    private Boolean acordGdpr;

    @Column(name = "data_inscriere")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime dataInscriere = LocalDateTime.now();

    public Long getIdCerere() {
        return idCerere;
    }

    public void setIdCerere(Long idCerere) {
        this.idCerere = idCerere;
    }

    public String getTelefon() {
        return telefon;
    }

    public void setTelefon(String telefon) {
        this.telefon = telefon;
    }

    public String getNumeComplet() {
        return numeComplet;
    }

    public void setNumeComplet(String numeComplet) {
        this.numeComplet = numeComplet;
    }

    public String getActivitateDorita() {
        return activitateDorita;
    }

    public void setActivitateDorita(String activitateDorita) {
        this.activitateDorita = activitateDorita;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisponibilitate() {
        return disponibilitate;
    }

    public void setDisponibilitate(String disponibilitate) {
        this.disponibilitate = disponibilitate;
    }

    public Boolean getAcordGdpr() {
        return acordGdpr;
    }

    public void setAcordGdpr(Boolean acordGdpr) {
        this.acordGdpr = acordGdpr;
    }

    public String getDespreTine() {
        return despreTine;
    }

    public void setDespreTine(String despreTine) {
        this.despreTine = despreTine;
    }

    public LocalDateTime getDataInscriere() {
        return dataInscriere;
    }

    public void setDataInscriere(LocalDateTime dataInscriere) {
        this.dataInscriere = dataInscriere;
    }
    // Generează Getters și Setters în IntelliJ (Alt + Insert -> Getter and Setter)
}
