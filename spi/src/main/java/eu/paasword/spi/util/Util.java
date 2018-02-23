package eu.paasword.spi.util;

/**
 * Created by smantzouratos on 21/12/2016.
 */
public class Util {

    public static enum Mode {

        ALPHA, ALPHANUMERIC, NUMERIC, SYMBOL
    }

    /**
     *
     * Generates a new Random String for Password
     *
     * @param length
     * @param mode
     *
     * @return A String object
     *
     */
    public static String generateRandomString(int length, Mode mode) {

        StringBuffer buffer = new StringBuffer();
        String characters = "";

        switch (mode) {

            case ALPHA:
                characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
                break;

            case ALPHANUMERIC:
                characters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!@#$";
                break;

            case NUMERIC:
                characters = "1234567890";
                break;

        }

        int charactersLength = characters.length();

        for (int i = 0; i < length; i++) {
            double index = Math.random() * charactersLength;
            buffer.append(characters.charAt((int) index));
        }
        return buffer.toString();
    } // EoM generateRandomPassword

}
