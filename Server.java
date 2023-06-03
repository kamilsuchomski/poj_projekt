import java.io.PrintWriter;
import java.io.IOException;

import java.net.ServerSocket;
import java.net.Socket;

import java.util.Set;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Scanner;
import java.util.logging.Handler;


/**
 * Wielowatkowy(obsluga wielu klientow) czat sieciowy. Kiedy klient sie polaczy, serwer pyta o nazwe uzytkownika
 * poprzez wyslanie do klienta tekstu "SUBMITNAME" i robi to tak dlugo, dopoki otrzymana nazwa nie jest unikalna.
 * Po wyslaniu przez klienta unikalnej nazwy, serwer akceptuje nazwe i wysyla tekst "NAMEACCEPTED".
 * Nastepnie wszystkie wiadomosci od tego klienta beda nadawane do wszystkich pozostalych klientow, ktorzy wyslali do
 * serwera swoja unikalna nazwe. Nadawana wiadomosc jest poprzedzona przez prefix "MESSAGE".
 *
 * "//q" - powoduje wyjscie z czatu i zamkniecie klienta
 */


public class Server extends initConnection {

    // Wszystkie nazwy klientow, dzieki temu mozemy sprawdzic czy sie nie powtarzaja
    private static Set<String> names = new HashSet<>();

    // Zbior wszystkich strumieni wyjsciowych, uzywany do nadawania
    private static Set<PrintWriter> writers = new HashSet<>();

    public static void main(String[] args) throws Exception{

        initConnection config = new initConnection(21974);

        System.out.println("Serwer czatu dziala...");

        // Tworzenie nowej puli watkow(max. 10 klientow)
        ExecutorService pool = Executors.newFixedThreadPool(10);

        // Proba utworzenia gniazda serwerowego
        try(ServerSocket listener = new ServerSocket(config.getPort())){
            // Nieskonczona petla do ciaglego nasluchiwania gniazda serwerowego
            while(true){
                pool.execute(new Handler(listener.accept()));
            }
        }
    }


    /**
     * Uchwyt do task'u klienta.
     */

    private static class Handler implements Runnable{

        private String name;
        private Socket socket;
        private Scanner in;
        private PrintWriter out;

        /**
         * Publiczny konstruktor do uchwytu watku, przechowujacy gniazdo. Wszystkie mechanizmy(kontrola nazwy itp.)
         * sa zawarte w metodzie run. Z racji wywolywania konstruktora w glownej metodzie serwera, powinien byc jak
         * najprostszy.
         */
        public Handler(Socket socket){
            this.socket = socket;
        }

        /**
         * Zadanie tego watku polega na pytaniu klienta o jego nazwe, dopoki nie bedzie unikalna. Nastepnie akceptuje
         * nazwe i rejestruje strumien wyjsciowy klienta w globalnym zbiorze strumieni wyjsciowych, po czym w petli
         * pobiera input i go nadaje.
         */
        public void run(){
            try{
                in = new Scanner(socket.getInputStream());
                out = new PrintWriter(socket.getOutputStream(), true);

                // Pobieranie nazwy uzytkownika, dopoki nie bedzie unikalna
                while(true){
                    out.println("SUBMITNAME");
                    name = in.nextLine();
                    if(name == null){
                        return;
                    }
                    synchronized (names){
                        if (!name.isBlank() && !names.contains(name)){
                            names.add(name);
                            break;
                        }
                    }
                }

                // Unikalna nazwa zostala podana, mozna dodac gniazdo do zbioru strumieni wyjsciowych, ktore beda nadawane
                // dzieki czemu klient bedzie rowniez otrzymywal wiadomosci nadawane przez innych klientow

                // Wiadomosc powitalna

                out.println("NAMEACCEPTED " + name);
                for(PrintWriter writer : writers){
                    writer.println("MESSAGE " + name + " dolaczyl do czatu.");
                }
                writers.add(out);
                System.out.println(name + " dolaczyl do czatu.");

                // Akceptowanie wiadomosci od klienta i nadawanie ich
                while(true){
                    String input = in.nextLine();
                    if(input.toLowerCase().startsWith("//q")){
                        return;
                    }
                    for(PrintWriter writer : writers){
                        writer.println("MESSAGE " + name + ": " + input);
                    }
                }
            }catch (Exception e){
                System.out.println("Blad wiadomosci!\n" + e);
            }finally {
                if (out != null){
                    writers.remove(out);
                }
                if (name != null){
                    System.out.println(name + " opuszcza czat.");
                    names.remove(name);
                    for (PrintWriter writer : writers){
                        writer.println("MESSAGE " + name + " opuscil czat.");
                    }
                }
                try{
                    socket.close();
                }catch (IOException e){
                    System.out.println("Blad zamkniecia gniazda!");
                }
            }
//            try{
//                socket.close();
//            }catch (IOException e){
//                System.out.println("Blad zamkniecia gniazda!");
//            }

        }

    }
}
