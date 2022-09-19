package fr.bvedrenne.state;

import fr.bvedrenne.state.command.Command;
import fr.bvedrenne.console.completer.Completable;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.io.PrintWriter;
import java.util.List;

public class DefaultState implements State, Completable {
    private static final List<Command> COMMAND_LIST = List.of(new Command(List.of("databases", "list"), new ListState()));

    /**
     *
     */
    @Override
    public void enter(PrintWriter writer) {
        writer.println("Enter your command: ");
    }

    /**
     * @return
     */
    @Override
    public State doAction(PrintWriter writer, String line) {
        return COMMAND_LIST.stream().filter(c -> c.matches(line)).map(Command::state).findAny().orElse(this);
    }

    /**
     * @return
     */
    @Override
    public Completer getCompleter() {
        return new Completer() {
            @Override
            public void complete(LineReader lineReader, ParsedLine parsedLine, List<Candidate> list) {
                COMMAND_LIST.stream().map(c -> c.commandMatching(parsedLine.line().trim())).flatMap(x -> x.stream())
                        .map(Candidate::new).forEach(list::add);
            }
        };
    }
}
