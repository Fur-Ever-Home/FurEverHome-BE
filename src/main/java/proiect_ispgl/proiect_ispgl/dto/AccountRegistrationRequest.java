package proiect_ispgl.proiect_ispgl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AccountRegistrationRequest(
        String role,
        String firstName,
        String lastName,
        String shelterName,
        String cui,
        String countryCode,
        String phone,
        String email,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) String password
) {
    @Override
    public String toString() {
        return "AccountRegistrationRequest[redacted]";
    }
}
