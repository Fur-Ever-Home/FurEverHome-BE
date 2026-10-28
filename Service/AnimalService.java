package proiect_ispgl.proiect_ispgl.Service;

import org.springframework.stereotype.Service;
import proiect_ispgl.proiect_ispgl.Model.Animal;
import proiect_ispgl.proiect_ispgl.Model.FisaMedicala;
import proiect_ispgl.proiect_ispgl.Model.ImagineAnimal;
import proiect_ispgl.proiect_ispgl.Repository.AnimalRepository;
import proiect_ispgl.proiect_ispgl.Repository.FisaMedicalaRepository;
import proiect_ispgl.proiect_ispgl.Repository.ImagineAnimalRepository;
import proiect_ispgl.proiect_ispgl.dto.AnimalResponseDto;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AnimalService {
    private final AnimalRepository animalRepository;
    private final FisaMedicalaRepository fisaMedicalaRepository;
    private final ImagineAnimalRepository imagineAnimalRepository;

    public AnimalService(AnimalRepository animalRepository, FisaMedicalaRepository fisaMedicalaRepository, ImagineAnimalRepository imagineAnimalRepository)
    {
        this.animalRepository = animalRepository;
        this.fisaMedicalaRepository = fisaMedicalaRepository;
        this.imagineAnimalRepository = imagineAnimalRepository;
    }

    /*public AnimalService(AnimalRepository animalRepository)
    {
        this.animalRepository = animalRepository;
    }*/

    public List<AnimalResponseDto> getAnimals(String species)
    {
        List<Animal> animals;

        if(species == null || species.trim().isEmpty() || species.equalsIgnoreCase("all"))
        {
            animals = animalRepository.findAll();
        }
        else
        {
            animals = animalRepository.findBySpeciesIgnoreCase(species.trim());
        }

        return animals.stream()
                .sorted((a1,a2) -> Integer.compare(extractAgeInMonths(a2.getAge()), extractAgeInMonths(a1.getAge())))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }
    private int extractAgeInMonths(String ageStr)
    {
        if(ageStr == null || ageStr.trim().isEmpty())
        {
            return 0;
        }
        try {
            String lower = ageStr.toLowerCase();
            String numberOnly = lower.replaceAll("[^0-9]","");
            if(numberOnly.isEmpty())
                return 0;
            int val = Integer.parseInt(numberOnly);
            if(lower.contains("an"))
            {
                return val * 12;
            }
            return val;
        }
        catch (Exception e)
        {
            return 0;
        }
    }


    private AnimalResponseDto mapToDto(Animal animal) {

        Optional<FisaMedicala> fisaMedicala = fisaMedicalaRepository.findByAnimalId(animal.getId());
        boolean isVaccinated = fisaMedicala.map(FisaMedicala::getIsVaccinated).orElse(false);
        boolean isDewormed = fisaMedicala.map(FisaMedicala::getIsDewormed).orElse(false);
        boolean isSterilized = fisaMedicala.map(FisaMedicala::getIsSterilized).orElse(false);

        String photoUrl = imagineAnimalRepository.findFirstByAnimalIdAndIsMainTrue(animal.getId())
                .or(() -> imagineAnimalRepository.findFirstByAnimalId(animal.getId()))
                .map(ImagineAnimal::getImageUrl)
                .orElse(null);


        return AnimalResponseDto.builder()
                .id(animal.getId())
                .name(animal.getName())
                .species(animal.getSpecies())
                .status(animal.getStatus())
                .gender(animal.getGender())
                .age(animal.getAge())
                .size(animal.getSize())
                .shelter("Centrul #" + (animal.getCenterId() != null ? animal.getCenterId() : "1"))
                .imageUrl(photoUrl)
                .vaccinated(isVaccinated)
                .dewormed(isDewormed)
                .sterilized(isSterilized)
                .build();


    }

}
