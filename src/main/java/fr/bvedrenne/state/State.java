package fr.bvedrenne.state;

import org.jline.reader.LineReader;
import org.jline.terminal.Terminal;

import java.io.PrintWriter;

public interface State {
    void enter(PrintWriter writer);

    State doAction(PrintWriter writer, String line);
}
