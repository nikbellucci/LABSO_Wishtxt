package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;

public class FileHandler {

    private String path;

    public FileHandler(String path) {
        this.path = path;
    }

    /**
     * It takes a path to a directory, and returns a string containing the names of
     * all the files in
     * that directory
     * 
     * @return A string of all the files in the directory.
     */
    public HashMap<String, String> getFilesName() {
        File curDir = new File(path);
        File[] filesList = curDir.listFiles();
        HashMap<String, String> fileStr = new HashMap<String, String>();

        for (File f : filesList) {
            if (f.isFile()) {
                fileStr.put(f.getName(), new Date(f.lastModified()).toString());

            }
        }
        return fileStr;
    }

    /**
     * The function takes two strings as parameters, the old name of the file and
     * the new name of the
     * file. It then renames the old file to the new file.
     * 
     * @param oldName the name of the file you want to rename
     * @param newName The new name of the file
     * @return The method returns a String.
     */
    public String renameFile(String oldName, String newName) {
        File file = new File(path + File.separator + oldName);
        File renamedFile = new File(path + File.separator + newName);
        boolean rename = file.renameTo(renamedFile); // if true the file is successfully renamed
        if (rename == true)
            return "Rename successful";
        else {
            if (renamedFile.exists())
                return "Filename already exists";
            if (!file.exists())
                return "File dosen't exists";
            else
                return "Internal error";
        }

    }

    /**
     * It deletes a file from the database
     * 
     * @param fileName The name of the file to be deleted.
     */
    public String deleteFile(String fileName) {
        File f = new File(path + File.separator + fileName);
        if (f.delete())
            return "File deleted";
        else {
            if (!f.exists())
                return "File dosen't exist";
            return "internal error";
        }

    }

    /**
     * This function reads a file and returns the contents of the file as a string
     * 
     * @param fileName the name of the file to be read
     * @return The data that is being returned is the data that is being read from
     *         the file.
     */
    public String readFile(String fileName) throws FileNotFoundException{
        
        File obj = new File(path + File.separator + fileName);
        Scanner myReader = new Scanner(obj);
        // if code passes the scanner initialization w/out exceptions, we know that the
        // file exists. We are ready to enter into the critical section

        String data = "";
        while (myReader.hasNextLine()) {
            data = data.concat(myReader.nextLine() + "\n");
        }
        myReader.close();
        return data;
    }

    /**
     * It creates a new file in the directory specified by the path variable
     * 
     * @param fileName The name of the file to create.
     * @return A string
     */
    public String newFile(String fileName) throws IOException {
        File obj = new File(path + File.separator + fileName);
        if (obj.createNewFile()) {
            return "File created: " + fileName;
        } else {
            return "File already exists";
        }
    }

    /**
     * It writes a new string to a file.
     * 
     * @param fileName The name of the file you want to write to.
     * @param input    The string to be written to the file
     */
    public void writeLine(String fileName, String input) throws IOException {
        FileWriter fw = new FileWriter(path + File.separator + fileName + File.separator, true);
        fw.write(input);
        fw.close();
    }

    /**
     * 
     * It reads each charachter backwards until it finds the ASCII value 10 (= new
     * line), at that point it sets a new lenght for the file, truncating the last
     * line.
     * 
     * 
     * @param fileName The name of the file you want to delete the last line from.
     */
    public void backSpace(String fileName) throws IOException {

        RandomAccessFile f = new RandomAccessFile(path + File.separator + fileName, "rw");
        if (f.length() > 0) {
            long length = f.length() - 1;
            byte b;
            do {
                length -= 1;
                if (length == -1) {
                    break;
                }
                f.seek(length);
                b = f.readByte();
            } while (b != 10);
            f.setLength(length + 1);
            f.close();
        }

    }
}
