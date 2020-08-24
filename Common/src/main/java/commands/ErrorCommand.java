package commands;

import java.io.Serializable;

public class ErrorCommand implements Serializable {

    private static final long serialVersionUID = 1L;

    private final String errorMessage;

    public ErrorCommand(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}
