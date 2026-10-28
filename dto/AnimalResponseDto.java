package proiect_ispgl.proiect_ispgl.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnimalResponseDto {
    private Long id;
    private String name;
    private String species;
    private String status;
    private String gender;
    private String age;
    private String size;
    private String shelter;
    private boolean vaccinated;
    private boolean dewormed;
    private boolean sterilized;
    private String imageUrl;

}
