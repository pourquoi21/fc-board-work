package org.fc.fcboardwork.service;

import lombok.RequiredArgsConstructor;
import org.fc.fcboardwork.domain.type.SearchType;
import org.fc.fcboardwork.dto.ArticleDto;
import org.fc.fcboardwork.dto.ArticleUpdateDto;
import org.fc.fcboardwork.repository.ArticleRepository;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
@Transactional
@Service
public class ArticleService {

    private final ArticleRepository articleRepository;

    @Transactional(readOnly = true)
    public Page<ArticleDto> searchArticles(SearchType searchType, String searchKeyword) {
        return Page.empty();
    }

    public ArticleDto searchArticles(long l) {
        return null;
    }

    public void saveArticle(ArticleDto dto) {
    }

    public void updateArticle(long articleId, ArticleUpdateDto dto) {
    }

    public void deleteArticle(long articleId) {
    }
}
