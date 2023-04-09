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
@Table(schema = "public", name = "documents")
@Getter
@Setter
@ToString
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotNull
    @Column(columnDefinition = "text")
    private String value;

    @Column(columnDefinition = "text")
    private String lemmas;

    @Column(name = "tf_length")
    private BigDecimal tfLength;

    @Column(name = "log_tf_length")
    private BigDecimal logTfLength;

    @Column(name = "length")
    private Integer length;

    @OneToMany(mappedBy = "document")
    private Set<DocumentWord> documentWords = new LinkedHashSet<>();
}
