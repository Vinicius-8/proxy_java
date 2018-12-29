package despair;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Vinicius
 */
public class Rek implements Runnable{
    private final Socket conexaoclienteservidor;
    private BlackList blacklist;
    private BufferedReader browsertoproxy; //intoserv
    private BufferedWriter proxytobrowser;//outtoserv
    private String logpath;
    
    public Rek(Socket socket, String blacklistPath, String logpath) throws IOException {
        this.conexaoclienteservidor = socket;
        this.blacklist = new BlackList(blacklistPath);
        this.logpath = logpath;
        try{
            this.conexaoclienteservidor.setSoTimeout(5000);
            this.browsertoproxy = new BufferedReader(new InputStreamReader(this.conexaoclienteservidor.getInputStream()));
            this.proxytobrowser = new BufferedWriter(new OutputStreamWriter(this.conexaoclienteservidor.getOutputStream()));
            
        }catch(IOException e){
            System.out.println("E:"+ e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            
            
            
            
            String header;
            header = this.browsertoproxy.readLine();
           
            String method = header.substring(0,header.indexOf(" "));            
            String url = header.substring(header.indexOf(" ")+1);
            url = url.substring(0, url.indexOf(" "));                 

            //----metodo pra verificar se essa porra ta bloqueada
            
            isBlocked(url);
            
            new Log(this.logpath).gravar(this.conexaoclienteservidor.getInetAddress().getHostAddress(), url, header);
            
            if(!url.contains("http")){
                url = "http://"+url;
            }

            
            
            
            if(method.equals("CONNECT")){
               connectRequest(url);
            }else{
                getRequest(url);
            }
            
        }catch(UnknownHostException e){
            
        }catch (SocketTimeoutException e) {
                       
        }catch (SocketException e){
            
        }catch (IOException ex) {
            Logger.getLogger(Rek.class.getName()).log(Level.SEVERE, null, ex);
        }catch(NullPointerException e){
            System.out.println("exc[run]: "+e.getMessage());
        } catch (SiteBloqueado ex) {
            System.err.println(ex.getMessage());
        } catch (Throwable ex) {
            Logger.getLogger(Rek.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public void getRequest(String url){
        //this.proxytobrowser é a conxao com o browser do client
        url = url.substring(0, url.lastIndexOf("/"));
        try{
            URL rUrl = new URL(url);
            HttpURLConnection conexaoWeb = (HttpURLConnection) rUrl.openConnection();
            conexaoWeb.setUseCaches(false);
            conexaoWeb.setDoOutput(true);
            conexaoWeb.setRequestProperty("Content-Type","application/x-www-form-urlencoded");
            conexaoWeb.setRequestProperty("Content-Language", "pt-BR");
            BufferedReader readerProxyServer = new BufferedReader(new InputStreamReader(conexaoWeb.getInputStream()));
            
            String l = "HTTP/1.0 200 OK\n"
                    + "Proxy-agent: ProxyServer/1.0\n"
                    + "\r\n";
            
            this.proxytobrowser.write(l);
            this.proxytobrowser.flush();
            
            DataLink dl = new DataLink(this.conexaoclienteservidor.getInputStream(), conexaoWeb.getOutputStream());
            Thread dlh = new Thread(dl);
            dlh.start();
            
            char[] buffer = new char[4096];

            int r;
            do {
                r = readerProxyServer.read(buffer);
                if (r > 0) {
                    System.out.println("R: >><: "+ r);
                    this.proxytobrowser.write(buffer, 0, r);
                    if (conexaoWeb.getInputStream().available() < 1) {
                        this.proxytobrowser.flush();
                    }
                }
            } while (r >= 0);

            if(readerProxyServer != null)
                readerProxyServer.close();
            
                
        } catch (IOException ex) {
            System.out.println("ex[io/getREq]:"+ ex.getMessage());
        }
    }
    
    public void connectRequest(String url){
        url = url.substring(7);
        String up[] = url.split(":");
        int porta = Integer.parseInt(up[1]);
        url = up[0];

        try{
            String a ="";
            do{
                a = this.browsertoproxy.readLine();
            }
            while(a !=null);
            
//            for (int i = 0; i < 5; i++) {
//                this.browsertoproxy.readLine();
//            }
            InetAddress ip_add  = InetAddress.getByName(url);
            Socket conexaoWeb = new Socket(ip_add, porta);
            conexaoWeb.setSoTimeout(5000);
            
            String l = "HTTP/1.0 200 Connection established\r\n"
                    + "Proxy-Agent: ProxyServer/1.0\r\n"
                    + "\r\n";
            
           
            this.proxytobrowser.write(l);
            this.proxytobrowser.flush();
            
         
            DataLink dl = new DataLink(this.conexaoclienteservidor.getInputStream(), conexaoWeb.getOutputStream());
            Thread dlh = new Thread(dl);
            dlh.start();
            
                byte[] buffer = new byte[4096];

                int r;
                do{
                    r = conexaoWeb.getInputStream().read(buffer);
                    
                    if(r > 0){
                        
                        this.conexaoclienteservidor.getOutputStream().write(buffer, 0, r);
                        if(conexaoWeb.getInputStream().available() <1)
                            this.conexaoclienteservidor.getOutputStream().flush();
                        
                    }
                        
                }while(r >= 0);

            
            
            if(conexaoWeb !=null)
                conexaoWeb.close();
            

            if(this.proxytobrowser != null)
                this.proxytobrowser.close();

            
        } catch (SocketTimeoutException e) {
                    
		} catch (IOException ex) {
            Logger.getLogger(Rek.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }

    public void isBlocked(String url) throws UnknownHostException, IOException, SiteBloqueado, Throwable{
        url = url.substring(0,url.lastIndexOf(":"));
        InetAddress ia = InetAddress.getByName(url);
        String ip = ia.getHostName();
        String host = ia.getHostAddress();

        if(verifyBlock(this.blacklist.getHosts(), ip, host)){
            
            if(!vb(this.blacklist.getHosts(),host))//aparentemente essa budega de contains nao funfa
                this.blacklist.getNewHosts().add(host);
            
            if(!vb(this.blacklist.getHosts(),ip))
                this.blacklist.getNewHosts().add(host);
            
            this.blacklist.close();
            this.conexaoclienteservidor.close();
            throw new SiteBloqueado(host, ip);   
        }
        
        
    }
    private boolean verifyBlock(HashSet<String> hs, String i, String h){
        Iterator it = hs.iterator();
        
        while(it.hasNext()){
            String a = (String) it.next();
            if(a.equals(i) || a.equals(h))
                return true;
        }
        return false;
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
}


