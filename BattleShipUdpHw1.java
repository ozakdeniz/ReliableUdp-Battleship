package udpSeymaYaldiz;
import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;

/**
 *
 * @author Ozgur Akdeniz ve Seyma Yald�z
 */
public class BattleShipUdpHw1 {

	static int[][] board = new int[5][5]; // �nce oyun alan�n�n alan�n� tan�mlad�k /////�ABUK B�TS�N D�YE BE�EBE�L�K
											// A�TIK

	
	public static void gameServer() throws IOException {

		int port = 9876;// Servera sabit bir port numaras� atad�k
		try { // olas� bir hata yaratabilicek k�sm� try blo�unun i�ine yaz�yoruz
			DatagramSocket serverSocket = new DatagramSocket(port); // Server i�in socket olu�turduk bu socketi verileri
																	// g�nderip alabilmek i�in olu�turuyoruz

			byte[] receiveData = new byte[1024]; // al�nacak data
			byte[] sendData = new byte[1024]; // g�nderilecek data

			int paketNumaras� = 1;//bir seyin yollarken bunu kullanarak kontrol edicez
			
			
			
			//ilk baglantida kontrole gerek yok
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length); // al�nacak datalar
																								// i�in datagram
																								// packeti
																								// olu�turuyoruz
			System.out.println("Clientin ba�lanmas� bekleniyor " + port);
			System.out.println("\n L�tfen Bekleyiniz ...");
			serverSocket.receive(receivePacket); // al�nacak paketin socketini �p adresini ve portunu e�itliyoruz
			InetAddress IPAddress = receivePacket.getAddress(); // ********VARSAYILAN IP ADRES� LOCALHOSTTUR
			port = receivePacket.getPort();
			clearConsole();
			// Console u temizleyip al�nacak paketin bilgilerini �ekiyoruz getData ile
			receivePacket.getData();
			
			
			
			

			System.out.println("Client'e ba�lan�yor");
			System.out.println("Oyun alan� olu�turuluyor");
			// OYun alan� haz�rlan�yor
			createBoard(board);
			setRandomShip();
			setRandomShip();
			setRandomShip();
			showBoard(board);

			System.out.println("Client oyun alan� olu�turulurken bekleyin");

			sendData = new String(" ").getBytes();

			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port); // g�nderilecek
																										// bilgiler
																										// i�in bir
																										// datagram
																										// paketi
																										// olu�turuyoruz
			serverSocket.send(sendPacket);

			waitYourTurn();

			serverSocket.receive(receivePacket);

			// Oyuna ba�la
			clearConsole();
			showBoard(board);

			BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("�lk vuru�u senin ;)");

			String currentPackageNumber = "";
			
			boolean running = true;
			while (running) { // Oyun buradan ba�l�yor

				
				
				
				// At��� haz�rla
				int[] shot = new int[2]; // �ki at�� verilik at�� arrayi

				System.out.println("X koordinat�n� girin: ");
				int x = new Integer(inFromUser.readLine());

				System.out.println("Y koordinat�n� girin: ");
				int y = new Integer(inFromUser.readLine());
				shot[0] = y; // y koordinat t�r�
				shot[1] = x; // x koordinat t�r�

				System.out.println("Pozisyonunu vurdun " + shot[1] + "," + shot[0]);

				//Guvenli Gonderme
				
				String packageNumberStr = String.valueOf(paketNumaras�);
				DatagramPacket packetNumberForClient = new DatagramPacket (packageNumberStr.getBytes(),packageNumberStr.length(),IPAddress,9855);
				serverSocket.send(packetNumberForClient);
				
				System.out.println("yolla");
				sendData = intsToBytes(shot);
				sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port); // vuru�un bilgileri g�nderiliyor
				serverSocket.send(sendPacket);
				System.out.println("Yollad�m�");
				
				CRC32 packageCRC = new CRC32();
				packageCRC.update(sendData);
				byte [] packageCRCtoBytes = new byte [20];
				packageCRCtoBytes = String.valueOf(packageCRC.getValue()).getBytes();
				
				DatagramPacket packetOfFileChecksum = new DatagramPacket(packageCRCtoBytes, packageCRCtoBytes.length,IPAddress,9855);
				serverSocket.send(packetOfFileChecksum);
				
