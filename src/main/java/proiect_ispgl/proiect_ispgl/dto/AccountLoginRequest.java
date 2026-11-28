package proiect_ispgl.proiect_ispgl.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AccountLoginRequest(
        String role,
        String email,
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) String password
) {
    @Override
    public String toString() {
        return "AccountLoginRequest[redacted]";
    }
}
