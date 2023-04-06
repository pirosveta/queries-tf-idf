package ru.bmstu.labs.queriestfidf.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.bmstu.labs.queriestfidf.repository.QueryDocumentRepository;

@Service
@RequiredArgsConstructor
public class QueryDocumentService {

    private final QueryDocumentRepository queryDocumentRepository;
}
