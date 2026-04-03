package org.cognizant.programmanagement.controller;

import jakarta.validation.Valid;
import org.cognizant.programmanagement.Enum.ResourceStatus;
import org.cognizant.programmanagement.dto.request.ResourceRequestDTO;
import org.cognizant.programmanagement.dto.response.ResourceResponseDTO;
import org.cognizant.programmanagement.entity.Resource;
import org.cognizant.programmanagement.service.ResourceService;
//import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/resources")
public class ResourceController {

    @Autowired
    private ResourceService resourceService;

    @PostMapping("/add")
    public ResponseEntity<ResourceResponseDTO> addResource(@Valid @RequestBody ResourceRequestDTO dto) {
        Resource entity = toEntity(dto);
        resourceService.addResource(dto.getProgramId(), entity);
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponseDTO(entity));
    }

   /* @PutMapping("/{id}/consume")
    public ResponseEntity<ResourceResponseDTO> consumeResource(
            @PathVariable int id,
            @RequestParam double amount,
            @RequestParam String receiverName,
            @RequestParam int managerId) {

        Resource updated = resourceService.consumeResource(id, amount, receiverName, managerId);
        return ResponseEntity.ok(toResponseDTO(updated));
    }*/

    @GetMapping("/viewAll")
    public ResponseEntity<List<ResourceResponseDTO>> getAllResources() {
        List<ResourceResponseDTO> responseList = resourceService.getAllResources()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responseList);
    }



    private ResourceResponseDTO toResponseDTO(Resource entity) {
        ResourceResponseDTO dto = new ResourceResponseDTO();
        dto.setResourceId(entity.getResourceId());
        dto.setName(entity.getName());
        dto.setType(entity.getType());
        dto.setQuantity(entity.getQuantity());
        dto.setUnit(entity.getUnit());
        dto.setStatus(entity.getStatus());
        dto.setReceivedBy(entity.getReceivedBy());

        if (entity.getRecoveryProgram() != null) {
            dto.setProgramId(entity.getRecoveryProgram().getProgramId());
        }
        return dto;
    }

    private Resource toEntity(ResourceRequestDTO dto) {
        Resource entity = new Resource();
        entity.setName(dto.getName());
        entity.setType(dto.getType());
        entity.setQuantity(dto.getQuantity());
        entity.setUnit(dto.getUnit());

        entity.setStatus(ResourceStatus.ALLOCATED);
        entity.setReceivedBy("");
        return entity;
    }
}