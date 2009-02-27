package com.googlecode.hibernate.audit.exception;

public class ObjectConcurrentModificationException extends ConcurrentModificationException {
    private String className;
    private String classLabel;
    private String id;

    public ObjectConcurrentModificationException(String className, String classLabel, String id) {
        super("Entity " + (classLabel != null ? classLabel : className) + "[id='" + id + "'] is already modified.");
        this.className = className;
        this.classLabel = classLabel;
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public String getClassLabel() {
        return classLabel;
    }

    public String getId() {
        return id;
    }

}
