package dev.minkin.ingot.backend.projection.repository;

import dev.minkin.ingot.backend.projection.entity.PersonalRecordEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonalRecordRepository extends JpaRepository<PersonalRecordEntity, String> {}
