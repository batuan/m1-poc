// -*- coding: utf-8 -*-

package tpB;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class SeptNains {
    static private SimpleDateFormat sdf = new SimpleDateFormat("hh'h 'mm'mn 'ss','SSS's'");

    public static void main(String[] args) throws InterruptedException {
        Date début = new Date(System.currentTimeMillis());
        System.out.println("[" + sdf.format(début) + "] Début du programme.");

        final BlancheNeige bn = new BlancheNeige();
        final int nbNains = 7;
        final String noms [] = {"Simplet", "Dormeur",  "Atchoum", "Joyeux", "Grincheux",
                "Prof", "Timide"};
        final Nain nain [] = new Nain [nbNains];
        for(int i = 0; i < nbNains; i++) nain[i] = new Nain(noms[i],bn);
        for(int i = 0; i < nbNains; i++) nain[i].start();

        try {
            Thread.sleep(5000);
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            for(int i = 0; i < nbNains; i++) nain[i].interrupt();
        }

        for(int i = 0; i < nbNains; i++) {
            try {
                nain[i].join();
            } catch(InterruptedException e) {
                System.out.println("[" + noms[i] + "] Erreur dans le join : " + e.getMessage());
            }
        }

    }
}

class BlancheNeige {
    private final BlockingQueue<Nain> fileAttente =  new ArrayBlockingQueue<Nain>(7) ;
    private volatile boolean libre = true;        // Initialement, Blanche-Neige est libre.
    public synchronized void requérir () {
        Nain currentThread = (Nain) Thread.currentThread();
        fileAttente.add(currentThread);
        System.out.println("\t" + currentThread.getName()
                + " veut la ressource.");
    }

    public synchronized void accéder () throws InterruptedException {
        Nain currentThread = (Nain) Thread.currentThread();
        while ( ! libre || !currentThread.equals(fileAttente.peek())) {
                wait();                    // Le nain s'endort sur l'objet bn
        }
        libre = false;
        System.out.println("\t" + Thread.currentThread().getName()
                + " accède à la ressource.");
    }

    public synchronized void relâcher () {
        System.out.println("\t" + Thread.currentThread().getName()
                + " relâche la ressource.");
        libre = true;
        fileAttente.poll();
        notifyAll();
    }
}

class Nain extends Thread {
    private BlancheNeige bn;
    public Nain(String nom, BlancheNeige bn) {
        this.setName(nom);
        this.bn = bn;
    }
    public void run() {
        while(!isInterrupted()) {
            try {
                bn.requérir();
                bn.accéder();
                System.out.println(getName() + " a un accès (exclusif) à Blanche-Neige.");
                sleep(2000);
                System.out.println(getName() + " s'apprête à quitter Blanche-Neige.");
                bn.relâcher();
            } catch (InterruptedException ex) {
                this.interrupt();
            }
        }
        System.out.println(getName() + " a terminé!");
    }
}

/*
$ make
$ java SeptNains
[09h 34mn 01,834s] Début du programme.
	Simplet veut la ressource.
	Simplet accède à la ressource.
	Timide veut la ressource.
	Prof veut la ressource.
	Grincheux veut la ressource.
	Joyeux veut la ressource.
	Atchoum veut la ressource.
	Dormeur veut la ressource.
Simplet a un accès (exclusif) à Blanche-Neige.
Simplet s'apprête à quitter à Blanche-Neige.
	Simplet relâche la ressource.
	Simplet veut la ressource.
	Simplet accède à la ressource.
Simplet a un accès (exclusif) à Blanche-Neige.
	Timide accède à la ressource.
Timide a un accès (exclusif) à Blanche-Neige.
	Dormeur accède à la ressource.
Dormeur a un accès (exclusif) à Blanche-Neige.
	Atchoum accède à la ressource.
Atchoum a un accès (exclusif) à Blanche-Neige.
	Joyeux accède à la ressource.
Joyeux a un accès (exclusif) à Blanche-Neige.
	Grincheux accède à la ressource.
Grincheux a un accès (exclusif) à Blanche-Neige.
	Prof accède à la ressource.
Prof a un accès (exclusif) à Blanche-Neige.
^C
*/
