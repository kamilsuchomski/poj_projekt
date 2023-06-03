import java.util.Scanner;

public class initConnection implements initConnectionI{
    private int port;
    private String addressIP;

    public String getAddressIP(){
        return this.addressIP;
    }

    private void setAddressIP(){
        Scanner in = new Scanner(System.in);
        System.out.println("Domyslny port: " + getPort());
        System.out.println("Wprowadz adres IP serwera czatu: ");
        addressIP = in.nextLine();
        System.out.println("Podany adres: " + addressIP);
    }

    private void setAddressIP(String addressIP){
        this.addressIP = addressIP;
    }

    private void setPort(int port){
        this.port = port;
    }

    @Override
    public int getPort() {
        return this.port;
    }

    public initConnection(){
        setPort(21974);
        setAddressIP();
    }

    public initConnection(String addressIP){
        setPort(21974);
        setAddressIP(addressIP);
    }

    public initConnection(int port){
        setPort(port);
    }
}
