package proiect_ispgl.proiect_ispgl.Controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;
import proiect_ispgl.proiect_ispgl.Service.AnimalService;
import proiect_ispgl.proiect_ispgl.dto.AnimalResponseDto;
import proiect_ispgl.proiect_ispgl.dto.MedicalTreatmentDto;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AnimalControllerTest {
    private AnimalService service;
    private MockMvc mvc;

    @BeforeEach
    void setUp() {
        service = mock(AnimalService.class);
        mvc = MockMvcBuilders.standaloneSetup(new AnimalController(service)).build();
    }

    @Test
    void detailsEndpointSerializesDistanceFlagAndTreatmentHistory() throws Exception {
        when(service.getAnimal(7L)).thenReturn(AnimalResponseDto.builder()
                .id(7L).name("Animal test").adoptionType("distanta").isDistanceAdoption(true)
                .locationType("Foster").microchipped(true).healthStatus("Sub tratament")
                .images(List.of("main.jpg"))
                .treatments(List.of(new MedicalTreatmentDto(3, "Control test", "Evaluare",
                        "Descriere test", LocalDate.of(2026, 11, 12))))
                .build());

        mvc.perform(get("/api/animals/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7))
                .andExpect(jsonPath("$.adoptionType").value("distanta"))
                .andExpect(jsonPath("$.isDistanceAdoption").value(true))
                .andExpect(jsonPath("$.distanceAdoption").doesNotExist())
                .andExpect(jsonPath("$.locationType").value("Foster"))
                .andExpect(jsonPath("$.microchipped").value(true))
                .andExpect(jsonPath("$.healthStatus").value("Sub tratament"))
                .andExpect(jsonPath("$.images[0]").value("main.jpg"))
                .andExpect(jsonPath("$.treatments[0].id").value(3))
                .andExpect(jsonPath("$.treatments[0].name").value("Control test"))
                .andExpect(jsonPath("$.treatments[0].category").value("Evaluare"))
                .andExpect(jsonPath("$.treatments[0].description").value("Descriere test"))
                .andExpect(jsonPath("$.treatments[0].recordedAt").value("2026-11-12"));
        verify(service).getAnimal(7L);
    }

    @Test
    void missingAnimalIsAnHttp404() throws Exception {
        when(service.getAnimal(404L)).thenThrow(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Animalul nu exista"));

        mvc.perform(get("/api/animals/404")).andExpect(status().isNotFound());
    }

    @Test
    void nonNumericAnimalIdIsAnHttp400() throws Exception {
        mvc.perform(get("/api/animals/invalid")).andExpect(status().isBadRequest());
        verifyNoInteractions(service);
    }
}
