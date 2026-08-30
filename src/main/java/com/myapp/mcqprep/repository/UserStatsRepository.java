package com.myapp.mcqprep.repository;

import com.myapp.mcqprep.entity.UserStats;
import com.myapp.mcqprep.entity.UserStats.UserStatsId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface UserStatsRepository extends JpaRepository<UserStats, UserStatsId> {
    List<UserStats> findByIdUserId(UUID userId);
}