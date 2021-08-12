package fuzzing4j.api;

/**
 * @author ALi
 * @version 1.0
 * @date 2021-07-23 15:00
 * @description
 */
public class NONE implements Configurator{
    @Override
    public void init() throws Exception {

    }

    @Override
    public Object lookup(String name) {
        return null;
    }

    @Override
    public void close() throws Exception {

    }
}
