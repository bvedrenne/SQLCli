package fr.bvedrenne.state.dbcreation;

import fr.bvedrenne.database.DatabaseInformation;
import fr.bvedrenne.state.State;

import java.io.PrintWriter;

public class LoginState implements State {
    private DatabaseInformation databaseInformation;

    public LoginState(DatabaseInformation databaseInformation) {
        this.databaseInformation = databaseInformation;
    }

    /**
     *
     */
    @Override
    public void enter(PrintWriter writer) {
        writer.println("Enter a valid user:");
    }

    /**
     * @param line
     * @return
     */
    @Override
    public State doAction(PrintWriter writer, String line) {
        databaseInformation.setUsername(line);
        return new PasswordState(databaseInformation);
    }
}
