package proiect_ispgl.proiect_ispgl.Service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import proiect_ispgl.proiect_ispgl.Model.Animal;
import proiect_ispgl.proiect_ispgl.Model.CentruAnimale;
import proiect_ispgl.proiect_ispgl.Model.FisaMedicala;
import proiect_ispgl.proiect_ispgl.Model.ImagineAnimal;
import proiect_ispgl.proiect_ispgl.Repository.AnimalRepository;
import proiect_ispgl.proiect_ispgl.Repository.CentruAnimaleRepository;
import proiect_ispgl.proiect_ispgl.Repository.FisaMedicalaRepository;
import proiect_ispgl.proiect_ispgl.Repository.ImagineAnimalRepository;
import proiect_ispgl.proiect_ispgl.Repository.MedicalTreatmentRepository;
import proiect_ispgl.proiect_ispgl.dto.AnimalFundingDto;
import proiect_ispgl.proiect_ispgl.dto.AnimalResponseDto;
import proiect_ispgl.proiect_ispgl.dto.MedicalTreatmentDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.*;

class AnimalServiceTest {
    private AnimalRepository animals;
    private FisaMedicalaRepository medical;
    private ImagineAnimalRepository images;
    private CentruAnimaleRepository centers;
    private MedicalTreatmentRepository treatments;
    private AnimalFundingService funding;
    private AnimalService service;

    @BeforeEach
    void setUp() {
        animals = mock(AnimalRepository.class);
        medical = mock(FisaMedicalaRepository.class);
        images = mock(ImagineAnimalRepository.class);
        centers = mock(CentruAnimaleRepository.class);
        treatments = mock(MedicalTreatmentRepository.class);
        funding = mock(AnimalFundingService.class);
        service = new AnimalService(animals, medical, images, centers, treatments, funding);
        when(medical.findByAnimalIdIn(anyCollection())).thenReturn(List.of());
        when(images.findByAnimalIdInOrderByIsMainDescIdAsc(anyCollection())).thenReturn(List.of());
        when(centers.findAllById(anyCollection())).thenReturn(List.of());
        when(treatments.findByAnimalIds(anyCollection())).thenReturn(Map.of());
        when(funding.getFunding(anyCollection())).thenReturn(Map.of());
    }

    @ParameterizedTest
    @ValueSource(strings = {"distanta", "la_distanta", "la distanță", "  LA_DISTANTA  "})
    void distanceFilterAcceptsRequestAliasesButOnlyCanonicalDatabaseType(String alias) {
        Animal distance = animal(1L, "distanta");
        when(animals.findAll()).thenReturn(List.of(distance, animal(2L, "fizica"),
                animal(3L, "mixt"), animal(4L, "nu distanta"),
                animal(5L, "la_distanta"), animal(6L, "distanta suplimentara"),
                animal(7L, null)));

        List<AnimalResponseDto> result = list(alias);

        assertEquals(List.of(1L), result.stream().map(AnimalResponseDto::getId).toList());
        assertTrue(result.getFirst().getIsDistanceAdoption());
        verify(medical).findByAnimalIdIn(List.of(1L));
        verify(treatments).findByAnimalIds(List.of(1L));
        verify(funding).getFunding(List.of(distance));
    }

    @Test
    void physicalFilterAndUnfilteredCatalogStillWork() {
        Animal distance = animal(1L, "distanta");
        Animal physical = animal(2L, "fizica");
        when(animals.findAll()).thenReturn(List.of(distance, physical));

        assertEquals(List.of(2L), list("fizica").stream().map(AnimalResponseDto::getId).toList());
        assertEquals(2, list(null).size());
    }

