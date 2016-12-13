package com.cloudage.membercenter.repository;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import com.cloudage.membercenter.entity.Article;
import com.cloudage.membercenter.entity.User;

@Repository
public interface IArticleRepository extends PagingAndSortingRepository<Article, Integer> {
	
	@Query("from Article article where article.author = ?1")
	List<Article> findAllByAuthor(User user);

	@Query("from Article article where article.author.id = ?1")
	List<Article> findAllByAuthorId(Integer userId);
}
