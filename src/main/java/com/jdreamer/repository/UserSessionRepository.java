package com.jdreamer.repository;

import com.jdreamer.model.UserSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSessionRepository extends JpaRepository<UserSession, Integer> {
    UserSession findByBookId(int bookId);

    List<UserSession> findUserSessionsByOpenAtStartup(boolean flag);
}
