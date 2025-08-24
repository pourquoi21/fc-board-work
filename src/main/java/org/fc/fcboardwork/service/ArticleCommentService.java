package org.fc.fcboardwork.service;

import lombok.RequiredArgsConstructor;
import org.fc.fcboardwork.dto.ArticleCommentDto;
import org.fc.fcboardwork.repository.ArticleCommentRepository;
import org.fc.fcboardwork.repository.ArticleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Transactional
@Service
public class ArticleCommentService {

    private final ArticleCommentRepository articleCommentRepository;
    private final ArticleRepository articleRepository;

    @Transactional(readOnly = true)
    public List<ArticleCommentDto> searchArticleComment(Long articleId) {
        return List.of();
    }

    public void saveArticleComment(ArticleCommentDto dto) {
    }
}
