package fuzzing4j.examples.util;

/**
 * @author ALi
 * @version 1.0
 * @date 2021-07-23 15:17
 * @description
 */
public class NumberUtil {
    public boolean check(int number){
        return number<100;
    }
    public void shutdown(){
        System.out.println("NumberUtil is shutting down...");
    }
}
