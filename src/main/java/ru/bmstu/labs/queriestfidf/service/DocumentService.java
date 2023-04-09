package ru.bmstu.labs.queriestfidf.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.bmstu.labs.queriestfidf.Lemmatizer;
import ru.bmstu.labs.queriestfidf.model.Document;
import ru.bmstu.labs.queriestfidf.model.DocumentWord;
import ru.bmstu.labs.queriestfidf.model.Word;
import ru.bmstu.labs.queriestfidf.repository.DocumentRepository;
import ru.bmstu.labs.queriestfidf.repository.DocumentWordRepository;
import ru.bmstu.labs.queriestfidf.repository.WordRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final DocumentWordRepository documentWordRepository;
    private final WordRepository wordRepository;

    private final Lemmatizer lemmatizer;

    public List<Document> getEntities() {
        return documentRepository.findAll();
    }

    public Document getEntity(Integer id) {
        return documentRepository.findById(id).orElseGet(Document::new);
    }

    public void fillLemmasFieldForAll() {
        List<Document> documents = documentRepository.findAll();

        for (Document document : documents) {
            List<String> lemmas = lemmatizer.extractLemmasFromText(document.getValue());
            document.setLength(lemmas.size());
            document.setLemmas(lemmas.stream().map(Objects::toString).collect(Collectors.joining("@")));
            document = documentRepository.save(document);

            for (String lemma : lemmas) {
                Optional<Word> wordOptional = wordRepository.getByValue(lemma);
                Word databaseWord;

                if (wordOptional.isPresent()) {
                    databaseWord = wordOptional.get();
                } else {
                    databaseWord = new Word();
                    databaseWord.setValue(lemma);
                    databaseWord = wordRepository.save(databaseWord);
                }

                Optional<DocumentWord> documentWordOptional = documentWordRepository.getByDocumentAndWord(document, databaseWord);
                DocumentWord documentWord;

                if (documentWordOptional.isPresent()) {
                    documentWord = documentWordOptional.get();
                    documentWord.setFrequency(documentWord.getFrequency() + 1L);
                } else {
                    documentWord = new DocumentWord();

                    documentWord.setDocument(document);
                    documentWord.setWord(databaseWord);
                    documentWord.setFrequency(1L);
                }

                documentWordRepository.save(documentWord);
            }
        }
    }

    public void fillLengthFieldForAll() {
        List<Document> documents = documentRepository.findAll();

        for (Document document : documents) {
            List<DocumentWord> documentWords = documentWordRepository.getByDocument(document);

            double tfLength = 0;
            double logTfLength = 0;
            for (DocumentWord documentWord : documentWords) {
                Word word = wordRepository.findById(documentWord.getWord().getId()).orElseGet(Word::new);
                double idfMultiply = word.getIdf().doubleValue() * word.getIdf().doubleValue();
                logTfLength += (Math.log(documentWord.getFrequency() + 1) * Math.log(documentWord.getFrequency() + 1))
                        * idfMultiply;
                tfLength += (documentWord.getFrequency() * documentWord.getFrequency())
                        * idfMultiply;
            }

            document.setTfLength(BigDecimal.valueOf(Math.sqrt(tfLength)));
            document.setLogTfLength(BigDecimal.valueOf(Math.sqrt(logTfLength)));
            documentRepository.save(document);
        }
    }
}
