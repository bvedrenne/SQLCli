package fr.bvedrenne.state.command;

import fr.bvedrenne.state.State;

import java.util.List;
import java.util.stream.Collectors;

public record Command(List<String> commandName, State state) {
    public List<String> commandMatching(String prefix) {
        return commandName().stream().filter(s -> s.startsWith(prefix)).collect(Collectors.toList());
    }

    public boolean matches(String name) {
        return commandName().stream().anyMatch(s -> s.equalsIgnoreCase(name));
    }
}
