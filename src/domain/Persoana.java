package domain;

import java.time.LocalDate;

public class Persoana extends User {
    private String nume;
    private String prenume;
    private LocalDate dataNasterii;
    private String ocupatie;
    private double nivelEmpatie;

    public Persoana(Long id, String username, String email, String password, String nume, String prenume, LocalDate dataNasterii, String ocupatie, double nivelEmpatie) {
        super(id, username, email, password);
        this.nume = nume;
        this.prenume = prenume;
        this.dataNasterii = dataNasterii;
        this.ocupatie = ocupatie;
        this.nivelEmpatie = nivelEmpatie;
    }
    public String getNume() {
        return nume;
    }
    public void setNume(String nume) {
        this.nume = nume;
    }
    public String getPrenume() {
        return prenume;
    }
    public void setPrenume(String prenume) {
        this.prenume = prenume;
    }
    public LocalDate getDataNasterii() {
        return dataNasterii;
    }
    public void setDataNasterii(LocalDate dataNasterii) {
        this.dataNasterii = dataNasterii;
    }
    public String getOcupatie() {
        return ocupatie;
    }
    public void setOcupatie(String ocupatie) {
        this.ocupatie = ocupatie;
    }
    public double getNivelEmpatie() {
        return nivelEmpatie;
    }
    public void setNivelEmpatie(double nivelEmpatie) {
        this.nivelEmpatie = nivelEmpatie;
    }

    public void createEvent(String event){
        System.out.println("Event: " + event);
    }

    @Override
    public void sendMessage(User receiver, String message){
        System.out.println("Mesaj trimis "+username+"->"+receiver.getUsername()+": "+message);
    }

    @Override
    public void receiveMessage(String user, String message){
        System.out.println("Mesaj primit pentru "+username+" de la "+user+": "+message);
    }

    @Override
    public String toString() {
        return "Person{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", nume='" + nume + '\'' +
                ", prenume='" + prenume + '\'' +
                ", ocupatie='" + ocupatie + '\'' +
                ", empatie=" + nivelEmpatie +
                '}';
    }
}