    @Test
    void popupContainsDatabaseMedicalHistoryHousingDescriptionsAndMainImageFirst() {
        Animal animal = animal(12L, "distanta");
        animal.setLocationType("Foster");
        animal.setCenterId(8);
        animal.setDescription("Descriere de test");
        animal.setDescriptionEn("Test description");
        animal.setApartmentFriendly(true);
        animal.setHouseFriendly(false);
        animal.setNeedType("Tratament continuu");
        animal.setFundingPercent(99);
        when(animals.findById(12L)).thenReturn(Optional.of(animal));
        when(centers.findAllById(List.of(8))).thenReturn(List.of(new CentruAnimale(8, "Centru test")));
        FisaMedicala file = FisaMedicala.builder().id(4L).animalId(12L)
                .isVaccinated(true).isDewormed(false).isSterilized(true)
                .microchipped(true).allergiesAndConditions("Alergie de test")
                .healthStatus("Sub tratament").build();
        when(medical.findByAnimalIdIn(List.of(12L))).thenReturn(List.of(file));
        when(images.findByAnimalIdInOrderByIsMainDescIdAsc(List.of(12L))).thenReturn(List.of(
                photo(9L, 12L, "main.jpg", true), photo(1L, 12L, "secondary.jpg", false),
                photo(2L, 12L, "secondary.jpg", false), photo(3L, 12L, null, false)));
        MedicalTreatmentDto treatment = new MedicalTreatmentDto(6, "Control", "Evaluare",
                "Descriere tratament", LocalDate.of(2026, 11, 12));
        when(treatments.findByAnimalIds(List.of(12L))).thenReturn(Map.of(12L, List.of(treatment)));
        when(funding.getFunding(List.of(animal))).thenReturn(Map.of(12L, new AnimalFundingDto(
                "2026-11", new BigDecimal("200.00"), new BigDecimal("80.00"),
                new BigDecimal("120.00"), 40, true)));

        AnimalResponseDto dto = service.getAnimal(12L);

        assertAll(
                () -> assertEquals("Foster", dto.getLocationType()),
                () -> assertEquals("Centru test", dto.getShelter()),
                () -> assertEquals("Descriere de test", dto.getDescription()),
                () -> assertEquals("Test description", dto.getDescriptionEn()),
                () -> assertTrue(dto.isApartmentFriendly()),
                () -> assertFalse(dto.isHouseFriendly()),
                () -> assertTrue(dto.isVaccinated()),
                () -> assertFalse(dto.isDewormed()),
                () -> assertTrue(dto.isSterilized()),
                () -> assertTrue(dto.getMicrochipped()),
                () -> assertEquals("Alergie de test", dto.getAllergiesAndConditions()),
                () -> assertEquals("Sub tratament", dto.getHealthStatus()),
                () -> assertEquals(List.of(treatment), dto.getTreatments()),
                () -> assertEquals(List.of("main.jpg", "secondary.jpg"), dto.getImages()),
                () -> assertEquals("main.jpg", dto.getImageUrl()),
                () -> assertEquals(99, dto.getFundingPercent()),
                () -> assertEquals(40, dto.getCalculatedFundingPercent()),
                () -> assertEquals(new BigDecimal("200.00"), dto.getMonthlyDonationTarget()),
                () -> assertEquals(new BigDecimal("80.00"), dto.getFundedAmount()),
                () -> assertEquals(new BigDecimal("120.00"), dto.getRemainingAmount()),
                () -> assertTrue(dto.isFundingTargetConfigured())
        );
    }

    @Test
    void displayedPercentageTracksDatabaseValueOnEachReadIncludingZeroAndNull() {
        Animal animal = animal(52L, "distanta");
        when(animals.findById(52L)).thenReturn(Optional.of(animal));
        when(animals.findAll()).thenReturn(List.of(animal));
        when(funding.getFunding(List.of(animal))).thenReturn(Map.of(52L,
                new AnimalFundingDto("2026-11", null, BigDecimal.ZERO, null, 0, false)));

        for (Integer percent : new Integer[]{15, 10, 20, 0, null}) {
            animal.setFundingPercent(percent);
            assertEquals(percent, service.getAnimal(52L).getFundingPercent());
            assertEquals(percent, list("la_distanta").getFirst().getFundingPercent());
            assertNull(service.getAnimal(52L).getCalculatedFundingPercent());
        }
    }

