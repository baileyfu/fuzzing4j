package fuzzing4j.core.config;

/**
 * @author ALi
 * @version 1.0
 * @date 2021-07-23 15:06
 * @description
 */
public class FuzzBean {
    private String classFullName;
    private String method;
    private int times;
    private String duration;

    public String getClassFullName() {
        return classFullName;
    }

    public void setClassFullName(String classFullName) {
        this.classFullName = classFullName;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public int getTimes() {
        return times;
    }

    public void setTimes(int times) {
        this.times = times;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
