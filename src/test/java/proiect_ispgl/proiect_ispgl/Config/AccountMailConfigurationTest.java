package proiect_ispgl.proiect_ispgl.Config;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

class AccountMailConfigurationTest {
    private static final String ENDPOINT = "https://api.brevo.com/v3/smtp/email";

    @Test
    void sendsPlainTextAccountMailOverHttps() {
        var builder = RestClient.builder();
        var server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(ENDPOINT)).andExpect(method(HttpMethod.POST))
                .andExpect(header("api-key", "test-key"))
                .andExpect(jsonPath("$.sender.email").value("no-reply@example.test"))
                .andExpect(jsonPath("$.to[0].email").value("shelter@example.test"))
                .andExpect(jsonPath("$.subject").value("FurEverHome"))
                .andExpect(jsonPath("$.textContent").value("https://site.example.test/login"))
                .andRespond(withStatus(HttpStatus.CREATED));

        AccountMailConfiguration.brevo("test-key", builder.build()).send(message());
        server.verify();
    }

    @Test
    void doesNotExposeProviderResponseDataWhenSendingFails() {
        var builder = RestClient.builder();
        var server = MockRestServiceServer.bindTo(builder).build();
        server.expect(requestTo(ENDPOINT)).andRespond(withStatus(HttpStatus.BAD_REQUEST).body("private data"));

        var mail = AccountMailConfiguration.brevo("test-key", builder.build());
        assertThatThrownBy(() -> mail.send(message())).isInstanceOf(MailSendException.class)
                .hasMessage("HTTPS account mail delivery failed.").hasNoCause();
        server.verify();
    }

    private static SimpleMailMessage message() {
        var message = new SimpleMailMessage();
        message.setFrom("no-reply@example.test");
        message.setTo("shelter@example.test");
        message.setSubject("FurEverHome");
        message.setText("https://site.example.test/login");
        return message;
    }
}
