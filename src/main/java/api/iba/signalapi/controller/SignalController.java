package api.iba.signalapi.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import api.iba.signalapi.models.Signal;
import api.iba.signalapi.repository.SignalRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/signals")
public class SignalController {

        @Autowired
        private SignalRepository signalRepository;

        /**
         * * Récupérer tous les signaux
         * 
         * @return
         */
        @GetMapping
        public List<Signal> getAllSignals() {
                return signalRepository.findAll();
        }

        /**
         * * Récupérer un signal par son nodeId
         * 
         * @param nodeId
         * @return
         */
        @GetMapping("/by-node-id")
        public ResponseEntity<Signal> getSignalByNodeId(@RequestParam("node_id") String nodeId) {
                return signalRepository.findById(nodeId)
                                .map(ResponseEntity::ok)
                                .orElse(ResponseEntity.notFound().build());
        }

        /**
         * * Récupérer les statistiques des signaux
         * 
         * @return
         */
        @GetMapping("/stats")
        public ResponseEntity<Map<String, Object>> getSignalStats() {
                List<Signal> signals = signalRepository.findAll();

                if (signals.isEmpty()) {
                        return ResponseEntity.noContent().build();
                }

                long totalSignals = signals.size();
                long activeSignals = signals.stream()
                                .filter(s -> Boolean.TRUE.equals(s.getActive())) // Remplace null-safe par
                                                                                 // Boolean.TRUE.equals()
                                .count();
                long inactiveSignals = totalSignals - activeSignals;

                // Récupérer et trier les sampling_interval_ms valides
                List<Integer> samplingIntervals = signals.stream()
                                .map(Signal::getSamplingIntervalMs)
                                .filter(Objects::nonNull)
                                .sorted(Comparator.naturalOrder())
                                .collect(Collectors.toList());

                double averageSamplingInterval = samplingIntervals.stream()
                                .mapToInt(Integer::intValue)
                                .average()
                                .orElse(0.0);

                // Calculer la médiane
                double medianSamplingInterval = 0;
                int size = samplingIntervals.size();
                if (size > 0) {
                        medianSamplingInterval = (size % 2 == 0)
                                        ? (samplingIntervals.get(size / 2 - 1) + samplingIntervals.get(size / 2)) / 2.0
                                        : samplingIntervals.get(size / 2);
                }

                // Répartition des types de deadband (en évitant les null)
                Map<String, Long> deadbandTypeCount = signals.stream()
                                .map(s -> Optional.ofNullable(s.getDeadbandType()).orElse("UNKNOWN"))
                                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

                // Récupérer et trier les valeurs de deadband_value valides
                List<Integer> deadbandValues = signals.stream()
                                .map(Signal::getDeadbandValue)
                                .filter(Objects::nonNull)
                                .sorted(Comparator.naturalOrder())
                                .collect(Collectors.toList());

                int minDeadbandValue = deadbandValues.isEmpty() ? 0 : deadbandValues.get(0);
                int maxDeadbandValue = deadbandValues.isEmpty() ? 0 : deadbandValues.get(deadbandValues.size() - 1);

                // Construction de la réponse JSON
                Map<String, Object> stats = Map.ofEntries(
                                Map.entry("total_signals", totalSignals),
                                Map.entry("active_signals", activeSignals),
                                Map.entry("inactive_signals", inactiveSignals),
                                Map.entry("average_sampling_interval", averageSamplingInterval),
                                Map.entry("median_sampling_interval", medianSamplingInterval),
                                Map.entry("deadband_type_distribution", deadbandTypeCount),
                                Map.entry("min_deadband_value", minDeadbandValue),
                                Map.entry("max_deadband_value", maxDeadbandValue));

                return ResponseEntity.ok(stats);
        }

}