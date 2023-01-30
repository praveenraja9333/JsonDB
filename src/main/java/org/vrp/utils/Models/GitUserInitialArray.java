package org.vrp.utils.Models;

import org.vrp.utils.meta.FieldMap;
import org.vrp.utils.meta.RjsonArray;

import java.util.List;

public class GitUserInitialArray {
    /*
    Initial Array points to the source that starts with Array notation rather than the object notation
    Always should be mapped with the @Rjsonarray Annotation
    and have empty FieldMap
     */
    @RjsonArray(type = "org.vrp.utils.Models.GitUserlogin")
    @FieldMap(fieldname = "")
    private List<GitUserlogin> gitUserList;

    public List<GitUserlogin> getGitUserList() {
        return gitUserList;
    }

    public void setGitUserList(List<GitUserlogin> gitUserList) {
        this.gitUserList = gitUserList;
    }
}
