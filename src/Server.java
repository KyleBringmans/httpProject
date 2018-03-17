import javax.xml.crypto.Data;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URISyntaxException;

public class Server {
	
    public static void main(String argv[]) throws IOException, URISyntaxException {
        // Initialize a socket at port X
        ServerSocket socket = new ServerSocket(3000);

        while(true){
            Socket client = socket.accept();
            new Thread(new Task(client)).start();
        }
    }
}

class Task implements Runnable {
    public Task(Socket client) throws IOException {
        this.client = client;
    }

    @Override
    public void run(){
        try {
			this.inputs = new DataInputStream(client.getInputStream());
	        this.outputs = new DataOutputStream(client.getOutputStream());
	        outputs.write(("test").getBytes());
	        Thread.sleep(5000);
	        inputs.close();
	        outputs.close();
	        System.out.println("t is gebeurd");
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    private Socket client;
    private DataInputStream inputs;
    private DataOutputStream outputs;
}
