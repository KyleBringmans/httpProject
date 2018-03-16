import javax.xml.crypto.Data;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;

public class Server {
    public static void main(String argv[]) throws IOException, URISyntaxException {
        // Initialise a socket at port X
        ServerSocket socket = new ServerSocket(3000);

        while(true){
            Socket client = socket.accept();
            Task task = new Task(client);
            task.run();
        }
    }
}

class Task implements Runnable {
    public Task(Socket client) throws IOException {
        this.client = client;
        this.inputs = new DataInputStream(client.getInputStream());
        this.outputs = new DataOutputStream(client.getOutputStream());
    }

    @Override
    public void run(){
        try {
            (new Thread(new Task(client))).start();
            String test = inputs.readLine();
            System.out.println(test);

        } catch (IOException e) {
            System.out.println("Something went wrong (server)");
        }
    }

    private Socket client;
    private DataInputStream inputs;
    private DataOutputStream outputs;
}
