// -*- coding: utf-8 -*-
package tpB1;
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

        // Attendre 5 secondes
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            System.err.println(e.getMessage());
        } finally {
            // Interruption tous les nains.
            System.out.println("[" + sdf.format(new Date(System.currentTimeMillis())) + "] " +"Interruption des 7 nains.");
            for(int i = 0; i < nbNains; i++) {
                nain[i].interrupt();
            }
            for(int i = 0; i < nbNains; i++) {
                nain[i].join();
            }
        }

        // Afficher l'heure et le message de fin
        System.out.println("[" + sdf.format(new Date(System.currentTimeMillis())) + "] Tous les nains ont terminé.");

    }
}

class BlancheNeige {
    private volatile boolean libre = true;        // Initialement, Blanche-Neige est libre.
    // File d'attente des nains.
    // Ref: https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/concurrent/BlockingQueue.html
    private final BlockingQueue<Nain> fileAttente =  new ArrayBlockingQueue<Nain>(7) ;

    public synchronized void requérir () {
        //Lorsque le nain fait une "requérir", la file d'attente ajoute le nain
        Nain currentThread = (Nain) Thread.currentThread();
        fileAttente.add(currentThread) ;
        System.out.println("\t" + currentThread.getName()
                + " veut la ressource.");
    }

    public synchronized void accéder() throws InterruptedException{
        Nain currentThread = (Nain) Thread.currentThread();
        // Vérifiez si le nain a la liberté et son ordre dans la file d'attente
        // méthode peek() retourner la head de file d'anntente
        while ( ! libre || !currentThread.equals(fileAttente.peek())) {
            wait(); // Le nain s'endort sur l'objet bn
        }
        libre = false;
        System.out.println("\t" + Thread.currentThread().getName()
                + " accède à la ressource.");
    }

    public synchronized void relâcher () {
        System.out.println("\t" + Thread.currentThread().getName()
                + " relâche la ressource.");
        libre = true;
        //remove head de file d'atente
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

    static private SimpleDateFormat sdf = new SimpleDateFormat("hh'h 'mm'mn 'ss','SSS's'");

    public void run() {
        // si le nain n'est interrompus, lui permettre un accès à la ressource
        while(!isInterrupted()) {
            try {
                bn.requérir();
                bn.accéder();
                System.out.println("[" + sdf.format(new Date(System.currentTimeMillis())) + "] "
                                     + getName() + " a un accès (exclusif) à Blanche-Neige.");
                try {
                    sleep(2000);
                } catch (InterruptedException e) {
                    this.interrupt();
                } finally {
                    System.out.println("[" + sdf.format(new Date(System.currentTimeMillis())) + "] "
                                        + getName() + " s'apprête à quitter Blanche-Neige.");
                    bn.relâcher();
                }
            } catch (InterruptedException exception) {
                this.interrupt();
            }
        }

        // le nain est interrompus, afficher la terminer et le temp
        System.out.println("[" + sdf.format(new Date(System.currentTimeMillis())) + "] "
                             + getName() + " a terminé!");
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
