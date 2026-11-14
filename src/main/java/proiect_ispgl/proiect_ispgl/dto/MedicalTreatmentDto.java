package proiect_ispgl.proiect_ispgl.dto;

import java.time.LocalDate;

public record MedicalTreatmentDto(
        Integer id,
        String name,
        String category,
        String description,
        LocalDate recordedAt
) {
}
