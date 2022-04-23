package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Scanner;

public class FileHandler {

    String path;


    public FileHandler(String path) {
        this.path = path;
    }

    public String getFilesName() {
        File curDir = new File(path);
        File[] filesList = curDir.listFiles();
        String fileStr ="";
        for(File f : filesList) {
            if (f.isFile()) {
                fileStr += f.getName() + "\n";
            }
        }
        return fileStr.substring(0, fileStr.length() - 1);

    }
    public String renameFile(String oldName, String newName){
        File file = new File(path+"\\" +oldName);
        File renamedFile = new File(path+"\\" +newName);
        boolean rename = file.renameTo(renamedFile); //if true the file is successfully renamed
        if(rename == true)
            return "Rename successful";
        else {
            if (renamedFile.exists()) return "Filename already exists";
            else return "Internal error"; // TODO: there might be other possible reasons for an error occurring, we could need some more specific error messages
        }

    }
    public String deleteFile(String fileName){
        File f = new File(path + "\\" + fileName);
        if(f.delete())
            return "File deleted";
        else{
            if (!f.exists()) return "File dosen't exist";
            return "internal error";
        }

    }
    public String readFile(String fileName){
        try {
            File obj = new File(path+"\\"+fileName);
            Scanner myReader = new Scanner(obj);
            //if code passes the scanner initialization w/out exceptions, we know that the file exists. We are ready to enter into the critical section

            String data = "";
            while (myReader.hasNextLine()) {
                data = data.concat(myReader.nextLine()+"\n");
            }
            myReader.close();
            return data.substring(0, data.length() - 1); //this substring removes the \n that was previously concatenated

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return "File not found";
        }
    }
    public String newFile(String fileName) throws IOException{
        File obj = new File(path + "\\" + fileName);
        if (obj.createNewFile()) {
            return "File created: " + fileName;
        } else {
            return "File already exists";
        }
    }
    public void writeLine(String fileName, String input) throws IOException {
        FileWriter fw = new FileWriter(path+ "\\" + fileName,true);
        fw.write("\n"+input);
        fw.close();
    }

    //TODO: understand this method, i copy-pasted it from th internet
    public void backSpace(String fileName) throws IOException {
        RandomAccessFile f = new RandomAccessFile(path+"\\"+fileName, "rw");
        long length = f.length() - 1;
        byte b;
        do {
            length -= 1;
            f.seek(length);
            b = f.readByte();
        } while(b != 10);
        f.setLength(length+1);
        f.close();
    }
}
