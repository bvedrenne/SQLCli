package fr.bvedrenne.console.completer;

import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.util.List;

public class SQLCliCompleter implements Completer {
    private Completable completable;

    /**
     * @param lineReader
     * @param parsedLine
     * @param list
     */
    @Override
    public void complete(LineReader lineReader, ParsedLine parsedLine, List<Candidate> list) {
        if (this.completable != null)
            completable.getCompleter().complete(lineReader, parsedLine, list);
    }

    public void declareCompletable(Completable completable) {
        this.completable = completable;
    }

    public void flush() {
        this.completable = null;
    }
}
