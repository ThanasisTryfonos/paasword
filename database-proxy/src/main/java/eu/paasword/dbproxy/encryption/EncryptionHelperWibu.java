package eu.paasword.dbproxy.encryption;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;

//import com.wibu.cm.CodeMeter;
import eu.paasword.dbproxy.utils.ConfigParser;
import org.apache.commons.codec.binary.Base64;

/**
 * Encryption Helper für Wibu CmStick
 * Die Ver-/Entschlüsselung wird nicht wie in EncryptionHelperBase.java in Software, 
 * sondern komplett im Wibu CmStick durchgeführt. Wird genutzt, wenn in Adapter.java 
 * HARDWARE_ENC = true gesetzt ist.
 * 
 * @author Tobias Beck
 */
public class EncryptionHelperWibu implements Encryption {

	// CodeMeter CmStick Parameter

	// Werte werden aus der XML Konfigurationsdatei xmlConfig ausgelesen.
	//private String xmlConfig = "config.xml";
	private String yamlConfig = "config.yaml";

//	private CodeMeter.CMCRYPT cmcrypt;
	private long flCtrl;
	// Handle to the used CodeMeter entry
	private long hcmse; // = 0L;
	// The FirmCode that is used by this application
	private long firmCode; // = 10;
	// The ProductCode that is used by this application
	private long productCode; // = 1608;
	// The FeatureCode that is needed to be set for this application
	private long featureCode; // = 1;

	// Während der Verschlüsselung auch nochmal zur Sicherheit entschlüsseln 
	// und testen (m == dec(enc(m))), außerdem Debug-Ausgaben anzeigen
	private final boolean encDebug = false;
	private final boolean decDebug = false;
	private final boolean silent = true;
	private final boolean checkCRC = false; // TODO BUG! Nicht aktivieren!!! GEFAHR!
	// Checksummen Verifikationsfehler bei Ausführung: 
	// CmCrypt() failed with error The checksum verification failed, Error 203.

	public final static int LED_NONE = 0;
	public final static int LED_GREEN = 1;
	public final static int LED_RED = 2;
	public final static int LED_BOTH = (LED_GREEN | LED_RED);

	public EncryptionHelperWibu() {

		// Variablen aus Configdatei einlesen
		getParameterFromConfig();

		accessCmStick(firmCode, productCode); // TODO eigentlich nur bei Programmstart einmalig nötig
		System.out.println("Funktionstest des CmSticks...");
		if (!encDecTest()) { // Funktionstest des CmSticks
			System.exit(0);
		} else {
			System.out.println("OK");
		}
		//System.exit(0);

		// set encryption parameters		
//		cmcrypt = new CodeMeter.CMCRYPT();
//		// for encryption in CM-Stick (hardware encryption): use AES (and calculate CRC)
//
//		if (!checkCRC) {
//			cmcrypt.cmBaseCrypt.ctrl = CodeMeter.CM_CRYPT_AES;
//			System.out.println("cmBaseCrypt.ctrl: " + cmcrypt.cmBaseCrypt.ctrl);
//		} else {
//			cmcrypt.cmBaseCrypt.ctrl = CodeMeter.CM_CRYPT_AES
//					| CodeMeter.CM_CRYPT_CALCCRC;
//			System.out.println("cmBaseCrypt.ctrl (1): " + cmcrypt.cmBaseCrypt.ctrl);
//		}
//		// some options for encryption
//		cmcrypt.cmBaseCrypt.encryptionCodeOptions = CodeMeter.CM_CRYPT_UCIGNORE;
//		// set the FeatureCode
//		cmcrypt.cmBaseCrypt.featureCode = featureCode;

		// Temporäre Festsetzung des Zufalls, da dieser Wert sonst für die Entschlüsselung 
		// irgendwo gespeichert werden müsste. TODO
		//cmcrypt.cmBaseCrypt.encryptionCode = pseudozufall;

		System.out.println("Test der Ver und Entschlüsselungsmethoden...");
		String meinText = "Gehts noch?";
		//System.out.println("Text: " + meinText);		
		String chiffrat = encrypt(meinText);
		//System.out.println("Chiffrat: " + chiffrat);		
		String meinErgebnis = decrypt(chiffrat);
		if (!meinErgebnis.trim().equals(meinText.trim())) {
			System.out.println("Fehler!");
			System.exit(0);
		} else {
			System.out.println("OK");
		}
		
		//System.exit(0);
	}

