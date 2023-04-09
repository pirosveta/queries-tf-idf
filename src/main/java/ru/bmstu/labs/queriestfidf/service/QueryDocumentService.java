package ru.bmstu.labs.queriestfidf.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.bmstu.labs.queriestfidf.model.Document;
import ru.bmstu.labs.queriestfidf.model.Query;
import ru.bmstu.labs.queriestfidf.model.QueryDocument;
import ru.bmstu.labs.queriestfidf.repository.QueryDocumentRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QueryDocumentService {

    private final QueryDocumentRepository queryDocumentRepository;

    private final QueryService queryService;
    private final DocumentService documentService;

    public List<QueryDocument> getByQueryId(Integer queryId) {
        Query query = queryService.getEntity(queryId);
        return queryDocumentRepository.getByQuery(query);
    }

    public Optional<QueryDocument> getByQueryIdAndDocumentId(Integer queryId, Integer documentId) {
        Query query = queryService.getEntity(queryId);
        Document document = documentService.getEntity(documentId);

        return queryDocumentRepository.getByQueryAndDocument(query, document);
    }
}
