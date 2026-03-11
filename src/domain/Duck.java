package domain;

import javax.smartcardio.Card;

public abstract class Duck extends User {
    public enum TipRata{
        Flying,
        Swimming,
        Flying_and_Swimming
    }
    private TipRata tip;
    private double viteza;
    private double rezistenta;
    private Card card;

    public Duck(Long id, String username, String email, String password,
                TipRata tip, double viteza, double rezistenta) {
        super(id, username, email, password);
        this.tip = tip;
        this.viteza = viteza;
        this.rezistenta = rezistenta;
    }

    public TipRata getTip() {
        return tip;
    }
    public void setTip(TipRata tip) {
        this.tip = tip;
    }
    public double getViteza() {
        return viteza;
    }
    public void setViteza(double viteza) {
        this.viteza = viteza;
    }
    public double getRezistenta() {
        return rezistenta;
    }
    public void setRezistenta(double rezistenta) {
        this.rezistenta = rezistenta;
    }
    public Card getCard() {
        return card;
    }
    public void setCard(Card card) {
        this.card = card;
    }

    public void participateEvent(String eventName){
        System.out.println(username+" participa la evenimentul "+eventName);
    }

    public void quack(){
        System.out.println(username+" Quack! Am terminat antrenamentul!");
    }

    @Override
    public String toString() {
        return "Duck{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", tip=" + tip +
                ", viteza=" + viteza +
                ", rezistenta=" + rezistenta +
                '}';
    }
}
