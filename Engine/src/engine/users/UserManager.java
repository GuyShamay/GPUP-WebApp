package engine.users;

import dto.users.UserDTO;

import java.util.*;

public class UserManager {
    private final Map<String, Role> usersList;

    public UserManager() {
        usersList = new HashMap<>();
    }

    public synchronized void addUser(String username, Role role) {
        usersList.put(username, role);
    }

    public synchronized void removeUser(String username) {
        usersList.remove(username);
    }

    public synchronized Map<String, Role> getUsers() {
        return Collections.unmodifiableMap(usersList);
    }

    public synchronized boolean isWorker(String name) {
        if (usersList.containsKey(name)) {
            return usersList.get(name) == Role.Worker;
        }
        return false;
    }

    public synchronized Role getRole(String name){
        if (usersList.containsKey(name)) {
            return usersList.get(name);
        }
        return null;
    }

    public synchronized List<UserDTO> getUsersDTO() {
        List<UserDTO> list = new ArrayList<>();
        usersList.forEach((s, role) -> {
            list.add(new UserDTO(s, role.toString()));
        });
        return list;
    }

    public boolean isUserExists(String username) {
        return usersList.containsKey(username);
    }
}
