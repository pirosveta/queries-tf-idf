package ru.bmstu.labs.queriestfidf.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.bmstu.labs.queriestfidf.model.*;

import java.util.List;
import java.util.Optional;

@Repository
public interface QueryDocumentRepository extends JpaRepository<QueryDocument, Integer> {

    Optional<QueryDocument> getByQueryAndDocument(Query query, Document document);

    List<QueryDocument> getByQuery(Query query);

    List<QueryDocument> getByDocument(Document document);
}
