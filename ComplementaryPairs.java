package ru.anglerhood.ref.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/*
Sort array for O(n log n) and traverse array for O(n) from both sides to the middle while
checking sum of values, if sum is less than K increase it by moving front index otherwise
move rear index in order to reduce it. If sum equals K then we found required pair, thus
we store it in result. Final computational complexity is O(n log n) for time and O(n) for space. 
*/
public class ComplimentaryPairs {

    public static void main(String [] args){
        int[] numbers = new int[]{7, 1, 1, 1, 7, 7 , 5, 6, 9, 18, 3, 11, -1, -3, 13 };
        int K = 8;
        List<int[]> output = getComplimentaryPairs(K, numbers);
        boolean passed = true;
        for(int[] pair : output){
            System.out.println(pair[0] + ":" + pair[1]);
            if(! (K==(pair[0] + pair[1]))){
                passed = false;
            };
        }
        System.out.println("Passed: " + passed);
    }

    public static List<int[]> getComplimentaryPairs(int K, int [] input){
        if (input == null || input.length == 0) {
            return Collections.emptyList();
        }
        Arrays.sort(input);
        List<int[]> result = new ArrayList<>();

        int frontIndex = 0;
        int rearIndex = input.length - 1 ;

        while(frontIndex < rearIndex){
            int sum = input[frontIndex] + input[rearIndex];
            if(sum == K){
                int temp [] = {input[frontIndex], input[rearIndex]};
                result.add(temp);
                frontIndex++;
                rearIndex--;
            } else if (sum > K){
                rearIndex--;
            } else {
                frontIndex++;
            }
        }

        return result;
    }
}
