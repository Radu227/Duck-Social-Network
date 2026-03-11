package domain;

public class SwimmingDuck extends Duck implements Inotator {
    public SwimmingDuck(Long id, String username, String email, String password, double viteza, double rezistenta){
        super(id,username,email,password,TipRata.Swimming,viteza,rezistenta);
    }

    @Override
    public void inoata()
    {
        System.out.println(getUsername()+" inoata in lac");
    }
}