    @Test
    void absentMedicalFileDoesNotInventMedicalOrPhotoData() {
        Animal animal = animal(14L, "fizica");
        animal.setShelterName("Foster test");
        when(animals.findById(14L)).thenReturn(Optional.of(animal));

        AnimalResponseDto dto = service.getAnimal(14L);

        assertAll(
                () -> assertFalse(dto.isVaccinated()),
                () -> assertFalse(dto.isDewormed()),
                () -> assertFalse(dto.isSterilized()),
                () -> assertNull(dto.getMicrochipped()),
                () -> assertNull(dto.getAllergiesAndConditions()),
                () -> assertNull(dto.getHealthStatus()),
                () -> assertEquals(List.of(), dto.getTreatments()),
                () -> assertEquals(List.of(), dto.getImages()),
                () -> assertNull(dto.getImageUrl()),
                () -> assertFalse(dto.getIsDistanceAdoption()),
                () -> assertEquals("Foster test", dto.getShelter())
        );
    }

    @Test
    void nullableMedicalFlagsAreHandledWithoutUnboxingNull() {
        when(animals.findById(15L)).thenReturn(Optional.of(animal(15L, "fizica")));
        when(medical.findByAnimalIdIn(List.of(15L))).thenReturn(List.of(
                FisaMedicala.builder().id(5L).animalId(15L).build()));

        AnimalResponseDto dto = assertDoesNotThrow(() -> service.getAnimal(15L));

        assertFalse(dto.isVaccinated());
        assertFalse(dto.isDewormed());
        assertFalse(dto.isSterilized());
        assertNull(dto.getMicrochipped());
    }

    @Test
    void mainPhotoWinsOverUnorderedPhotosWithNullableMainFlags() {
        when(animals.findById(16L)).thenReturn(Optional.of(animal(16L, "fizica")));
        ImagineAnimal nullableFlag = ImagineAnimal.builder().id(1L).animalId(16L)
                .imageUrl("unknown-main-flag.jpg").isMain(null).build();
        when(images.findByAnimalIdInOrderByIsMainDescIdAsc(List.of(16L))).thenReturn(List.of(
                photo(8L, 16L, "last-secondary.jpg", false), nullableFlag,
                photo(9L, 16L, "actual-main.jpg", true),
                photo(3L, 16L, "first-secondary.jpg", false)));

        AnimalResponseDto dto = assertDoesNotThrow(() -> service.getAnimal(16L));

        assertEquals("actual-main.jpg", dto.getImageUrl());
        assertEquals(List.of("actual-main.jpg", "unknown-main-flag.jpg",
                "first-secondary.jpg", "last-secondary.jpg"), dto.getImages());
    }

    @Test
    void unknownAnimalReturns404WithoutQueryingPopupData() {
        when(animals.findById(404L)).thenReturn(Optional.empty());

        ResponseStatusException error = assertThrows(ResponseStatusException.class,
                () -> service.getAnimal(404L));

        assertEquals(HttpStatus.NOT_FOUND, error.getStatusCode());
        verifyNoInteractions(medical, images, centers, treatments, funding);
    }

    @Test
    void noMatchingAnimalsSkipsAllAuxiliaryQueries() {
        when(animals.findAll()).thenReturn(List.of(animal(2L, "fizica")));

        assertEquals(List.of(), list("la_distanta"));
        verifyNoInteractions(medical, images, centers, treatments, funding);
    }

    private List<AnimalResponseDto> list(String adoptionType) {
        return service.getAnimals(adoptionType, null, null, null, null, null, null,
                null, null, null, null, null, null, null);
    }

    private Animal animal(Long id, String adoptionType) {
        Animal animal = new Animal();
        animal.setId(id);
        animal.setName("Animal test " + id);
        animal.setAdoptionType(adoptionType);
        animal.setAge("2 ani");
        return animal;
    }

    private ImagineAnimal photo(Long id, Long animalId, String url, boolean main) {
        return ImagineAnimal.builder().id(id).animalId(animalId).imageUrl(url).isMain(main).build();
    }
}
