package api.iba.signalapi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import api.iba.signalapi.models.Signal;
import api.iba.signalapi.repository.SignalRepository;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

/*
 * Composant Spring géré par le conteneur
 * Cette classe sera exécutée au démarrage de l'application,
 * sauf si le profil actif est "test"
 */
@Component
@Profile("!test")
public class DataLoader implements CommandLineRunner {

    /*
     * Repository pour interagir avec la base de données des signaux
     */
    @Autowired
    private SignalRepository signalRepository;

    /*
     * Hash MD5 attendu pour vérifier l'intégrité du fichier CSV
     */
    private static final String EXPECTED_MD5 = "f5baf0111b1fac6f1a06b4a2f5085af9";

    /*
     * Nombre de signaux à traiter par lot lors de la sauvegarde
     */
    private static final int BATCH_SIZE = 1000;

    /*
     * Méthode principale exécutée au lancement de l'application
     * Vérifie si la base est vide, et si oui, charge les signaux depuis le fichier
     * CSV
     */
    @Override
    public void run(String... args) throws Exception {
        ClassPathResource resource = new ClassPathResource("signals.csv");
        /*
         * Vérification de l'intégrité du fichier CSV avec le hash MD5 attendu
         */
        if (!verifyMd5Hash(resource)) {
            throw new RuntimeException("Le fichier signals.csv a été modifié ou est corrompu !");
        }

        long count = signalRepository.count();

        /*
         * Si la base est vide, chargement des signaux depuis le fichier CSV
         */
        if (count == 0) {
            System.out.println("Base vide détectée (" + count + " signaux). Chargement des données...");
            loadSignalsFromCsv(resource);
            System.out.println("Données chargées : " + signalRepository.count() + " signaux.");
        } else {
            System.out.println("Base déjà peuplée avec " + count + " signaux. Aucun rechargement nécessaire.");
        }
    }

    /*
     * Charge les signaux depuis le fichier CSV et les insère en base de données
     * en traitant les données par lots pour des performances optimales
     */
    public void loadSignalsFromCsv(ClassPathResource resource) throws Exception {
        List<Signal> batch = new ArrayList<>(BATCH_SIZE);

        /*
         * Lecture du fichier CSV ligne par ligne
         */
        try (InputStream inputStream = resource.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

            String line;
            br.readLine(); // Ignore la ligne d'en-tête

            while ((line = br.readLine()) != null) {
                String[] data = line.split(",", -1);

                /*
                 * Vérifie que la ligne contient bien les 5 colonnes attendues
                 */
                if (data.length < 5) {
                    System.err.println("Ligne incorrecte ignorée : " + line);
                    continue;
                }

                /*
                 * Extraction des données de la ligne avec gestion des champs vides
                 */
                String nodeId = data[0];
                Integer samplingIntervalMs = data[1].isEmpty() ? null : Integer.parseInt(data[1]);
                Integer deadbandValue = data[2].isEmpty() ? null : Integer.parseInt(data[2]);
                String deadbandType = data[3].isEmpty() ? null : data[3];
                Boolean active = data[4].equals("1");

                /*
                 * Ajout du signal au lot actuel
                 */
                batch.add(new Signal(nodeId, samplingIntervalMs, deadbandValue, deadbandType, active));

                /*
                 * Si le lot atteint la taille définie, sauvegarde en base et réinitialise le
                 * lot
                 */
                if (batch.size() >= BATCH_SIZE) {
                    signalRepository.saveAll(batch);
                    batch.clear();
                }
            }

            /*
             * Sauvegarde du dernier lot restant après la lecture complète du fichier
             */
            if (!batch.isEmpty()) {
                signalRepository.saveAll(batch);
            }
        }
    }

    /*
     * Vérifie l'intégrité du fichier CSV en calculant son hash MD5
     * et en le comparant à la valeur attendue
     */
    public boolean verifyMd5Hash(ClassPathResource resource) throws Exception {
        try (InputStream inputStream = resource.getInputStream()) {
            byte[] fileBytes = inputStream.readAllBytes();
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(fileBytes);

            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }

            return EXPECTED_MD5.equals(sb.toString());
        }
    }
}
