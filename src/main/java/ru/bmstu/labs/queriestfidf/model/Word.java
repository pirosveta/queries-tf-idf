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
@Table(schema = "public", name = "words")
@Getter
@Setter
@ToString
public class Word {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    private String value;

    @Column(columnDefinition = "double")
    private BigDecimal idf;

    @OneToMany(mappedBy = "word")
    private Set<DocumentWord> documentWords = new LinkedHashSet<>();
}
