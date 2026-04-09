package org.cognizant.programmanagement.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.cognizant.programmanagement.dto.request.RecoveryProgramRequestDTO;
import org.cognizant.programmanagement.dto.response.RecoveryProgramResponseDTO;
import org.cognizant.programmanagement.service.RecoveryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/programs")
@AllArgsConstructor
public class RecoveryController {

    private final RecoveryService recoveryService;

    @PostMapping("/create")
    public ResponseEntity<RecoveryProgramResponseDTO> createProgram(@Valid @RequestBody RecoveryProgramRequestDTO dto) {
        // The service now handles both the mapping and the persistence
        RecoveryProgramResponseDTO response = recoveryService.createProgram(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/viewAll")
    public ResponseEntity<List<RecoveryProgramResponseDTO>> getAllPrograms() {
        List<RecoveryProgramResponseDTO> programs = recoveryService.getAllPrograms();
        return ResponseEntity.ok(programs);
    }

    @GetMapping("/view/{id}")
    public ResponseEntity<RecoveryProgramResponseDTO> getProgramById(@PathVariable int id) {
        RecoveryProgramResponseDTO program = recoveryService.getProgramById(id);
        return ResponseEntity.ok(program);
    }
}