package com.aiml.voice.repository;

import com.aiml.voice.model.Conversation;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, Long> {
    List<Conversation> findBySessionIdOrderByCreatedAtDesc(String sessionId);
    List<Conversation> findBySessionIdOrderByCreatedAtAsc(String sessionId);
    List<Conversation> findByUsedForTrainingFalse();
    long countByUsedForTrainingFalse();
    List<Conversation> findByIntent(String intent);

     // Paginated versions
    Page<Conversation> findByUsedForTrainingFalse(Pageable pageable);
    Page<Conversation> findByIntent(String intent, Pageable pageable);
}