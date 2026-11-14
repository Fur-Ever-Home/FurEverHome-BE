package proiect_ispgl.proiect_ispgl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnimalResponseDto {
    private Long id;
    private String name;
    private String species;
    private String breed;
    private String status;
    private String gender;
    private String age;
    private String ageGroup;
    private String locationType;
    private String temperament;
    private String color;
    private String size;
    private String shelter;
    private boolean petFriendly;
    private boolean childFriendly;
    private boolean vaccinated;
    private boolean dewormed;
    private boolean sterilized;
    private String imageUrl;
    private String adoptionType;
    private String needType;
    private Integer fundingPercent;
    private LocalDateTime createdAt;

}
