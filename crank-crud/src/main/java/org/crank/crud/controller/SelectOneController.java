package org.crank.crud.controller;

import org.apache.log4j.Logger;
import static org.crank.core.LogUtils.debug;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.io.Serializable;

public abstract class SelectOneController<T extends Serializable, PK extends Serializable> implements Selectable {

    /**
     * Used to paginate over the listing and allow the user to select.
     */
    private FilterablePageable paginator;
    /**
     * Should the selection listing show.
     */
    private boolean show;
    /**
     * Target property.
     */
    protected String targetPropertyName;
    protected Class entityClass;
    protected String idProperty = "id";
    protected String labelProperty = "name";
    protected String sourcePropertyName = null;
    protected SelectSupport selectSupport = new SelectSupport(this);
    protected CrudControllerBase<T, PK> controller;
    protected Logger logger = Logger.getLogger(SelectOneController.class);
    protected Object parentEntity = null;


    @SuppressWarnings("unchecked")
    public SelectOneController(Class entityClass, String propertyName, FilterablePageable pageable, CrudOperations crudController) {
        debug(logger, "JsfSelectOneListingController(entityClass=%s, propertyName=%s, pageable=%s, crudController=%s)", entityClass, propertyName, pageable, crudController);
        this.paginator = pageable;
        controller = (CrudControllerBase<T, PK>) crudController;
        this.targetPropertyName = propertyName;
        this.entityClass = entityClass;
    }

    public SelectOneController(Class entityClass, String propertyName, FilterablePageable pageable, CrudOperations crudController, String sourceProperty) {
        this(entityClass, propertyName, pageable, crudController);
        this.sourcePropertyName = sourceProperty;
        debug(logger, "sourcePropertyName=%s", sourceProperty);
    }

    public SelectOneController(Class entityClass, FilterablePageable pageable) {
        this(entityClass, null, null, pageable);
    }


    public SelectOneController(Class entityClass, Object parentEntity, String controllerProperty, FilterablePageable pageable) {
        debug(logger, "JsfSelectOneListingController(entityClass=%s, parentEntity=%s, controllerProperty=%s, pageable=%s)", entityClass, parentEntity, controllerProperty, pageable);
        this.entityClass = entityClass;
        this.paginator = pageable;
        this.parentEntity = parentEntity;
        this.targetPropertyName = controllerProperty;
    }


    public FilterablePageable getPaginator() {
        return paginator;
    }

    public void setPaginator(FilterablePageable paginator) {
        this.paginator = paginator;
    }

    public abstract Row getSelectedRow();

    public void select() {
        Row selectedRow = getSelectedRow();
        Object valueBean = selectedRow.getObject();
        debug(logger, "select(): selectedRow=%s, valueBean=%s", selectedRow, valueBean);

        /* If no source property is found than the value property is set to the selected object. */
        Object valueProperty = valueBean;

        /* If the sourceProperty is found,
           * then get the current value of the source property from the selected row. */
        if ((sourcePropertyName != null) && !"".equals(sourcePropertyName)) {
            debug(logger, "select() found source property: sourcePropertyName=%s", sourcePropertyName);
            BeanWrapper valueWrapper = new BeanWrapperImpl(valueBean);
            valueProperty = valueWrapper.getPropertyValue(this.sourcePropertyName);
            debug(logger, "select(): this value was selected from sourcePropertyName : valueProperty = %s", valueProperty);
        }
        BeanWrapper wrappedParentEntity = extractWrappedParent();

        /* If we found a target object, then use the target property to set the new value. */
        if (wrappedParentEntity != null) {
            logger.debug("Setting the property value in the 'parent' object.");
            wrappedParentEntity.setPropertyValue(this.targetPropertyName, valueProperty);
        } else {
            logger.error("Unable to find wrapped parent object. This controller is misconfigured");
        }

        selectSupport.fireSelect(valueProperty);
        this.show = false;
        prepareUI();
    }

    private BeanWrapper extractWrappedParent() {
        /* Try to find the target property. */
        BeanWrapper wrappedParentEntity = null;
        /* If the parentEnity is not equal to null then it is the object that owns the target property. */
        if (this.parentEntity != null) {
            logger.debug("Setting property based on parentEntity");
            wrappedParentEntity = new BeanWrapperImpl(this.parentEntity);
        } else if (controller != null) {
            /* If the parentEntity was null then try to the controller as the target object. */
            logger.debug("Setting property based on controller");
            wrappedParentEntity = new BeanWrapperImpl(controller.getEntity());
        } else {
            logger.error("Either the parentEntity or controller must be set. This bean is misconfigured.");
        }
        return wrappedParentEntity;
    }

    public void unselect() {
        BeanWrapper wrappedParentEntity = extractWrappedParent();

        /* If we found a target object, then use the target property to set the new value. */
        if (wrappedParentEntity != null) {
            logger.debug("Setting the property value in the 'parent' object.");
            wrappedParentEntity.setPropertyValue(this.targetPropertyName, null);
        } else {
            logger.error("Unable to find wrapped parent object. This controller is misconfigured");
        }
        this.show = false;
        prepareUI();

    }

    public abstract void prepareUI();

    public void cancel() {
        this.show = false;
    }

    public void showSelection() {
        this.show = true;
    }


    public boolean isShow() {
        return show;
    }

    public void setShow(boolean show) {
        this.show = show;
    }

    public CrudControllerBase<T, PK> getController() {
        return controller;
    }

    public void setController(CrudControllerBase<T, PK> controller) {
        this.controller = controller;
    }


    public Class getEntityClass() {
        return entityClass;
    }


    public void setEntityClass(Class entityClass) {
        this.entityClass = entityClass;
    }

    public String getPropertyName() {
        return targetPropertyName;
    }


    public void setPropertyName(String propertyName) {
        this.targetPropertyName = propertyName;
    }


    public String getIdProperty() {
        return idProperty;
    }


    public void setIdProperty(String idProperty) {
        this.idProperty = idProperty;
    }


    public String getLabelProperty() {
        return labelProperty;
    }


    public void setLabelProperty(String labelProperty) {
        this.labelProperty = labelProperty;
    }

    public void addSelectListener(SelectListener listener) {
        selectSupport.addSelectListener(listener);
    }


    public void removeSelectListener(SelectListener listener) {
        selectSupport.removeSelectListener(listener);
    }



}