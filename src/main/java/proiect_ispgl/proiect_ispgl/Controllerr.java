package proiect_ispgl.proiect_ispgl;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class Controllerr {
    private final TestRepo testRepo;

    public Controllerr(TestRepo testRepo)
    {
        this.testRepo=testRepo;
    }

    @GetMapping("/test")
    public List<Test> getAll()
    {
        return testRepo.findAll();
    }
}
