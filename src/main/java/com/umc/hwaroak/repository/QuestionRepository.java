package com.umc.hwaroak.repository;

import com.umc.hwaroak.domain.Question;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {

    // DB에서 태그에 맞는 멘트 랜덤으로 하나 가져옴.
    @Query("SELECT q FROM Question q WHERE q.tag = :tag ORDER BY function('RAND')")
    List<Question> findRandomOneByTag(@Param("tag") String tag, Pageable pageable);

    //tag로 등록된 멘트가 하나라도 존재하는지
    @Query("SELECT COUNT(q) > 0 FROM Question q WHERE q.tag = :tag")
    boolean existsByTag(@Param("tag") String tag);

}
