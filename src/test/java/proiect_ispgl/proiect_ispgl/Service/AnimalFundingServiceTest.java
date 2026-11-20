package proiect_ispgl.proiect_ispgl.Service;

import org.junit.jupiter.api.Test;
import proiect_ispgl.proiect_ispgl.Model.Animal;
import proiect_ispgl.proiect_ispgl.Repository.DonationRepository;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AnimalFundingServiceTest {
    private final DonationRepository repository = mock(DonationRepository.class);
    // UTC October 31 is already November in Romania.
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-31T22:30:00Z"), ZoneId.of("Europe/Bucharest"));
    private final AnimalFundingService service = new AnimalFundingService(repository, clock);

    @Test
    void calculatesCurrentRomanianMonthWithExclusiveUpperBound() {
        when(repository.sumConfirmedByAnimal(any(), any(), any())).thenReturn(Map.of(1L, new BigDecimal("250.00")));
        var result = service.getFunding(List.of(animal(1L, "500.00"))).get(1L);
        assertThat(result.month()).isEqualTo("2026-11");
        assertThat(result.percent()).isEqualTo(50);
        assertThat(result.remainingAmount()).isEqualByComparingTo("250.00");
        verify(repository).sumConfirmedByAnimal(List.of(1L), LocalDateTime.of(2026, 11, 1, 0, 0),
                LocalDateTime.of(2026, 12, 1, 0, 0));
    }

    @Test
    void capsProgressAndRemainingButPreservesActualDonatedAmount() {
        when(repository.sumConfirmedByAnimal(any(), any(), any())).thenReturn(Map.of(1L, new BigDecimal("750.00")));
        var result = service.getFunding(List.of(animal(1L, "500"))).get(1L);
        assertThat(result.percent()).isEqualTo(100);
        assertThat(result.remainingAmount()).isEqualByComparingTo("0");
        assertThat(result.fundedAmount()).isEqualByComparingTo("750");
    }

    @Test
    void doesNotInventTargetOrReuseStoredTestPercentage() {
        when(repository.sumConfirmedByAnimal(any(), any(), any())).thenReturn(Map.of());
        for (String target : new String[]{null, "0", "-5"}) {
            Animal animal = animal(1L, target);
            animal.setFundingPercent(75);
            var result = service.getFunding(List.of(animal)).get(1L);
            assertThat(result.targetConfigured()).isFalse();
            assertThat(result.remainingAmount()).isNull();
            assertThat(result.percent()).isZero();
            assertThat(result.fundedAmount()).isEqualByComparingTo("0");
        }
    }

    @Test
    void noDonationsIsZeroAndAlmostCompleteDoesNotRoundToOneHundred() {
        when(repository.sumConfirmedByAnimal(any(), any(), any())).thenReturn(Map.of(2L, new BigDecimal("499.99")));
        var result = service.getFunding(List.of(animal(1L, "500"), animal(2L, "500")));
        assertThat(result.get(1L).percent()).isZero();
        assertThat(result.get(1L).targetConfigured()).isTrue();
        assertThat(result.get(1L).remainingAmount()).isEqualByComparingTo("500");
        assertThat(result.get(2L).percent()).isEqualTo(99);
    }

    @Test
    void skipsDatabaseForEmptyInput() {
        assertThat(service.getFunding(List.of())).isEmpty();
        verifyNoInteractions(repository);
    }

    private Animal animal(Long id, String target) {
        var animal = new Animal();
        animal.setId(id);
        animal.setMonthlyDonationTarget(target == null ? null : new BigDecimal(target));
        return animal;
    }
}