				byte[] receivedAck = new byte[100];
				DatagramPacket receivedAckPackage = new DatagramPacket(receivedAck,receivedAck.length);
				serverSocket.receive(receivedAckPackage);
				String AckStr = new String(new String(receivedAckPackage.getData(), 0, receivedAckPackage.getLength())).trim();
				serverSocket.setSoTimeout(6000); //6000 ms yi gecerse java.net.SocketTimeoutException olacak.
				
				if(AckStr.equalsIgnoreCase("ack"))
				{
					System.out.println(paketNumaras�+ ". Acknowledgement "  +" Geldi.");
					paketNumaras�++;
				}
				
				//----
				
				
				
				//Guvenli Alma
				byte[] fromServerPackageNumber = new byte[20];
				DatagramPacket receivingPacketNumber = new DatagramPacket(fromServerPackageNumber, fromServerPackageNumber.length); 
				serverSocket.receive(receivingPacketNumber);
				currentPackageNumber = new String(new String(receivingPacketNumber.getData(),0,receivingPacketNumber.getData().length)).trim();
				
				receivePacket = new DatagramPacket(receiveData, receiveData.length); // vuru�un bilgileri al�n�yor
				serverSocket.receive(receivePacket);
				
				packageCRC = new CRC32();
				packageCRC.update(sendData);				
				byte[] fromServerForChecksum = new byte[20];
				DatagramPacket receivingFileChecksum = new DatagramPacket(fromServerForChecksum, fromServerForChecksum.length);
				serverSocket.receive(receivingFileChecksum);
				
				String checksumData = new String(new String(receivingFileChecksum.getData(), 0, receivingFileChecksum.getData().length)).trim();
				long receivedChecksum = Long.parseLong(checksumData);
				
				if(receivedChecksum == packageCRC.getValue())
				{					
				
					System.out.println(currentPackageNumber+".paket alindi uzunlu�u:"+receivePacket.getLength()+" KABUL(sirali)");
					
					String Ack = "ack";
					DatagramPacket AckPacket = new DatagramPacket(Ack.getBytes(), Ack.getBytes().length, IPAddress, 9847);
					serverSocket.send(AckPacket);
					
					
				}else
					System.out.println(currentPackageNumber+".paket alinamadi uzunlu�u:"+receivePacket.getLength()+"RET");
				
				//----
				
				
				

				String hit = new String(receivePacket.getData()); // gelen bilgiler tan�mlan�yor
				if (hit.contains("vurdun")) {
					System.out.print("at�� isabetli!");
				} else if (hit.contains("gameover")) {
					System.out.print("D��man�n son gemisini vurdun!! Oyunu kazand�n ^.^");
					break;
				} /*
					 * else{ System.out.print("olmad�  >.< ");}
					 */

				System.out.println(" ");
				showBoard(board);

				System.out.println("Clientin ate� etmesi bekleniyor...");

				
				//Guvenli alma
				fromServerPackageNumber = new byte[20];
				receivingPacketNumber = new DatagramPacket(fromServerPackageNumber, fromServerPackageNumber.length); 
				serverSocket.receive(receivingPacketNumber);
				currentPackageNumber = new String(new String(receivingPacketNumber.getData(),0,receivingPacketNumber.getData().length)).trim();
				
				serverSocket.receive(receivePacket);	//Aliyor
				
				packageCRC = new CRC32();
				packageCRC.update(sendData);				
				fromServerForChecksum = new byte[20];
				receivingFileChecksum = new DatagramPacket(fromServerForChecksum, fromServerForChecksum.length);
				serverSocket.receive(receivingFileChecksum);
				
				checksumData = new String(new String(receivingFileChecksum.getData(), 0, receivingFileChecksum.getData().length)).trim();
				receivedChecksum = Long.parseLong(checksumData);
				
				if(receivedChecksum == packageCRC.getValue())
				{					
				
					System.out.println(currentPackageNumber+".paket alindi uzunlu�u:"+receivePacket.getLength()+" KABUL(sirali)");
					
					String Ack = "ack";
					DatagramPacket AckPacket = new DatagramPacket(Ack.getBytes(), Ack.getBytes().length, IPAddress, 9847);
					serverSocket.send(AckPacket);
					
					
				}else
					System.out.println(currentPackageNumber+".paket alinamadi uzunlu�u:"+receivePacket.getLength()+"RET");
				
				//----
				
				shot = bytesToInts(receivePacket.getData());
				clearConsole();

