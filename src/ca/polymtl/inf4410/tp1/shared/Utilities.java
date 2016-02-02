package ca.polymtl.inf4410.tp1.shared;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.io.File;
import java.io.FileInputStream;
import java.security.NoSuchAlgorithmException;


public class Utilities {
    public static String getChecksumFromFile(String filepath){
       File file = new File(filepath);
       String checksum = "-1";
       if (file.exists()) {
           try {
               //Based on: http://bit.ly/1nZQixR

               //Use MD5 algorithm
               MessageDigest digest = MessageDigest.getInstance("MD5");

               //Get file input stream for reading the file content
               FileInputStream fis = new FileInputStream(file);

               //Create byte array to read data in chunks
               byte[] byteArray = new byte[1024];
               int bytesCount = 0;

               //Read file data and update in message digest
               while ((bytesCount = fis.read(byteArray)) != -1) {
                   digest.update(byteArray, 0, bytesCount);
               };

               //close the stream; We don't need it now.
               fis.close();

               checksum = getChecksumFromBytes(digest.digest());
           } catch (NoSuchAlgorithmException | IOException ex) {
               ex.printStackTrace();
           }

       }
       
       return checksum;
    }
    
    public static String getChecksumFromBytes(byte[] bytes) {
        if (bytes == null || bytes.length == 0) {
            return "-1";
        }
        
        //This bytes[] has bytes in decimal format;
        //Convert it to hexadecimal format
        StringBuilder sb = new StringBuilder();
        for(int i=0; i< bytes.length ;i++)
        {
            sb.append(Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        
        return sb.toString();
    }
}
