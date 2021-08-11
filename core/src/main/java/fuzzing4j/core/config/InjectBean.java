package fuzzing4j.core.config;

import fuzzing4j.api.Configurator;

/**
 * @author ALi
 * @version 1.0
 * @date 2021-07-23 15:06
 * @description
 */
public class InjectBean {
    private String fullClassName;
    private String fieldName;
    private Class fieldClazz;
    private Class<? extends Configurator> cfgClazz;
    private String name;

    public String getFullClassName() {
        return fullClassName;
    }

    public void setFullClassName(String fullClassName) {
        this.fullClassName = fullClassName;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Class getFieldClazz() {
        return fieldClazz;
    }

    public void setFieldClazz(Class fieldClazz) {
        this.fieldClazz = fieldClazz;
    }

    public Class<? extends Configurator> getCfgClazz() {
        return cfgClazz;
    }

    public void setCfgClazz(Class<? extends Configurator> cfgClazz) {
        this.cfgClazz = cfgClazz;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
