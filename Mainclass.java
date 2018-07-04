package udpSeymaYaldiz;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import udpSeymaYaldiz.BattleShipUdpHw1;
import static udpSeymaYaldiz.BattleShipUdpHw1.gameClient;
import static udpSeymaYaldiz.BattleShipUdpHw1.gameServer;
import java.io.IOException;

/**
 *
 * @author Ozgur and Seyma
 */
public class Mainclass {

    public static void main(String[] args) throws Exception {
                try{

                             BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
                             System.out.println("server icin 1 e basiniz, client icin 2 e basiniz...");

                             int selection = Integer.parseInt(input.readLine()); //input kullan�c�dan al�nan girdiyi selectiona e�itledik

                             if(selection == 2) {  // 2 girerse cliente y�nleniyor
                                 gameClient();
                             }
                             if(selection == 1){  // 1 girerse servera y�nleniyor
                                 gameServer();
                             }
                }catch(IOException e){
                             System.out.println(" Hatal� se�enek girdiniz! "); 
                 }    
        }
}
