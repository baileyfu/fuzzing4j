package fuzzing4j.api;

/**
 * @author ALi
 * @version 1.0
 * @date 2021-07-23 14:58
 * @description 全局资源配置器
 */
public interface Configurator {
    public void init() throws Exception;

    default public Object lookup() {
        return null;
    }

    public Object lookup(String name);

    public void close() throws Exception;
}
