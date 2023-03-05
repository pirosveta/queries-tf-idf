package ru.bmstu.labs.queriestfidf.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.bmstu.labs.queriestfidf.model.Document;
import ru.bmstu.labs.queriestfidf.model.DocumentWord;
import ru.bmstu.labs.queriestfidf.model.Word;
import ru.bmstu.labs.queriestfidf.repository.DocumentWordRepository;

import java.util.Optional;

@Service
@AllArgsConstructor
public class DocumentWordService {

    private DocumentWordRepository documentWordRepository;

    public Optional<DocumentWord> getByDocumentAndWord(Document document, Word word) {
        return documentWordRepository.getByDocumentAndWord(document, word);
    }

}
