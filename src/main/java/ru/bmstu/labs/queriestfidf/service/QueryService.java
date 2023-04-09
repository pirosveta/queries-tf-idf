package ru.bmstu.labs.queriestfidf.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.bmstu.labs.queriestfidf.model.Query;
import ru.bmstu.labs.queriestfidf.repository.QueryRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class QueryService {

    private final QueryRepository queryRepository;

    public List<Query> getEntities() {
        return queryRepository.findAll();
    }

    public Query getEntity(Integer id) {
        return queryRepository.findById(id).orElseGet(Query::new);
    }
}
