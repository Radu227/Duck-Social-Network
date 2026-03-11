package validation;
import domain.User;

public interface UserValidator {
    void validate(User user) throws ValidationException;
}
