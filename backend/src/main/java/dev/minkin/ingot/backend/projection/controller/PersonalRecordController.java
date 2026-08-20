package dev.minkin.ingot.backend.projection.controller;

import dev.minkin.ingot.backend.projection.entity.PersonalRecordEntity;
import dev.minkin.ingot.backend.projection.repository.PersonalRecordRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class PersonalRecordController {

    private final PersonalRecordRepository repository;

    public PersonalRecordController(PersonalRecordRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/prs")
    public List<PersonalRecordEntity> getPRs() {
        return repository.findAll();
    }
}
