package org.vrp.utils.Models;

import org.vrp.utils.meta.FieldMap;
import org.vrp.utils.meta.RjsonArray;

import java.util.List;

public class Model1Array {
    @RjsonArray(type="org.vrp.utils.Models.Model1")
    @FieldMap(fieldname = "")
    private List<Model1> model1Array;

    public List<Model1> getModel1Array() {
        return model1Array;
    }

    public void setModel1Array(List<Model1> model1Array) {
        this.model1Array = model1Array;
    }
}
