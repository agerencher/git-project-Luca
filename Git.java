import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.channels.FileChannel;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Objects;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Git implements GitInterface{
    public static void main(String[] args) throws IOException {
        initializeRepo();
    }

    public static void initializeRepo() throws IOException{
        File git = new File("git");
        File objects = new File("git/objects");
        File index = new File("git/index");
        File head = new File("git/HEAD");

        if (git.exists() && objects.exists() && index.exists() && head.exists()) {
            System.out.println ("Git Repository already exists");
        }
        else {
            if (!git.exists())
                git.mkdir();
            if (!objects.exists())
                objects.mkdir();
            if (!index.exists())
                index.createNewFile();
            if (!head.exists())
                head.createNewFile();
        }
    }

    public static void deleteRepo() {
        File git = new File("git");
        File objects = new File(git, "objects");
        File index = new File(git, "index");
        index.delete(); objects.delete(); git.delete();
    }

    public static String generateFileName(String path) throws IOException, NoSuchAlgorithmException {

        //if file is not a directory:
        if (!new File(path).isDirectory()){
            FileInputStream fileInputStream = new FileInputStream(path);
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            DigestInputStream digestInputStream = new DigestInputStream(fileInputStream, digest);

            //not sure what these two lines do
            byte[] bytes = new byte[1024];
            // read all file content
            while (digestInputStream.read(bytes) > 0);

    //        digest = digestInputStream.getMessageDigest();
            byte[] resultByteArry = digest.digest();
            digestInputStream.close();
            return bytesToHexString(resultByteArry);
        }

        //if file is a directory:
        else{
            File file = new File(path);
            File[] contents = file.listFiles();
            StringBuilder sb = new StringBuilder();
            for (File temp : contents) {
                if (temp.isDirectory()){
                    sb.append("tree " + generateFileName(temp.getPath()) + " " + temp.getPath() + "\n");
                }
                else{
                    sb.append("blob " + generateFileName(temp.getPath()) + " " + temp.getPath() + "\n");
                }
            }
            String fileNames = sb.toString();


            MessageDigest digester = MessageDigest.getInstance("SHA-1");
            byte[] sha1bytes = digester.digest(fileNames.getBytes());
            return bytesToHexString(sha1bytes);
        }
    }

    public static String bytesToHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            int value = b & 0xFF;
            if (value < 16) {
                // if value less than 16, then it's hex String will be only
                // one character, so we need to append a character of '0'
                sb.append("0");
            }
            sb.append(Integer.toHexString(value));
        }
        return sb.toString();
    }

    //takes in a path that might be a directory or normal file
    public static void createNewBlob(String path) throws IOException, NoSuchAlgorithmException {
        //initialize original file
        File file = new File(path);
        if (!file.exists()) {
            System.out.println("File DNE.");
            return;
        }

        // Check if file or directory is readable
        if (!file.canRead()) {
            System.out.println("permission denied, unable to read " + path);
            return;
        }
        //generate file name string
        String hash = generateFileName(path);

        //initialize copy file
        File newBlob = new File("git/objects/" + hash);
        //copy the file
        if (!newBlob.exists()){
            File original = new File(path);
            if (!file.isDirectory()){
                Files.copy(original.toPath(), newBlob.toPath());
            }
            else{
                newBlob.createNewFile();
                File[] contents = original.listFiles();
                FileWriter fw = new FileWriter(newBlob);
                for (File temp : contents) {
                    if (temp.isDirectory()){
                        fw.append("tree " + generateFileName(temp.getPath()) + " " + temp.getPath() + "\n");
                    }
                    else{
                        fw.append("blob " + generateFileName(temp.getPath()) + " " + temp.getPath() + "\n");
                    }
                }
                fw.close();
            }
        }



        //index update
        if (!file.isDirectory()){

            //checks if already eists
            String fileName = file.getPath();
            BufferedReader reader = new BufferedReader(new FileReader("git/index"));
            while (reader.ready()) {
                String line = reader.readLine();
                if (Objects.equals(line.substring(line.length() - fileName.length()), fileName)) {
                    reader.close();
                    return;
                }
            }
            reader.close();

            //add the blob entry to the index file
            File index1 = new File("git/index");
            FileWriter writer1 = new FileWriter(index1, true);
            writer1.write("blob " + hash + " " + path);
            writer1.write(System.lineSeparator());
            writer1.close();

        }
        else{
            //recursively calls create new blob on all the files within directory
            File[] filesInDirectory = file.listFiles();
            if (filesInDirectory != null) {
                for (File f : filesInDirectory) {
                    //recursively call createNewBlob
                    createNewBlob(f.getPath());
                }
            }

            //add the tree entry to the index file
            File index = new File("git/index");
            FileWriter writer = new FileWriter(index, true);
            writer.write("tree " + hash + " " + path);
            writer.write(System.lineSeparator());
            writer.close();
        }
    }

    public static String makeCommit(String author, String message) throws IOException, NoSuchAlgorithmException{
        File rootTree = new File("git/rootTree");
        String rootTreeHash = generateFileName(rootTree.getPath());
        //creates commit file
        File commit = new File("git/objects/tempCommit");
        commit.createNewFile();
        File head = new File("git/HEAD");


        //writes tree line using the rootTree Hash
        BufferedWriter commitWriter = new BufferedWriter(new FileWriter(commit.getPath()));
        commitWriter.write("tree: " + rootTreeHash);
        commitWriter.newLine();

        File newTree = new File("git/objects/" + rootTreeHash);
        //newTree.createNewFile();
        rootTree.renameTo(newTree);
        rootTree.createNewFile();


        //writes parent line using the HEAD
        commitWriter.write("parent: ");
        BufferedReader headReader = new BufferedReader(new FileReader(head.getPath()));
        String headHash = headReader.readLine();
        headReader.close();
        if(headHash != null) {
            commitWriter.write(headHash);
        }
        else {
            //if it is the first commit, parent is null
            commitWriter.write("null");
        }
        commitWriter.newLine();

        //writes the author from parameter
        commitWriter.write("author: " + author);
        commitWriter.newLine();

        //from google
        //writes date
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");
        String date = currentDate.format(formatter);
        commitWriter.write("date: " + date);
        commitWriter.newLine();

        //writes message from parameter
        commitWriter.write("message: " + message);
        commitWriter.close();

        File newCommitFile = new File("git/objects/" + generateFileName(commit.getPath()));
        //using the data in commit, makes a new file, then renames the original commit file form line 191 with the hash of the new file
        commit.renameTo(newCommitFile);
        if (headHash != null) {
            //erases the hash in head
            head.delete();
            head.createNewFile();
        }
        //writes new latest commit into head
        BufferedWriter headWriter = new BufferedWriter(new FileWriter(head.getPath()));
        headWriter.write(newCommitFile.getName());
        headWriter.newLine();
        headWriter.close();

        //resets index for any furture stages
        File index = new File("git/index");
        index.delete();
        index.createNewFile();
        return newCommitFile.getName();
    }

    public static void createTree() throws IOException {
        File head = new File("git/HEAD");
        File rootTree = new File("git/rootTree");
        if (!rootTree.exists())
            rootTree.createNewFile();
        File index = new File("git/index");

        BufferedReader indexReader = new BufferedReader(new FileReader(index.getPath()));
        BufferedWriter treeWriter = new BufferedWriter(new FileWriter(rootTree.getPath(), true));

        //makes arraylist for later to check if there are duplicate names
        ArrayList<String> indexNames = new ArrayList<>();
        //writes everything from index to tree
        while (indexReader.ready()) {
            String tempIndex = indexReader.readLine();
            indexNames.add(tempIndex.substring(46));
            treeWriter.write(tempIndex);
            treeWriter.newLine();
        }
        indexReader.close();
        treeWriter.close();


        BufferedReader headReader = new BufferedReader(new FileReader(head.getPath()));
        //gets the hash of the prev. commit
        String headHash = headReader.readLine();
        headReader.close();
        if(headHash != null) {
            File previousCommit = new File("git/objects/" + headHash);
            BufferedReader commitReader = new BufferedReader(new FileReader(previousCommit.getPath()));
            //gets the hash of the tree of the previous commit
            String prevTreeHash = commitReader.readLine().substring(6);
            commitReader.close();
            File previousTree = new File("git/objects/" + prevTreeHash);

            //uses the previous commit's tree to copy all non-duplicates into the new tree
            BufferedReader prevTreeReader = new BufferedReader(new FileReader(previousTree.getPath()));
            BufferedWriter treeWriter2 = new BufferedWriter(new FileWriter(rootTree.getPath(), true));
            while (prevTreeReader.ready()) {
                String tempTree = prevTreeReader.readLine();

                //this is where the arraylist is used. If the path of the file is in the arraylist, and tree, it was edited
                //and there is no need to copy it from the tree
                boolean containsCopy = false;
                for (int i = 0; i < indexNames.size(); i++) {
                    if(tempTree.contains(indexNames.get(i)))
                        containsCopy = true;
                }
                if (!containsCopy) {
                    //if there is no duplicate, it just copies from previous commit's tree
                    treeWriter2.write(tempTree);
                    treeWriter2.newLine();
                    containsCopy = false;
                }
            }
            prevTreeReader.close();
            treeWriter2.close();
        }
    }

    public void stage(String filePath) throws IOException, NoSuchAlgorithmException {
        createNewBlob(filePath);
    }

    public String commit(String author, String message) throws IOException, NoSuchAlgorithmException {
        createTree();
        return makeCommit(author, message);
    }
}