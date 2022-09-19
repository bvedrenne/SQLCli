package fr.bvedrenne.state.dbcreation;

import fr.bvedrenne.database.DatabaseInformation;
import fr.bvedrenne.state.State;

import java.io.PrintWriter;

public class EnterUrlState implements State {
    DatabaseInformation databaseInformation;

    /**
     *
     */
    @Override
    public void enter(PrintWriter writer) {
        writer.println("Enter a valid URL:");
    }

    /**
     * @param line
     * @return
     */
    @Override
    public State doAction(PrintWriter writer, String line) {
        databaseInformation.setUrl(line);
        return new LoginState(databaseInformation);
    }
}
