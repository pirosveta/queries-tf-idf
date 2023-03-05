package ru.bmstu.labs.queriestfidf;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.stachek66.nlp.mystem.holding.Factory;
import ru.stachek66.nlp.mystem.holding.MyStem;
import ru.stachek66.nlp.mystem.holding.MyStemApplicationException;
import ru.stachek66.nlp.mystem.holding.Request;
import scala.Option;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class Lemmatizer {

    private MyStem myStemAnalyzer;

    private Logger log = LoggerFactory.getLogger(Lemmatizer.class);

    public Lemmatizer() {
        System.setProperty("os.name", "Windows7");
        System.setProperty("os.arch", "x86_64");

        this.myStemAnalyzer = new Factory("-igd --eng-gr --format json --weight")
                .newMyStem("3.1", Option.empty()).get();
    }

    public List<String> extractLemmasFromText(String text) {
        List<String> lemmas = new ArrayList<>();

        Pattern pattern = Pattern.compile("([a-zA-Z]+)|([\\u0400-\\u044F\\u0401\\u0451]+)");
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            String word = text.substring(matcher.start(), matcher.end()).toLowerCase();
            String lemma = word;

            try {
                lemma = new String(myStemAnalyzer.analyze(Request.apply(word))
                        .info().head().lex().get().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
//                log.trace("word={} lemma={}", word, lemma);
            } catch (MyStemApplicationException | NoSuchElementException e) {
                log.debug("method=extractLemmasFromText message='Failed to analyze word {}: error={}'", word, e.getMessage());
            } finally {
                lemmas.add(lemma);
            }
        }

        return lemmas;
    }
}
