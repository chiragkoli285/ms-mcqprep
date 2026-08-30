package com.myapp.mcqprep.repository;

import com.myapp.mcqprep.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface QuestionRepository extends JpaRepository<Question, UUID> {
    boolean existsByContentHash(String contentHash);
    Optional<Question> findByContentHash(String contentHash);
}
