package cern.lhc.commons.web.rbac;

import java.util.Objects;

/**
 * Bean representing an RBAC property that is used for user authorization
 */
public class RbacProtectionProperty {

    private final String deviceClass;
    private final String deviceName;
    private final String propertyName;
    private final PropertyOperation operation;

    public RbacProtectionProperty(String deviceClass, String deviceName, String propertyName,
            PropertyOperation operation) {
        this.deviceClass = deviceClass;
        this.deviceName = deviceName;
        this.propertyName = propertyName;
        this.operation = operation;
    }

    public String deviceClass() {
        return deviceClass;
    }

    public String deviceName() {
        return deviceName;
    }

    public String propertyName() {
        return propertyName;
    }

    public PropertyOperation operation() {
        return operation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        RbacProtectionProperty that = (RbacProtectionProperty) o;
        return Objects.equals(deviceClass, that.deviceClass) && Objects.equals(deviceName, that.deviceName) && Objects.equals(propertyName, that.propertyName) && operation == that.operation;
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceClass, deviceName, propertyName, operation);
    }

    @Override
    public String toString() {
        return "RbacProtectionProperty{" + "deviceClass='" + deviceClass + '\'' + ", deviceName='" + deviceName + '\'' + ", propertyName='" + propertyName + '\'' + ", operation=" + operation + '}';
    }

    public enum PropertyOperation {
        SET("set"), GET("get");

        private final String ccdbName;

        PropertyOperation(String ccdbName) {
            this.ccdbName = ccdbName;
        }

        public String ccdbName() {
            return ccdbName;
        }
    }

}
