import org.apache.commons.codec.digest.DigestUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
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
        for (String s : lines) {
            String[] subString = s.split(" ");
            if (subString.length != 3) {
                throw new IllegalArgumentException("Error: check file with HASH sums.");
            }
            String fileName = subString[0];
            String hashSumType = subString[1];
            String hashSum = subString[2];

            if (filesToCheckBuilder.charAt(filesToCheckBuilder.length() - 1) != '/' || filesToCheckBuilder.charAt(filesToCheckBuilder.length() - 1) != '\\') {
                if (System.getProperty("os.name").contains("Windows")) {
                    filesToCheckBuilder.append("\\");
                } else {
                    filesToCheckBuilder.append("/");
                }
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
                    System.out.println(fileName + " ERROR");
            }
        }
    }

    /*
     * @return -1 if file not found, 0 if hashcodes not equal, 1 if hashcodes equal, -2 another errors.
     * */
    private static int checkHashSum(String filePath, String hashSumType, String hashSum) {
        try (InputStream inputStream = Files.newInputStream(Paths.get(filePath))) {
            String fileHash = "";
            switch (hashSumType.toLowerCase()) {
                case "md5":
                    fileHash = DigestUtils.md5Hex(inputStream);
                    break;
                case "sha1":
                    fileHash = DigestUtils.sha1Hex(inputStream);
                    break;
                case "sha256":
                    fileHash = DigestUtils.sha256Hex(inputStream);
                    break;
                default:
                    System.out.println("Unknown HASH type!");
                    return -2;
            }
            return hashSum.equalsIgnoreCase(fileHash) ? 1 : 0;
        } catch (IOException e) {
            if (e instanceof NoSuchFileException) {
                return -1;
            }
            System.out.println(e.getMessage());
            return -2;
        }
    }
}
