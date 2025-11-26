package com.eshiam.lifecycle.model;

import java.util.ArrayList;
import java.util.List;

public class Access {
    private List<String> add = new ArrayList<>();
    private List<String> remove = new ArrayList<>();

    public Access() {
    }

    public List<String> getAdd() {
        return add;
    }

    public void setAdd(List<String> add) {
        this.add = add;
    }

    public List<String> getRemove() {
        return remove;
    }

    public void setRemove(List<String> remove) {
        this.remove = remove;
    }
}
