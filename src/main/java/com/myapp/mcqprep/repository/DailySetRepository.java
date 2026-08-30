package com.myapp.mcqprep.repository;

import com.myapp.mcqprep.entity.DailySet;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public interface DailySetRepository extends JpaRepository<DailySet, UUID> {
    Optional<DailySet> findTopByUserIdAndDateOrderByIdDesc(UUID userId, LocalDate date);
}
