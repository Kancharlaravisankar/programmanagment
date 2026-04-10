package org.cognizant.programmanagement.controller;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.cognizant.programmanagement.dto.request.ResourceRequestDTO;
import org.cognizant.programmanagement.dto.response.ResourceResponseDTO;
import org.cognizant.programmanagement.service.ResourceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/resources")
@AllArgsConstructor
public class ResourceController {

    private final ResourceService resourceService;

    @PostMapping("/add")
    public ResponseEntity<ResourceResponseDTO> addResource(
            @Valid @RequestBody ResourceRequestDTO dto,
            @RequestParam int managerId) {

        // Service handles conversion from RequestDTO and returns ResponseDT
        ResourceResponseDTO response = resourceService.addResource(dto, managerId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}/consume")
    public ResponseEntity<ResourceResponseDTO> consumeResource(
            @PathVariable int id,
            @RequestParam double amount,
            @RequestParam String receiverName,
            @RequestParam int managerId) {

        ResourceResponseDTO updated = resourceService.consumeResource(id, amount, receiverName, managerId);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/viewAll")
    public ResponseEntity<List<ResourceResponseDTO>> getAllResources() {
        return ResponseEntity.ok(resourceService.getAllResources());
    }
}