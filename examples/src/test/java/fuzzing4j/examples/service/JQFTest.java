package fuzzing4j.examples.service;

import edu.berkeley.cs.jqf.fuzz.Fuzz;
import edu.berkeley.cs.jqf.fuzz.JQF;
import org.junit.Assume;
import org.junit.runner.RunWith;

/**
 * @author ALi
 * @version 1.0
 * @date 2021-08-11 15:29
 * @description
 */
//@RunWith(JQF.class)
public class JQFTest {
    @Fuzz
    public void m(String x){
        Assume.assumeTrue(x.length()>10);
        System.out.println(x);
    }
}
