package ru.bmstu.labs.queriestfidf.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.bmstu.labs.queriestfidf.model.Word;

import java.util.Optional;

@Repository
public interface WordRepository extends JpaRepository<Word, Long> {

    Optional<Word> getByValue(String value);

    Word getById(Integer id);
}
