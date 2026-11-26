package proiect_ispgl.proiect_ispgl.dto;

import java.math.BigDecimal;

public record ShelterResponseDto(
        int id,
        String name,
        String county,
        String city,
        String address,
        String phone,
        String schedule,
        String email,
        Integer capacity,
        long animalsCount,
        long catsCount,
        long dogsCount,
        long otherCount,
        SocialLinks socialLinks,
        BigDecimal latitude,
        BigDecimal longitude
) {
    public record SocialLinks(String instagram, String facebook) {}
}
