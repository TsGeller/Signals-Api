package api.iba.signalapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import api.iba.signalapi.models.Signal;

public interface SignalRepository extends JpaRepository<Signal, String> {
}