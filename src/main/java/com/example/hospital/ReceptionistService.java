package com.example.hospital;

import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReceptionistService {

    private final ReceptionistRepository receptionistRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final UserService userService;

    public ReceptionistService(ReceptionistRepository receptionistRepository,
                               UserRepository userRepository,
                               CurrentUserService currentUserService,
                               UserService userService) {
        this.receptionistRepository = receptionistRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.userService = userService;
    }

    public Map<String, Object> createReceptionist(String username, String email,
                                                  Receptionist details) {
        User user = userService.registerAccountGenerated(username, email, Roles.RECEPTIONIST);
        try {
            Receptionist receptionist = new Receptionist();
            receptionist.setUserId(user.getId());
            receptionist.setName(details.getName());
            receptionist.setPhone(details.getPhone());
            receptionistRepository.save(receptionist);
        } catch (RuntimeException e) {
            userRepository.deleteById(user.getId());
            throw e;
        }
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("id", user.getId());
        resp.put("username", user.getUsername());
        resp.put("password", user.getRawPassword());
        resp.put("role", Roles.RECEPTIONIST);
        return resp;
    }

    public List<Receptionist> getAllReceptionists() {
        return receptionistRepository.findAll().stream().map(this::enrich).toList();
    }

    public Receptionist getReceptionist(Long id) {
        return enrich(receptionistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receptionist not found")));
    }

    public Receptionist getMe() {
        return enrich(currentUserService.currentReceptionist());
    }

    public Receptionist updateReceptionist(Long id, Receptionist updates) {
        Receptionist receptionist = receptionistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receptionist not found"));
        if (updates.getName() != null && !updates.getName().isBlank()) receptionist.setName(updates.getName());
        if (updates.getPhone() != null) receptionist.setPhone(updates.getPhone());
        return receptionistRepository.save(receptionist);
    }

    public void deleteReceptionist(Long id) {
        Receptionist receptionist = receptionistRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Receptionist not found"));
        receptionistRepository.deleteById(id);
        if (receptionist.getUserId() != null) {
            userRepository.deleteById(receptionist.getUserId());
        }
    }

    public int countReceptionists() {
        return (int) receptionistRepository.count();
    }

    private Receptionist enrich(Receptionist receptionist) {
        if (receptionist == null) return null;
        if (receptionist.getUserId() != null) {
            userRepository.findById(receptionist.getUserId()).ifPresent(u ->
                    receptionist.setUsername(u.getUsername()));
        }
        return receptionist;
    }
}
