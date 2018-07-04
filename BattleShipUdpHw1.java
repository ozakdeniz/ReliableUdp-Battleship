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
 * @author Ozgur Akdeniz ve Seyma Yaldýz
 */
public class BattleShipUdpHw1 {

	static int[][] board = new int[5][5]; // önce oyun alanýnýn alanýný tanýmladýk /////ÇABUK BÝTSÝN DÝYE BEÞEBEÞLÝK
											// AÇTIK

	
	public static void gameServer() throws IOException {

		int port = 9876;// Servera sabit bir port numarasý atadýk
		try { // olasý bir hata yaratabilicek kýsmý try bloðunun içine yazýyoruz
			DatagramSocket serverSocket = new DatagramSocket(port); // Server için socket oluþturduk bu socketi verileri
																	// gönderip alabilmek için oluþturuyoruz

			byte[] receiveData = new byte[1024]; // alýnacak data
			byte[] sendData = new byte[1024]; // gönderilecek data

			int paketNumarasý = 1;//bir seyin yollarken bunu kullanarak kontrol edicez
			
			
			
			//ilk baglantida kontrole gerek yok
			DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length); // alýnacak datalar
																								// için datagram
																								// packeti
																								// oluþturuyoruz
			System.out.println("Clientin baðlanmasý bekleniyor " + port);
			System.out.println("\n Lütfen Bekleyiniz ...");
			serverSocket.receive(receivePacket); // alýnacak paketin socketini ýp adresini ve portunu eþitliyoruz
			InetAddress IPAddress = receivePacket.getAddress(); // ********VARSAYILAN IP ADRESÝ LOCALHOSTTUR
			port = receivePacket.getPort();
			clearConsole();
			// Console u temizleyip alýnacak paketin bilgilerini çekiyoruz getData ile
			receivePacket.getData();
			
			
			
			

			System.out.println("Client'e baðlanýyor");
			System.out.println("Oyun alaný oluþturuluyor");
			// OYun alaný hazýrlanýyor
			createBoard(board);
			setRandomShip();
			setRandomShip();
			setRandomShip();
			showBoard(board);

			System.out.println("Client oyun alaný oluþturulurken bekleyin");

			sendData = new String(" ").getBytes();

			DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port); // gönderilecek
																										// bilgiler
																										// için bir
																										// datagram
																										// paketi
																										// oluþturuyoruz
			serverSocket.send(sendPacket);

			waitYourTurn();

			serverSocket.receive(receivePacket);

			// Oyuna baþla
			clearConsole();
			showBoard(board);

			BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));
			System.out.println("Ýlk vuruþu senin ;)");

			String currentPackageNumber = "";
			
			boolean running = true;
			while (running) { // Oyun buradan baþlýyor

				
				
				
				// Atýþý hazýrla
				int[] shot = new int[2]; // Ýki atýþ verilik atýþ arrayi

				System.out.println("X koordinatýný girin: ");
				int x = new Integer(inFromUser.readLine());

				System.out.println("Y koordinatýný girin: ");
				int y = new Integer(inFromUser.readLine());
				shot[0] = y; // y koordinat türü
				shot[1] = x; // x koordinat türü

				System.out.println("Pozisyonunu vurdun " + shot[1] + "," + shot[0]);

				//Guvenli Gonderme
				
				String packageNumberStr = String.valueOf(paketNumarasý);
				DatagramPacket packetNumberForClient = new DatagramPacket (packageNumberStr.getBytes(),packageNumberStr.length(),IPAddress,9855);
				serverSocket.send(packetNumberForClient);
				
				System.out.println("yolla");
				sendData = intsToBytes(shot);
				sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port); // vuruþun bilgileri gönderiliyor
				serverSocket.send(sendPacket);
				System.out.println("Yolladýmý");
				
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
					System.out.println(paketNumarasý+ ". Acknowledgement "  +" Geldi.");
					paketNumarasý++;
				}
				
				//----
				
				
				
				//Guvenli Alma
				byte[] fromServerPackageNumber = new byte[20];
				DatagramPacket receivingPacketNumber = new DatagramPacket(fromServerPackageNumber, fromServerPackageNumber.length); 
				serverSocket.receive(receivingPacketNumber);
				currentPackageNumber = new String(new String(receivingPacketNumber.getData(),0,receivingPacketNumber.getData().length)).trim();
				
				receivePacket = new DatagramPacket(receiveData, receiveData.length); // vuruþun bilgileri alýnýyor
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
				
					System.out.println(currentPackageNumber+".paket alindi uzunluðu:"+receivePacket.getLength()+" KABUL(sirali)");
					
					String Ack = "ack";
					DatagramPacket AckPacket = new DatagramPacket(Ack.getBytes(), Ack.getBytes().length, IPAddress, 9847);
					serverSocket.send(AckPacket);
					
					
				}else
					System.out.println(currentPackageNumber+".paket alinamadi uzunluðu:"+receivePacket.getLength()+"RET");
				
				//----
				
				
				

				String hit = new String(receivePacket.getData()); // gelen bilgiler tanýmlanýyor
				if (hit.contains("vurdun")) {
					System.out.print("atýþ isabetli!");
				} else if (hit.contains("gameover")) {
					System.out.print("Düþmanýn son gemisini vurdun!! Oyunu kazandýn ^.^");
					break;
				} /*
					 * else{ System.out.print("olmadý  >.< ");}
					 */

				System.out.println(" ");
				showBoard(board);

				System.out.println("Clientin ateþ etmesi bekleniyor...");

				
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
				
					System.out.println(currentPackageNumber+".paket alindi uzunluðu:"+receivePacket.getLength()+" KABUL(sirali)");
					
					String Ack = "ack";
					DatagramPacket AckPacket = new DatagramPacket(Ack.getBytes(), Ack.getBytes().length, IPAddress, 9847);
					serverSocket.send(AckPacket);
					
					
				}else
					System.out.println(currentPackageNumber+".paket alinamadi uzunluðu:"+receivePacket.getLength()+"RET");
				
				//----
				
				shot = bytesToInts(receivePacket.getData());
				clearConsole();

				System.out.println("Client pozisyonunu vurdu " + shot[1] + "," + shot[0]);

				// Geminin vurulup vurulmadýðýný kontrol et
				String doYouHit = shotOnBoard(shot);
				showBoard(board);

				// Vuruþ yapýldýðýnda oyuncu bilgilendiriliyor
				
				
				//Guvenli Gonderme
				packageNumberStr = String.valueOf(paketNumarasý);
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
					System.out.println(paketNumarasý+ ". Acknowledgement "  +" Geldi.");
					paketNumarasý++;
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
				
					System.out.println(currentPackageNumber+".paket alindi uzunluðu:"+receivePacket.getLength()+" KABUL(sirali)");
					
					String Ack = "ack";
					DatagramPacket AckPacket = new DatagramPacket(Ack.getBytes(), Ack.getBytes().length, IPAddress, 9847);
					serverSocket.send(AckPacket);
					
					
				}else
					System.out.println(currentPackageNumber+".paket alinamadi uzunluðu:"+receivePacket.getLength()+"RET");
				
				
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
		// Veri paketini sunucuya göndermek için kullanýlacak soket.
		DatagramSocket clientSocket = new DatagramSocket(); // ******DATAGRAM SOKETÝ OLUÞTURULDU

		System.out.println(" sunucu adýný girin(localhost): ");// "localhost olarak belirlediðimiz için baþka biþey
																// giremiyor
		String server = inFromUser.readLine();

		// Sunucu adresini al

		InetAddress clientIPAddress = InetAddress.getByName(server); // ***** IP adresi kullanýcýnýn girmesini istedik
																		// ama serverda tanýmladýðýmýz için serverinki
																		// dýþýnda giremez

		System.out.println(" Sunucu portunu girin (9876): ");// "9876 olarak belirledik
		int port = new Integer(inFromUser.readLine());

		// Sunucu verilerini göndermek ve almak için kullanýlacak deðiþkenlerin
		// baþlatýlmasý
		byte[] sendData = new byte[1024];
		byte[] receiveData = new byte[1024];

		sendData = new String("Client baðlandý").getBytes();

		// Sunucuya gönderilecek paketi hazýrlayýn
		DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, clientIPAddress, port); // *******DATAGRAM
																											// PAKETÝ
																											// OLUÞTURULDU
																											// GÖNDERÝLECEK
																											// PAKET
		clientSocket.send(sendPacket);

		clearConsole();
		System.out.println("Baþarýyla baðlandý!");

		System.out.println("Client oyuna baðlanýrken bekleyin");
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length); // ********* DATAGRAM PAKETÝ
																							// ALINACAK PAKET KARÞI
																							// OYUNCUDADN GELEN BÝLGÝLER
		clientSocket.receive(receivePacket); // ****** RECEÝVE KOMUTU ALMA KOMUTU

		System.out.println("Sýra sizde!");
		createBoard(board);

		setRandomShip();
		setRandomShip();
		setRandomShip();

		showBoard(board);

		sendData = "alan hazýr".getBytes();
		sendPacket = new DatagramPacket(sendData, sendData.length, clientIPAddress, port);
		clientSocket.send(sendPacket); // *****BÝLGÝLERÝ GÖNDERÝYORUZ

		boolean running = true;
		// oyunu baþlat
		clearConsole();

		showBoard(board); // *****OYUN ALANI OLUÞTURULUYOR ARTIK OYUNA BAÞLÝYACAÐIZ
		
		
		int paketNumarasý = 1;//bir seyin yollarken bunu kullanarak kontrol edicez
		String currentPackageNumber = "";
		
		
		while (running) {

			// Vuruþu al
			System.out.println("Serverýn vuruþ için hazýrlanmasýný bekle");
			
			
			
			
			
			//Guvenli Alma
			
			byte[] fromServerPackageNumber = new byte[20];
			DatagramPacket receivingPacketNumber = new DatagramPacket(fromServerPackageNumber, fromServerPackageNumber.length); 
			clientSocket.receive(receivingPacketNumber);
			currentPackageNumber = new String(new String(receivingPacketNumber.getData(),0,receivingPacketNumber.getData().length)).trim();
			System.out.println("Guvenli Alma:" + currentPackageNumber);
			
			
			receivePacket = new DatagramPacket(receiveData, receiveData.length); // ****** ÝLK ATIÞI SERVER YAPICAK
																					// SERVERIN GÝRDÝÐÝ BÝLGÝLERÝ
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
				
				System.out.println(currentPackageNumber+".paket alindi uzunluðu:"+receivePacket.getLength()+" KABUL(sirali)");
				
				String Ack = "ack";
				DatagramPacket AckPacket = new DatagramPacket(Ack.getBytes(), Ack.getBytes().length, clientIPAddress, 9847);
				clientSocket.send(AckPacket);
				
				
			}else
				System.out.println(currentPackageNumber+".paket alinamadi uzunluðu:"+receivePacket.getLength()+"RET");
			
			
			//-----------
			
			
			
			

			int[] shot = new int[2]; // VURUÞLAR TANIMLANDI
			shot = bytesToInts(receivePacket.getData());
			clearConsole(); // ****** EKRANA YAZDIRMADAN ÖNCE CONSOLE U DÜZENLÝYORUZ
			System.out.println("Server þu pozisyonu vurdu " + shot[1] + "," + shot[0] + " "); // ****** SERVERIN ATIÞ
																								// VERÝSÝ ALINDI VE
																								// EKRANA YAZDIRIYORUZ

			// Geminin vurulup vurulmadýðýný kontrol et
			String doYouHit = shotOnBoard(shot);
			showBoard(board);

			// Vuruþ baþarýlýysa oyuncuyu rapor et
			
			
			
			//Guvenli Gonderme
			
			
			String packageNumberStr = String.valueOf(paketNumarasý);
			DatagramPacket packetNumberForClient = new DatagramPacket (packageNumberStr.getBytes(),packageNumberStr.length(),clientIPAddress,9855);
			clientSocket.send(packetNumberForClient);
			
			
			
			sendData = doYouHit.getBytes(); // ******** SERVERIN VURUÞUNDAN SONRA BÝZÝM OYUN ALANIMIZDAKÝ ETKÝSÝNÝ
											// GÖNDERÝYORUZ
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
				System.out.println(paketNumarasý+ ". Acknowledgement "  +" Geldi.");
				paketNumarasý++;
			}
			
			
			//-------
			
			
			
			if (doYouHit.equals("gameover")) { // ***** ATIÞ ÝSABETLÝ VE OYUNU BÝTÝREN ATIÞ DEÐÝLSE OYUN DEVAM EDÝYOR
				System.out.println(" Son gemini yok etti! T.T ");
				break;
			}

			// atýþ pozisyonu
			System.out.println("Vuruþunun x koordinatýný gir: ");
			int x = new Integer(inFromUser.readLine()); // ******* KULLANICIDAN ALINACAK BÝLGÝNÝN GÝRÝLMESÝNÝ SAÐLAR
														// BUFFERREADER ÝLE BÝRLÝKTE

			System.out.println("Vuruþunun y koordinatýný gir: "); // *****BÝZÝM VURUÞUMUZ
			int y = new Integer(inFromUser.readLine());
			shot[1] = x;
			shot[0] = y;

			System.out.println("Pozisyonunu vurdun " + shot[1] + "," + shot[0]);

			
			
			//Guvenli GOnderme
			packageNumberStr = String.valueOf(paketNumarasý);
			packetNumberForClient = new DatagramPacket (packageNumberStr.getBytes(),packageNumberStr.length(),clientIPAddress,9855);
			clientSocket.send(packetNumberForClient);
			
			
			
			
			sendData = intsToBytes(shot);
			sendPacket = new DatagramPacket(sendData, sendData.length, clientIPAddress, port); // ******VURUÞUMUZ
																								// GÖNDERÝLÝYOR
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
				System.out.println(paketNumarasý+ ". Acknowledgement "  +" Geldi.");
				paketNumarasý++;
			}
			
			
			
			//--------
			

			// Client bekleniyor
			
			//Guvenli Alma
			
			fromServerPackageNumber = new byte[20];
			receivingPacketNumber = new DatagramPacket(fromServerPackageNumber, fromServerPackageNumber.length); 
			clientSocket.receive(receivingPacketNumber);
			currentPackageNumber = new String(new String(receivingPacketNumber.getData(),0,receivingPacketNumber.getData().length)).trim();
			
			
			
			receivePacket = new DatagramPacket(receiveData, receiveData.length); // *** VURUÞ SONRASI SERVERIN VERDÝÐÝ
																					// TEPKÝ ALINIR
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
			
				System.out.println(currentPackageNumber+".paket alindi uzunluðu:"+receivePacket.getLength()+" KABUL(sirali)");
				
				String Ack = "ack";
				DatagramPacket AckPacket = new DatagramPacket(Ack.getBytes(), Ack.getBytes().length, clientIPAddress, 9847);
				clientSocket.send(AckPacket);
				
				
			}else
				System.out.println(currentPackageNumber+".paket alinamadi uzunluðu:"+receivePacket.getLength()+"RET");
			
			
			//------
			
			String hit = new String(receivePacket.getData());

			if (hit.contains("Vurdun")) {
				System.out.print("atýþ isabetli"); // ************VURDUYSAK
			} else if (hit.contains("gameover")) {
				System.out.print("Düþmanýn son gemisini vurdun! Oyunu kazandýn ^.^"); // ******** OYUN BÝTÝREN VURUÞSA
				break;
			} /*
				 * else{ //*****BUNU DAH GÜZEL OLSUN DÝYE EKLEDÝM AMA UMDUÐUM GÝBÝ ÇALIÞMADIÐI
				 * ÝÇÝN DEVREDIÞI BIRAKTIM System.out.print("olmadý  >.< "); }
				 */
			System.out.println(" ");

			showBoard(board);
			
			
			
			//Guvenli GOnderme
			packageNumberStr = String.valueOf(paketNumarasý);
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
				System.out.println(paketNumarasý+ ". Acknowledgement "  +" Geldi.");
				paketNumarasý++;
			}
			
			//-------
			
			
			if (isGameOver())
				running = false; //// OYUN BÝTMEDÝYSE DEVAM
		}
		System.out.println("  Oyun bitti !!!  "); //// BÝTTÝYSE ÝLETÝÞÝMÝ KESÝYOR
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
			return "kaçti";
		}
	}

	private static boolean setNewShip(int coordX, int coordY, int size, boolean isHorizontal) {

		if (!isHorizontal) {
			// gemiyi dikeyde oluþturup oyun alanýný aþmamasýný saðlamak Y dikey
			if ((coordY + size) < /* board.length */5) {
				// Seçilen pozisyonun uygun olup olmadýðýný kontrol edin
				for (int i = 0; i < size; i++) {

					if (board[coordX][coordY + i] != -1) {
						return false;
					}
				}
				for (int i = 0; i < size; i++) {
					board[coordX][coordY + i] = 1; // gemiyi oluþturduk
				}
			} else
				return false;

		} else {

			// gemiyi yatayda oluþturup oyun alanýný aþmamasýný saðlamak X yatay
			if ((coordX + size) < /* board.length */5) {
				// Seçilen pozisyonun uygun olup olmadýðýný kontrol edin
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
			tryAgain = setNewShip(x, y, 2, z); // gemiyi bire tanýmladýðýmýz için gemiye bir yazdýk
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

		System.out.println("Lütfen rakibin hareketini bekleyin ...");
		System.out.println(" ");
	}

	private static void clearConsole() { // iki el arasýndaki boþluðu console u düzenlemeye yarar

		String n = "\r\n";
		for (int i = 0; i < 10; i++) {
			n = n + "\r\n";
		}
		System.out.println(n);
	}
}