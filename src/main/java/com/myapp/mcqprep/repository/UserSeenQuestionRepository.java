package com.myapp.mcqprep.repository;

import com.myapp.mcqprep.entity.UserSeenQuestion;
import com.myapp.mcqprep.entity.UserSeenQuestion.UserSeenQuestionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface UserSeenQuestionRepository extends JpaRepository<UserSeenQuestion, UserSeenQuestionId> {

    // powers the "exclusion list" fed into the generation prompt
    @Query("""
        select s.id.questionHash from UserSeenQuestion s
        where s.id.userId = :userId
        order by s.seenAt desc
        limit 50
        """)
    List<String> findRecentHashesForUser(@Param("userId") UUID userId);
}
