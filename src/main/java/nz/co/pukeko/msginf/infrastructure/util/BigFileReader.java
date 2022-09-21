package nz.co.pukeko.msginf.infrastructure.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
*  BigFileReader contains two methods to read a file from a file system.
*  Two methods are used to illustrate optimized reading abilities
* 
*  @author  Serguei Eremenko sergeremenko@yahoo.com
*  @version 1.0
*/

public class BigFileReader {
   /**
   *  Default constuctor
   */   
   public BigFileReader(){ }
   /**
   *  Reads a file storing intermediate data into a list. Fast method.
   *  @param file the file to be read
   *  @return a file data
   */
   public byte[] read2list(String file) throws Exception {
      InputStream in = null;
      byte[] buf;
      int bufLen = 20000*1024;
      try{
         in = new BufferedInputStream(new FileInputStream(file));
         buf = new byte[bufLen];
         byte[] tmp;
         int len;
         List<byte[]> data  = new ArrayList<>(24); // keeps pieces of data
         while((len = in.read(buf,0,bufLen)) != -1){
            tmp = new byte[len];
            System.arraycopy(buf,0,tmp,0,len); // still need to do copy 
            data.add(tmp);
         }
         /*
            This part os optional. This method could return a List data
            for further processing, etc.
         */
         len = 0;
         if (data.size() == 1) return data.get(0);
         for (byte[] datum : data) len += datum.length;
         buf = new byte[len]; // final output buffer 
         len = 0;
         for (byte[] datum : data) { // fill with data
            System.arraycopy(datum, 0, buf, len, datum.length);
            len += datum.length;
         } 
      } finally {
         if (in != null) try{ in.close();}catch (Exception e){}
      }
      return buf;  
   }
   /**
   *  Reads a file storing intermediate data into an array.
   *  @param file the file to be read
   *  @return a file data
   */
   public byte[] read2array(String file) throws Exception {
      InputStream in = null;
      byte[] out             = new byte[0]; 
      try{
         in = new BufferedInputStream(new FileInputStream(file));
         // the length of a buffer can vary
         int bufLen = 20000*1024;
         byte[] buf = new byte[bufLen];
         byte[] tmp;
         int len;
         while((len = in.read(buf,0,bufLen)) != -1){
            // extend array
            tmp = new byte[out.length + len];
            // copy data
            System.arraycopy(out,0,tmp,0,out.length);
            System.arraycopy(buf,0,tmp,out.length,len);
            out = tmp;
         }
      }finally{
         // always close the stream 
         if (in != null) try{ in.close();}catch (Exception e){}
      }
      return out;  
   }
   /**
   *  Creates a big file with given name 
   *  @param file the file name
   */
   public void createData(String file) throws Exception {
      BufferedOutputStream os = new BufferedOutputStream(
         new FileOutputStream(file));
      byte[] b = new byte[]{0xC,0xA,0xF,0xE,0xB,0xA,0xB,0xE};
      int    c = 1000000;
      for (int i=0;i<c;i++){
         os.write(b);
         os.flush();
      }
   }

   /**
    *  Creates a big file with given name 
    *  @param file the file name
    *  @param rows the number of rows in the file
    */
   public void createData(String file, int rows) throws Exception {
    BufferedOutputStream os = new BufferedOutputStream(
       new FileOutputStream(file));
    byte[] b = new byte[]{0xC,0xA,0xF,0xE,0xB,0xA,0xB,0xE,0xF,0xC};
    for (int i=0;i<rows;i++){
       os.write(b);
       os.flush();
    }
 }

   /**
   *  Test two different data reading algorithms:
   *  First, it creates a file, then it reads data using read2list method and,
   *  finally, it reads data with read2array method. 
   */
   public static void main(String[] v) throws Exception {
      BigFileReader bfr = new BigFileReader();

      long t = System.currentTimeMillis();
      byte[] b = bfr.read2list("testfile.exe");
      t = System.currentTimeMillis()-t; 
      System.out.println("read "+(b == null ? 0 : b.length)+
         " byte(s) with read2list method in "+t+" ms");

      t = System.currentTimeMillis();
      b = bfr.read2array("testfile.exe");
      t = System.currentTimeMillis()-t; 
      System.out.println("read "+(b == null ? 0 : b.length)+
         " byte(s) with read2array method in "+t+" ms");

   }

}