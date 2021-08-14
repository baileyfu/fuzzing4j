package fuzzing4j.examples.service;

import com.pholser.junit.quickcheck.generator.GenerationStatus;
import com.pholser.junit.quickcheck.generator.Generator;
import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import fuzzing4j.api.annotation.Fuzz;
import fuzzing4j.api.annotation.Inject;
import fuzzing4j.examples.GlobalResourceConfigurator;
import fuzzing4j.examples.ToolsConfigurator;
import fuzzing4j.examples.util.NumberUtil;
import org.junit.Assert;
import org.junit.Assume;

import java.util.Date;

/**
 * @author ALi
 * @version 1.0
 * @date 2021-07-23 15:29
 * @description
 */
@Fuzz
public class SomeBizServiceFuzz {
    @Inject("applicationContext")
    private String applicationContext;
    @Inject(cfg = ToolsConfigurator.class)
    private NumberUtil numberUtil;
    @Inject(cfg = GlobalResourceConfigurator.class, value="someBizService")
    private SomeBizService someBizService;

    @Fuzz(duration = "PT15s")
    public void fuzzMethod(String paramStr, int paramInt, Date paraDate) throws Exception {
        Assume.assumeTrue(paramStr.length()>1);
        Assume.assumeTrue(numberUtil.check(paramInt));
        String result = someBizService.method(paramStr, paramInt, paraDate);
        Assert.assertEquals("SUCCESS", result);
    }
    public static class AlphabetaGen extends Generator<String>{
        public AlphabetaGen() {
            super(String.class);
        }
        @Override
        public String generate(SourceOfRandomness random, GenerationStatus status) {
            return "ABC";
        }
    }
}
