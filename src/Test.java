import java.io.DataInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.logging.FileHandler;

import org.jsoup.nodes.Element;

public class Test {
	public static void main(String argv[]) throws SecurityException, IOException{
		writeOutputToFile("blabla123123", "/testdir/test.txt");
		writeOutputToFile("456456", "/testdir/test.txt");
		File test = new File("test");
		if(!test.exists()){
			System.out.println("ok");
		}
		else{
			System.out.println("hier");
			Files.write(Paths.get("testdir/test.txt"), "789789".getBytes(), StandardOpenOption.APPEND);
		}
		DataInputStream in = new DataInputStream(System.in);
		String line = in.readLine();
		while(!line.equals("")){
			line = in.readLine();
			System.out.println(line.toUpperCase());
		}
	}
	
	public static void writeOutputToFile(String content, String fileNamet) throws IOException{
		String[] temp = fileNamet.split("/");
		String dirName = fileNamet.substring(0, fileNamet.length() - temp[temp.length-1].length());
		dirName = dirName.replace("%20", " ");
		String fileName = temp[temp.length-1];
		File dir = new File (System.getProperty("user.dir") + dirName);
		File file = new File (dir, fileName);

		String testPath = findPath(fileNamet);
		// Write content to file
        final FileOutputStream fileOut = new FileOutputStream(file);
        byte[] c = content.getBytes();
        fileOut.write(c);
        fileOut.flush();
        fileOut.close();;
	}
	
	public static String findPath(String path){
		// Parse the image path out of the html command
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
