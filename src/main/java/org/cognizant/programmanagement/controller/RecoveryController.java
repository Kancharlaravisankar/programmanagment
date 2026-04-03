package org.cognizant.programmanagement.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.cognizant.programmanagement.dto.request.RecoveryProgramRequestDTO;
import org.cognizant.programmanagement.dto.response.RecoveryProgramResponseDTO;
import org.cognizant.programmanagement.dto.response.ResourceResponseDTO;
import org.cognizant.programmanagement.entity.RecoveryProgram;
import org.cognizant.programmanagement.service.RecoveryService;
import org.cognizant.programmanagement.Enum.RecoveryStatus;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/programs")
@AllArgsConstructor
public class RecoveryController {

    private final RecoveryService recoveryService;


    @PostMapping("/create")
    public ResponseEntity<RecoveryProgramResponseDTO> createProgram(@Valid @RequestBody RecoveryProgramRequestDTO dto) {
        RecoveryProgram entity = toEntity(dto);//modelMapper.map(dto, RecoveryProgram.class);;
        recoveryService.createProgram(entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponseDTO(entity));
    }

    @GetMapping("/viewAll")
    public ResponseEntity<List<RecoveryProgramResponseDTO>> getAllPrograms() {
        List<RecoveryProgramResponseDTO> list = recoveryService.getAllPrograms()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }


    @GetMapping("/view/{id}")
    public ResponseEntity<RecoveryProgramResponseDTO> getProgramById(@PathVariable int id) {
        // Fetch from service (which throws exception if not found)
        RecoveryProgram program = recoveryService.getProgramById(id);

        return ResponseEntity.ok(toResponseDTO(program));
    }


    private RecoveryProgramResponseDTO toResponseDTO(RecoveryProgram entity) {
        RecoveryProgramResponseDTO res = new RecoveryProgramResponseDTO();
        res.setProgramId(entity.getProgramId());
        res.setTitle(entity.getTitle());
        res.setDescription(entity.getDescription());
        res.setStartDate(entity.getStartDate());
        res.setEndDate(entity.getEndDate());
        res.setBudget(entity.getBudget());
        res.setStatus(entity.getStatus());

        if (entity.getResources() != null) {
            res.setResources(entity.getResources().stream().map(resEntity -> {
                ResourceResponseDTO rDto = new ResourceResponseDTO();
                rDto.setResourceId(resEntity.getResourceId());
                rDto.setName(resEntity.getName());
                rDto.setType(resEntity.getType());
                rDto.setQuantity(resEntity.getQuantity());
                rDto.setUnit(resEntity.getUnit());
                rDto.setStatus(resEntity.getStatus());
                rDto.setReceivedBy(resEntity.getReceivedBy());
                return rDto;
            }).collect(Collectors.toList()));
        }
        return res;
    }

    private RecoveryProgram toEntity(RecoveryProgramRequestDTO dto) {
        RecoveryProgram entity = new RecoveryProgram();
        entity.setTitle(dto.getTitle());
        entity.setDescription(dto.getDescription());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setBudget(dto.getBudget());
        entity.setStatus(RecoveryStatus.PLANNED);
        return entity;
    }
}