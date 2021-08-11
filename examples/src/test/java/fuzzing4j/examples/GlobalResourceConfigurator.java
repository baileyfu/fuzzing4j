package fuzzing4j.examples;

import fuzzing4j.api.Configurator;
import fuzzing4j.examples.service.SomeBizService;

/**
 * @author ALi
 * @version 1.0
 * @date 2021-07-23 15:19
 * @description
 */
public class GlobalResourceConfigurator implements Configurator {
    String someGlobalResourceLikeApplicationContext;
    SomeBizService someBizService;
    @Override
    public void init() throws Exception {
        someGlobalResourceLikeApplicationContext = "ApplicationContext";
        someBizService = new SomeBizService();
    }

    @Override
    public Object lookup() {
        return someGlobalResourceLikeApplicationContext;
    }

    @Override
    public Object lookup(String name) {
        return "applicationContext".equals(name) ? someGlobalResourceLikeApplicationContext :
                "someBizService".equals(name)?someBizService:
                        null;
    }

    @Override
    public void close() throws Exception {
        System.out.println("ApplicationContext is shutting down...");
    }
}
