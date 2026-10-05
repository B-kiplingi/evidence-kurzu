package cz.uhk.ppro.akademietrutnov.domain.exception;

public class EntityNotFoundException extends RuntimeException {

    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(String entityName, Long id) {
        super(entityName + " s ID " + id + " nebyl(a) nalezen(a).");
    }
}
