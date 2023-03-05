package ru.bmstu.labs.queriestfidf.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

@Entity
@Table(schema = "public", name = "document_words",
        uniqueConstraints = {@UniqueConstraint(name = "document_words_unique", columnNames = {"word_id", "document_id"})})
@Getter
@Setter
@ToString
public class DocumentWord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id")
    private Word word;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id")
    private Document document;

    @NotNull
    @Column(name = "frequency")
    private Long frequency;
}