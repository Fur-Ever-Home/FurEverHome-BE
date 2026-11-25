package proiect_ispgl.proiect_ispgl.Controller;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;
import proiect_ispgl.proiect_ispgl.Model.CerereVoluntariat;
import proiect_ispgl.proiect_ispgl.Repository.CerereVoluntariatRepository;

@RestController
@RequestMapping("/api/voluntariat")
@CrossOrigin(origins = "*") // Permite cererile din Frontend (React)
public class VoluntariatController {

    private final CerereVoluntariatRepository repository;
    private final JavaMailSender mailSender;

    private final boolean notificationsEnabled;
    private final String notificationRecipient;

    public VoluntariatController(
            CerereVoluntariatRepository repository,
            JavaMailSender mailSender,
            @Value("${app.mail.notifications.enabled}") boolean notificationsEnabled,
            @Value("${app.mail.volunteer-to}") String notificationRecipient
    ) {
        this.repository = repository;
        this.mailSender = mailSender;
        this.notificationsEnabled = notificationsEnabled;
        this.notificationRecipient = notificationRecipient;
    }

    @PostMapping
    public CerereVoluntariat inscrieVoluntar(@RequestBody CerereVoluntariat cerere) {
        // 1. Salvează cererea în baza de date Neon
        CerereVoluntariat salvata = repository.save(cerere);

        if (!notificationsEnabled) {
            return salvata;
        }

        // 2. Trimite email de notificare către tine (echipa / fundația)
        SimpleMailMessage mesajAdmin = new SimpleMailMessage();
        mesajAdmin.setTo(notificationRecipient);
        mesajAdmin.setSubject("Cerere nouă de voluntariat: " + cerere.getNumeComplet());
        mesajAdmin.setText(
                "Ai primit o nouă înscriere în echipă!\n\n" +
                        "Nume: " + cerere.getNumeComplet() + "\n" +
                        "Telefon: " + cerere.getTelefon() + "\n" +
                        "Email: " + cerere.getEmail() + "\n" +
                        "Activitate: " + cerere.getActivitateDorita() + "\n" +
                        "Disponibilitate: " + cerere.getDisponibilitate() + "\n" +
                        "Despre: " + cerere.getDespreTine()
        );
        mailSender.send(mesajAdmin);

        // 3. Trimite email de confirmare către persoana care s-a înscris
        if (cerere.getEmail() != null && !cerere.getEmail().isBlank()) {
            SimpleMailMessage mesajVoluntar = new SimpleMailMessage();
            mesajVoluntar.setTo(cerere.getEmail());
            mesajVoluntar.setSubject("Confirmare înregistrare cerere voluntariat - FurEverHome");
            mesajVoluntar.setText(
                    "Bună, " + cerere.getNumeComplet() + "!\n\n" +
                            "Îți mulțumim că dorești să te alături echipei noastre! Cererea ta de voluntariat a fost înregistrată cu succes.\n\n" +
                            "Detaliile trimise de tine:\n" +
                            "- Activitate dorită: " + cerere.getActivitateDorita() + "\n" +
                            "- Disponibilitate: " + cerere.getDisponibilitate() + "\n\n" +
                            "Un membru al echipei noastre te va contacta în cel mai scurt timp la numărul " + cerere.getTelefon() + ".\n\n" +
                            "Cu drag,\n" +
                            "Echipa FurEverHome"
            );
            mailSender.send(mesajVoluntar);
        }

        return salvata;
    }
}