package proiect_ispgl.proiect_ispgl.Model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "donatii_bani")
public class DonatieBani {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_donatie")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer idDonatie;

    @Column(name = "id_centru")
    private Integer idCentru;

    @Column(name = "id_utilizator")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Integer idUtilizator;

    private BigDecimal suma;

    @Column(name = "id_tranzactie_stripe")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String idTranzactieStripe;

    @Column(name = "data_donatie")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime dataDonatie = LocalDateTime.now();

    @Column(name = "id_animal")
    private Long idAnimal;

    @Column(name = "status_plata")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private String statusPlata = "FINALIZATA";

    @Column(name = "nume_donator")
    private String numeDonator;

    @Column(name = "email_donator")
    private String emailDonator;

    @Column(name = "este_recurenta")
    private Boolean esteRecurenta = false;

    public Integer getIdDonatie() {
        return idDonatie;
    }

    public void setIdDonatie(Integer idDonatie) {
        this.idDonatie = idDonatie;
    }

    public Integer getIdCentru() {
        return idCentru;
    }

    public void setIdCentru(Integer idCentru) {
        this.idCentru = idCentru;
    }

    public Integer getIdUtilizator() {
        return idUtilizator;
    }

    public void setIdUtilizator(Integer idUtilizator) {
        this.idUtilizator = idUtilizator;
    }

    public BigDecimal getSuma() {
        return suma;
    }

    public void setSuma(BigDecimal suma) {
        this.suma = suma;
    }

    public String getIdTranzactieStripe() {
        return idTranzactieStripe;
    }

    public void setIdTranzactieStripe(String idTranzactieStripe) {
        this.idTranzactieStripe = idTranzactieStripe;
    }

    public LocalDateTime getDataDonatie() {
        return dataDonatie;
    }

    public void setDataDonatie(LocalDateTime dataDonatie) {
        this.dataDonatie = dataDonatie;
    }

    public Long getIdAnimal() {
        return idAnimal;
    }

    public void setIdAnimal(Long idAnimal) {
        this.idAnimal = idAnimal;
    }

    public String getStatusPlata() {
        return statusPlata;
    }

    public void setStatusPlata(String statusPlata) {
        this.statusPlata = statusPlata;
    }

    public String getNumeDonator() {
        return numeDonator;
    }

    public void setNumeDonator(String numeDonator) {
        this.numeDonator = numeDonator;
    }

    public String getEmailDonator() {
        return emailDonator;
    }

    public void setEmailDonator(String emailDonator) {
        this.emailDonator = emailDonator;
    }

    public Boolean getEsteRecurenta() {
        return esteRecurenta;
    }

    public void setEsteRecurenta(Boolean esteRecurenta) {
        this.esteRecurenta = esteRecurenta;
    }

    // Generează Getters și Setters în IntelliJ: Alt + Insert -> Getter and Setter (selectează toate câmpurile)
}
