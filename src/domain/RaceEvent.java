package domain;

import java.util.ArrayList;
import java.util.List;

public class RaceEvent extends Event {

    private List<Double> balize; // distantele balizelor
    private Duck[] theBestDucks;
    private double bestTime;
    private Duck[] ducksArr;
    private int m;

    public RaceEvent(String nume, List<Double> balize) {
        super(nume);
        this.balize = balize;
        this.bestTime = Double.MAX_VALUE;
    }

    public List<Double> getBalize() {
        return balize;
    }

    public void simuleazaCursa(List<Duck> toateRatele) {
        // filtram ratele care pot inota
        List<Duck> inotatori = new ArrayList<>();
        for (Duck d : toateRatele) {
            if (d instanceof Inotator) {
                inotatori.add(d);
            }
        }

        if (inotatori.size() < balize.size()) {
            System.out.println("Nu sunt suficiente rate pentru cursa " + nume);
            return;
        }

        ducksArr = inotatori.toArray(new Duck[0]);
        m = balize.size();
        theBestDucks = new Duck[m];
        bestTime = Double.MAX_VALUE;

        // backtracking ca în codul tau initial
        backtrack(0, new Duck[m], 0);

        // notificam subscriberii
        notifySubscribers("Cursa " + nume + " a început!");

        // afisam rezultatele
        System.out.println("Timp minim cursa: " + String.format("%.3f", bestTime) + " s");
        for (int i = 0; i < m; i++) {
            double time = 2 * balize.get(i) / theBestDucks[i].getViteza();
            System.out.println("Duck " + theBestDucks[i].getUsername() +
                    " pe culoarul " + (i + 1) +
                    ": t = " + String.format("%.3f", time) + " s");
        }

        notifySubscribers("Cursa " + nume + " s-a terminat. Timp optim: " + String.format("%.3f", bestTime) + " s");
    }

    private void backtrack(int i, Duck[] current, int startIndex) {
        if (i == m) {
            double maxTime = 0;
            for (int j = 0; j < m; j++) {
                double time = 2 * balize.get(j) / current[j].getViteza();
                if (time > maxTime) maxTime = time;
            }
            if (maxTime < bestTime) {
                bestTime = maxTime;
                System.arraycopy(current, 0, theBestDucks, 0, m);
            }
            return;
        }

        for (int k = startIndex; k < ducksArr.length; k++) {
            Duck candidate = ducksArr[k];

            // restricția de rezistenta descrescatoare
            if (i > 0 && candidate.getRezistenta() < current[i - 1].getRezistenta())
                continue;

            // verificam dacă rata a fost deja folosita
            boolean used = false;
            for (int j = 0; j < i; j++) {
                if (current[j] == candidate) {
                    used = true;
                    break;
                }
            }
            if (used) continue;

            current[i] = candidate;
            backtrack(i + 1, current, 0); // startIndex = 0
            current[i] = null; // backtrack
        }
    }
}
