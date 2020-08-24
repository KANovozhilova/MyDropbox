package commands;

import java.io.Serializable;

public class SignUpCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String login;
    private final String password;

    private boolean isSignUp;
    private String message;

    public SignUpCommand(String login, String password) {
        this.login = login;
        this.password = password;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    public boolean isSignUp() {
        return isSignUp;
    }

    public void setSignUp(boolean signUp) {
        isSignUp = signUp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
