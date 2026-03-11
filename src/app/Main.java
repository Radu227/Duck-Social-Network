package app;

import domain.*;
import domain.Duck.TipRata;

import repository.*;
import service.UserService;

import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Scanner;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        Properties props = new Properties();
        try {
            props.load(new FileReader("config.properties"));
        } catch (IOException e) {
            System.err.println("EROARE: Nu gasesc fisierul config.properties!");
            System.err.println("Asigura-te ca l-ai creat si contine: db.url, db.user, db.password");
            return;
        }

        String dbUrl = props.getProperty("db.url");
        String dbUser = props.getProperty("db.user");
        String dbPassword = props.getProperty("db.password");
        Repository<Long, Persoana> persoanaRepo = new PersoanaDbRepository(dbUrl, dbUser, dbPassword);

        Repository<Long, Duck> duckRepo = new DuckDbRepository(dbUrl, dbUser, dbPassword);

        Repository<Long, Card> cardRepo = new CardDbRepository(dbUrl, dbUser, dbPassword,duckRepo);

        Repository<Long, Event> eventRepo = new EventDbRepository(dbUrl, dbUser, dbPassword, persoanaRepo, duckRepo);

        UserFriendshipDbRepository friendshipRepo = new UserFriendshipDbRepository(dbUrl, dbUser, dbPassword, persoanaRepo, duckRepo);

        Repository<Long, Message> messageRepo = new MessageDbRepository(dbUrl, dbUser, dbPassword, persoanaRepo, duckRepo);

        Repository<Long, FriendRequest> requestRepo = new FriendRequestDbRepository(dbUrl, dbUser, dbPassword, persoanaRepo, duckRepo);

        UserService userService = new UserService(persoanaRepo, duckRepo, cardRepo, eventRepo, friendshipRepo, messageRepo, requestRepo);

        Scanner scanner = new Scanner(System.in);

        System.out.println("Bine ati venit la DuckSocialNetwork!");
        boolean running = true;
        while(running){
            System.out.println("1.Adauga utilizator ");
            System.out.println("2.Sterge utilizator ");
            System.out.println("3.Listeaza utilizatori ");
            System.out.println("4.Adauga prietenie ");
            System.out.println("5.Sterge prietenie ");
            System.out.println("6.Listeaza prietenii ");
            System.out.println("7.Afiseaza numarul de comunitati ");
            System.out.println("8.Afiseaza cea mai sociabila comunitate ");
            System.out.println("9.Creeaza card ");
            System.out.println("10.Adauga rata in card ");
            System.out.println("11.Afiseaza performanta medie a unui card ");
            System.out.println("12.Creeaza eveniment ");
            System.out.println("13.Ruleaza eveniment ");
            System.out.println("14.Abonare la eveniment ");
            System.out.println("15.Dezabonare de la eveniment ");
            System.out.println("16.Adauga rate si persoane predefinite ");
            System.out.println("17.Iesire aplicatie ");
            System.out.println("Alege optiunea: ");
            int opt =  scanner.nextInt();
            scanner.nextLine();
            switch (opt) {
                case 1:
                    System.out.println("Tip utilizator (1->Persoana, 2->Rata): ");
                    int tip =  scanner.nextInt();
                    scanner.nextLine();
                    System.out.println("ID: ");
                    Long  id = scanner.nextLong();
                    scanner.nextLine();
                    System.out.println("Username: ");
                    String username = scanner.nextLine();
                    System.out.println("Email: ");
                    String email = scanner.nextLine();
                    System.out.println("Parola: ");
                    String parola = scanner.nextLine();

                    if(tip == 1){
                        System.out.println("Nume: ");
                        String nume =  scanner.nextLine();
                        System.out.println("Prenume: ");
                        String prenume =  scanner.nextLine();
                        System.out.println("Data Nasterii: ");
                        String dataNasteriiStr = scanner.nextLine();
                        LocalDate dataNasterii = LocalDate.parse(dataNasteriiStr, DateTimeFormatter.ISO_LOCAL_DATE);
                        System.out.println("Ocupatie: ");
                        String ocupatie =  scanner.nextLine();
                        System.out.println("Nivel empatie: ");
                        double nivelEmpatie =  scanner.nextDouble();
                        scanner.nextLine();

                        Persoana p = new Persoana(id,username,email,parola,nume,prenume,dataNasterii,ocupatie,nivelEmpatie);
                        userService.addUser(p);
                        System.out.println("Persoana adaugata!");
                    }
                    else if(tip == 2){
                        System.out.print("Tip rață (1=FLYING, 2=SWIMMING, 3=FLYING_AND_SWIMMING): ");
                        int tipRata =  scanner.nextInt();
                        scanner.nextLine();
                        TipRata tiprata;
                        switch(tipRata){
                            case 1:
                                tiprata = TipRata.Flying;
                                break;
                            case 2:
                                tiprata = TipRata.Swimming;
                                break;
                            case 3:
                                tiprata = TipRata.Flying_and_Swimming;
                                break;
                            default: {
                                System.out.println("Tip invalid, o sa pun SWIMMING!");
                                tiprata = TipRata.Swimming;
                            }
                        }
                        System.out.println("Viteza: ");
                        double viteza =  scanner.nextDouble();
                        scanner.nextLine();
                        System.out.println("Rezistenta: ");
                        double rezistenta =  scanner.nextDouble();
                        scanner.nextLine();
                        Duck d = null;
                        if(tipRata == 1)
                            d = new FlyingDuck(id,username,email,parola,viteza,rezistenta);
                        else if(tipRata == 2)
                            d = new SwimmingDuck(id,username,email,parola,viteza,rezistenta);
                        else if(tipRata == 3)
                            d = new FlyingAndSwimmingDuck(id,username,email,parola,viteza,rezistenta);
                        userService.addUser(d);
                        System.out.println("Rata adaugata!");
                    }
                    break;
                case 2:
                    System.out.println("Ce fel de utilizator vrei sa stergi?");
                    System.out.println("1 - Persoana");
                    System.out.println("2 - Rata");
                    int tipStergere = scanner.nextInt();
                    scanner.nextLine();

                    System.out.println("Introduceti id-ul utilizatorului pe care vreti sa il stergeti:");
                    Long idSters =  scanner.nextLong();
                    scanner.nextLine();

                    boolean sters = false;
                    if (tipStergere == 1) {
                        sters = userService.removePersoanaById(idSters);
                    } else if (tipStergere == 2) {
                        sters = userService.removeDuckById(idSters);
                    } else {
                        System.out.println("Optiune invalida.");
                        break;
                    }

                    if(sters){
                        System.out.println("Utilizator sters cu succes!");
                    }
                    else{
                        System.out.println("Utilizatorul cu acest id si tip nu a fost gasit!");
                    }
                    break;
                case 3:
                    System.out.println("Lista Persoane=:");
                    for(Persoana p : persoanaRepo.findAll()){
                        System.out.println(p);
                    }
                    System.out.println("Lista Rațe=:");
                    for(Duck d : duckRepo.findAll()){
                        System.out.println(d);
                    }
                    break;
                case 4:
                    try {
                        System.out.println("\nAdaugare prietenie:");

                        System.out.print("ID user 1: ");
                        Long id1 = scanner.nextLong();
                        System.out.print("Tip user 1 (1-Persoana, 2-Rata): ");
                        int tip1 = scanner.nextInt();

                        System.out.print("ID user 2: ");
                        Long id2 = scanner.nextLong();
                        System.out.print("Tip user 2 (1-Persoana, 2-Rata): ");
                        int tip2 = scanner.nextInt();
                        scanner.nextLine();

                        User user1 = userService.findUserByIdAndType(id1, tip1);
                        User user2 = userService.findUserByIdAndType(id2, tip2);

                        if (user1 != null && user2 != null) {
                            userService.addFriendship(user1, user2);
                            System.out.println("Prietenie adăugată cu succes.");
                        } else {
                            System.err.println("Eroare: Unul sau ambii utilizatori nu au fost găsiți sau tipul specificat este invalid.");
                        }
                    } catch (InputMismatchException e) {
                        System.err.println("Eroare la citirea datelor. Asigurați-vă că introduceți numere pentru ID și tip.");
                        scanner.nextLine();
                    }
                    break;

                case 5:
                    try {
                        System.out.print("ID user 1: ");
                        Long idA = scanner.nextLong();
                        System.out.print("Tip user 1 (1-Persoana, 2-Rata): ");
                        int tipA = scanner.nextInt();

                        System.out.print("ID user 2: ");
                        Long idB = scanner.nextLong();
                        System.out.print("Tip user 2 (1-Persoana, 2-Rata): ");
                        int tipB = scanner.nextInt();
                        scanner.nextLine();

                        User userA = userService.findUserByIdAndType(idA, tipA);
                        User userB = userService.findUserByIdAndType(idB, tipB);

                        if (userA != null && userB != null) {
                            userService.removeFriendship(userA, userB);
                            System.out.println("Prietenie ștearsă (dacă a existat).");
                        } else {
                            System.err.println("Eroare: Unul sau ambii utilizatori nu au fost găsiți sau tipul specificat este invalid.");
                        }
                    } catch (InputMismatchException e) {
                        System.err.println("Eroare la citirea datelor. Asigurați-vă că introduceți numere pentru ID și tip.");
                        scanner.nextLine();
                    }
                    break;
                case 6:
                    System.out.println("Prietenii tuturor utilizatorilor:");
                    List<User> totiUtilizatorii = new ArrayList<>();
                    persoanaRepo.findAll().forEach(totiUtilizatorii::add);
                    duckRepo.findAll().forEach(totiUtilizatorii::add);

                    for (User u : totiUtilizatorii) {
                        String tipUser = (u instanceof Persoana) ? "PERSOANA" : "RATA";

                        List<User> prieteni = friendshipRepo.getFriendsOf(u.getId(), tipUser);

                        System.out.print(u.getUsername() + " (" + tipUser + ") are prietenii: ");
                        if (prieteni.isEmpty()) {
                            System.out.println("niciunul");
                        } else {
                            for (User f : prieteni) {
                                System.out.print(f.getUsername() + ", ");
                            }
                            System.out.println();
                        }
                    }
                    break;
                case 7:
                    userService.incarcaPrieteni();
                    int comunitati = userService.getNumarComunitati();
                    System.out.println("Numarul de comunitati este: "+comunitati);
                    break;
                case 8:
                    userService.incarcaPrieteni();
                    List<User> ceaMaiSociabila = userService.getCeaMaiSociabilaComunitate();
                    System.out.println("Cea mai sociabila comunitate are "+ ceaMaiSociabila.size()+" membri:");
                    for(User u : ceaMaiSociabila){
                        System.out.println(" - "+ u.getUsername());
                    }
                    break;
                case 9:
                    System.out.println("Nume card: ");
                    String numeCard =  scanner.nextLine();

                    Card<Duck> card = new Card<>(null,numeCard);
                    userService.addCard(card);
                    System.out.println("Card creeat cu succes!");
                    break;
                case 10:
                    System.out.println("Nume card: ");
                    String nume =  scanner.nextLine();
                    System.out.println("Username rata de adaugat: ");
                    String Duckname = scanner.nextLine();

                    Duck rata = userService.getDuckByUsername(Duckname);
                    if(rata == null){
                        System.out.println("Rata nu exista!");
                        break;
                    }
                    Card<Duck> cardGasit = null;
                    for (Card<? extends Duck> c : userService.getCarduri()) {
                        if (c.getNumeCard().equals(nume)) {
                            cardGasit = (Card<Duck>) c;
                            break;
                        }
                    }

                    if (cardGasit == null) {
                        System.out.println("Cardul nu a fost gasit!");
                        break;
                    }

                    cardGasit.adaugaRata(rata);
                    userService.addCard(cardGasit);
                    System.out.println("Raaa adaugata in card!");
                    break;
                case 11:
                    System.out.println("Introduceti numele cardului pentru performanta medie: ");
                    String numeCardPerformanta = scanner.nextLine();
                    userService.afiseazaPerformantaMedie(numeCardPerformanta);
                    break;
                case 12:
                    System.out.println("Nume eveniment: ");
                    String raceName  = scanner.nextLine();
                    System.out.println("Numar balize: ");
                    int nrBalize = scanner.nextInt();
                    scanner.nextLine();

                    List<Double> balize = new ArrayList<>();
                    for(int i = 0; i < nrBalize; i++){
                        System.out.println("Distanta balizei " + (i + 1) + ": ");
                        balize.add(scanner.nextDouble());
                        scanner.nextLine();
                    }

                    RaceEvent raceEvent = new RaceEvent(raceName, balize);
                    userService.addEvent(raceEvent);
                    System.out.println("Evenimentul a fost creat cu succes!");
                    break;

                case 13:
                    List<Event> evenimente = userService.getEvenimente();
                    if (evenimente.isEmpty()) {
                        System.out.println("Nu exista evenimente!");
                        break;
                    }

                    System.out.println("Alege indexul evenimentului de rulat: ");
                    for (int i = 0; i < evenimente.size(); i++) {
                        System.out.println(i + ": " + evenimente.get(i).getNume());
                    }

                    int idx = scanner.nextInt();
                    scanner.nextLine();

                    if (idx < 0 || idx >= evenimente.size()) {
                        System.out.println("Index invalid!");
                        break;
                    }

                    Event e = evenimente.get(idx);
                    if (e instanceof RaceEvent re) {
                        List<Duck> toateRateleInotatoare = new ArrayList<>();
                        for (Duck d : duckRepo.findAll()) {
                            if (d instanceof Inotator) {
                                toateRateleInotatoare.add(d);
                            }
                        }
                        re.simuleazaCursa(toateRateleInotatoare);

                    } else {
                        System.out.println("Acest eveniment nu este o cursa de natație!");
                    }
                    break;
                case 14:
                    System.out.println("Username-ul userului care vrea sa se aboneze: ");
                    String usernamee =  scanner.nextLine();
                    System.out.println("Id-ul evenimentului la care vrea sa se aboneze: ");
                    Long idd = scanner.nextLong();
                    scanner.nextLine();
                    Event eventGasit = eventRepo.findOne(idd);

                    if (eventGasit == null) {
                        System.out.println("Evenimentul cu ID-ul " + idd + " nu a fost gasit!");
                        break;
                    }

                    boolean succes = userService.subscribeUserToEvent(usernamee, eventGasit);

                    if(succes){
                        System.out.println(usernamee + " s-a abonat la eveniment!");
                    }
                    else System.out.println("Nu a functionat! (Poate userul nu exista?)");
                    break;
                case 15:
                    System.out.println("Username-ul userului care vrea sa se dezaboneze: ");
                    String usernamee1 =  scanner.nextLine();
                    System.out.println("Id-ul evenimentului la care vrea sa se dezaboneze: ");
                    int idx1 = scanner.nextInt();
                    scanner.nextLine();
                    List<Event> evenimente1 = userService.getEvenimente();
                    boolean succes1 = userService.unsubscribeUserFromEvent(usernamee1,evenimente1.get(idx1-1));
                    if(succes1){
                        System.out.println(usernamee1 + " s-a dezabonat la eveniment!");
                    }
                    else System.out.println("Nu a functionat!");
                    break;
                case 17:
                    running = false;
                    System.out.println("La revedere!");
                    break;
                case 16:
                    SwimmingDuck s1 = new SwimmingDuck(1L, "rata1", "rata1@email.com", "parola1", 5, 3);
                    SwimmingDuck s2 = new SwimmingDuck(2L, "rata2", "rata2@email.com", "parola2", 6, 4);
                    FlyingAndSwimmingDuck f1 = new FlyingAndSwimmingDuck(3L, "rata3", "rata3@email.com", "parola3", 8, 2);
                    FlyingAndSwimmingDuck fs1 = new FlyingAndSwimmingDuck(4L, "rata4", "rata4@email.com", "parola4", 7, 5);
                    userService.addUser(s1);
                    userService.addUser(s2);
                    userService.addUser(f1);
                    userService.addUser(fs1);

                    Persoana p1 = new Persoana(5L, "ana", "ana@email.com", "ana123", "Popescu", "Ana", LocalDate.of(1995, 5, 20), "Inginer", 8.5);
                    Persoana p2 = new Persoana(6L, "ion", "ion@email.com", "ion123", "Ionescu", "Ion", LocalDate.of(1990, 3, 15), "Doctor", 7.2);
                    Persoana p3 = new Persoana(7L, "maria", "maria@email.com", "maria123", "Georgescu", "Maria", LocalDate.of(1998, 7, 10), "Profesor", 9.0);

                    userService.addUser(p1);
                    userService.addUser(p2);
                    userService.addUser(p3);


            }
        }

        scanner.close();
    }
}
