package com.googlecode.hibernate.audit.exception;

public class PropertyConcurrentModificationException extends ConcurrentModificationException {
    private String className;
    private String classLabel;
    private String propertyName;
    private String propertyLabel;
    private String id;

    public PropertyConcurrentModificationException(String className, String propertyName, String classLabel, String propertyLabel, String id) {
        super("Property " + (propertyLabel != null ? propertyLabel : propertyName) + " in entity " + (classLabel != null ? classLabel : className) + " [id='" + id + "'] is already modified.");
        this.className = className;
        this.propertyName = propertyName;
        this.classLabel = classLabel;
        this.propertyLabel = propertyLabel;
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public String getClassLabel() {
        return classLabel;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public String getPropertyLabel() {
        return propertyLabel;
    }

    public String getId() {
        return id;
    }

}
