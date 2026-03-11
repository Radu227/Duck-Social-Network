package domain;

import java.util.ArrayList;
import java.util.List;

public class Card<T extends Duck> {
    private Long id;
    private String numeCard;
    private List<T> membri;

    public Card(Long id, String numeCard) {
        this.id = id;
        this.numeCard = numeCard;
        membri = new ArrayList<>();
    }

    public void adaugaRata(T rata){
        membri.add(rata);
    }

    public String  getNumeCard() {
        return numeCard;
    }

    public Long getId(){
        return id;
    }

    public List<T> getMembri() {
        return membri;
    }

    public double getPerformantaMedie(){
        if(membri.size()==0){
            return 0;
        }
        double total = 0;
        for(T rata : membri){
            double performanta = (rata.getViteza()+rata.getRezistenta())/2;
            total += performanta;
        }
        return total/membri.size();
    }

    public void setId(Long id) {
        this.id = id;
    }
    @Override
    public String toString() {
        return "Card{" +
                "id=" + id +
                ", numeCard='" + numeCard + '\'' +
                ", nrMembri=" + membri.size() +
                ", performantaMedie=" + getPerformantaMedie() +
                '}';
    }

}
