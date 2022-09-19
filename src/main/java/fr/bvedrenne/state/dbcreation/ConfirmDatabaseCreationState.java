package fr.bvedrenne.state.dbcreation;

import fr.bvedrenne.database.DatabaseInformation;
import fr.bvedrenne.io.Configuration;
import fr.bvedrenne.state.ListState;
import fr.bvedrenne.state.State;
import lombok.NonNull;
import org.apache.commons.lang3.BooleanUtils;

import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConfirmDatabaseCreationState implements State {
    private DatabaseInformation databaseInformation;

    private boolean isValid = true;

    public ConfirmDatabaseCreationState(@NonNull DatabaseInformation databaseInformation) {
        this.databaseInformation = databaseInformation;
    }

    /**
     *
     */
    @Override
    public void enter(PrintWriter writer) {
        try {
            java.sql.Driver driver = (java.sql.Driver) Class.forName(this.databaseInformation.getDriverClassName())
                    .getConstructor().newInstance();
            DriverManager.registerDriver(driver);

            Connection connection = DriverManager.getConnection(this.databaseInformation.getUrl(),
                    this.databaseInformation.getUsername(), this.databaseInformation.getPassword());

            writer.println("Do you want to create:");
            writer.println("\tUrl     : " + this.databaseInformation.getUrl());
            writer.println("\tUsername: " + this.databaseInformation.getUsername());
            writer.println("\tDrive   : " + this.databaseInformation.getDriverClassName());
            writer.println("yes to accept / no to go back to database list");
        } catch (SQLException | ClassNotFoundException | InvocationTargetException | InstantiationException |
                 IllegalAccessException | NoSuchMethodException e) {
            writer.println("Invalid information given. You have to redo configuration.");
            isValid = false;
        }
    }

    /**
     * @param line
     * @return
     */
    @Override
    public State doAction(PrintWriter writer, String line) {
        if(!isValid) {
            return new EnterUrlState();
        }
        try {
            if (BooleanUtils.toBoolean(line, "yes", "no")) {
                Configuration configuration = Configuration.getInstance();
                configuration.saveDatabase(databaseInformation);
            }

            return new ListState();
        } catch (IllegalArgumentException e) {
            return this;
        }
    }
}