				System.out.println("Client pozisyonunu vurdu " + shot[1] + "," + shot[0]);

				// Geminin vurulup vurulmad���n� kontrol et
				String doYouHit = shotOnBoard(shot);
				showBoard(board);

				// Vuru� yap�ld���nda oyuncu bilgilendiriliyor
				
				
				//Guvenli Gonderme
				packageNumberStr = String.valueOf(paketNumaras�);
				packetNumberForClient = new DatagramPacket (packageNumberStr.getBytes(),packageNumberStr.length(),IPAddress,9855);
				serverSocket.send(packetNumberForClient);
				
			
				sendData = doYouHit.getBytes();
				sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
				serverSocket.send(sendPacket);

				
				packageCRC = new CRC32();
				packageCRC.update(sendData);
				packageCRCtoBytes = new byte [20];
				packageCRCtoBytes = String.valueOf(packageCRC.getValue()).getBytes();
				
				packetOfFileChecksum = new DatagramPacket(packageCRCtoBytes, packageCRCtoBytes.length,IPAddress,9855);
				serverSocket.send(packetOfFileChecksum);
				
				receivedAck = new byte[100];
				receivedAckPackage = new DatagramPacket(receivedAck,receivedAck.length);
				serverSocket.receive(receivedAckPackage);
				AckStr = new String(new String(receivedAckPackage.getData(), 0, receivedAckPackage.getLength())).trim();
				serverSocket.setSoTimeout(6000); //6000 ms yi gecerse java.net.SocketTimeoutException olacak.
				
				if(AckStr.equalsIgnoreCase("ack"))
				{
					System.out.println(paketNumaras�+ ". Acknowledgement "  +" Geldi.");
					paketNumaras�++;
				}
				
				
				//------
				
				
				
				if (doYouHit.equals("gameover")) {
					System.out.println("Son gemini yok etti! T.T  ");
					break;

				}
				
				
				
				//Guvenli Alma
				
				
				fromServerPackageNumber = new byte[20];
				receivingPacketNumber = new DatagramPacket(fromServerPackageNumber, fromServerPackageNumber.length); 
				serverSocket.receive(receivingPacketNumber);
				currentPackageNumber = new String(new String(receivingPacketNumber.getData(),0,receivingPacketNumber.getData().length)).trim();
				
				serverSocket.receive(receivePacket); // Aliniyor

				packageCRC = new CRC32();
				packageCRC.update(sendData);				
				fromServerForChecksum = new byte[20];
				receivingFileChecksum = new DatagramPacket(fromServerForChecksum, fromServerForChecksum.length);
				serverSocket.receive(receivingFileChecksum);
				
				checksumData = new String(new String(receivingFileChecksum.getData(), 0, receivingFileChecksum.getData().length)).trim();
				receivedChecksum = Long.parseLong(checksumData);
				
