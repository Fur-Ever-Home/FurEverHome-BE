package proiect_ispgl.proiect_ispgl.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import proiect_ispgl.proiect_ispgl.Service.AnimalService;
import proiect_ispgl.proiect_ispgl.dto.AnimalResponseDto;

import java.util.List;

@RestController
@RequestMapping("/api/animals")
@CrossOrigin(origins = "*")
public class AnimalController {
    private final AnimalService animalService;

    public AnimalController(AnimalService animalService)
    {
        this.animalService = animalService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnimalResponseDto> getAnimal(@PathVariable Long id) {
        return ResponseEntity.ok(animalService.getAnimal(id));
    }

    @GetMapping
    public ResponseEntity<List<AnimalResponseDto>> getAnimals(
            @RequestParam(required = false) String adoptionType,
            @RequestParam(required = false) String species,
            @RequestParam(required = false) String gender,
            @RequestParam(required = false) String ageGroup,
            @RequestParam(required = false) String locationType,
            @RequestParam(required = false) String temperament,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String size,
            @RequestParam(required = false) String shelter,
            @RequestParam(required = false) Boolean petFriendly,
            @RequestParam(required = false) Boolean childFriendly,
            @RequestParam(required = false) Boolean vaccinated,
            @RequestParam(required = false) Boolean dewormed,
            @RequestParam(required = false) Boolean sterilized
    ) {
        List<AnimalResponseDto> animals = animalService.getAnimals(
                adoptionType, species, gender, ageGroup, locationType,
                temperament, color, size, shelter, petFriendly,
                childFriendly, vaccinated, dewormed, sterilized
        );
        return ResponseEntity.ok(animals);
    }


}
