package proiect_ispgl.proiect_ispgl.Service;

import org.springframework.stereotype.Service;
import proiect_ispgl.proiect_ispgl.Model.Animal;
import proiect_ispgl.proiect_ispgl.Model.FisaMedicala;
import proiect_ispgl.proiect_ispgl.Model.ImagineAnimal;
import proiect_ispgl.proiect_ispgl.Repository.AnimalRepository;
import proiect_ispgl.proiect_ispgl.Repository.FisaMedicalaRepository;
import proiect_ispgl.proiect_ispgl.Repository.ImagineAnimalRepository;
import proiect_ispgl.proiect_ispgl.dto.AnimalResponseDto;
import proiect_ispgl.proiect_ispgl.Model.CentruAnimale;
import proiect_ispgl.proiect_ispgl.Repository.CentruAnimaleRepository;

import java.text.Normalizer;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AnimalService {
    private final AnimalRepository animalRepository;
    private final FisaMedicalaRepository fisaMedicalaRepository;
    private final ImagineAnimalRepository imagineAnimalRepository;
    private final CentruAnimaleRepository centruAnimaleRepository;

    public AnimalService(AnimalRepository animalRepository, FisaMedicalaRepository fisaMedicalaRepository, ImagineAnimalRepository imagineAnimalRepository, CentruAnimaleRepository centruAnimaleRepository)
    {
        this.animalRepository = animalRepository;
        this.fisaMedicalaRepository = fisaMedicalaRepository;
        this.imagineAnimalRepository = imagineAnimalRepository;
        this.centruAnimaleRepository = centruAnimaleRepository;
    }

    /*public AnimalService(AnimalRepository animalRepository)
    {
        this.animalRepository = animalRepository;
    }*/

   /* public List<AnimalResponseDto> getAnimals(String species)
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
                .shelter("Centrul #" + (centre_animale.getnumecentru() != null ? animal.getCenterId() : "1"))
                .imageUrl(photoUrl)
                .vaccinated(isVaccinated)
                .dewormed(isDewormed)
                .sterilized(isSterilized)
                .build();


    }*/
   public List<AnimalResponseDto> getAnimals(
           String adoptionType, String species, String gender, String ageGroup,
           String locationType, String temperament, String color, String size,
           String shelter, Boolean petFriendly, Boolean childFriendly,
           Boolean vaccinated, Boolean dewormed, Boolean sterilized
   ) {
       List<Animal> animals = animalRepository.findAll();

       return animals.stream()
               .map(this::mapToDto)
               .filter(dto -> matchesAdoptionType(dto.getAdoptionType(), adoptionType))
               .filter(dto -> matchesExactIgnoreDiacritics(dto.getSpecies(), species))
               .filter(dto -> matchesExactIgnoreDiacritics(dto.getGender(), gender))
               .filter(dto -> matchesExactIgnoreDiacritics(dto.getAgeGroup(), ageGroup))
               .filter(dto -> matchesContainsIgnoreDiacritics(dto.getLocationType(), locationType))
               .filter(dto -> matchesExactIgnoreDiacritics(dto.getTemperament(), temperament))
               .filter(dto -> matchesColor(dto.getColor(), color))
               .filter(dto -> matchesExactIgnoreDiacritics(dto.getSize(), size))
               .filter(dto -> matchesContainsIgnoreDiacritics(dto.getShelter(), shelter))
               .filter(dto -> petFriendly == null || !petFriendly || dto.isPetFriendly())
               .filter(dto -> childFriendly == null || !childFriendly || dto.isChildFriendly())
               .filter(dto -> vaccinated == null || !vaccinated || dto.isVaccinated())
               .filter(dto -> dewormed == null || !dewormed || dto.isDewormed())
               .filter(dto -> sterilized == null || !sterilized || dto.isSterilized())
               .sorted((a1, a2) -> Integer.compare(extractAgeInMonths(a2.getAge()), extractAgeInMonths(a1.getAge())))
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

    private String normalize(String text) {
        if (text == null) return "";
        String nfd = Normalizer.normalize(text.trim().toLowerCase(), Normalizer.Form.NFD);
        return nfd.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    private boolean matchesAdoptionType(String dbValue, String filterValue) {
        if (filterValue == null || filterValue.trim().isEmpty()) return true;
        String normDb = normalize(dbValue);
        String normFilter = normalize(filterValue);
        if (normFilter.contains("distanta")) {
            return normDb.contains("distanta");
        }
        return normDb.equals(normFilter);
    }

    private boolean matchesExactIgnoreDiacritics(String dbValue, String filterValue) {
        if (filterValue == null || filterValue.trim().isEmpty() || filterValue.equalsIgnoreCase("all")) return true;
        return normalize(dbValue).equals(normalize(filterValue));
    }

    private boolean matchesContainsIgnoreDiacritics(String dbValue, String filterValue) {
        if (filterValue == null || filterValue.trim().isEmpty()) return true;
        return normalize(dbValue).contains(normalize(filterValue));
    }

    private boolean matchesColor(String dbColor, String filterColor) {
        if (filterColor == null || filterColor.trim().isEmpty()) return true;
        String normDb = normalize(dbColor);
        String normFilter = normalize(filterColor);

        if (normFilter.equals("bicolor")) {
            return normDb.contains("bicolor") || normDb.contains("tricolor") || normDb.contains("cu ") || normDb.contains("-");
        }
        return normDb.contains(normFilter);
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

        // 3. Caută numele centrului în tabela centre_animale după centerId
        String shelterDisplay = null;

        if (animal.getCenterId() != null) {
            shelterDisplay = centruAnimaleRepository.findById(animal.getCenterId())
                    .map(CentruAnimale::getNumeCentru)
                    .orElse(null);
        }

        // Dacă nu s-a găsit în centre_animale, verifică shelterName sau pune fallback-ul
        if (shelterDisplay == null || shelterDisplay.trim().isEmpty()) {
            shelterDisplay = (animal.getShelterName() != null && !animal.getShelterName().trim().isEmpty())
                    ? animal.getShelterName()
                    : "Centrul #" + (animal.getCenterId() != null ? animal.getCenterId() : "1");
        }

        return AnimalResponseDto.builder()
                .id(animal.getId())
                .name(animal.getName())
                .species(animal.getSpecies())
                .breed(animal.getBreed())
                .status(animal.getStatus())
                .gender(animal.getGender())
                .age(animal.getAge())
                .ageGroup(animal.getAgeGroup())
                .locationType(animal.getLocationType())
                .temperament(animal.getTemperament())
                .color(animal.getColor())
                .size(animal.getSize())
                .shelter(shelterDisplay)
                .petFriendly(Boolean.TRUE.equals(animal.getPetFriendly()))
                .childFriendly(Boolean.TRUE.equals(animal.getChildFriendly()))
                .vaccinated(isVaccinated)
                .dewormed(isDewormed)
                .sterilized(isSterilized)
                .imageUrl(photoUrl)
                .adoptionType(animal.getAdoptionType())
                .needType(animal.getNeedType())
                .fundingPercent(animal.getFundingPercent())
                .createdAt(animal.getCreatedAt())
                .build();
    }


}
