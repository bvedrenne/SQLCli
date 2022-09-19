package fr.bvedrenne.database;

import lombok.*;

@ToString
@Data
@NoArgsConstructor
public class DatabaseInformation {

    private String url;

    private String username;

    private String password;

    private String driverClassName;
}
