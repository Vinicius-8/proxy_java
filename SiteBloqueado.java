package despair;

/**
 *
 * @author Vinicius
 */
public class SiteBloqueado extends Exception{
    public SiteBloqueado(String h, String i){
        super("Esse site ["+h+" : "+i+"] está bloqueado");
    }
}