	/* (non-Javadoc)
	 * @see prototype.Encryption.Encryption#encrypt(java.lang.String)
	 * 
	 * Verschlüsselung eines Strings. Dieser wird zuerst auf 32 Zeichen gepaddet, 
	 * dann im CmStick verschlüsselt und dann Base64 codiert zurück gegeben.
	 */
	@Override
	public String encrypt(String valueToEnc) {
//		System.out.println("WibuEnc...");
//		// Bei der Verschlüsselung soll die rote LED leuchten.
//		setLed(LED_RED);
//
//		if (hcmse == 0) {
//			System.out.println("No handle to CM-Stick available.");
//			System.exit(0);
//		}
//
//		// for encryption: use AES with ECB and automatic key mechanism
//		//flCtrl = CodeMeter.CM_CRYPT_AUTOKEY | CodeMeter.CM_CRYPT_AES_ENC_ECB;
//		// 260 entspricht CodeMeter.CM_CRYPT_AUTOKEY | CodeMeter.CM_CRYPT_AES_ENC_ECB;
//		flCtrl = 260;
//
//		// random encryption code
//		Random rnd = new Random(System.currentTimeMillis());
//		long zufall = rnd.nextLong();
//		cmcrypt.cmBaseCrypt.encryptionCode = zufall;
//
//		if (!silent)
//			System.out.println("Zufall: " + cmcrypt.cmBaseCrypt.encryptionCode);
//
//		// Zufallszahl -> Bytearray
//		ByteArrayOutputStream bos = new ByteArrayOutputStream();
//		DataOutputStream dos = new DataOutputStream(bos);
//		try {
//			dos.writeLong(zufall);
//		} catch (IOException e) {
//			System.out.println("IO Exeption in encrypt!");
//			e.printStackTrace();
//			System.exit(0);
//		}
//		try {
//			dos.flush();
//		} catch (IOException e) {
//			System.out.println("IO Exeption 2 in encrypt!");
//			e.printStackTrace();
//			System.exit(0);
//		}
//		byte[] zahl = bos.toByteArray();
//
//		//System.out.println("Klartext m: " + valueToEnc);
//		// create a padded sequence to encrypt (>32 chars)
//		byte[] abBuffer = String.format("%32s", valueToEnc).getBytes();
//
//		// encrypt sequence
//		int nRet = CodeMeter.cmCrypt(hcmse, flCtrl, cmcrypt, abBuffer);
//		String abBufferStr = new String(abBuffer);
//		if (encDebug)
//			System.out.println("Chiffrat enc(m): " + abBufferStr);
//
//		// Test, ob korrekt entschlüsselt werden kann
//		// byte[] wieder entschlüsseln, bevor es Base64 codiert wurde
//		if (encDebug)
//			System.out.println("Enc flCtrl: " + flCtrl);
//		//decrypt(abBuffer);
//
//		// Vor das Chiffrat noch den Zufall in das Bytearray schreiben
//		byte[] zahlUndText = concat(zahl, abBuffer);
//
//		// TODO im produktiven Betrieb kürzen wenn Geschwindigkeit gefordert ist (Tests entfernen)
//		// Test, ob Base64 Enc/Decodierung funktioniert:
//		// WICHTIG! Base64.* ist nicht threadsave, d.h. jede Funktionsinstanz muss in eigener
//		// Zeile stehen!
//
//		// Base64 representation of byte[]
//		String encodedBase64String = Base64.encodeBase64String(zahlUndText)
//				.toString();
//		if (encDebug)
//			System.out.println("abBuffer Base64 encoded: "
//					+ encodedBase64String);
//		String decodedBase64String = new String(Base64
//				.decodeBase64(encodedBase64String));
//		if (encDebug)
//			System.out.println("abBuffer Base64 decoded: "
//					+ decodedBase64String);
//
//// Test ist nicht mehr so einfach, seit der Zufall noch mit vorne dran steht
////		if (!decodedBase64String.equals(abBufferStr.substring(8, abBufferStr
////				.length()))) {
////			System.out.println(abBufferStr.substring(1, abBufferStr
////				.length()));
////			System.out.println("Fehler bei Base64 Codierung.");
////			System.exit(0);
////		}
//
//		// Test, ob Ergebnis korrekt decodiert werden kann
//		if (encDebug)
//			System.out.println("decrypt(decode(encode(encrypt(m)))): "
//					+ decrypt(encodedBase64String));
//
//		if (0 == nRet) {
//			System.out.println("CmCrypt() failed with error "
//					+ CodeMeter.cmGetLastErrorText());
//			System.exit(0);
//		} else {
//			// LED ausschalten
//			setLed(LED_NONE);
//			return encodedBase64String;
//		}
		return "Fehler!";
	}

