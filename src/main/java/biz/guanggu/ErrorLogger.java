package biz.guanggu;

import java.io.*;
import java.util.Date;

/**
 * Created by Li He on 2014/6/13.
 * @author Li He
 */
public class ErrorLogger {

    private PrintWriter writer;


    public void startDeploy(){
        try{
            writer = new PrintWriter(new OutputStreamWriter(new FileOutputStream(new File(ErrorLogger.class.getResource("/run.log").toURI()))));

            writer.println("=========================================================");
            writer.println("New Redeploy Start to work At "+new Date().toString());
            writer.println("=========================================================");
            writer.flush();

        }catch (Exception exception){
            exception.printStackTrace();
        }
    }
    public void log(String message) {
        if (writer!=null)
            writer.println(message);
    }

    public void error(String message){
        if (writer!=null)
        {
            writer.println("\nError Occurs!!!");
            writer.println(message);
            writer.println();
            writer.flush();
        }
    }

    public void endDeploy(){
        if (writer!=null)
        {
            writer.println("=========================== Deploy End ============================\n\n");
            writer.flush();
            writer.close();
        }
    }

    public static void main(String[] args){

    }

}
