package org.fc.fcboardwork.repository;

import org.fc.fcboardwork.domain.Article;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article, Long> {
}