package proiect_ispgl.proiect_ispgl.Service;

import org.springframework.mail.SimpleMailMessage;

@FunctionalInterface
public interface AccountMailDelivery {
    void send(SimpleMailMessage message);
}
