package old.exception;

public class ElementExistException extends RuntimeException {

    public ElementExistException(String type, String name) {
        super("There is already a " + type + " named: " + name);
    }
}
