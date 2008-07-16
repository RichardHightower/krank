package org.crank.config.spring.support;

import org.crank.crud.controller.CrudManagedObject;

import java.util.Map;


public interface DeferredResourceCreator {
    void createResource(Map map, CrudManagedObject cmo) throws Exception;
}