	/**
	 * Übergebenen Byte-Buffer auf dem CmStick entschlüsseln
	 * 
	 * @param abBuffer 
	 * 				Bytearray, das vom CmStick entschlüsselt werden soll
	 * @return Klartext
	 */
	private String decrypt(byte[] abBuffer) {
//		System.out.println("WibuDec byte[]...");
//		if (hcmse == 0) {
//			System.out.println("No handle to CM-Stick available.");
//			System.exit(0);
//		}
//		// remove algorithm flag
//		//flCtrl &= ~CodeMeter.CM_CRYPT_AES_ENC_ECB;
//		// add flag for decryption
//		//flCtrl |= CodeMeter.CM_CRYPT_AES_DEC_ECB;
//		// Fest gesetzt, da es sonst zu Problemen kommt, wenn nicht vorher flCtrl durch
//		// Enc auf 260 gesetzt wurde
//		flCtrl = 261;
//
//		cmcrypt.cmBaseCrypt.encryptionCode = 0;
//		// set it again
//		ByteArrayInputStream bis = new ByteArrayInputStream(abBuffer);
//		DataInputStream dis = new DataInputStream(bis);
//		try {
//			// Zufallszahl vom restlichen Text extrahieren
//			long l = dis.readLong();
//			cmcrypt.cmBaseCrypt.encryptionCode = l;
//			System.out.println("Zufall: " + l);
//
//		} catch (IOException e) {
//			System.out.println("IO Exeption 3 in decryption!");
//			e.printStackTrace();
//			System.exit(0);
//		}
//
//		// Bytearray, das nur noch das Chiffrat, ohne den Zufall enthält
//		byte[] cipher = new byte[abBuffer.length - 8];
//
//		System.arraycopy(abBuffer, 8, cipher, 0, abBuffer.length - 8);
//		//System.out.println("Chiffrat: " + new String(cipher));
//
//		if (decDebug)
//			System.out.println("Dec flCtrl: " + flCtrl);
//
//		if (checkCRC) {
//			// remove calculation of CRC
//			cmcrypt.cmBaseCrypt.ctrl &= ~CodeMeter.CM_CRYPT_CALCCRC;
//			System.out.println("cmBaseCrypt.ctrl (2): " + cmcrypt.cmBaseCrypt.ctrl);
//			// add check of CRC
//			cmcrypt.cmBaseCrypt.ctrl |= CodeMeter.CM_CRYPT_CHKCRC;
//			System.out.println("cmBaseCrypt.ctrl (3): " + cmcrypt.cmBaseCrypt.ctrl);
//		}
//		// decrypt sequence
//		int nRet = CodeMeter.cmCrypt(hcmse, flCtrl, cmcrypt, cipher);
//		if (0 == nRet) {
//			System.out.println("Dec: CmCrypt() failed with error "
//					+ CodeMeter.cmGetLastErrorText());
//			System.exit(0);
//		} else {
//			String abBufferStr = new String(cipher);
//			if (decDebug)
//				System.out.println("Klartext dec(enc(m)): " + abBufferStr);
//			return abBufferStr;
//		}
		return "Fehler!";
	}

	/* (non-Javadoc)
	 * @see prototype.Encryption.Encryption#decrypt(java.lang.String)
	 * 
	 * Base64 codierten String decodieren und dann in decrypt(byte[]) wieder entschlüsseln.
	 */
	@Override
	public String decrypt(String encryptedValue) {
		System.out.println("WibuDec Base64...");
		// LED anschalten
		setLed(LED_GREEN);
		if (decDebug)
			System.out.println("Encoded value to decrypt: " + encryptedValue);

		if (hcmse == 0) {
			System.out.println("No handle to CM-Stick available.");
			System.exit(0);
		}

		// decrypt sequence
		byte[] decodedValue;

		String decodedBase64String = new String(Base64
				.decodeBase64(encryptedValue));
		if (decDebug)
			System.out.println("Value to decrypt: " + decodedBase64String);

		decodedValue = Base64.decodeBase64(encryptedValue);
		String returnString = decrypt(decodedValue);

		// LED ausschalten
		setLed(LED_NONE);

		return returnString;
	}

