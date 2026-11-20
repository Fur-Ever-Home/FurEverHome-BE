package proiect_ispgl.proiect_ispgl.Controller;

import org.springframework.web.bind.annotation.*;
import proiect_ispgl.proiect_ispgl.Model.DonatieBani;
import proiect_ispgl.proiect_ispgl.Repository.DonatieBaniRepository;

import java.util.UUID;

@RestController
@RequestMapping("/api/donatii")
@CrossOrigin(origins = "*")
public class DonatiiController {

    private final DonatieBaniRepository repository;

    public DonatiiController(DonatieBaniRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public DonatieBani salveazaDonatie(@RequestBody DonatieBani donatie) {
        // Generăm un ID simulat de tranzacție și setăm statusul ca finalizat (fără redirect la plată)
        donatie.setIdTranzactieStripe("SIM_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        donatie.setStatusPlata("FINALIZATA");
        return repository.save(donatie);
    }
}