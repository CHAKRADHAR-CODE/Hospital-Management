package com.example.hospital;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/receptionists")
public class ReceptionistController {

    private final ReceptionistService receptionistService;

    public ReceptionistController(ReceptionistService receptionistService) {
        this.receptionistService = receptionistService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<Receptionist> list() {
        return receptionistService.getAllReceptionists();
    }

    @GetMapping("/me")
    public Receptionist me() {
        return receptionistService.getMe();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Receptionist get(@PathVariable Long id) {
        return receptionistService.getReceptionist(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> create(@RequestBody Requests.ReceptionistCreate req) {
        if (req.receptionist == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error(400, "Receptionist details are required"));
        }
        return ResponseEntity.ok(receptionistService.createReceptionist(req.username, req.email, req.receptionist));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Receptionist update(@PathVariable Long id, @RequestBody Receptionist updates) {
        return receptionistService.updateReceptionist(id, updates);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        receptionistService.deleteReceptionist(id);
        return ResponseEntity.ok(ApiResponse.success("Receptionist removed"));
    }
}
