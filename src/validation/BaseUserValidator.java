package validation;

import javax.xml.validation.Validator;
import domain.*;

public class BaseUserValidator implements UserValidator {
    @Override
    public void validate(User user) throws ValidationException{
        if(user.getUsername()==null || user.getUsername().isEmpty())
            throw new ValidationException("Username-ul nu poate sa fie gol!");
        if(user.getEmail()==null || user.getEmail().isEmpty() || !user.getEmail().contains("@"))
            throw new ValidationException("Email invalid!");
        if(user.getPassword()==null || user.getPassword().isEmpty() || user.getPassword().length() < 4)
            throw new ValidationException("Parola trebuie sa aiba minim 4 caractere!");
    }
}
