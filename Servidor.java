package despair;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Vinicius
 */
public class Servidor{
    private ServerSocket servidorS;

    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        File f = new File("hosts.txt");
        f.createNewFile();
        File g = new File("logs.txt");
        g.createNewFile();
        final String HOSTS = f.getPath();
        final String LOGS = g.getPath();
        
        Scanner s = new Scanner(System.in);
        int porta;
        ArrayList<String> ar = new ArrayList<>(); //array list com os hosts a serem bloqueados
        
        try{

            System.err.println("[Atenção] Google Chrome não suportado, favor testar no mozzila fifefox");
            System.out.println("\t\t-- USO --");
            System.out.println("O primeiro argumento deve ser o número da porta (Obrigatório)");
            System.out.println("Os Demais argumentos são os hosts que deseja bloquear (Opicionais)\n");
            System.out.println("EX: [porta] [   host 1   ] [   host 2  ]");
            System.out.println("EX: 8888 www.facebook.com www.google.com");
            System.out.print("\nDigite a porta: ");
            String in = s.nextLine();
            String etory[] = in.split(" ");
            
            if(etory.length > 1){
                for (int i = 1; i < etory.length; i++) {
                    ar.add(etory[i]);
                }
            }
            
            porta = Integer.parseInt(etory[0]);

            BlackList bl = new BlackList(HOSTS);
            if(!ar.isEmpty())
                bl.verify(ar);
            bl.close();
            
            Servidor se = new Servidor();
            se.a(porta, HOSTS, LOGS);

            
            
        } catch (IOException ex) {
            System.out.println("IOException: "+ ex.getCause());
        }catch(NumberFormatException e){
            System.err.println("[ERROR] Apenas numeros devem ser informados para a porta");
        }
    }
    ///--- passar o log
    public void a(int porta, String bl, String log) {
        
        try {
        
            
            this.servidorS = new ServerSocket(porta);
            System.out.println("[SERVER]Servidor proxy iniciado.");
            System.out.println("[SERVER]["+this.servidorS.getInetAddress().getHostAddress()+"] Aguardando conexao...");
            while(true){
                Socket socket = this.servidorS.accept();
                Thread thread = new Thread(new Rek(socket, bl, log));
                thread.start();
            }
          
        
        } catch (IOException ex) {
            Logger.getLogger(Servidor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
}