	/**
	 * Test of the CmStick Cryptosystem
	 * 
	 * @return true if testtext = dec(enc(testtext))
	 */
	public boolean encDecTest() {
//		if (hcmse == 0) {
//			System.out.println("No handle to CM-Stick available.");
//			System.exit(0);
//		}
//		// set encryption parameters
//		CodeMeter.CMCRYPT cmcrypt;
//		cmcrypt = new CodeMeter.CMCRYPT();
//		// for encryption in CM-Stick (hardware encryption): use AES, calculate CRC
//		cmcrypt.cmBaseCrypt.ctrl = CodeMeter.CM_CRYPT_AES
//				| CodeMeter.CM_CRYPT_CALCCRC;
//		System.out.println("Enc cmBaseCrypt.ctrl A: " + cmcrypt.cmBaseCrypt.ctrl);
//		// random encryption code
//		Random rnd = new Random(System.currentTimeMillis());
//		long zufall = rnd.nextLong();
//		cmcrypt.cmBaseCrypt.encryptionCode = zufall;
//		// some options for encryption
//		cmcrypt.cmBaseCrypt.encryptionCodeOptions = CodeMeter.CM_CRYPT_UCIGNORE;
//		// set the FeatureCode
//		cmcrypt.cmBaseCrypt.featureCode = 1;
//		// for software encryption: use AES with ECB and automatic key mechanism
//		long flCtrl = CodeMeter.CM_CRYPT_AUTOKEY
//				| CodeMeter.CM_CRYPT_AES_ENC_ECB;
//		// string to encrypt
//		//String strTest = "Mein Text, der ver- und entschlüsselt wird.";
//		//String strTest = "Dieser Text muss min 32 Zeichen lang sein!";
//		String strTest = String.format("%32s", "foo").replace(' ', '*');
//		//System.out.println("Klartext m: " + strTest);
//
//		// create an encrypted sequence [8 Byte randomness + n Byte cipher]
//
//		// Zufallszahl -> Bytearray
//		ByteArrayOutputStream bos = new ByteArrayOutputStream();
//		DataOutputStream dos = new DataOutputStream(bos);
//		try {
//			dos.writeLong(zufall);
//		} catch (IOException e) {
//			System.out.println("IO Exeption in encDecTest!");
//			e.printStackTrace();
//			System.exit(0);
//		}
//		try {
//			dos.flush();
//		} catch (IOException e) {
//			System.out.println("IO Exeption 2 in encDecTest!");
//			e.printStackTrace();
//			System.exit(0);
//		}
//		byte[] zahl = bos.toByteArray();
//		System.out.println("Zufall: " + zufall);
//		System.out.println("Länge des Zufallsbytearrays: " + zahl.length);
//
//		byte[] abBuffer = strTest.getBytes();
//
//		// copy sequence for later check
//		byte[] abBufferSource = (byte[]) abBuffer.clone();
//
//		// encrypt sequence
//		int nRet = CodeMeter.cmCrypt(hcmse, flCtrl, cmcrypt, abBuffer);
//		String abBufferStr = new String(abBuffer);
//		if (!silent) System.out.println("Chiffrat enc(m): " + abBufferStr);
//
//		// Vor das Chiffrat noch den Zufall in das Bytearray schreiben
//		byte[] zahlUndText = concat(zahl, abBuffer);
//
//		if (0 == nRet) {
//			System.out.println("CmCrypt() failed with error "
//					+ CodeMeter.cmGetLastErrorText());
//			System.exit(0);
//		} else {
//			// now let's decrypt
//			// remove algorithm flag
//			flCtrl &= ~CodeMeter.CM_CRYPT_AES_ENC_ECB;
//			// add flag for decryption
//			flCtrl |= CodeMeter.CM_CRYPT_AES_DEC_ECB;
//			// remove calculation of CRC
//			cmcrypt.cmBaseCrypt.ctrl &= ~CodeMeter.CM_CRYPT_CALCCRC;
//			System.out.println("Dec cmBaseCrypt.ctrl B: " + cmcrypt.cmBaseCrypt.ctrl);
//			// add check of CRC
//			cmcrypt.cmBaseCrypt.ctrl |= CodeMeter.CM_CRYPT_CHKCRC;
//			System.out.println("Dec cmBaseCrypt.ctrl C: " + cmcrypt.cmBaseCrypt.ctrl);
//			// reset randomness
//			cmcrypt.cmBaseCrypt.encryptionCode = 0;
//			// set it again
//			ByteArrayInputStream bis = new ByteArrayInputStream(zahlUndText);
//			DataInputStream dis = new DataInputStream(bis);
//			try {
//				// Zufallszahl vom restlichen Text extrahieren
//				long l = dis.readLong();
//				cmcrypt.cmBaseCrypt.encryptionCode = l;
//				System.out.println("Zufall: " + l);
//
//			} catch (IOException e) {
//				System.out.println("IO Exeption 3 in encDecTest!");
//				e.printStackTrace();
//				System.exit(0);
//			}
//
//			// Bytearray, das nur noch das Chiffrat, ohne den Zufall enthält
//			byte[] cipher = new byte[zahlUndText.length - 8];
//
//			System.arraycopy(zahlUndText, 8, cipher, 0, zahlUndText.length - 8);
//			//System.out.println("Chiffrat: " + new String(cipher));
//
//			// decrypt sequence
//			nRet = CodeMeter.cmCrypt(hcmse, flCtrl, cmcrypt, cipher);
//			if (0 == nRet) {
//				System.out.println("CmCrypt() failed with error "
//						+ CodeMeter.cmGetLastErrorText());
//				System.exit(0);
//			} else {
//				// compare decrypted with original
//				if (!arraycompare(cipher, abBufferSource)) {
//					System.out
//							.println("Error: Decrypted string is not same as source string!");
//					return false;
//				} // if
//			} // if
//		} // if
//		abBufferStr = new String(abBuffer);
//		if (!silent) System.out.println("Klartext dec(enc(m)): " + abBufferStr);

		return true;
	}

