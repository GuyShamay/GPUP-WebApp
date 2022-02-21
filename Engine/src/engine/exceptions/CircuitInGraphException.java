package engine.exceptions;

public class CircuitInGraphException extends RuntimeException{
    private final String message;
    private final String title;

    public CircuitInGraphException() {
        message = "Invalid Graph! The graph contains a circuit";
        title = "Circuit in graph";
    }

    @Override
    public String getMessage() {
        return message;
    }
    public String getTitle() {
        return title;
    }
}
