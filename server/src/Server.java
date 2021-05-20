import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static int port = 1500;

    public static void receiveFile(Socket client) throws IOException {
        String fileName;
        long fileSize;
        try (DataInputStream iStream = new DataInputStream(client.getInputStream())) {
            try {
                fileName = iStream.readUTF();
                fileSize = iStream.readLong();
            } catch (IOException e) {
                System.out.println("Wystąpił błąd! Nie można odczytać danych z pliku.");
                client.close();
                return;
            }
            File fFile = new File(".", fileName);
            System.out.println("Pobieranie pliku: " + fileName + ". Od użytkownika: " +  client.getInetAddress().toString() + ", rozmiar pliku to: "+ fileSize + " bajtów.");
            fFile.createNewFile();
            FileOutputStream oStream = new FileOutputStream(fFile);
            try {
                int uploaded = 0;
                int read;
                byte[] buffer = new byte[4096];
                while (uploaded < fileSize) {
                    read = iStream.read(buffer);
                    if (read != -1) uploaded += read;
                    else throw new IOException("Bład! Plik jest za duży.");
                    oStream.write(buffer, 0, read);
                }
                System.out.println("Pobieranie " + fileName + " ukończone!");
                oStream.close();
            } catch (IOException e) {
                System.out.println("Błąd! Zerwano połaczenie z serwerem. " + e.getMessage());
                fFile.delete();
            }
        }
        client.close();
    }

    public static void main(String[] args){
        int threads = 8;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        System.out.println("Server uruchomiony");
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            while (true) {
                try {
                    final Socket client = serverSocket.accept();
                    executor.submit(() -> {
                        try {
                            Server.receiveFile(client);
                        } catch (IOException e) {
                            System.out.println("Błąd! " + e.getMessage());
                        }
                    });
                } catch (IOException e) {
                    System.out.println("Błąd! Nowy klient nie może zostać zaakceptowany.");
                    System.exit(1);
                }
            }
        } catch(IOException e) {
            System.out.println("Upss... Coś poszło nie tak!");
            System.exit(1);
        }
    }
}
