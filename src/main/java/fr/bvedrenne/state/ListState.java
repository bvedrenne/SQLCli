package fr.bvedrenne.state;

import fr.bvedrenne.console.completer.Completable;
import fr.bvedrenne.database.DatabaseInformation;
import fr.bvedrenne.io.Configuration;
import fr.bvedrenne.state.dbcreation.EnterUrlState;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

public class ListState implements State, Completable {
    private final List<DatabaseInformation> databaseInformations;

    public ListState() {
        Configuration configuration = Configuration.getInstance();
        databaseInformations = configuration.getDatabaseInformations();
    }

    /**
     *
     */
    @Override
    public void enter(PrintWriter writer) {
        writer.println("0 : Create database");
        for (int i = 0; i < 10 && i < databaseInformations.size(); i++) {
            writer.println(i + 1 + " : " + databaseInformations.get(i).getUrl());
        }
        writer.println("11 : < Back");
    }

    /**
     *
     */
    @Override
    public State doAction(PrintWriter writer, String line) { // TODO Manage multiple page
        try {
            int inputNumber = Integer.parseInt(line);
            if (inputNumber >= 0 && inputNumber <= 11) {
                if (inputNumber == 0) {
                    return new EnterUrlState();
                }
                if (inputNumber == 11) {
                    return new DefaultState();
                }
                if (inputNumber <= databaseInformations.size()) {
                    DatabaseInformation databaseInformation = databaseInformations.get(inputNumber - 1);
                    writer.println("Connecting to " + databaseInformation.getUrl());
                    try {
                        java.sql.Driver driver = (java.sql.Driver) Class.forName(databaseInformation.getDriverClassName())
                                .getConstructor().newInstance();
                        DriverManager.registerDriver(driver);

                        Connection connection = DriverManager.getConnection(databaseInformation.getUrl(),
                                databaseInformation.getUsername(), databaseInformation.getPassword());

                        writer.println("Connected");
                        return new DatabaseRequestState(connection);
                    } catch (SQLException | ClassNotFoundException | InvocationTargetException |
                             InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                        writer.println("Cannot connect to url");
                        e.printStackTrace();
                        return this;
                    }
                }
            }
        } catch (NumberFormatException e) {
        }
        writer.println("Please type a valid option number"); // ERROR
        return this;
    }

    @Override
    public Completer getCompleter() {
        return new Completer() {
            @Override
            public void complete(LineReader lineReader, ParsedLine parsedLine, List<Candidate> list) {
                list.add(new Candidate("0"));
                for (int i = 0; i < 10 && i < databaseInformations.size(); i++) {
                    list.add(new Candidate(Integer.toString(i + 1)));
                }

                list.add(new Candidate("11"));
            }
        };
    }
}
