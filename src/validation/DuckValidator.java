package validation;

import domain.*;

public class DuckValidator implements UserValidator {
    @Override
    public void validate(User user) throws ValidationException {
        if(!(user instanceof Duck)) return;
        Duck d = (Duck)user;

        if(d.getViteza() <= 0)
            throw new ValidationException("Viteza nu poate sa fie vida!");
        if(d.getRezistenta() <= 0)
            throw new ValidationException("Rezistenta nu poate sa fie vida!");
    }
}
