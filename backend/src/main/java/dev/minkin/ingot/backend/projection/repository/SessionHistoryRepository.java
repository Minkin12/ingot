package dev.minkin.ingot.backend.projection.repository;

import dev.minkin.ingot.backend.projection.entity.SessionHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SessionHistoryRepository extends JpaRepository<SessionHistoryEntity, UUID> {
    List<SessionHistoryEntity> findAllByOrderByWeekNumberAscDayNumberAsc();
}
