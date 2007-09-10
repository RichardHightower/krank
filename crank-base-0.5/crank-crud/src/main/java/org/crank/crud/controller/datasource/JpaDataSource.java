package org.crank.crud.controller.datasource;

import java.io.Serializable;
import java.util.List;

import org.crank.crud.GenericDao;

public class JpaDataSource<T, PK extends Serializable> implements DataSource {
    protected GenericDao<T, PK> dao;

    public void setDao( GenericDao<T, PK> dao ) {
        this.dao = dao;
    }

    public List list() {
        return dao.find(  );
    }
    
    public JpaDataSource() {
        super();
    }

}