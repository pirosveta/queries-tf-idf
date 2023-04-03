package ru.bmstu.labs.queriestfidf.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.bmstu.labs.queriestfidf.model.Document;
import ru.bmstu.labs.queriestfidf.model.DocumentWord;
import ru.bmstu.labs.queriestfidf.model.Word;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentWordRepository extends JpaRepository<DocumentWord, Integer> {

    Optional<DocumentWord> getByDocumentAndWord(Document document, Word word);

    List<DocumentWord> getByWord(Word word);

    Long countByWord(Word word);

    List<DocumentWord> getByDocument(Document document);
}
