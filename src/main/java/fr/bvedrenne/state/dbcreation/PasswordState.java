package fr.bvedrenne.state.dbcreation;

import fr.bvedrenne.database.DatabaseInformation;
import fr.bvedrenne.state.State;

import java.io.PrintWriter;

public class PasswordState implements State {
    private DatabaseInformation databaseInformation;

    public PasswordState(DatabaseInformation databaseInformation) {
        this.databaseInformation = databaseInformation;
    }

    /**
     *
     */
    @Override
    public void enter(PrintWriter writer) {
        writer.println("Enter a valid password:");
    }

    /**
     * @param line
     * @return
     */
    @Override
    public State doAction(PrintWriter writer, String line) {
        databaseInformation.setPassword(line);
        return new DriverState(databaseInformation);
    }
}
