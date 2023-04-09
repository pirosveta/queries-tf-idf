package ru.bmstu.labs.queriestfidf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import ru.bmstu.labs.queriestfidf.model.*;
import ru.bmstu.labs.queriestfidf.service.*;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static ru.bmstu.labs.queriestfidf.CalculationMethod.LANGUAGE_MODEL_LAMBDA_0_5;
import static ru.bmstu.labs.queriestfidf.CalculationMethod.LANGUAGE_MODEL_LAMBDA_0_9;

@SpringBootApplication
public class QueriesTfIdfApplication {

    private static final double LAMBDA_0_5 = 0.5, LAMBDA_0_9 = 0.9, TOP_POSITIONS = 10;
    private static final String OUTPUT_EXTENSION = ".txt";

    private static DocumentService documentService;
    private static WordService wordService;
    private static DocumentWordService documentWordService;
    private static QueryService queryService;
    private static QueryDocumentService queryDocumentService;

    private static Lemmatizer lemmatizer;
    private static CalculationMethod calculationMethod;

    private static Integer totalCollectionWords;

    public static void main(String[] args) {
        ApplicationContext applicationContext = SpringApplication.run(ru.bmstu.labs.queriestfidf.QueriesTfIdfApplication.class, args);

        /**
         * Services initialization
         */
        documentService = applicationContext.getBean(DocumentService.class);
        wordService = applicationContext.getBean(WordService.class);
        documentWordService = applicationContext.getBean(DocumentWordService.class);
        queryService = applicationContext.getBean(QueryService.class);
        queryDocumentService = applicationContext.getBean(QueryDocumentService.class);

        /**
         * Additional beans initialization
         */
        lemmatizer = applicationContext.getBean(Lemmatizer.class);

        /**
         * Database initialization
         */
        initializeDatabase();

        /**
         * Relevance and NDCG calculation for all methods
         */
        calculate();
    }

    private static void initializeDatabase() {
//        documentService.fillLemmasFieldForAll();
//        wordService.countIdfForAll();
//        documentService.fillLengthFieldForAll();
    }

    private static void calculate() {
        HashMap<CalculationMethod, HashMap<String, Double>> ndcgs = new HashMap<>();
        totalCollectionWords = 0;

        List<Document> documents = documentService.getEntities();
        for (Document document : documents) {
            totalCollectionWords += document.getLength();
        }

        Arrays.stream(CalculationMethod.values()).forEach(m -> {
            StringBuilder prefix = new StringBuilder();
            calculationMethod = m;

            List<Map.Entry<Document, Double>> triggerRelevance = getRelevanceForQuery(1);
            List<Map.Entry<Document, Double>> knotRelevance = getRelevanceForQuery(2);
            List<Map.Entry<Document, Double>> tsrRelevance = getRelevanceForQuery(3);

            switch (calculationMethod) {
                case VECTOR_MODEL_TF -> prefix.append("vm-tf");
                case VECTOR_MODEL_LOG_TF -> prefix.append("vm-log-tf");
                case LANGUAGE_MODEL_LAMBDA_0_5 -> prefix.append("lm-lambda-0.5");
                case LANGUAGE_MODEL_LAMBDA_0_9 -> prefix.append("lm-lambda-0.9");
            }
            prefix.append("-");

            printRelevanceToFile(triggerRelevance, prefix + "trigger-relevance");
            printRelevanceToFile(knotRelevance, prefix + "knot-relevance");
            printRelevanceToFile(tsrRelevance, prefix + "tsr-relevance");

            ndcgs.put(m, new HashMap<>() {{
                put("trigger", ndcgCalculate(1, triggerRelevance));
            }});
            ndcgs.get(m).put("knot", ndcgCalculate(2, knotRelevance));
            ndcgs.get(m).put("tsr", ndcgCalculate(3, tsrRelevance));
        });

        printNdcgsToFile(ndcgs);
    }

    private static double ndcgCalculate(Integer queryId, List<Map.Entry<Document, Double>> relevance) {
        double idcg = 0;
        double dcg = 0;

        List<QueryDocument> queryDocuments = queryDocumentService.getByQueryId(queryId);
        queryDocuments.sort(Comparator.comparing(QueryDocument::getRelevance));

        int currentPosition = 1;
        for (QueryDocument queryDocument : queryDocuments) {
            idcg += queryDocument.getRelevance() / Math.log(currentPosition + 1);
            currentPosition++;
        }

        currentPosition = 1;
        while (currentPosition <= TOP_POSITIONS) {
            Integer documentId = relevance.get(currentPosition - 1).getKey().getId();
            Optional<QueryDocument> queryDocumentOptional = queryDocumentService.getByQueryIdAndDocumentId(queryId, documentId);

            if (queryDocumentOptional.isPresent()) {
                dcg += queryDocumentOptional.get().getRelevance() / Math.log(currentPosition + 1);
            }

            currentPosition++;
        }

        return dcg / idcg;
    }

