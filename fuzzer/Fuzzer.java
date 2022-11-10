import java.io.IOException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;


/* Heuristic Mutation: corrupt a good input in a better way */
public class Fuzzer {

    private static final String OUTPUT_FILE = "fuzz.txt";
    
    private static final String CHARACTERS = " !'#$%&'()*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\\]^_`abcdefghijklmnopqrstuvwxyz{|}~";
    private static final String ALPHABETS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String DIGITS = "0123456789";
    
    private static final int MAX_LINE_LENGTH = 1022;

    	
	private static final String input = "push -9\n" +
										"push 0\n" +
										"pop\n" +
										"push 35\n" +
										"+\n" +
										"push 24\n" +
										"-\n" +
										"push -8\n" +
										"*\n" +
										"push 3" +
										"\n" +
										"push 8\n" +
										"/\n" +
										"print\n" +
										"store x\n" +
										"load x\n" +
										"store y\n" +
										"remove x\n" +
										"list\n" +
										"save test.txt";
    
    public static void main(String[] args) throws IOException {
        //System.out.println(Instruction.getBNF());
        FileOutputStream out = null;
        PrintWriter pw = null;
        try {
            out = new FileOutputStream(OUTPUT_FILE);
            pw = new PrintWriter(out);
            //System.out.println(input);
            String output = "";
            
            String[] splitOutput = input.split("\n");
            for (int i = 0; i < splitOutput.length; i++) {
            	String[] splitInstruction = splitOutput[i].split(" ");
            	if (splitInstruction.length != 1) {
            		/* corrupt numbers */
            		if (isNumeric(splitInstruction[splitInstruction.length - 1])) {            			
            			splitInstruction[splitInstruction.length - 1] = randomDigits();
            		/* corrupt alphabets */
            		} else if (isAlphabetic(splitInstruction[splitInstruction.length - 1])) {
            			splitInstruction[splitInstruction.length - 1] = randomAlphabets();
            		/* corrupt filename */
            		} else {
            			splitInstruction[splitInstruction.length - 1] = randomCharacters();
            		}
            	}
            	output += Arrays.toString(splitInstruction) + "\n";
            	
            }
            output = output.replace(",", "").replace("[", "").replace("]", "");
            
            /* shuffle */
            boolean shuffle = flipCoin();
            if (shuffle) {
            	String result;
            	String[] instructions = output.split("\n");
            	List<String> stringList = Arrays.asList(instructions);
            	Collections.shuffle(stringList);
            	stringList.toArray(instructions);
            	output = "";
            	for (String s : stringList) {
            		output += s + "\n";
            	}
            }
            
            //System.out.println(output);
        	pw.println(output);
            
        }catch (Exception e){
            e.printStackTrace(System.err);
            System.exit(1);
        }finally{
            if (pw != null){
                pw.flush();
            }
            if (out != null){
                out.close();
            }
        }

    }
    
    private static boolean flipCoin() {
    	Random r = new Random();
    	if (r.nextBoolean()) {
    		return true;
    	}
    	return false;
    }
    
    /***
     * Generate random number in a range (biased)
     * @param index determine the curve (2: close to zero; 1/2: close to one)
     * @param upper upper bound
     * @return
     */
    private static int randomNumber(int index, int upper) {
    	Random r = new Random();
    	double d;
    	int n;
    	while (true) {
    		d = Math.pow(r.nextDouble(), index);
        	n = (int) Math.round(d * upper);
        	if (n < upper) {
        		break;
        	}
    	}	
    	return n;
    }
    
    private static String randomDigits() {
    	Random r = new Random();
    	/* may generate 1024+ char instruction due to the first argument (command) */
    	int n = randomNumber(4, MAX_LINE_LENGTH);
    	String digits = "";
    	/* negative sign */
    	if (flipCoin()) {
    		digits += "-";
    	}
    	/* pick random digits */
    	for (int i = 0; i < n; i++) {
    		int d = randomNumber(1, DIGITS.length());
    		digits += DIGITS.charAt(d);
    	}
    	return digits;
    }
    
    private static String randomAlphabets() {
    	Random r = new Random();
    	/* may generate 1024+ char instruction due to the first argument (command) */
    	int n = randomNumber(4, MAX_LINE_LENGTH);
    	String alphabets = "";
    	/* pick random alphabets */
    	for (int i = 0; i < n; i++) {
    		int d = randomNumber(1, ALPHABETS.length());
    		alphabets += ALPHABETS.charAt(d);
    	}
    	return alphabets;
    }
    
    private static String randomCharacters() {
    	Random r = new Random();
    	/* may generate 1024+ char instruction due to the first argument (command) */
    	int n = randomNumber(4, MAX_LINE_LENGTH);
    	String characters = "";
    	/* pick random characters */
    	for (int i = 0; i < n; i++) {
    		int d = randomNumber(1, CHARACTERS.length());
    		characters += CHARACTERS.charAt(d);
    	}
    	return characters;
    }
    
    private static boolean isNumeric(String s) {
    	if (s == null || s.length() == 0) {
    		return false;
    	}
    	return s.chars().allMatch(Character::isDigit) || (s.charAt(0) == '-' && s.substring(1).chars().allMatch(Character::isDigit));
    }
    
    private static boolean isAlphabetic(String s) {
    	if (s == null || s.length() == 0) {
    		return false;
    	}
    	return s.chars().allMatch(Character::isAlphabetic);
    }
}
