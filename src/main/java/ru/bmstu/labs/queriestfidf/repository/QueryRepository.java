package ru.bmstu.labs.queriestfidf.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.bmstu.labs.queriestfidf.model.Query;

@Repository
public interface QueryRepository extends JpaRepository<Query, Integer> {
}
