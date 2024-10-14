import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class LucaTester {
    public static void main(String[] args) throws IOException, NoSuchAlgorithmException {
        deleteDirectory("root");
        deleteDirectory("git");
        deleteDirectory("temp folder");

        testSetup1();

        File tempFile = new File("tempFile.txt");
        tempFile.createNewFile();
        FileWriter fw1 = new FileWriter(tempFile);
        fw1.append("new file");
        fw1.close();


        Git.initializeRepo();
        Git.createNewBlob("root");

        Git.createTree();
        Git.makeCommit("anthonyTest1", "please work");

        File testAddFileThing = new File("root/testAddFileThing");
        testAddFileThing.createNewFile();
        FileWriter fw2 = new FileWriter(testAddFileThing);
        fw2.append("test 2nd commit");
        fw2.close();



        Git.createNewBlob("root/testAddFileThing");
        Git.createTree();
        Git.makeCommit("anthonyTest2", "please work!!");

        FileWriter fw3 = new FileWriter(testAddFileThing);
        fw3.append("\nthis is new edited text");
        fw3.close();

        Git.createNewBlob("root/testAddFileThing");
        Git.createTree();
        Git.makeCommit("anthonyTest3", "please work123123!!");

    }

    public static void testSetup1() throws IOException{

        File root = new File("root");
        root.mkdir();
        
        File newfile = new File("root/new.txt");
        newfile.createNewFile();
        FileWriter fw1 = new FileWriter(newfile);
        fw1.append("new file");
        fw1.close();
        
        File test1 = new File("root/test.txt");
        newfile.createNewFile();
        FileWriter fw2 = new FileWriter(test1);
        fw2.append("version 2");
        fw2.close();

        File bak = new File("root/bak");
        bak.mkdir();

        File test2 = new File("root/bak/test.txt");
        newfile.createNewFile();
        FileWriter fw3 = new FileWriter(test2);
        fw3.append("version 1");
        fw3.close();
    }

    public static void deleteDirectory(String fileName){
        File file = new File(fileName);
        if (!file.exists()){
            return;
        }
        File[] contents = file.listFiles();
        for (File temp : contents){
            if (temp.isDirectory()){
                deleteDirectory(temp.getPath());
            }
            temp.delete();
        }
        file.delete();
    }
}
