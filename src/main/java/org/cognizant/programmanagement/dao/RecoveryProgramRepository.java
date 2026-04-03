package org.cognizant.programmanagement.dao;
import org.cognizant.programmanagement.entity.RecoveryProgram;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface RecoveryProgramRepository extends JpaRepository<RecoveryProgram, Integer> {

}