	/**
	 * Access the CM-Stick using the desired FirmCode and ProductCode. This is
	 * normally done only on start of application, the received handle is stored
	 * in the global variable hcmse. 
	 * 
	 * @param firmCode
	 *          FirmCode to use
	 * @param productCode
	 *          ProductCode to use
	 */
	private void accessCmStick(long firmCode, long productCode) {
//		CodeMeter.CMACCESS cmacc = new CodeMeter.CMACCESS();
//		cmacc.ctrl = CodeMeter.CM_ACCESS_CONVENIENT;
//		cmacc.firmCode = firmCode;
//		cmacc.productCode = productCode;
//		hcmse = CodeMeter.cmAccess(CodeMeter.CM_ACCESS_LOCAL, cmacc);
//
//		if (0 == hcmse) {
//			System.out.println("CmAccess() failed with error "
//					+ CodeMeter.cmGetLastErrorText());
//		}
//
//		// LED Initialisierung
//		setLed(LED_NONE);
	}

	/**
	 * Setzen von bestimmten LEDs des CmSticks
	 * @param ledStatus LED_NONE = 0; LED_GREEN = 1; LED_RED = 2; LED_BOTH = (1 | 2);
	 */
	private void setLed(int ledStatus) {
//		CodeMeter.CMBOXCONTROL hcmBoxCtrl = new CodeMeter.CMBOXCONTROL();
//		CodeMeter.CMPROGRAM_BOXCONTROL hcmPgmBoxCtrl = new CodeMeter.CMPROGRAM_BOXCONTROL();
//		// Now retrieve the the old data from the CmStick.
//		// We only change the LED settings.
//		CodeMeter.cmGetInfo(hcmse, CodeMeter.CM_GEI_BOXCONTROL, hcmBoxCtrl);
//		// Now set the LED information to green.
//		hcmPgmBoxCtrl.ctrl = CodeMeter.CM_BC_ABSOLUTE;
//		hcmPgmBoxCtrl.indicatorFlags = (short) ((hcmBoxCtrl.indicatorFlags & 0x0fffffffc) | ledStatus);
//		hcmPgmBoxCtrl.reserve = 0;
//		CodeMeter.cmProgram(hcmse, CodeMeter.CM_GF_SET_BOXCONTROL,
//				hcmPgmBoxCtrl, null);

	}

	/**
	 * Compares two arrays of bytes
	 * 
	 * @param a
	 *          first byte array
	 * @param b
	 *          second byte array
	 * @return true if arrays equal in every byte, otherwise false
	 */
	private boolean arraycompare(byte[] a, byte[] b) {
		int len = a.length;
		if (len > b.length)
			len = b.length;
		int i;
		for (i = 0; i < len; ++i) {
			if (a[i] != b[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @param first byte array
	 * @param second byte array
	 * @return one big fat byte array
	 */
	public static byte[] concat(byte[] first, byte[] second) {
		byte[] result = Arrays.copyOf(first, first.length + second.length);
		System.arraycopy(second, 0, result, first.length, second.length);
		return result;
	}

	/**
	 * Liest die Configuration für den CmStick aus dem YAML Config-File
	 *
	 * @throws FileNotFoundException
	 */
	private void getParameterFromConfig() {
		Map<String, String> wibuConf = ConfigParser.getInstance(yamlConfig).getLocalDatabase();
		
		hcmse = Long.valueOf(wibuConf.get("hcmse")).longValue();
		firmCode = Long.valueOf(wibuConf.get("firmcode")).longValue(); 
		productCode = Long.valueOf(wibuConf.get("productcode")).longValue(); 
		featureCode = Long.valueOf(wibuConf.get("featurecode")).longValue(); 
	}
}