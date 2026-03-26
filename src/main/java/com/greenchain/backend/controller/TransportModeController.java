package com.greenchain.backend.controller;

import com.greenchain.backend.model.TransportMode;
import com.greenchain.backend.repository.TransportModeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/transport-modes")
class TransportModeController {

    @Autowired
    private TransportModeRepository transportModeRepository;

    @GetMapping
    public List<TransportMode> getAllModes() {
        return transportModeRepository.findAll();
    }

    @PostMapping
    public TransportMode createMode(@RequestBody TransportMode mode) {
        return transportModeRepository.save(mode);
    }
}