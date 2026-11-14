package proiect_ispgl.proiect_ispgl.Service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import proiect_ispgl.proiect_ispgl.Model.Animal;
import proiect_ispgl.proiect_ispgl.Model.CentruAnimale;
import proiect_ispgl.proiect_ispgl.Model.FisaMedicala;
import proiect_ispgl.proiect_ispgl.Model.ImagineAnimal;
import proiect_ispgl.proiect_ispgl.Repository.*;
import proiect_ispgl.proiect_ispgl.dto.AnimalFundingDto;
import proiect_ispgl.proiect_ispgl.dto.AnimalResponseDto;

import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AnimalService {
    private final AnimalRepository animalRepository;
    private final FisaMedicalaRepository fisaMedicalaRepository;
    private final ImagineAnimalRepository imagineAnimalRepository;
    private final CentruAnimaleRepository centruAnimaleRepository;
    private final MedicalTreatmentRepository medicalTreatmentRepository;
    private final AnimalFundingService fundingService;

    public AnimalService(AnimalRepository animalRepository,
                         FisaMedicalaRepository fisaMedicalaRepository,
                         ImagineAnimalRepository imagineAnimalRepository,
                         CentruAnimaleRepository centruAnimaleRepository,
                         MedicalTreatmentRepository medicalTreatmentRepository,
                         AnimalFundingService fundingService) {
        this.animalRepository = animalRepository;
        this.fisaMedicalaRepository = fisaMedicalaRepository;
        this.imagineAnimalRepository = imagineAnimalRepository;
        this.centruAnimaleRepository = centruAnimaleRepository;
        this.medicalTreatmentRepository = medicalTreatmentRepository;
        this.fundingService = fundingService;
    }

    public AnimalResponseDto getAnimal(Long id) {
        Animal animal = animalRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Animalul nu exista"));
        return mapAnimals(List.of(animal)).getFirst();
    }

    public List<AnimalResponseDto> getAnimals(
            String adoptionType, String species, String gender, String ageGroup,
            String locationType, String temperament, String color, String size,
            String shelter, Boolean petFriendly, Boolean childFriendly,
            Boolean vaccinated, Boolean dewormed, Boolean sterilized) {
        List<Animal> animals = animalRepository.findAll().stream()
                .filter(animal -> matchesAdoptionType(animal.getAdoptionType(), adoptionType))
                .toList();
        return mapAnimals(animals).stream()
                .filter(dto -> matchesExactIgnoreDiacritics(dto.getSpecies(), species))
                .filter(dto -> matchesExactIgnoreDiacritics(dto.getGender(), gender))
                .filter(dto -> matchesExactIgnoreDiacritics(dto.getAgeGroup(), ageGroup))
                .filter(dto -> matchesContainsIgnoreDiacritics(dto.getLocationType(), locationType))
                .filter(dto -> matchesExactIgnoreDiacritics(dto.getTemperament(), temperament))
                .filter(dto -> matchesColor(dto.getColor(), color))
                .filter(dto -> matchesExactIgnoreDiacritics(dto.getSize(), size))
                .filter(dto -> matchesContainsIgnoreDiacritics(dto.getShelter(), shelter))
                .filter(dto -> !Boolean.TRUE.equals(petFriendly) || dto.isPetFriendly())
                .filter(dto -> !Boolean.TRUE.equals(childFriendly) || dto.isChildFriendly())
                .filter(dto -> !Boolean.TRUE.equals(vaccinated) || dto.isVaccinated())
                .filter(dto -> !Boolean.TRUE.equals(dewormed) || dto.isDewormed())
                .filter(dto -> !Boolean.TRUE.equals(sterilized) || dto.isSterilized())
                .sorted((a1, a2) -> Integer.compare(extractAgeInMonths(a2.getAge()), extractAgeInMonths(a1.getAge())))
                .toList();
    }

    private List<AnimalResponseDto> mapAnimals(List<Animal> animals) {
        if (animals.isEmpty()) return List.of();
        List<Long> ids = animals.stream().map(Animal::getId).toList();
        var medical = fisaMedicalaRepository.findByAnimalIdIn(ids).stream()
                .collect(Collectors.toMap(FisaMedicala::getAnimalId, f -> f));
        var photos = imagineAnimalRepository.findByAnimalIdInOrderByIsMainDescIdAsc(ids).stream()
                .collect(Collectors.groupingBy(ImagineAnimal::getAnimalId));
        var centerIds = animals.stream().map(Animal::getCenterId).filter(Objects::nonNull).distinct().toList();
        var centers = centruAnimaleRepository.findAllById(centerIds).stream()
                .collect(Collectors.toMap(CentruAnimale::getIdCentru, CentruAnimale::getNumeCentru));
        var treatments = medicalTreatmentRepository.findByAnimalIds(ids);
        var funding = fundingService.getFunding(animals.stream()
                .filter(animal -> isDistanceAdoption(animal.getAdoptionType())).toList());

        return animals.stream().map(animal -> {
            FisaMedicala file = medical.get(animal.getId());
            List<String> images = photos.getOrDefault(animal.getId(), List.of()).stream()
                    .sorted(Comparator.comparing((ImagineAnimal image) -> !Boolean.TRUE.equals(image.getIsMain()))
                            .thenComparing(ImagineAnimal::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                    .map(ImagineAnimal::getImageUrl).filter(Objects::nonNull).distinct().toList();
            String shelterDisplay = animal.getCenterId() == null ? null : centers.get(animal.getCenterId());
            if (shelterDisplay == null || shelterDisplay.isBlank()) shelterDisplay = animal.getShelterName();
            AnimalFundingDto support = funding.get(animal.getId());
            var dto = AnimalResponseDto.builder()
                    .id(animal.getId()).name(animal.getName()).species(animal.getSpecies())
                    .breed(animal.getBreed()).status(animal.getStatus()).gender(animal.getGender())
                    .age(animal.getAge()).ageGroup(animal.getAgeGroup()).locationType(animal.getLocationType())
                    .temperament(animal.getTemperament()).color(animal.getColor()).size(animal.getSize())
                    .shelter(shelterDisplay)
                    .petFriendly(Boolean.TRUE.equals(animal.getPetFriendly()))
                    .childFriendly(Boolean.TRUE.equals(animal.getChildFriendly()))
                    .apartmentFriendly(Boolean.TRUE.equals(animal.getApartmentFriendly()))
                    .houseFriendly(Boolean.TRUE.equals(animal.getHouseFriendly()))
                    .vaccinated(file != null && Boolean.TRUE.equals(file.getIsVaccinated()))
                    .dewormed(file != null && Boolean.TRUE.equals(file.getIsDewormed()))
                    .sterilized(file != null && Boolean.TRUE.equals(file.getIsSterilized()))
                    .microchipped(file == null ? null : file.getMicrochipped())
                    .allergiesAndConditions(file == null ? null : file.getAllergiesAndConditions())
                    .healthStatus(file == null ? null : file.getHealthStatus())
                    .treatments(treatments.getOrDefault(animal.getId(), List.of()))
                    .imageUrl(images.isEmpty() ? null : images.getFirst()).images(images)
                    .description(animal.getDescription()).descriptionEn(animal.getDescriptionEn())
                    .adoptionType(animal.getAdoptionType())
                    .isDistanceAdoption(isDistanceAdoption(animal.getAdoptionType()))
                    .needType(animal.getNeedType()).createdAt(animal.getCreatedAt())
                    // Do not use the stored test percentage for distance adoption.
                    .fundingPercent(isDistanceAdoption(animal.getAdoptionType())
                            ? Integer.valueOf(0) : animal.getFundingPercent());
            if (support != null) {
                dto.fundingPercent(support.percent())
                        .monthlyDonationTarget(support.monthlyTarget()).fundedAmount(support.fundedAmount())
                        .remainingAmount(support.remainingAmount()).fundingMonth(support.month())
                        .fundingTargetConfigured(support.targetConfigured());
            }
            return dto.build();
        }).toList();
    }

    private boolean isDistanceAdoption(String value) {
        return normalize(value).equals("distanta");
    }

    private boolean matchesAdoptionType(String dbValue, String filterValue) {
        String filter = normalize(filterValue);
        if (filter.isEmpty() || filter.equals("all")) return true;
        if (Set.of("distanta", "la_distanta", "la distanta").contains(filter)) return isDistanceAdoption(dbValue);
        return normalize(dbValue).equals(filter);
    }

    private String normalize(String text) {
        if (text == null) return "";
        String nfd = Normalizer.normalize(text.trim().toLowerCase(Locale.ROOT), Normalizer.Form.NFD);
        return nfd.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
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

    private int extractAgeInMonths(String ageStr) {
        if (ageStr == null || ageStr.isBlank()) return 0;
        try {
            String lower = ageStr.toLowerCase(Locale.ROOT);
            String numberOnly = lower.replaceAll("[^0-9]", "");
            if (numberOnly.isEmpty()) return 0;
            int val = Integer.parseInt(numberOnly);
            return lower.contains("an") ? val * 12 : val;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
