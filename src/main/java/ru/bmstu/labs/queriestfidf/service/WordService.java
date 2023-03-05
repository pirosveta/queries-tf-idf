package ru.bmstu.labs.queriestfidf.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.bmstu.labs.queriestfidf.model.Word;
import ru.bmstu.labs.queriestfidf.repository.DocumentRepository;
import ru.bmstu.labs.queriestfidf.repository.DocumentWordRepository;
import ru.bmstu.labs.queriestfidf.repository.WordRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class WordService {

    private WordRepository wordRepository;
    private DocumentWordRepository documentWordRepository;
    private DocumentRepository documentRepository;

    public Optional<Word> getByValue(String value) {
        return wordRepository.getByValue(value);
    }

    public Word getById(Integer id) {
        return wordRepository.getById(id);
    }

    public void countIdfForAll() {
        List<Word> words = wordRepository.findAll();
        long countOfDocuments = documentRepository.count();

        for (Word word : words) {
            Long countByWord = documentWordRepository.countByWord(word);
            word.setIdf(BigDecimal.valueOf(Math.log10((double) countOfDocuments / countByWord)));
            wordRepository.save(word);
        }
    }

}
