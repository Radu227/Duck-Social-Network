package domain;

public class FlyingAndSwimmingDuck extends Duck implements Zburator, Inotator {
    public FlyingAndSwimmingDuck(Long id, String username, String email, String password, double viteza, double rezistenta){
        super(id,username,email,password,TipRata.Flying_and_Swimming,viteza,rezistenta);
    }

    @Override
    public void inoata()
    {
        System.out.println(getUsername()+" inoata in lac");
    }

    @Override
    public void zboara()
    {
        System.out.println(getUsername()+" zboara pe cer!");
    }
}
