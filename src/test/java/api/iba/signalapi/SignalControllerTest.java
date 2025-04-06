package api.iba.signalapi;

import api.iba.signalapi.models.Signal;
import api.iba.signalapi.repository.SignalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test") // Ajout du profil "test
public class SignalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SignalRepository signalRepository;

    /**
     * * Méthode exécutée avant chaque test pour préparer l'environnement de test.
     */
    @BeforeEach
    public void setUp() {

        signalRepository.deleteAll();

        Signal s1 = new Signal("NODE1", 2000, 1, "ABSOLUTE", true);
        Signal s2 = new Signal("NODE2", 1000, 2, "RELATIVE", false);

        signalRepository.saveAll(Arrays.asList(s1, s2));
        System.out.println("setUp completed: 2 signals inserted into the database");
    }

    /**
     * * Test de la méthode getAllSignals() du contrôleur SignalController.
     * 
     * @throws Exception
     */
    @Test
    public void testGetAllSignals() throws Exception {
        mockMvc.perform(get("/signals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
        System.out.println("testGetAllSignals passed: Retrieved 2 signals successfully");
    }

    /**
     * * Test de la méthode getSignalByNodeId() du contrôleur SignalController.
     * 
     * @throws Exception
     */
    @Test
    public void testGetSignalByNodeId() throws Exception {
        mockMvc.perform(get("/signals/by-node-id").param("node_id", "NODE1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nodeId", is("NODE1")));
        System.out.println("testGetSignalByNodeId passed: Retrieved signal with nodeId NODE1 successfully");
    }

    /**
     * * Test de la méthode getSignalStats() du contrôleur SignalController.
     * 
     * @throws Exception
     */
    @Test
    public void testGetSignalStats() throws Exception {
        mockMvc.perform(get("/signals/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_signals", is(2))) // Total des signaux
                .andExpect(jsonPath("$.active_signals", is(1))) // Signaux actifs
                .andExpect(jsonPath("$.inactive_signals", is(1))) // Signaux inactifs
                .andExpect(jsonPath("$.average_sampling_interval", is(1500.0))) // Moyenne : (2000 + 1000) / 2
                .andExpect(jsonPath("$.median_sampling_interval", is(1500.0))) // Médiane : (2000 + 1000) / 2
                .andExpect(jsonPath("$.deadband_type_distribution.ABSOLUTE", is(1))) // Distribution : 1 ABSOLUTE
                .andExpect(jsonPath("$.deadband_type_distribution.RELATIVE", is(1))) // Distribution : 1 RELATIVE
                .andExpect(jsonPath("$.min_deadband_value", is(1))) // Min deadband_value
                .andExpect(jsonPath("$.max_deadband_value", is(2))); // Max deadband_value
        System.out.println("testGetSignalStats passed: Signal stats retrieved successfully");
    }
}