				if(receivedChecksum == packageCRC.getValue())
				{					
				
					System.out.println(currentPackageNumber+".paket alindi uzunlu�u:"+receivePacket.getLength()+" KABUL(sirali)");
					
					String Ack = "ack";
					DatagramPacket AckPacket = new DatagramPacket(Ack.getBytes(), Ack.getBytes().length, IPAddress, 9847);
					serverSocket.send(AckPacket);
					
					
				}else
					System.out.println(currentPackageNumber+".paket alinamadi uzunlu�u:"+receivePacket.getLength()+"RET");
				
				
				//-----
				sendPacket = null;
				if (isGameOver())
					running = false;

			}
			System.out.println("  Oyun bitti !!!  ");
			new Integer(inFromUser.readLine());
			serverSocket.close();

		} catch (SocketException ex) {
			Logger.getLogger(BattleShipUdpHw1.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

	public static void gameClient() throws SocketException, IOException {

		clearConsole();

		BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
		// Veri paketini sunucuya g�ndermek i�in kullan�lacak soket.
		DatagramSocket clientSocket = new DatagramSocket(); // ******DATAGRAM SOKET� OLU�TURULDU

		System.out.println(" sunucu ad�n� girin(localhost): ");// "localhost olarak belirledi�imiz i�in ba�ka bi�ey
																// giremiyor
		String server = inFromUser.readLine();

		// Sunucu adresini al

		InetAddress clientIPAddress = InetAddress.getByName(server); // ***** IP adresi kullan�c�n�n girmesini istedik
																		// ama serverda tan�mlad���m�z i�in serverinki
																		// d���nda giremez

		System.out.println(" Sunucu portunu girin (9876): ");// "9876 olarak belirledik
		int port = new Integer(inFromUser.readLine());

		// Sunucu verilerini g�ndermek ve almak i�in kullan�lacak de�i�kenlerin
		// ba�lat�lmas�
		byte[] sendData = new byte[1024];
		byte[] receiveData = new byte[1024];

		sendData = new String("Client ba�land�").getBytes();

		// Sunucuya g�nderilecek paketi haz�rlay�n
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientIPAddress, port); // *******DATAGRAM
																											// PAKET�
																											// OLU�TURULDU
																											// G�NDER�LECEK
																											// PAKET
		clientSocket.send(sendPacket);

		clearConsole();
		System.out.println("Ba�ar�yla ba�land�!");

		System.out.println("Client oyuna ba�lan�rken bekleyin");
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length); // ********* DATAGRAM PAKET�
																							// ALINACAK PAKET KAR�I
																							// OYUNCUDADN GELEN B�LG�LER
		clientSocket.receive(receivePacket); // ****** RECE�VE KOMUTU ALMA KOMUTU

		System.out.println("S�ra sizde!");
		createBoard(board);

		setRandomShip();
		setRandomShip();
		setRandomShip();

		showBoard(board);

		sendData = "alan haz�r".getBytes();
		sendPacket = new DatagramPacket(sendData, sendData.length, clientIPAddress, port);
		clientSocket.send(sendPacket); // *****B�LG�LER� G�NDER�YORUZ

		boolean running = true;
		// oyunu ba�lat
		clearConsole();

		showBoard(board); // *****OYUN ALANI OLU�TURULUYOR ARTIK OYUNA BA�L�YACA�IZ
		
		
		int paketNumaras� = 1;//bir seyin yollarken bunu kullanarak kontrol edicez
		String currentPackageNumber = "";
		
		
		while (running) {

			// Vuru�u al
			System.out.println("Server�n vuru� i�in haz�rlanmas�n� bekle");
			
			
			
			
			
			//Guvenli Alma
			
			byte[] fromServerPackageNumber = new byte[20];
			DatagramPacket receivingPacketNumber = new DatagramPacket(fromServerPackageNumber, fromServerPackageNumber.length); 
			clientSocket.receive(receivingPacketNumber);
			currentPackageNumber = new String(new String(receivingPacketNumber.getData(),0,receivingPacketNumber.getData().length)).trim();
			System.out.println("Guvenli Alma:" + currentPackageNumber);
			
			
			receivePacket = new DatagramPacket(receiveData, receiveData.length); // ****** �LK ATI�I SERVER YAPICAK
																					// SERVERIN G�RD��� B�LG�LER�
																					// ALIYORUZ
			clientSocket.receive(receivePacket);
			
			System.out.println("Guvenli Alma2");
			
			CRC32 packageCRC = new CRC32();
			packageCRC.update(receiveData);				
			byte[] fromServerForChecksum = new byte[20];
			DatagramPacket receivingFileChecksum = new DatagramPacket(fromServerForChecksum, fromServerForChecksum.length);
			clientSocket.receive(receivingFileChecksum);
			
			String checksumData = new String(new String(receivingFileChecksum.getData(), 0, receivingFileChecksum.getData().length)).trim();
			
			long receivedChecksum = Long.parseLong(checksumData);
			
			if(receivedChecksum == packageCRC.getValue())
			{					
				
				System.out.println(currentPackageNumber+".paket alindi uzunlu�u:"+receivePacket.getLength()+" KABUL(sirali)");
				
				String Ack = "ack";
				DatagramPacket AckPacket = new DatagramPacket(Ack.getBytes(), Ack.getBytes().length, clientIPAddress, 9847);
				clientSocket.send(AckPacket);
				
				
			}else
				System.out.println(currentPackageNumber+".paket alinamadi uzunlu�u:"+receivePacket.getLength()+"RET");
			
			
			//-----------
			
			
			
			

			int[] shot = new int[2]; // VURU�LAR TANIMLANDI
			shot = bytesToInts(receivePacket.getData());
			clearConsole(); // ****** EKRANA YAZDIRMADAN �NCE CONSOLE U D�ZENL�YORUZ
			System.out.println("Server �u pozisyonu vurdu " + shot[1] + "," + shot[0] + " "); // ****** SERVERIN ATI�
																								// VER�S� ALINDI VE
																								// EKRANA YAZDIRIYORUZ

			// Geminin vurulup vurulmad���n� kontrol et
			String doYouHit = shotOnBoard(shot);
			showBoard(board);

			// Vuru� ba�ar�l�ysa oyuncuyu rapor et
			
			
			
			//Guvenli Gonderme
			
			
			String packageNumberStr = String.valueOf(paketNumaras�);
			DatagramPacket packetNumberForClient = new DatagramPacket (packageNumberStr.getBytes(),packageNumberStr.length(),clientIPAddress,9855);
			clientSocket.send(packetNumberForClient);
			
			
			
			sendData = doYouHit.getBytes(); // ******** SERVERIN VURU�UNDAN SONRA B�Z�M OYUN ALANIMIZDAK� ETK�S�N�
											// G�NDER�YORUZ
			sendPacket = new DatagramPacket(sendData, sendData.length, clientIPAddress, port);
			clientSocket.send(sendPacket);

			
			packageCRC = new CRC32();
			packageCRC.update(sendData);
			byte [] packageCRCtoBytes = new byte [20];
			packageCRCtoBytes = String.valueOf(packageCRC.getValue()).getBytes();
			
			DatagramPacket packetOfFileChecksum = new DatagramPacket(packageCRCtoBytes, packageCRCtoBytes.length,clientIPAddress,9855);
			clientSocket.send(packetOfFileChecksum);
			
			byte[] receivedAck = new byte[100];
			DatagramPacket receivedAckPackage = new DatagramPacket(receivedAck,receivedAck.length);
			clientSocket.receive(receivedAckPackage);
			String AckStr = new String(new String(receivedAckPackage.getData(), 0, receivedAckPackage.getLength())).trim();
			clientSocket.setSoTimeout(6000); //6000 ms yi gecerse java.net.SocketTimeoutException olacak.
			
			if(AckStr.equalsIgnoreCase("ack"))
			{
				System.out.println(paketNumaras�+ ". Acknowledgement "  +" Geldi.");
				paketNumaras�++;
			}
			
			
			//-------
			
			
			
			if (doYouHit.equals("gameover")) { // ***** ATI� �SABETL� VE OYUNU B�T�REN ATI� DE��LSE OYUN DEVAM ED�YOR
				System.out.println(" Son gemini yok etti! T.T ");
				break;
			}

			// at�� pozisyonu
			System.out.println("Vuru�unun x koordinat�n� gir: ");
			int x = new Integer(inFromUser.readLine()); // ******* KULLANICIDAN ALINACAK B�LG�N�N G�R�LMES�N� SA�LAR
														// BUFFERREADER �LE B�RL�KTE

			System.out.println("Vuru�unun y koordinat�n� gir: "); // *****B�Z�M VURU�UMUZ
			int y = new Integer(inFromUser.readLine());
			shot[1] = x;
			shot[0] = y;

			System.out.println("Pozisyonunu vurdun " + shot[1] + "," + shot[0]);

			
			
			//Guvenli GOnderme
			packageNumberStr = String.valueOf(paketNumaras�);
			packetNumberForClient = new DatagramPacket (packageNumberStr.getBytes(),packageNumberStr.length(),clientIPAddress,9855);
			clientSocket.send(packetNumberForClient);
			
			
			
			
			sendData = intsToBytes(shot);
			sendPacket = new DatagramPacket(sendData, sendData.length, clientIPAddress, port); // ******VURU�UMUZ
																								// G�NDER�L�YOR
			clientSocket.send(sendPacket);
			
			packageCRC = new CRC32();
			packageCRC.update(sendData);
			packageCRCtoBytes = new byte [20];
			packageCRCtoBytes = String.valueOf(packageCRC.getValue()).getBytes();
			
			packetOfFileChecksum = new DatagramPacket(packageCRCtoBytes, packageCRCtoBytes.length,clientIPAddress,9855);
			clientSocket.send(packetOfFileChecksum);
			
			receivedAck = new byte[100];
			receivedAckPackage = new DatagramPacket(receivedAck,receivedAck.length);
			clientSocket.receive(receivedAckPackage);
			AckStr = new String(new String(receivedAckPackage.getData(), 0, receivedAckPackage.getLength())).trim();
			clientSocket.setSoTimeout(6000); //6000 ms yi gecerse java.net.SocketTimeoutException olacak.
			
			if(AckStr.equalsIgnoreCase("ack"))
			{
				System.out.println(paketNumaras�+ ". Acknowledgement "  +" Geldi.");
				paketNumaras�++;
			}
			
			
			
			//--------
			

			// Client bekleniyor
			
			//Guvenli Alma
			
			fromServerPackageNumber = new byte[20];
			receivingPacketNumber = new DatagramPacket(fromServerPackageNumber, fromServerPackageNumber.length); 
			clientSocket.receive(receivingPacketNumber);
			currentPackageNumber = new String(new String(receivingPacketNumber.getData(),0,receivingPacketNumber.getData().length)).trim();
			
			
			
			receivePacket = new DatagramPacket(receiveData, receiveData.length); // *** VURU� SONRASI SERVERIN VERD���
																					// TEPK� ALINIR
			clientSocket.receive(receivePacket);
			
			
			
			packageCRC = new CRC32();
			packageCRC.update(sendData);				
			fromServerForChecksum = new byte[20];
			receivingFileChecksum = new DatagramPacket(fromServerForChecksum, fromServerForChecksum.length);
			clientSocket.receive(receivingFileChecksum);
			
			checksumData = new String(new String(receivingFileChecksum.getData(), 0, receivingFileChecksum.getData().length)).trim();
			receivedChecksum = Long.parseLong(checksumData);
			
			if(receivedChecksum == packageCRC.getValue())
			{					
			
				System.out.println(currentPackageNumber+".paket alindi uzunlu�u:"+receivePacket.getLength()+" KABUL(sirali)");
				
				String Ack = "ack";
				DatagramPacket AckPacket = new DatagramPacket(Ack.getBytes(), Ack.getBytes().length, clientIPAddress, 9847);
				clientSocket.send(AckPacket);
				
				
			}else
				System.out.println(currentPackageNumber+".paket alinamadi uzunlu�u:"+receivePacket.getLength()+"RET");
			
			
			//------
			
			String hit = new String(receivePacket.getData());

			if (hit.contains("Vurdun")) {
				System.out.print("at�� isabetli"); // ************VURDUYSAK
			} else if (hit.contains("gameover")) {
				System.out.print("D��man�n son gemisini vurdun! Oyunu kazand�n ^.^"); // ******** OYUN B�T�REN VURU�SA
				break;
			} /*
				 * else{ //*****BUNU DAH G�ZEL OLSUN D�YE EKLED�M AMA UMDU�UM G�B� �ALI�MADI�I
				 * ���N DEVREDI�I BIRAKTIM System.out.print("olmad�  >.< "); }
				 */
			System.out.println(" ");

			showBoard(board);
			
			
			
			//Guvenli GOnderme
			packageNumberStr = String.valueOf(paketNumaras�);
			packetNumberForClient = new DatagramPacket (packageNumberStr.getBytes(),packageNumberStr.length(),clientIPAddress,9855);
			clientSocket.send(packetNumberForClient);
			
			
			clientSocket.send(sendPacket); //// DATALARI YOLLUYO

			
			packageCRC = new CRC32();
			packageCRC.update(sendData);
			packageCRCtoBytes = new byte [20];
			packageCRCtoBytes = String.valueOf(packageCRC.getValue()).getBytes();
			
			packetOfFileChecksum = new DatagramPacket(packageCRCtoBytes, packageCRCtoBytes.length,clientIPAddress,9855);
			clientSocket.send(packetOfFileChecksum);
			
			receivedAck = new byte[100];
			receivedAckPackage = new DatagramPacket(receivedAck,receivedAck.length);
			clientSocket.receive(receivedAckPackage);
			AckStr = new String(new String(receivedAckPackage.getData(), 0, receivedAckPackage.getLength())).trim();
			clientSocket.setSoTimeout(6000); //6000 ms yi gecerse java.net.SocketTimeoutException olacak.
			
			if(AckStr.equalsIgnoreCase("ack"))
			{
				System.out.println(paketNumaras�+ ". Acknowledgement "  +" Geldi.");
				paketNumaras�++;
			}
			
			//-------
			
			
			if (isGameOver())
				running = false; //// OYUN B�TMED�YSE DEVAM
		}
		System.out.println("  Oyun bitti !!!  "); //// B�TT�YSE �LET���M� KES�YOR
		clientSocket.close();
	}

	public static void breakLine() {
		System.out.println(" ");
		System.out.println(
				"-------------------------------------------------------------------------------------------------");
		System.out.println(" ");
	}

	private static void createBoard(int[][] board) {
		for (int row = 0; row < /* board.length */ 5; row++) {
			for (int column = 0; column < /* board.length */5; column++) {
				board[row][column] = -1;
				if (board[row][column] == -1) {
					System.out.print("\t" + "~");
				}
			}
		}
	}

	private static void showBoard(int[][] board) {

		breakLine();

		for (int row = 0; row < 5; row++) {

			for (int column = 0; column < 5; column++) {
				switch (board[row][column]) {
				case -1:
					System.out.print("\t" + " -- ");
					break;
				case 0:
					System.out.print("\t" + " X ");
					break;
				case 1:
					System.out.print("\t" + " O ");
					break;
				default:
					break;
				}

			}
			System.out.println();
			System.out.println();
		}
		for (int row = 0; row < 5; row++) {

			for (int column = 0; column < 5; column++) {

				// System.out.println( " "+board[row][column] +" ");

			}
		}
		breakLine();
	}

	private static String shotOnBoard(int[] shot) {
		if (board[shot[0]][shot[1]] == 1) {

			System.out.print("Gemilerinden biri vuruldu!");
			board[shot[0]][shot[1]] = 0;

			System.out.println(" ");
			if (isGameOver()) {
				return "gameover";
			}
			return "vurdu";

		} else {

			System.out.println(" Iska ");
			return "ka�ti";
		}
	}

	private static boolean setNewShip(int coordX, int coordY, int size, boolean isHorizontal) {

		if (!isHorizontal) {
			// gemiyi dikeyde olu�turup oyun alan�n� a�mamas�n� sa�lamak Y dikey
			if ((coordY + size) < /* board.length */5) {
				// Se�ilen pozisyonun uygun olup olmad���n� kontrol edin
				for (int i = 0; i < size; i++) {

					if (board[coordX][coordY + i] != -1) {
						return false;
					}
				}
				for (int i = 0; i < size; i++) {
					board[coordX][coordY + i] = 1; // gemiyi olu�turduk
				}
			} else
				return false;

		} else {

			// gemiyi yatayda olu�turup oyun alan�n� a�mamas�n� sa�lamak X yatay
			if ((coordX + size) < /* board.length */5) {
				// Se�ilen pozisyonun uygun olup olmad���n� kontrol edin
				for (int i = 0; i < size; i++) {
					if (board[coordX + i][coordY] == -1) {
						return false;
					}
				}
				for (int i = 0; i < size; i++) {
					board[coordX + i][coordY] = 1;
				}
			} else
				return false;
		}
		return true;
	}

	private static void setRandomShip() {
		Random randomize = new Random();
		boolean tryAgain = false;
		while (!tryAgain) {

			int x = randomize.nextInt(/* board.length */5);
			int y = randomize.nextInt(/* board.length */5);
			boolean z = randomize.nextBoolean();
			tryAgain = setNewShip(x, y, 2, z); // gemiyi bire tan�mlad���m�z i�in gemiye bir yazd�k
		}
	}

	private static boolean isGameOver() {

		for (int i = 0; i < /* board.length */5; i++) {
			for (int j = 0; j < /* board.length */5; j++) {
				if (board[i][j] == 1) {
					return false;
				}
			}
		}
		return true;
	}

	public static byte[] intsToBytes(int[] ints) {

		ByteBuffer bb = ByteBuffer.allocate(ints.length * 4);
		IntBuffer ib = bb.asIntBuffer();
		for (int i : ints)
			ib.put(i);
		return bb.array();
	}

	public static int[] bytesToInts(byte[] bytes) {

		int[] ints = new int[bytes.length / 4];
		ByteBuffer.wrap(bytes).asIntBuffer().get(ints);
		return ints;
	}

	private static void waitYourTurn() {

		System.out.println("L�tfen rakibin hareketini bekleyin ...");
		System.out.println(" ");
	}

	private static void clearConsole() { // iki el aras�ndaki bo�lu�u console u d�zenlemeye yarar

		String n = "\r\n";
		for (int i = 0; i < 10; i++) {
			n = n + "\r\n";
		}
		System.out.println(n);
	}
}