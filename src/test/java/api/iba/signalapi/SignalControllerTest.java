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
@ActiveProfiles("test")
public class SignalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SignalRepository signalRepository;

    @BeforeEach
    public void setUp() {
        signalRepository.deleteAll();

        Signal s1 = new Signal("NODE1", 2000, 1, "ABSOLUTE", true);
        Signal s2 = new Signal("NODE2", 1000, 2, "RELATIVE", false);

        signalRepository.saveAll(Arrays.asList(s1, s2));
        System.out.println("setUp completed: 2 signals inserted into the database");
    }

    @Test
    public void testGetAllSignals() throws Exception {
        mockMvc.perform(get("/signals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
        System.out.println("testGetAllSignals passed: Retrieved 2 signals successfully");
    }

    @Test
    public void testGetSignalByNodeId() throws Exception {
        mockMvc.perform(get("/signals/by-node-id").param("node_id", "NODE1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nodeId", is("NODE1")));
        System.out.println("testGetSignalByNodeId passed: Retrieved signal with nodeId NODE1 successfully");
    }

    @Test
    public void testGetSignalByInvalidNodeId() throws Exception {
        mockMvc.perform(get("/signals/by-node-id").param("node_id", "INVALID_NODE"))
                .andExpect(status().isNotFound());
        System.out.println("testGetSignalByInvalidNodeId passed: Correct 404 error returned for invalid nodeId");
    }

    @Test
    public void testGetSignalStats() throws Exception {
        mockMvc.perform(get("/signals/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total_signals", is(2)))
                .andExpect(jsonPath("$.active_signals", is(1)))
                .andExpect(jsonPath("$.inactive_signals", is(1)))
                .andExpect(jsonPath("$.average_sampling_interval", is(1500.0)))
                .andExpect(jsonPath("$.median_sampling_interval", is(1500.0)))
                .andExpect(jsonPath("$.deadband_type_distribution.ABSOLUTE", is(1)))
                .andExpect(jsonPath("$.deadband_type_distribution.RELATIVE", is(1)))
                .andExpect(jsonPath("$.min_deadband_value", is(1)))
                .andExpect(jsonPath("$.max_deadband_value", is(2)));
        System.out.println("testGetSignalStats passed: Signal stats retrieved successfully");
    }

    @Test
    public void testGetSignalStatsWhenNoSignals() throws Exception {
        signalRepository.deleteAll(); // Supprimer tous les signaux pour tester ce cas

        mockMvc.perform(get("/signals/stats"))
                .andExpect(status().isNoContent()) // Vérification du statut 204
                .andExpect(jsonPath("$").doesNotExist()); // Aucune donnée retournée
        System.out
                .println("testGetSignalStatsWhenNoSignals passed: Correct 204 response when no signals are available");
    }

    @Test
    public void testGetInvalidEndpoint() throws Exception {
        mockMvc.perform(get("/signals/invalid-endpoint"))
                .andExpect(status().isNotFound());
        System.out.println("testGetInvalidEndpoint passed: Correct 404 error returned for invalid endpoint");
    }
}
