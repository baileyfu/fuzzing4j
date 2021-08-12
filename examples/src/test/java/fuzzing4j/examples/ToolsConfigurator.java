package fuzzing4j.examples;

import fuzzing4j.api.Configurator;
import fuzzing4j.examples.util.NumberUtil;

/**
 * @author ALi
 * @version 1.0
 * @date 2021-07-23 15:25
 * @description
 */
public class ToolsConfigurator implements Configurator {
    NumberUtil numberUtil;
    @Override
    public void init() throws Exception {
        numberUtil=new NumberUtil();
    }

    @Override
    public Object lookup() {
        return numberUtil;
    }

    @Override
    public Object lookup(String name) {
        return "numberUtil".equals(name) ? numberUtil : null;
    }

    @Override
    public void close() throws Exception {
        numberUtil.shutdown();
    }
}
