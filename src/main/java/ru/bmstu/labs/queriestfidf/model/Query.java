package ru.bmstu.labs.queriestfidf.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(schema = "public", name = "queries")
@Getter
@Setter
@ToString
public class Query {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Column(columnDefinition = "text")
    private String value;

    @OneToMany(mappedBy = "query")
    private Set<QueryDocument> queryDocuments = new LinkedHashSet<>();
}
