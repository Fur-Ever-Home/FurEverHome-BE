package proiect_ispgl.proiect_ispgl.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "mesaje_contact")
public class MesajContact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mesaj")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long idMesaj;

    private String nume;
    private String email;
    private String subiect;
    private String mesaj;

    @Column(name = "acord_gdpr")
    private Boolean acordGdpr;

    @Column(name = "data_trimitere")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime dataTrimitere = LocalDateTime.now();

    public Long getIdMesaj() {
        return idMesaj;
    }

    public void setIdMesaj(Long idMesaj) {
        this.idMesaj = idMesaj;
    }

    public String getNume() {
        return nume;
    }

    public void setNume(String nume) {
        this.nume = nume;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSubiect() {
        return subiect;
    }

    public void setSubiect(String subiect) {
        this.subiect = subiect;
    }

    public String getMesaj() {
        return mesaj;
    }

    public void setMesaj(String mesaj) {
        this.mesaj = mesaj;
    }

    public Boolean getAcordGdpr() {
        return acordGdpr;
    }

    public void setAcordGdpr(Boolean acordGdpr) {
        this.acordGdpr = acordGdpr;
    }

    public LocalDateTime getDataTrimitere() {
        return dataTrimitere;
    }

    public void setDataTrimitere(LocalDateTime dataTrimitere) {
        this.dataTrimitere = dataTrimitere;
    }
    // Generează Getters și Setters în IntelliJ (Alt + Insert -> Getter and Setter)
}
