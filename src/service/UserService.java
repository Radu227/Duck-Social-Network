package service;

import domain.*;
import repository.Repository;
import validation.*;
import repository.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.util.function.Consumer;
import app.PasswordHasher;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class UserService {


    private Repository<Long, Persoana> persoanaRepo;
    private Repository<Long, Duck> duckRepo;
    private Repository<Long, Card> cardRepo;
    private Repository<Long, Event> eventRepo;
    private UserFriendshipDbRepository friendshipRepo;
    private Repository<Long, Message> messageRepo;

    private Repository<Long, FriendRequest> requestRepo;

    private ExecutorService executorService = Executors.newFixedThreadPool(2);
    private List<Consumer<FriendRequest>> requestListeners = new ArrayList<>();

    private List<UserValidator> validators = new ArrayList<>();

    public UserService(
            Repository<Long, Persoana> persoanaRepo,
            Repository<Long, Duck> duckRepo,
            Repository<Long, Card> cardRepo,
            Repository<Long, Event> eventRepo,
            UserFriendshipDbRepository friendshipRepo,
            Repository<Long, Message> messageRepo,
            Repository<Long, FriendRequest> requestRepo
    ) {
        this.persoanaRepo = persoanaRepo;
        this.duckRepo = duckRepo;
        this.cardRepo = cardRepo;
        this.eventRepo = eventRepo;
        this.friendshipRepo = friendshipRepo;
        this.messageRepo = messageRepo;
        this.requestRepo = requestRepo;

        validators.add(new BaseUserValidator());
        validators.add(new DuckValidator());
        validators.add(new PersoanaValidator());
    }


    public void addUser(User user) {
        try {
            for (UserValidator validator : validators) {
                validator.validate(user);
            }

            String plainPassword = user.getPassword();
            String hashedPassword = PasswordHasher.hash(plainPassword);
            user.setPassword(hashedPassword);

            if (user instanceof Persoana p) {
                persoanaRepo.save(p);
            } else if (user instanceof Duck d) {
                duckRepo.save(d);
            }

        } catch (ValidationException e) {
            System.out.println("Eroare validare: " + e.getMessage());
        }
    }

    public User login(String username, String password) {
        //Cautam userul dupa username
        User foundUser = null;
        for (User u : getAllUsers()) {
            if (u.getUsername().equals(username)) {
                foundUser = u;
                break;
            }
        }

        if (foundUser == null) {
            return null;
        }

        // Criptam parola introdusa acum si o comparam cu cea din baza de date
        String inputHash = PasswordHasher.hash(password);

        if (inputHash.equals(foundUser.getPassword())) {
            foundUser.login();
            return foundUser;
        }

        return null;
    }

    public boolean removePersoanaById(Long id) {
        Persoana pStearsa = persoanaRepo.delete(id);
        return pStearsa != null;
    }

    public boolean removeDuckById(Long id) {
        Duck dStearsa = duckRepo.delete(id);
        return dStearsa != null;
    }

    public User findUserByIdAndType(Long id, int n) {
        if (id == null) return null;

        if (n == 1) { // 1 -> Persoana
            return persoanaRepo.findOne(id);
        } else if (n == 2) { // 2 -> Duck (Rata)
            return duckRepo.findOne(id);
        }
        return null;
    }

    public void addFriendship(User u1, User u2) {
        if (u1 == null || u2 == null) {
            System.err.println("Eroare: Ambii utilizatori trebuie să existe pentru a adăuga prietenia.");
            return;
        }
        if (u1.equals(u2)) {
            System.err.println("Eroare: Nu poți adăuga prietenie cu tine însuți.");
            return;
        }
        friendshipRepo.addFriendship(u1, u2);
    }

    public void removeFriendship(User u1, User u2) {
        if (u1 == null || u2 == null) {
            System.err.println("Eroare: Ambii utilizatori trebuie să existe pentru a șterge prietenia.");
            return;
        }
        friendshipRepo.removeFriendship(u1, u2);
    }

    public List<User> getAllUsers() {
        List<User> allUsers = new ArrayList<>();
        persoanaRepo.findAll().forEach(allUsers::add);
        duckRepo.findAll().forEach(allUsers::add);
        return allUsers;
    }

    public int getNumarComunitati() {
        List<User> users = getAllUsers();
        Set<User> vizitati = new HashSet<>();
        int comunitati = 0;
        for (User user : users) {
            if (!vizitati.contains(user)) {
                comunitati++;
                exploreazaComunitate(user, vizitati);
            }
        }
        return comunitati;
    }

    public void addCard(Card<? extends Duck> card) {
        cardRepo.save((Card) card);
    }

    public List<Card<? extends Duck>> getCarduri() {
        List<Card<? extends Duck>> list = new ArrayList<>();
        StreamSupport.stream(cardRepo.findAll().spliterator(), false)
                .forEach(c -> list.add((Card<? extends Duck>) c));
        return list;
    }

    public void addEvent(Event e) {
        eventRepo.save(e);
    }

    public List<Event> getEvenimente() {
        return StreamSupport.stream(eventRepo.findAll().spliterator(), false)
                .toList();
    }

    public Duck getDuckByUsername(String username) {
        for (Duck d : duckRepo.findAll()) {
            if (d.getUsername().equals(username)) {
                return d;
            }
        }
        return null;
    }

    private void exploreazaComunitate(User user, Set<User> vizitati) {
        vizitati.add(user);

        List<User> prieteni = friendshipRepo.getFriendsOf(user.getId(), getUserType(user));

        for (User prieten : prieteni) {
            if (!vizitati.contains(prieten)) {
                exploreazaComunitate(prieten, vizitati);
            }
        }
    }

    private void exploreazaComunitate(User user, Set<User> vizitati, List<User> componenta) {
        vizitati.add(user);
        componenta.add(user);

        List<User> prieteni = friendshipRepo.getFriendsOf(user.getId(), getUserType(user));

        for (User prieten : prieteni) {
            if (!vizitati.contains(prieten)) {
                exploreazaComunitate(prieten, vizitati, componenta);
            }
        }
    }
    public List<List<User>> getToateComunitatile() {
        List<User> users = getAllUsers();
        Set<User> vizitati = new HashSet<>();
        List<List<User>> comunitati = new ArrayList<>();

        for (User user : users) {
            if (!vizitati.contains(user)) {
                List<User> componenta = new ArrayList<>();
                exploreazaComunitate(user, vizitati, componenta);
                comunitati.add(componenta);
            }
        }
        return comunitati;
    }

    public List<User> getCeaMaiSociabilaComunitate() {
        List<List<User>> comunitati = getToateComunitatile(); // Acum o avem
        List<User> ceaMaiMare = new ArrayList<>();

        for (List<User> comunitate : comunitati) {
            if (comunitate.size() > ceaMaiMare.size()) {
                ceaMaiMare = comunitate;
            }
        }

        return ceaMaiMare;
    }

    public void afiseazaPerformantaMedie(String numeCard) {
        for (Card<? extends Duck> c : getCarduri()) {
            if (c.getNumeCard().equals(numeCard)) {
                System.out.println("Performanta medie pentru cardul '" + numeCard + "' este: " + c.getPerformantaMedie());
                return;
            }
        }
        System.out.println("Cardul cu numele '" + numeCard + "' nu a fost gasit.");
    }

    public boolean subscribeUserToEvent(String username, Event event) {
        if (event == null) return false;

        User userGasit = null;
        for (User u : getAllUsers()) {
            if (u.getUsername().equals(username)) {
                userGasit = u;
                break;
            }
        }

        if (userGasit != null) {
            event.subscribe(userGasit);
            eventRepo.save(event);
            return true;
        }

        return false;
    }

    public boolean unsubscribeUserFromEvent(String username, Event event) {
        if (event == null) return false;

        User userGasit = null;
        for (User u : getAllUsers()) {
            if (u.getUsername().equals(username)) {
                userGasit = u;
                break;
            }
        }

        if (userGasit != null) {
            event.unsubscribe(userGasit);
            return true;
        }

        return false;
    }
    private String getUserType(User u) {
        if (u instanceof Persoana) return "PERSOANA";
        if (u instanceof Duck) return "RATA";
        return null;
    }

    public List<User[]> incarcaPrieteni() {
        List<User[]> relatii = new ArrayList<>();

        for (Object[] row : friendshipRepo.findAll()) {

            Long id1 = (Long) row[0];
            String type1 = (String) row[1];
            Long id2 = (Long) row[2];
            String type2 = (String) row[3];

            User u1 = friendshipRepo.findUserByIdAndType(id1, type1);
            User u2 = friendshipRepo.findUserByIdAndType(id2, type2);

            if (u1 != null && u2 != null) {
                relatii.add(new User[]{u1, u2});
            }
        }

        return relatii;
    }

    public Page<Duck> getDucksOnPage(int pageNumber, int pageSize, String filter) {
        if (duckRepo instanceof DuckDbRepository) {
            return ((DuckDbRepository) duckRepo).findAllOnPage(new Pageable(pageNumber, pageSize), filter);
        }
        return new Page<>(new ArrayList<>(), 0);
    }

    public void sendMessage(Long fromId, String fromType, List<Long> toIds, List<String> toTypes, String text, Message replyTo) {
        User sender = findUserByIdAndType(fromId, fromType.equals("PERSOANA") ? 1 : 2);

        List<User> receivers = new ArrayList<>();
        if (toIds.size() != toTypes.size()) {
            throw new IllegalArgumentException("Listele de ID-uri si Tipuri trebuie sa aiba aceeasi marime!");
        }

        for(int i=0; i<toIds.size(); i++) {
            User u = findUserByIdAndType(toIds.get(i), toTypes.get(i).equals("PERSOANA") ? 1 : 2);
            if (u != null) receivers.add(u);
        }

        if (receivers.isEmpty()) throw new IllegalArgumentException("Nu exista destinatari valizi!");

        Message msg;
        if (replyTo == null) {
            // Mesaj nou
            msg = new Message(null, sender, receivers, text, LocalDateTime.now());
        } else {
            // Reply
            msg = new ReplyMessage(null, sender, receivers, text, LocalDateTime.now(), replyTo);
        }

        notifyChatListeners(msg);

        messageRepo.save(msg);
    }
    //Returneaza conversatia dintre doi useri
    public List<Message> getConversation(User u1, User u2) {
        Iterable<Message> allMessages = messageRepo.findAll();

        return StreamSupport.stream(allMessages.spliterator(), false)
                .filter(m -> isMessageBetween(m, u1, u2)) // Filtram
                .sorted(Comparator.comparing(Message::getDate)) // Sortam dupa data
                .collect(Collectors.toList());
    }

    private boolean isMessageBetween(Message m, User u1, User u2) {
        // Cazul A: u1 a trimis, u2 este printre destinatari
        boolean u1SendsToU2 = m.getFrom().equals(u1) && m.getTo().contains(u2);

        // Cazul B: u2 a trimis, u1 este printre destinatari
        boolean u2SendsToU1 = m.getFrom().equals(u2) && m.getTo().contains(u1);

        return u1SendsToU2 || u2SendsToU1;
    }

    private List<Consumer<Message>> chatListeners = new ArrayList<>();

    public void addChatListener(Consumer<Message> listener) {
        chatListeners.add(listener);
    }

    public void removeChatListener(Consumer<Message> listener) {
        chatListeners.remove(listener);
    }

    private void notifyChatListeners(Message msg) {
        for (Consumer<Message> listener : chatListeners) {
            listener.accept(msg); // Executa functia de update a ferestrei
        }
    }

    public Iterable<Persoana> getAllPersoane() {
        return persoanaRepo.findAll();
    }

    public void sendFriendRequest(String senderUsername, String receiverUsername) {
        User sender = findUserByUsername(senderUsername);
        User receiver = findUserByUsername(receiverUsername);

        if (sender == null || receiver == null) throw new IllegalArgumentException("Utilizatorul nu exista!");
        if (sender.getId().equals(receiver.getId()) && sender.getClass().equals(receiver.getClass()))
            throw new IllegalArgumentException("Nu iti poti trimite cerere tie!");

        FriendRequest existingReq = findRequestBetween(sender, receiver);

        if (existingReq != null) {
            // Daca exista, verificam statusul
            if ("PENDING".equals(existingReq.getStatus())) {
                throw new IllegalArgumentException("Exista deja o cerere in asteptare!");
            } else if ("APPROVED".equals(existingReq.getStatus())) {
                throw new IllegalArgumentException("Sunteti deja prieteni!");
            } else if ("REJECTED".equals(existingReq.getStatus())) {
                existingReq.setStatus("PENDING");
                existingReq.setDate(LocalDateTime.now());

                if (requestRepo instanceof FriendRequestDbRepository) {
                    ((FriendRequestDbRepository) requestRepo).update(existingReq);
                }
                notifyRequestListeners(existingReq);
                return;
            }
        }

        FriendRequest req = new FriendRequest(null, sender, receiver, "PENDING", LocalDateTime.now());
        requestRepo.save(req);
        notifyRequestListeners(req);
    }

    public void acceptFriendRequest(FriendRequest req) {
        req.setStatus("APPROVED");
        if (requestRepo instanceof FriendRequestDbRepository) {
            ((FriendRequestDbRepository) requestRepo).update(req);
        }

        friendshipRepo.addFriendship(req.getFrom(), req.getTo());

        notifyRequestListeners(req);
    }

    public void rejectFriendRequest(FriendRequest req) {
        req.setStatus("REJECTED");
        if (requestRepo instanceof FriendRequestDbRepository) {
            ((FriendRequestDbRepository) requestRepo).update(req);
        }
        notifyRequestListeners(req);
    }

    public List<FriendRequest> getRequests(User receiver) {
        List<FriendRequest> result = new ArrayList<>();
        for (FriendRequest req : requestRepo.findAll()) {
            if (req.getTo().equals(receiver)) {
                result.add(req);
            }
        }
        return result;
    }

    private User findUserByUsername(String username) {
        for (User u : getAllUsers()) {
            if (u.getUsername().equals(username)) return u;
        }
        return null;
    }

    private FriendRequest findRequestBetween(User sender, User receiver) {
        for (FriendRequest req : requestRepo.findAll()) {
            if (req.getFrom().equals(sender) && req.getTo().equals(receiver)) {
                return req;
            }
        }
        return null;
    }

    public void addRequestListener(Consumer<FriendRequest> listener) { requestListeners.add(listener); }
    public void removeRequestListener(Consumer<FriendRequest> listener) { requestListeners.remove(listener); }
    private void notifyRequestListeners(FriendRequest req) {
        requestListeners.forEach(listener -> listener.accept(req));
    }

    public CompletableFuture<String> simuleazaCursaAsincron(Long eventId) {
        return CompletableFuture.supplyAsync(() -> {
                    // Rularea cursei pe thread
                    System.out.println("[Thread " + Thread.currentThread().getName() + "] Începe simularea cursei...");

                    Event event = eventRepo.findOne(eventId);
                    if (event == null || !(event instanceof RaceEvent)) {
                        throw new RuntimeException("Eveniment invalid sau nu este de tip cursă!");
                    }
                    RaceEvent race = (RaceEvent) event;

                    // Simulam un timp de procesare ca sa vedem bara de progres în UI
                    try {
                        Thread.sleep(3000); // 3 secunde "gândire"
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // Preluam ratele participante
                    List<Duck> participanti = new ArrayList<>();
                    duckRepo.findAll().forEach(d -> {
                        if (d.getTip() == Duck.TipRata.Swimming || d.getTip() == Duck.TipRata.Flying_and_Swimming) {
                            participanti.add(d);
                        }
                    });

                    race.simuleazaCursa(participanti);

                    return "Cursa '" + race.getNume() + "' s-a încheiat cu succes!";

                }, executorService)

                .thenApplyAsync(rezultatAnterior -> {
                    // Trimiterea Notificărilor
                    System.out.println("[Thread " + Thread.currentThread().getName() + "] Trimit notificări participanților...");

                    Event event = eventRepo.findOne(eventId);

                    // Trimitem mesaje userilor abonati
                    for (User u : event.getSubscribers()) {
                        System.out.println(" -> Notificare trimisă (simulat) către user: " + u.getUsername());
                    }

                    return rezultatAnterior + "\n(Notificările au fost trimise către " + event.getSubscribers().size() + " abonați)";
                }, executorService);
    }

    public void updateUserImage(User user) {
        if (user instanceof Persoana) {
            ((PersoanaDbRepository) persoanaRepo).update((Persoana) user);
        } else if (user instanceof Duck) {
            ((DuckDbRepository) duckRepo).update((Duck) user);
        }
    }

    public List<User> getPrietenii(User user) {
        if (user == null) return new ArrayList<>();
        return friendshipRepo.getFriendsOf(user.getId(), getUserType(user));
    }

}