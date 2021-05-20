package sample;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import javafx.concurrent.Task;

public class SendFileTask extends Task<Void> {
    private File plik;
    private int port = 1500;

    public SendFileTask(File file) {
        this.plik = file;
    }

    @Override
    protected Void call() {
        updateMessage("Łączenie...");
        updateProgress(0, plik.length());
        try {
            Socket server = new Socket("localhost", port);
            try (DataOutputStream oStream = new DataOutputStream(server.getOutputStream())) {
                oStream.writeUTF(plik.getName());
                oStream.writeLong(plik.length());
                long fileSize = plik.length();
                long read = 0;
                updateMessage("Wysyłanie...");
                updateProgress(0, fileSize);
                FileInputStream iStream = new FileInputStream(plik);
                byte[] buffer = new byte[4096];
                while (read != fileSize) {
                    int success = iStream.read(buffer);
                    oStream.write(buffer, 0, success);
                    read += success;
                    updateProgress(read, fileSize);
                }
            }
        } catch (IOException e) {
            updateMessage("Wystąpił błąd!\n" + e.getMessage());
            return null;
        }
        updateMessage("Wysłano plik!");
        return null;
    }
}
