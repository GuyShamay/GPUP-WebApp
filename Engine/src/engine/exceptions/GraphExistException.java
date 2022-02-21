package engine.exceptions;

public class GraphExistException extends RuntimeException {
    private final String message;
    private final String title;

    public GraphExistException(String name) {
        message = "There is already a graph named: " + name + ".";
        title = "Adding Graph Failure";
    }

    @Override
    public String getMessage() {
        return message;
    }

    public String getTitle() {
        return title;
    }
}
