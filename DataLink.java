
package despair;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Vinicius
 */
public class DataLink implements Runnable{
    private InputStream proxyInData;
    private OutputStream proxyOutData;

    public DataLink(InputStream proxyInData, OutputStream proxyOutData) {
        this.proxyInData = proxyInData;
        this.proxyOutData = proxyOutData;
    }
    
    @Override
    public void run() {
        
        try{
            
            byte [] buffer = new byte[4096];
            int r;
            
            do{
                r = this.proxyInData.read(buffer);
                if(r > 0 ){
                    this.proxyOutData.write(buffer, 0, r);
                    if(this.proxyInData.available() < 1)
                        this.proxyOutData.flush();
                }
            }while(r >= 0);
            
        }catch (SocketTimeoutException | SocketException e) {
                    
		}
        catch (IOException ex) {
            Logger.getLogger(DataLink.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
