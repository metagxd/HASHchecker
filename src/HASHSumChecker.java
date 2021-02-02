import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class HASHSumChecker {
    public static void main(String[] args) {

        if (args.length != 2) {
            System.out.println("Illegal program arguments!" + "\nUse: java -jar HASHSumChecker.jar <path to the input file>" +
                    " <path to the directory containing the files to check>");
            return;
        }
        String sumFilePath = args[0];
        String filesToCheck = args[1];

        hashComparator(sumFilePath, filesToCheck);
    }

    private static void hashComparator(String sumFilePath, String filesToCheck) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(sumFilePath))) {
            String line = reader.readLine();
            while (line != null) {
                lines.add(line);
                line = reader.readLine();
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        StringBuilder filesToCheckBuilder = new StringBuilder(filesToCheck);
        for (int i = 0, linesSize = lines.size(); i < linesSize; i++) {
            String s = lines.get(i);
            String[] subString = s.split(" ");
            if (subString.length != 3) {
                System.out.println("Error: not enough arguments " + "at line " + i + " " + s);
                continue;
            }

            String fileName = subString[0];
            String hashSumType = subString[1];
            String hashSum = subString[2];

            if ((filesToCheckBuilder.charAt(filesToCheckBuilder.length() - 1) != '/')
                    && (filesToCheckBuilder.charAt(filesToCheckBuilder.length() - 1) != '\\')) {
                filesToCheckBuilder.append(File.separator);
            }

            switch (checkHashSum(filesToCheckBuilder + fileName, hashSumType, hashSum)) {
                case 1:
                    System.out.println(fileName + " OK");
                    break;
                case 0:
                    System.out.println(fileName + " FAIL");
                    break;
                case -1:
                    System.out.println(fileName + " NOT FOUND");
                    break;
                default:
                    System.out.println(fileName + " ERROR at line " + i + " " + s);
            }
        }
    }

    /*
     * @return -1 if file not found, 0 if hashcodes not equal, 1 if hashcodes equal, -2 another errors.
     * */
    private static int checkHashSum(String filePath, String hashSumType, String hashSum) {
        //https://stackoverflow.com/a/304275
        MessageDigest complete = null;
        try (InputStream fis = new FileInputStream(filePath)) {
            byte[] buffer = new byte[1024];
            if (hashSumType.equalsIgnoreCase("sha1")) {
                hashSumType = "SHA-1";
            } else if (hashSumType.equalsIgnoreCase("sha256")) {
                hashSumType = "SHA-256";
            }
            complete = MessageDigest.getInstance(hashSumType.toUpperCase());
            int numRead;
            do {
                numRead = fis.read(buffer);
                if (numRead > 0) {
                    complete.update(buffer, 0, numRead);
                }
            } while (numRead != -1);
        } catch (IOException | NoSuchAlgorithmException e) {
            if (e instanceof FileNotFoundException) {
                return -1;
            } else if (e instanceof NoSuchAlgorithmException) {
                System.out.println("Unknown HASH type: " + hashSumType);
                return -2;
            }
        }
        byte[] digest = complete.digest();
        return byteArrayToString(digest).equalsIgnoreCase(hashSum) ? 1 : 0;
    }

    private static String byteArrayToString(byte[] arr) {
        StringBuilder result = new StringBuilder();
        for (byte b : arr) {
            result.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }
        return result.toString();
    }
}
