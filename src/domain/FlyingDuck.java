package domain;

public class FlyingDuck extends Duck implements Zburator{
    public FlyingDuck(Long id, String username, String email, String password, double viteza, double rezistenta){
        super(id,username,email,password,TipRata.Flying,viteza,rezistenta);
    }

    @Override
    public void zboara() {
        System.out.println(getUsername() + " zboara pe cer!");
    }
}