    private static List<Map.Entry<Document, Double>> getRelevanceForQuery(Integer id) {
        Query query = queryService.getEntity(id);

        List<String> lemmas = lemmatizer.extractLemmasFromText(query.getValue());
        HashMap<Integer, Double> vector = getVectorOfQuery(lemmas);

        List<Document> documents = documentService.getEntities();
        HashMap<Document, Double> relevances = new HashMap<>();

        for (Document document : documents) {
            double relevanceValue = switch (calculationMethod) {
                case VECTOR_MODEL_TF, VECTOR_MODEL_LOG_TF -> 0;
                case LANGUAGE_MODEL_LAMBDA_0_5, LANGUAGE_MODEL_LAMBDA_0_9 -> 1;
            };

            for (Map.Entry<Integer, Double> queryValue : vector.entrySet()) {
                if (queryValue.getKey() > 0) {
                    Word word = wordService.getById(queryValue.getKey());
                    Optional<DocumentWord> documentWord = documentWordService.getByDocumentAndWord(document, word);

                    if (documentWord.isPresent()) {
                        switch (calculationMethod) {
                            case VECTOR_MODEL_TF ->
                                    relevanceValue += (documentWord.get().getFrequency() * word.getIdf().doubleValue())
                                            * queryValue.getValue();                // Using of tf
                            case VECTOR_MODEL_LOG_TF ->
                                    relevanceValue += (Math.log(documentWord.get().getFrequency() + 1) * word.getIdf().doubleValue())
                                            * Math.log(queryValue.getValue() + 1);  // Using of log(tf+1)
                            case LANGUAGE_MODEL_LAMBDA_0_5 ->
                                    relevanceValue *= (LAMBDA_0_5 * documentWord.get().getFrequency() / document.getLength())
                                            + ((1 - LAMBDA_0_5) * queryValue.getValue() / totalCollectionWords);
                            case LANGUAGE_MODEL_LAMBDA_0_9 ->
                                    relevanceValue *= (LAMBDA_0_9 * documentWord.get().getFrequency() / document.getLength())
                                            + ((1 - LAMBDA_0_9) * queryValue.getValue() / totalCollectionWords);
                        }
                    } else {
                        switch (calculationMethod) {
                            case LANGUAGE_MODEL_LAMBDA_0_5 ->
                                    relevanceValue *= (1 - LAMBDA_0_5) * queryValue.getValue() / totalCollectionWords;
                            case LANGUAGE_MODEL_LAMBDA_0_9 ->
                                    relevanceValue *= (1 - LAMBDA_0_9) * queryValue.getValue() / totalCollectionWords;
                        }

                    }
                } else if (calculationMethod == LANGUAGE_MODEL_LAMBDA_0_5
                        || calculationMethod == LANGUAGE_MODEL_LAMBDA_0_9) {
                    switch (calculationMethod) {
                        case LANGUAGE_MODEL_LAMBDA_0_5 ->
                                relevanceValue *= (LAMBDA_0_5 * 1 / (document.getLength() + 1))
                                        + ((1 - LAMBDA_0_5) * queryValue.getValue() / (totalCollectionWords + 1));
                        case LANGUAGE_MODEL_LAMBDA_0_9 ->
                                relevanceValue *= (LAMBDA_0_9 * 1 / (document.getLength() + 1))
                                        + ((1 - LAMBDA_0_9) * queryValue.getValue() / (totalCollectionWords + 1));
                    }
                }
            }

            switch (calculationMethod) {
                case VECTOR_MODEL_TF -> relevanceValue /= document.getTfLength().doubleValue();
                case VECTOR_MODEL_LOG_TF -> relevanceValue /= document.getLogTfLength().doubleValue();
            }

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

        if (calculationMethod == CalculationMethod.VECTOR_MODEL_TF
                || calculationMethod == CalculationMethod.VECTOR_MODEL_LOG_TF) {
            double length = 0;
            for (Map.Entry<Integer, Integer> entry : frequencyDictionary.entrySet()) {
                switch (calculationMethod) {
                    case VECTOR_MODEL_TF -> length += entry.getValue() * entry.getValue();
                    case VECTOR_MODEL_LOG_TF ->
                            length += Math.log(entry.getValue() + 1) * Math.log(entry.getValue() + 1);
                }
            }
            length = Math.sqrt(length);

            for (Map.Entry<Integer, Integer> entry : frequencyDictionary.entrySet()) {
                vector.put(entry.getKey(), entry.getValue().doubleValue() / length);
            }
        } else if (calculationMethod == LANGUAGE_MODEL_LAMBDA_0_5
                || calculationMethod == LANGUAGE_MODEL_LAMBDA_0_9) {
            for (Map.Entry<Integer, Integer> entry : frequencyDictionary.entrySet()) {
                int totalWordFrequency = 1;

                if (entry.getKey() > 0) {
                    List<DocumentWord> documentWords = documentWordService.getByWord(wordService.getById(entry.getKey()));

                    for (DocumentWord documentWord : documentWords) {
                        totalWordFrequency += documentWord.getFrequency();
                    }
                }

                vector.put(entry.getKey(), (double) totalWordFrequency);
            }
        }

        return vector;
    }

    private static void printRelevanceToFile(List<Map.Entry<Document, Double>> relevances, String fileName) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(fileName + OUTPUT_EXTENSION));
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

    private static void printNdcgsToFile(HashMap<CalculationMethod, HashMap<String, Double>> ndcgs) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter("ndcg" + OUTPUT_EXTENSION));
            for (Map.Entry<CalculationMethod, HashMap<String, Double>> ndcg : ndcgs.entrySet()) {
                writer.append(ndcg.getKey().name())
                        .append("\n");

                for (Map.Entry<String, Double> queryNdcg : ndcg.getValue().entrySet()) {
                    writer.append(queryNdcg.getKey())
                            .append("\t\t")
                            .append(queryNdcg.getValue().toString())
                            .append("\n");
                }
                writer.append("\n");
            }
            writer.close();
        } catch (IOException ignored) {
        }
    }

}
