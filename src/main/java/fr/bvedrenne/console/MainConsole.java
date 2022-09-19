package fr.bvedrenne.console;

import fr.bvedrenne.console.completer.Completable;
import fr.bvedrenne.console.completer.SQLCliCompleter;
import fr.bvedrenne.state.DefaultState;
import fr.bvedrenne.state.State;
import org.apache.commons.lang3.StringUtils;
import org.fusesource.jansi.AnsiConsole;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import java.io.Closeable;
import java.io.IOException;

public class MainConsole {
    private static final String DEFAULT_PROMPT = "> ";

    private String appName = "";

    private State currentState = new DefaultState();

    public MainConsole(String appName) {
        this.appName = appName;
    }

    public void start() {
        AnsiConsole.systemInstall();
        try (Terminal terminal = TerminalBuilder.builder()
                .system(true)
                .signalHandler(Terminal.SignalHandler.SIG_IGN)
                .nativeSignals(true)
                .jansi(true)
                .name(this.appName)
                .build()) {

            SQLCliCompleter completer = new SQLCliCompleter();
            LineReader lineReader = LineReaderBuilder.builder()
                    .terminal(terminal)
                    .completer(completer)
                    .appName(this.appName)
                    .completer(completer)
                    .build();

            while (true) {
                try {
                    if (currentState instanceof Completable completable) {
                        completer.declareCompletable(completable);
                    }
                    currentState.enter(terminal.writer());

                    String promptInfo = managePrompt(this.appName, currentState);

                    String line = lineReader.readLine(promptInfo).trim();
                    if (StringUtils.equalsAnyIgnoreCase(line, "quit", "exit")) {
                        throw new EndOfFileException(line);
                    }
                    if ("reset".equals(line)) {
                        if (this.currentState instanceof Closeable closeable) {
                            closeable.close();
                        }
                        this.currentState = new DefaultState();
                        continue;
                    }
                    currentState = currentState.doAction(terminal.writer(), line);
                    completer.flush();
                } catch (EndOfFileException | UserInterruptException e) {
                    e.printStackTrace(terminal.writer());
                    if (this.currentState instanceof Closeable closeable) {
                        closeable.close();
                    }
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace(System.err);
        }
    }

    private String managePrompt(String prompt, State currentState) {
        if (currentState instanceof Promptable promptable) {
            return prompt + " " + promptable.prompt() + " " + DEFAULT_PROMPT;
        }

        return prompt + " " + DEFAULT_PROMPT;
    }
}
