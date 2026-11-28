package proiect_ispgl.proiect_ispgl.Config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import proiect_ispgl.proiect_ispgl.Service.AccountMailDelivery;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Configuration
public class AccountMailConfiguration {
    private static final String BREVO_ENDPOINT = "https://api.brevo.com/v3/smtp/email";

    @Bean
    public AccountMailDelivery accountMailDelivery(JavaMailSender smtp,
                                                   @Value("${app.account-mail.transport:smtp}") String transport,
                                                   @Value("${app.account-mail.brevo-api-key:}") String apiKey) {
        return switch (transport) {
            case "smtp" -> smtp::send;
            case "brevo" -> brevo(apiKey, httpsClient());
            default -> throw new IllegalStateException("Unknown account mail transport.");
        };
    }

    static AccountMailDelivery brevo(String apiKey, RestClient client) {
        if (apiKey.isBlank()) throw new IllegalStateException("Brevo account mail requires an API key.");
        return message -> {
            var recipients = Arrays.stream(message.getTo()).map(Address::new).toList();
            var body = new BrevoMessage(new Address(message.getFrom()), recipients, message.getSubject(),
                    message.getText());
            try {
                var reply = client.post().uri(BREVO_ENDPOINT)
                        .header("api-key", apiKey)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(body)
                        .retrieve().toBodilessEntity();
                if (reply.getStatusCode() != HttpStatus.CREATED) {
                    throw new MailSendException("The HTTPS mail provider did not accept the message.");
                }
            } catch (RestClientException exception) {
                // Provider errors may contain message data. Do not include their response body in logs.
                throw new MailSendException("HTTPS account mail delivery failed.");
            }
        };
    }

    private static RestClient httpsClient() {
        var http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(3)).build();
        var requests = new JdkClientHttpRequestFactory(http);
        requests.setReadTimeout(Duration.ofSeconds(5));
        return RestClient.builder().requestFactory(requests).build();
    }

    private record Address(String email) {}

    private record BrevoMessage(Address sender, List<Address> to, String subject, String textContent) {}
}
