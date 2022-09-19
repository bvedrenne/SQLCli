package fr.bvedrenne.state;

import fr.bvedrenne.console.Promptable;
import fr.bvedrenne.console.completer.Completable;
import fr.bvedrenne.database.DatabaseInformation;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.parser.ParseException;
import net.sf.jsqlparser.statement.Statements;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;
import net.sf.jsqlparser.util.validation.Validation;
import net.sf.jsqlparser.util.validation.ValidationError;
import net.sf.jsqlparser.util.validation.ValidationException;
import net.sf.jsqlparser.util.validation.feature.FeaturesAllowed;
import net.sf.jsqlparser.util.validation.metadata.JdbcDatabaseMetaDataCapability;
import net.sf.jsqlparser.util.validation.metadata.NamesLookup;
import org.apache.commons.lang3.StringUtils;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.LineReader;
import org.jline.reader.ParsedLine;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class DatabaseRequestState implements State, Closeable, Promptable, Completable {
    private Connection connection;

    public DatabaseRequestState(Connection connection) {
        this.connection = connection;
    }

    /**
     *
     */
    @Override
    public void enter(PrintWriter writer) {
    }

    /**
     * @param line
     * @return
     */
    @Override
    public State doAction(PrintWriter writer, String line) {
        if (StringUtils.isNoneBlank(line)) {
            try {
                net.sf.jsqlparser.statement.Statement stmt = CCJSqlParserUtil.parse(line);
                if (stmt instanceof Select select) {
                    TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
                    List<String> tableList = tablesNamesFinder.getTableList(select);
                    if (tableList.isEmpty()) {
                        writer.println();
                        writer.println("No table selected");
                        return this;
                    }
                }

                executeSQLQuery(writer, stmt.toString());
            } catch (JSQLParserException e) {
                e.printStackTrace();
                return this;
            }
        }
        return this;
    }

    private void executeSQLQuery(PrintWriter writer, String sqlStatement) {
        try (PreparedStatement statement = connection.prepareStatement(sqlStatement)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                ResultSetMetaData rsmd = resultSet.getMetaData();

                int columnsNumber = rsmd.getColumnCount();
                writer.print("| ");
                for (int i = 1; i <= columnsNumber; i++) {
                    writer.print(String.format("%20s", rsmd.getColumnLabel(i)) + " |");
                }
                writer.println();
                writer.println(StringUtils.repeat('*', columnsNumber * 22 + 2));

                while (resultSet.next()) {
                    writer.print("| ");
                    for (int i = 1; i <= columnsNumber; i++) {
                        writer.print(String.format("%20s", resultSet.getString(i)) + " |");
                    }
                    writer.println();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Closes this stream and releases any system resources associated
     * with it. If the stream is already closed then invoking this
     * method has no effect.
     *
     * <p> As noted in {@link AutoCloseable#close()}, cases where the
     * close may fail require careful attention. It is strongly advised
     * to relinquish the underlying resources and to internally
     * <em>mark</em> the {@code Closeable} as closed, prior to throwing
     * the {@code IOException}.
     *
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return
     */
    @Override
    public String prompt() {
        try {
            return "(" + this.connection.getCatalog() + ")";
        } catch (SQLException e) {
            try {
                return "(" + this.connection.getSchema() + ")";
            } catch (SQLException ex) {
                return "";
            }
        }
    }

    /**
     * @return
     */
    @Override
    public Completer getCompleter() {
        return new Completer() {
            @Override
            public void complete(LineReader lineReader, ParsedLine parsedLine, List<Candidate> list) {
                String line = parsedLine.line();
                Validation validation = new Validation(Arrays.asList(
                        new JdbcDatabaseMetaDataCapability(connection, NamesLookup.UPPERCASE)), line);
                List<ValidationError> errors = validation.validate();
                if (errors.isEmpty()) {
                    try {
                        net.sf.jsqlparser.statement.Statement stmt = CCJSqlParserUtil.parse(line);
                        if (stmt instanceof Select select) {
                            TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
                            List<String> tableList = tablesNamesFinder.getTableList(select);
                            if (tableList.isEmpty()) {
                                list.add(new Candidate("FROM"));
                            } else if (tableList.size() == 1) {
                                extractAllTable(list, p -> p.startsWith(tableList.get(0)));
                            } else {
                                extractAllTable(list);
                            }
                        }
                    } catch (JSQLParserException e) {
                        e.printStackTrace();
                    }

                } else {
                    ValidationError validationError = errors.iterator().next();
                    final Set<ValidationException> validationExceptionSet = validationError.getErrors();
                    final ValidationException validationException = validationExceptionSet.iterator().next();
                    if (findExceptionInStackTrace(validationException, ArrayIndexOutOfBoundsException.class) != null) {
                        list.add(new Candidate("SELECT"));
                        list.add(new Candidate("INSERT"));
                        list.add(new Candidate("UPDATE"));
                    } else if (validationException.getMessage().split("\n")[0].endsWith("\"FROM\"")) {
                        extractAllTable(list);
                    } else if (validationException.getMessage().endsWith(" does not exist.")) {
                        String firstWord = validationException.getMessage().split(" ")[0];
                        extractAllTable(list, p -> p.startsWith(firstWord));
                    } else if (findExceptionInStackTrace(validationException, ParseException.class) != null) {
                        final ParseException parserException = findExceptionInStackTrace(validationException,
                                ParseException.class);
                        Arrays.stream(parserException.tokenImage).map(s -> s.replaceAll("\"", ""))
                                .map(Candidate::new).forEach(list::add);
                    } else {
                        System.out.println(validationError);
                    }
                }
            }
        };
    }

    private <T extends Throwable> T findExceptionInStackTrace(Exception e, Class<T> classToFind) {
        Throwable exception = e.getCause();
        while (exception != null) {
            if (classToFind.isAssignableFrom(exception.getClass())) {
                return (T) exception;
            }
            exception = exception.getCause();
        }

        return null;
    }

    private void extractAllTable(List<Candidate> list, Predicate<String> predicate) {
        try (PreparedStatement statement = connection.prepareStatement("SHOW TABLES")) {
            try (ResultSet resultSet = statement.executeQuery()) {
                ResultSetMetaData rsmd = resultSet.getMetaData();

                int columnsNumber = rsmd.getColumnCount();
                while (resultSet.next()) {
                    for (int i = 1; i <= columnsNumber; i++) {
                        final String resultSetString = resultSet.getString(1);
                        if (predicate.test(resultSetString)) {
                            list.add(new Candidate(resultSetString));
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void extractAllTable(List<Candidate> list) {
        extractAllTable(list, p -> true);
    }
}
