import java.io.*;

public class FileHandler {

    public FileHandler() {

    }

    public String getContent(DataInputStream in, int length) throws IOException {
        // Get data from client
        StringBuilder response = new StringBuilder();
        for(int i = 0; i<length; i++) {
            response.append((char) in.read());
        }
        System.out.println(response);
        return response.toString();
    }

    public void writeOutputToFile(String content, String pathName) throws IOException {
        // Make the folders so the file can be written in the right place
    	makeFolders(pathName.substring(1));

        String[] inputs = pathName.split("/");
        // Find directory name
        String dirName = pathName.substring(0, pathName.length() - inputs[inputs.length-1].length());
        // Remove wrong characters
        dirName = dirName.replace("%20", " ");
        String fileName = inputs[inputs.length-1];
        File dir = new File (System.getProperty("user.dir") + dirName);
        File file = new File (dir, fileName);

        // Write content to file
        final FileOutputStream fileOut = new FileOutputStream(file);
        fileOut.write(content.getBytes());
        fileOut.flush();
        fileOut.close();

    }

    public static String makeFolders(String path){
        String[] folders = path.split("/");

        // Make the folder names
        String newFolders = "";
        for(int i = 0; i < folders.length -1; i++){
            newFolders += folders[i] + "/";
        }

        // Make the new folders
        if(newFolders.length() > 0){
            newFolders = newFolders.substring(0, newFolders.length()-1);
            newFolders = newFolders.replace("%20", " ");
            new File(newFolders).mkdirs();
        }

        return "/" + path;
    }
}
