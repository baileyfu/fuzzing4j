package fuzzing4j.core.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * @author ALi
 * @version 1.0
 * @date 2021-07-23 16:25
 * @description
 */
public class PathUtil {
    public static URL[] strings2Urls(List<String> paths)throws MalformedURLException {
        URL[] urls=new URL[paths.size()];
        for(int i=0;i<paths.size();i++){
            urls[i]=new File(paths.get(i)).toURI().toURL();
        }
        return urls;
    }
}
