package ru.bmstu.labs.queriestfidf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import ru.bmstu.labs.queriestfidf.model.Document;
import ru.bmstu.labs.queriestfidf.model.DocumentWord;
import ru.bmstu.labs.queriestfidf.model.Word;
import ru.bmstu.labs.queriestfidf.service.DocumentService;
import ru.bmstu.labs.queriestfidf.service.DocumentWordService;
import ru.bmstu.labs.queriestfidf.service.WordService;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

@SpringBootApplication
public class QueriesTfIdfApplication {

    private static DocumentService documentService;
    private static WordService wordService;
    private static DocumentWordService documentWordService;
    private static Lemmatizer lemmatizer;

    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(ru.bmstu.labs.queriestfidf.QueriesTfIdfApplication.class, args);

        documentService = applicationContext.getBean(DocumentService.class);
        wordService = applicationContext.getBean(WordService.class);
        documentWordService = applicationContext.getBean(DocumentWordService.class);
        lemmatizer = applicationContext.getBean(Lemmatizer.class);

        initializeDatabase();

        String triggerQuery = "«Самый умный конь кино» умел сидеть на стуле, развязывать верёвки и стрелять из пистолета";
        String knotQuery = "«Виктория» и «принц Альберт» — галстучные узлы";
        String tsrQuery = "Международный конгресс по правописанию английского языка одобрил переход на пересмотренное традиционное правописание";

        List<Map.Entry<Document, Double>> triggerRelevance = getRelevanceForQuery(triggerQuery);
        List<Map.Entry<Document, Double>> knotRelevance = getRelevanceForQuery(knotQuery);
        List<Map.Entry<Document, Double>> tsrRelevance = getRelevanceForQuery(tsrQuery);

        printRelevanceToFile(triggerRelevance, "trigger-relevance-2");
        printRelevanceToFile(knotRelevance, "knot-relevance-2");
        printRelevanceToFile(tsrRelevance, "tsr-relevance-2");
    }

    private static void initializeDatabase() {
//        documentService.fillLemmasFieldForAll();
//        wordService.countIdfForAll();
//        documentService.fillLengthFieldForAll();
    }

    private static List<Map.Entry<Document, Double>> getRelevanceForQuery(String query) {
        List<String> lemmas = lemmatizer.extractLemmasFromText(query);
        HashMap<Integer, Double> vector = getVectorOfQuery(lemmas);

        List<Document> documents = documentService.getEntities();
        HashMap<Document, Double> relevances = new HashMap<>();

        for (Document document : documents) {
            double relevanceValue = 0;
            for (Map.Entry<Integer, Double> queryValue : vector.entrySet()) {
                if (queryValue.getKey() > 0) {
                    Word word = wordService.getById(queryValue.getKey());
                    Optional<DocumentWord> documentWord = documentWordService.getByDocumentAndWord(document, word);
                    if (documentWord.isPresent()) {
                       /* relevanceValue += (documentWord.get().getFrequency() * word.getIdf().doubleValue())
                         * queryValue.getValue();*/    // Using of tf
                        relevanceValue += (Math.log(documentWord.get().getFrequency() + 1) * word.getIdf().doubleValue())
                                * Math.log(queryValue.getValue() + 1);    // Using of log(tf+1)
                    }
                }
            }
//            relevanceValue /= document.getTfLength().doubleValue();
            relevanceValue /= document.getLogTfLength().doubleValue();

            relevances.put(document, relevanceValue);
        }

        List<Map.Entry<Document, Double>> relevanceList = new ArrayList<>(relevances.entrySet());
        Comparator<Map.Entry<Document, Double>> valueComparator = (o1, o2) -> o2.getValue().compareTo(o1.getValue());

        relevanceList.sort(valueComparator);

        return relevanceList;
    }

    private static HashMap<Integer, Double> getVectorOfQuery(List<String> lemmas) {
        HashMap<Integer, Integer> frequencyDictionary = new HashMap<>();
        HashMap<Integer, Double> vector = new HashMap<>();
        HashMap<String, Integer> localDatabaseWords = new HashMap<>();
        int notExistKey = -1;

        for (String lemma : lemmas) {
            Optional<Word> wordOptional = wordService.getByValue(lemma);
            if (wordOptional.isPresent()) {
                Word word = wordOptional.get();
                frequencyDictionary.merge(word.getId(), 1, Integer::sum);
            } else {
                if (localDatabaseWords.containsKey(lemma)) {
                    frequencyDictionary.merge(localDatabaseWords.get(lemma), 1, Integer::sum);
                } else {
                    localDatabaseWords.put(lemma, notExistKey);
                    frequencyDictionary.put(notExistKey, 1);
                    notExistKey -= 1;
                }
            }
        }

        double length = 0;
        for (Map.Entry<Integer, Integer> entry : frequencyDictionary.entrySet()) {
//            length += entry.getValue() * entry.getValue();
            length += Math.log(entry.getValue() + 1) * Math.log(entry.getValue() + 1);
        }
        length = Math.sqrt(length);

        for (Map.Entry<Integer, Integer> entry : frequencyDictionary.entrySet()) {
            vector.put(entry.getKey(), entry.getValue().doubleValue() / length);
        }

        return vector;
    }

    private static void printRelevanceToFile(List<Map.Entry<Document, Double>> relevances, String fileName) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName));
            for (Map.Entry<Document, Double> relevance : relevances) {
                writer.append(relevance.getValue().toString())
                        .append("\t\t")
                        .append(relevance.getKey().getValue())
                        .append("\n");
            }
            writer.close();
        } catch (IOException ignored) {
        }
    }

}
