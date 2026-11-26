package proiect_ispgl.proiect_ispgl.Controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import proiect_ispgl.proiect_ispgl.Repository.ShelterRepository;
import proiect_ispgl.proiect_ispgl.dto.ShelterResponseDto;

import java.util.List;

@RestController
@RequestMapping("/api/shelters")
@CrossOrigin(origins = "*")
public class ShelterController {
    private final ShelterRepository shelterRepository;

    public ShelterController(ShelterRepository shelterRepository) {
        this.shelterRepository = shelterRepository;
    }

    @GetMapping
    public List<ShelterResponseDto> getShelters() {
        return shelterRepository.findShelterCards();
    }
}
