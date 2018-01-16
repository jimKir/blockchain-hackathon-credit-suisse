package com.template.vo;

import net.corda.core.serialization.CordaSerializable;

import java.util.Objects;

/**
 * Created by pai on 15.01.18.
 */

@CordaSerializable
public class WorkStateValue {

    String description;

    public WorkStateValue(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkStateValue that = (WorkStateValue) o;
        return Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(description);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }




}
