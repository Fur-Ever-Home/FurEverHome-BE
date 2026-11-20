package proiect_ispgl.proiect_ispgl.Controller;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.*;
import proiect_ispgl.proiect_ispgl.Model.MesajContact;
import proiect_ispgl.proiect_ispgl.Repository.MesajContactRepository;

@RestController
@RequestMapping("/api/contact")
@CrossOrigin(origins = "*")
public class ContactController {

    private final MesajContactRepository repository;
    private final JavaMailSender mailSender;

    public ContactController(MesajContactRepository repository, JavaMailSender mailSender) {
        this.repository = repository;
        this.mailSender = mailSender;
    }

    @PostMapping
    public MesajContact trimiteMesaj(@RequestBody MesajContact contact) {
        // 1. Salvează mesajul în baza de date Neon
        MesajContact salvat = repository.save(contact);

        // 2. Trimite email de notificare doar către voi
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo("diensoarec@gmail.com");
        mail.setSubject("Mesaj nou pe site [" + contact.getSubiect() + "] - de la " + contact.getNume());
        mail.setText(
                "Ai primit un mesaj nou prin formularul de contact!\n\n" +
                        "Nume: " + contact.getNume() + "\n" +
                        "Email expeditor: " + contact.getEmail() + "\n" +
                        "Subiect: " + contact.getSubiect() + "\n\n" +
                        "Mesaj:\n" + contact.getMesaj()
        );

        mailSender.send(mail);
        return salvat;
    }
}