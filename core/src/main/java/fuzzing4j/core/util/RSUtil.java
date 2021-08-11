package fuzzing4j.core.util;

import fuzzing4j.api.Configurator;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ALi
 * @version 1.0
 * @date 2021-07-23 15:08
 * @description
 */
public class RSUtil {
    static Map<String,Object> resources=new HashMap<>();
    public static void put(Object configurator){
        resources.put(configurator.getClass().getName(),configurator);
    }
    public static <T>T get(String name){
        T t=null;
        Method lookup=null;
        try{
            for(Object configurator:resources.values()){
                lookup=lookup==null?configurator.getClass().getMethod("lookup",String.class):lookup;
                t=(T)lookup.invoke(configurator,name);
                if(t!=null){
                    break;
                }
            }
        }catch(Exception e){
            throw new RuntimeException(e);
        }
        return t;
    }
    public static <T>T get(Class<? extends Configurator> clazz){
        Object configurator=resources.get(clazz.getName());
        if(configurator!=null){
            try{
                return (T)configurator.getClass().getMethod("lookup").invoke(configurator);
            }catch(Exception e){
                throw new RuntimeException(e);
            }
        }
        return null;
    }
    public static <T>T get(Class<? extends Configurator> clazz,String name){
        Object configurator=resources.get(clazz.getName());
        if(configurator!=null){
            try{
                return (T)configurator.getClass().getMethod("lookup",String.class).invoke(configurator,name);
            }catch(Exception e){
                throw new RuntimeException(e);
            }
        }
        return null;
    }
    public static void shutdown() {
        for (Object resource : resources.values()) {
            try {
                Method closeMethod = resource.getClass().getDeclaredMethod("close");
                closeMethod.invoke(resource);
            } catch (Exception e) {
                System.err.println(e);
            }
        }
        resources.clear();
    }
}
