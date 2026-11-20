package proiect_ispgl.proiect_ispgl.Service;

import org.springframework.stereotype.Service;
import proiect_ispgl.proiect_ispgl.Model.Animal;
import proiect_ispgl.proiect_ispgl.Repository.DonationRepository;
import proiect_ispgl.proiect_ispgl.dto.AnimalFundingDto;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.YearMonth;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Service
public class AnimalFundingService {
    private final DonationRepository donationRepository;
    private final Clock clock;

    public AnimalFundingService(DonationRepository donationRepository, Clock clock) {
        this.donationRepository = donationRepository;
        this.clock = clock;
    }

    public Map<Long, AnimalFundingDto> getFunding(Collection<Animal> animals) {
        if (animals.isEmpty()) return Map.of();
        YearMonth month = YearMonth.now(clock);
        var totals = donationRepository.sumConfirmedByAnimal(
                animals.stream().map(Animal::getId).toList(),
                month.atDay(1).atStartOfDay(), month.plusMonths(1).atDay(1).atStartOfDay());
        Map<Long, AnimalFundingDto> result = new HashMap<>();
        for (Animal animal : animals) {
            BigDecimal target = animal.getMonthlyDonationTarget();
            BigDecimal funded = totals.getOrDefault(animal.getId(), BigDecimal.ZERO);
            boolean configured = target != null && target.signum() > 0;
            BigDecimal remaining = configured ? target.subtract(funded).max(BigDecimal.ZERO) : null;
            // Floor avoids displaying 100% until the target is actually reached.
            int percent = configured ? funded.multiply(BigDecimal.valueOf(100))
                    .divide(target, 0, RoundingMode.DOWN).min(BigDecimal.valueOf(100)).intValue() : 0;
            result.put(animal.getId(), new AnimalFundingDto(
                    month.toString(), target, funded, remaining, percent, configured));
        }
        return result;
    }
}
