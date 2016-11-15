
/*
Convert string into char array and compare corresponding characters.

Computational complexity: iteration thru n characters of string results in O(n) space and time.
*/
public class Palindrome {
    public static void main(String [] args){
        String positiveTests [] = {"1", "121",  "1221", ""};
        String negativeTests [] = { "12", "1211", "122", null};
        for(String test : negativeTests){
            if(!isPalindrome(test)) {
                System.out.println("Passed: " + test);
            } else {
                System.out.println("Failed: " + test);
            }
        }

        for(String test: positiveTests){
            if(isPalindrome(test)){
                System.out.println("Passed: " + test);
            } else {
                System.out.println("Failed: " + test);
            }
        }

    }


    public static boolean isPalindrome(String in){
        if(in == null){
            return false;
        }
        char[] test = in.toCharArray();
	//implementation pressuposes that string length is less then Integer.MAX_VALUE/2.
	//types of indecies can be changed accordingly for longer strings.
        int i1 = 0;
        int i2 = test.length - 1;
        while(i1 < i2){
            if(test[i1] != test[i2]){
                return false;
            }
            i1++; i2--;
        }
        return true;
    }
}
