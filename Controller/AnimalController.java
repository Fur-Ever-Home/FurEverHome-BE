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

    @GetMapping
    public ResponseEntity<List<AnimalResponseDto>> getAnimals(@RequestParam(required = false) String species)
    {
        List<AnimalResponseDto> animals = animalService.getAnimals(species);
        return ResponseEntity.ok(animals);
    }


}
