package fr.bvedrenne.state.dbcreation;

import fr.bvedrenne.database.DatabaseInformation;
import fr.bvedrenne.state.State;

import java.io.PrintWriter;

public class DriverState implements State {
    private DatabaseInformation databaseInformation;

    public DriverState(DatabaseInformation databaseInformation) {
        this.databaseInformation = databaseInformation;
    }

    /**
     *
     */
    @Override
    public void enter(PrintWriter writer) {
        writer.println("Enter driver class name:");
    }

    /**
     * MySQL 	com.mysql.jdbc.Driver 	jdbc:mysql://hostname/ databaseName
     * ORACLE 	oracle.jdbc.driver.OracleDriver 	jdbc:oracle:thin:@hostname:port Number:databaseName
     * DB2 	COM.ibm.db2.jdbc.net.DB2Driver 	jdbc:db2:hostname:port Number/databaseName
     * Sybase 	com.sybase.jdbc.SybDriver 	jdbc:sybase:Tds:hostname: port Number/databaseName
     *
     * Source : https://www.tutorialspoint.com/jdbc/jdbc-db-connections.htm
     *
     * @param line
     * @return
     */
    @Override
    public State doAction(PrintWriter writer, String line) {
        try {
            Class.forName(line);
            databaseInformation.setDriverClassName(line);
            return new ConfirmDatabaseCreationState(databaseInformation);
        }
        catch(ClassNotFoundException ex) {
            writer.println("Error: unable to load driver class or ensure it is in the classloader");
            return this;
        }
    }
}
