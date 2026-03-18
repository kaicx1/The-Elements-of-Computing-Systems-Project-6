import java.io.*;
import java.nio.file.*;
import java.util.*;

public class Assembler {
    private static final Map<String, Integer> symbolTable = new HashMap<>();
    private static final Map<String, String> compTable = new HashMap<>();
    private static final Map<String, String> destTable = new HashMap<>();
    private static final Map<String, String> jumpTable = new HashMap<>();
    static {
        symbolTable.put("R0", 0);
        symbolTable.put("R1", 1);
        symbolTable.put("R2", 2);
        symbolTable.put("R3", 3);
        symbolTable.put("R4", 4);
        symbolTable.put("R5", 5);
        symbolTable.put("R6", 6);
        symbolTable.put("R7", 7);
        symbolTable.put("R8", 8);
        symbolTable.put("R9", 9);
        symbolTable.put("R10", 10);
        symbolTable.put("R11", 11);
        symbolTable.put("R12", 12);
        symbolTable.put("R13", 13);
        symbolTable.put("R14", 14);
        symbolTable.put("R15", 15);
        symbolTable.put("SP", 0);
        symbolTable.put("LCL", 1);
        symbolTable.put("ARG", 2);
        symbolTable.put("THIS", 3);
        symbolTable.put("THAT", 4);
        symbolTable.put("SCREEN", 16384);
        symbolTable.put("KBD", 24576);
        compTable.put("0", "0101010");
        compTable.put("1", "0111111");
        compTable.put("-1", "0111010");
        compTable.put("D", "0001100");
        compTable.put("A", "0110000");
        compTable.put("!D", "0001101");
        compTable.put("!A", "0110001");
        compTable.put("-D", "0001111");
        compTable.put("-A", "0110011");
        compTable.put("D+1", "0011111");
        compTable.put("A+1", "0110111");
        compTable.put("D-1", "0001110");
        compTable.put("A-1", "0110010");
        compTable.put("D+A", "0000010");
        compTable.put("D-A", "0010011");
        compTable.put("A-D", "0000111");
        compTable.put("D&A", "0000000");
        compTable.put("D|A", "0010101");
        compTable.put("M", "1110000");
        compTable.put("!M", "1110001");
        compTable.put("-M", "1110011");
        compTable.put("M+1", "1110111");
        compTable.put("M-1", "1110010");
        compTable.put("D+M", "1000010");
        compTable.put("D-M", "1010011");
        compTable.put("M-D", "1000111");
        compTable.put("D&M", "1000000");
        compTable.put("D|M", "1010101");
        destTable.put("", "000");
        destTable.put("M", "001");
        destTable.put("D", "010");
        destTable.put("MD", "011");
        destTable.put("A", "100");
        destTable.put("AM", "101");
        destTable.put("AD", "110");
        destTable.put("AMD", "111");
        jumpTable.put("", "000");
        jumpTable.put("JGT", "001");
        jumpTable.put("JEQ", "010");
        jumpTable.put("JGE", "011");
        jumpTable.put("JLT", "100");
        jumpTable.put("JNE", "101");
        jumpTable.put("JLE", "110");
        jumpTable.put("JMP", "111");
    }

    // clean up text. remove white space and comments
    private static String cleanLine(String line) {
        int comment = line.indexOf("//");
        if (comment >= 0) {
            line = line.substring(0, comment);
        }
        return line.trim();
    }

    // finding label and adding to symbol table
    private static void lInstruction(List<String> lines) {
        int address = 0;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            line = cleanLine(line);
            if (line.isEmpty()) {
                continue;
            }
            if (line.startsWith("(") && line.endsWith(")")) {
                String label = line.replace("(", "").replace(")", "");
                symbolTable.put(label, address);
            } else {
                address++;
            }
        }
    }

    // translate instructions
    private static List<String> acInstruction(List<String> lines) {
        List<String> output = new ArrayList<>();
        int nextAddress = 16;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i);
            line = cleanLine(line);
            // skip empty or l instruction
            if (line.isEmpty() || line.startsWith("(") && line.endsWith(")")) {
                continue;
            }
            // a instruction
            if (line.startsWith("@")) {
                String text = line.substring(1);
                int address;
                boolean isNumb = true;
                // checking if symbol is a number or not
                for (int j = 0; j < text.length(); j++) {
                    if (!Character.isDigit(text.charAt(j))) {
                        isNumb = false;
                    }
                }
                // if text is number address = text
                if (isNumb) {
                    address = Integer.parseInt(text);
                } else {
                    // else find in symbol table
                    if (!symbolTable.containsKey(text)) {
                        symbolTable.put(text, nextAddress);
                        nextAddress++;
                    }
                    address = symbolTable.get(text);
                }
                // convert to binary
                String binaryAddress = Integer.toBinaryString(address);
                while (binaryAddress.length() < 16) {
                    binaryAddress = "0" + binaryAddress;
                }
                output.add(binaryAddress);
            }
            // c instruction
            else {
                String comp;
                String dest = "";
                String jump = "";
                // find destination
                int equal = line.indexOf('=');
                if (equal >= 0) {
                    dest = line.substring(0, equal).trim();
                    line = line.substring(equal + 1).trim();
                }
                // find comp
                int semi = line.indexOf(';');
                if (semi >= 0) {
                    comp = line.substring(0, semi).trim();
                    jump = line.substring(semi + 1).trim();
                } else {
                    comp = line.trim();
                }
                String cBits = compTable.get(comp);
                String dBits = destTable.get(dest);
                String jBits = jumpTable.get(jump);
                output.add("111" + cBits + dBits + jBits);
            }
        }
        return output;
    }

    public static void main(String[] args) throws IOException {
        List<String> lines;
        List<String> binaryLines;
        String input = args[0];
        String output;
        output = input.substring(0, input.length() - 4) + ".hack";
        // run all
        lines = Files.readAllLines(Paths.get(input));
        lInstruction(lines);
        binaryLines = acInstruction(lines);
        Files.write(Paths.get(output), binaryLines);
    }
}