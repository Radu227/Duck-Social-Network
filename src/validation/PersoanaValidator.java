package validation;
import domain.*;

import java.time.LocalDate;

public class PersoanaValidator implements UserValidator {
    @Override
    public void validate(User user) throws ValidationException {
        if(!(user instanceof Persoana)) return;
        Persoana p = (Persoana) user;
        if(p.getNume() == null || p.getNume().isEmpty())
            throw new ValidationException("Numele nu poate sa fie gol!");
        if(p.getPrenume() == null || p.getPrenume().isEmpty())
            throw new ValidationException("Prenumele nu poate sa fie gol!");
        if(p.getDataNasterii().isAfter(LocalDate.now()))
            throw new ValidationException("Data naste poate sa fie in viitor!");
        if(p.getOcupatie() == null || p.getOcupatie().isEmpty())
            throw new ValidationException("Campul de ocupatie nu poate sa fie gol!");
        if(p.getNivelEmpatie() < 0 || p.getNivelEmpatie() > 10)
            throw new ValidationException("Nivel trebuie sa fie intre 0 si 10!");

    }
}
