package org.cognizant.programmanagement.dao;

import org.cognizant.programmanagement.entity.EmergencyReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EmergencyRepository extends JpaRepository<EmergencyReport, Integer> {

    List<EmergencyReport> findByCitizenId(Integer citizenId);

}