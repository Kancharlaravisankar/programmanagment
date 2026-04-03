package org.cognizant.programmanagement.service;

import org.cognizant.programmanagement.entity.RecoveryProgram;
import org.cognizant.programmanagement.dao.RecoveryProgramRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class RecoveryService {

    @Autowired
    private RecoveryProgramRepository programRepository;

    public void createProgram(RecoveryProgram program) {
        programRepository.save(program);
    }

    public List<RecoveryProgram> getAllPrograms() {
        return programRepository.findAll();
    }

    public RecoveryProgram getProgramById(int id) {
        return programRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Program not found with ID: " + id));
    }


}
