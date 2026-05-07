package com.jdreamer.repository;

import com.jdreamer.model.MediaLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MediaLinkRepository extends JpaRepository<MediaLink, String> {
    List<MediaLink> findMediaLinksByBookIdAndPageId(int bookId, int pageId);
}
