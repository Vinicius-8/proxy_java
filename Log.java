package despair;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author aluno
 */
public class Log {
    String path;
    //ip_client - time - request - ipDestino
    private String timestamp;

    
    public Log(String path) throws IOException{
        this.path = path;
        
    }


    public void gravar(String ipCliente, String ipDestino, String request) throws IOException{
        this.getTimestamp();
        String log = "["+ipCliente+"]: "+ipDestino+" ["+this.timestamp+"] \""+request+"\"";
        File file = new File(this.path);
        file.createNewFile();
        try {
            PrintWriter escrever = new PrintWriter(new FileWriter(file, true));
            escrever.println(log);
            escrever.close();
        
        
        } catch (IOException ex) {
            Logger.getLogger(Log.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("LOG: "+log);
      
        
    }
   
    public String getTimestamp() {
        Timestamp ts = new Timestamp(new Date().getTime());
        this.timestamp = ts.toString();
        return timestamp;
    }

    
    
    
}
