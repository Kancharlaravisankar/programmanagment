package org.cognizant.programmanagement.dao;

import org.cognizant.programmanagement.entity.Incident;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IncidentRepository extends JpaRepository<Incident, Integer> {
}