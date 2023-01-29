package org.vrp.utils.Models;

import org.vrp.utils.meta.FieldMap;
import org.vrp.utils.meta.RjsonArray;

import java.util.List;

public class Employees {
    @RjsonArray(type ="org.vrp.utils.Models.SampleArray")
    @FieldMap(fieldname = "Employees")
    public List<SampleArray> emps;

    public List<SampleArray> getEmps() {
        return emps;
    }

    public void setEmps(List<SampleArray> emps) {
        this.emps = emps;
    }
}
