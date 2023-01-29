package org.vrp.utils.Models;

import org.vrp.utils.meta.FieldMap;
import org.vrp.utils.meta.RjsonObject;
import org.vrp.utils.meta.RootElement;

public class GitUserlogin {

    @RootElement
    String id;
    String type;

    String login;
    @FieldMap(fieldname = "actor.id")
    String actorid;
    @RjsonObject
    Repo repo;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getActorid() {
        return actorid;
    }

    public void setActorid(String actorid) {
        this.actorid = actorid;
    }

    public Repo getRepo() {
        return repo;
    }

    public void setRepo(Repo repo) {
        this.repo = repo;
    }


}
