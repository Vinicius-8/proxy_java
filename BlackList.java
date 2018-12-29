package despair;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;


/**
 *
 * @author Vinicius
 */
public class BlackList {
    private final String hostsPath;
    private HashSet<String> hosts;
    private ArrayList<String> newHosts;

    public BlackList(String hostsPath) throws IOException {
        this.hostsPath = hostsPath;
        
        this.hosts = new HashSet<>();
        this.newHosts =  new ArrayList<>();
        loadFiles();
    }
    
    private void loadFiles() throws FileNotFoundException, IOException{
        //hosts
        String line;
        File file = new File(this.hostsPath);

        BufferedReader in = new BufferedReader(new FileReader(file.getPath()));
        
        line = in.readLine();

        while (line != null) {
           this.hosts.add(line);
            line = in.readLine();
        }
        in.close();

    }
    
    public void verify(ArrayList<String> ar) throws UnknownHostException{//verifica e bloqueia
        try {
            for (int i = 0; i < ar.size(); i++) {
                String host_ip = ar.get(i);

                    //ip
                    InetAddress ia = InetAddress.getByName(host_ip);
                    String host = ia.getHostName();
                    String ip = ia.getHostAddress();
                    
                    if(!vb(this.hosts, host))
                        this.newHosts.add(host);
                    if(!vb(this.hosts,ip))
                        this.newHosts.add(ip);
                    
            }
        }catch(IOException e){
            System.out.println(e.getCause());
        }
    }
    private boolean vb(HashSet<String> hs, String h){
        Iterator i = hs.iterator();
        
        while(i.hasNext()){
            String a = (String) i.next();
            if(a.equals(h))
                return true;
        }
        
        return false;
    }
    public void close() throws IOException{
        //host
        File file = new File(this.hostsPath);
        file.createNewFile();
        PrintWriter escrever = new PrintWriter(new FileWriter(file, true));
        
        
        for (int i = 0; i < this.newHosts.size(); i++) {
            escrever.println(this.newHosts.get(i));
        }
        
       escrever.close();
    }
    public void show(){
        Iterator i = this.hosts.iterator();
        
        while(i.hasNext()){
            System.out.println("[host]: "+ i.next());
        }
    }

    public HashSet<String> getHosts() {
        return hosts;
    }


    public ArrayList<String> getNewHosts() {
        return newHosts;
    }


    
    
      
}
