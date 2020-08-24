package commands;

import java.io.Serializable;

public class AuthCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String login;
    private final String password;

    private String username;
    private String userID;

    private boolean isAuthorized;

    private String message;

    public AuthCommand(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String username) {
        this.userID = username;
    }

    public boolean isAuthorized() {
        return isAuthorized;
    }

    public void setAuthorized(boolean isAuthorized) {
        this.isAuthorized = isAuthorized;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
}
