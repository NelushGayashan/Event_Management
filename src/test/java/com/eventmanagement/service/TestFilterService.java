// src/test/java/com/eventmanagement/service/TestFilterService.java
package com.eventmanagement.service;

public class TestFilterService extends FilterService {
    @Override
    public void enableSoftDeleteFilter() {}
    @Override
    public void disableSoftDeleteFilter() {}
    @Override
    public void withoutSoftDeleteFilter(Runnable operation) { operation.run(); }
}
