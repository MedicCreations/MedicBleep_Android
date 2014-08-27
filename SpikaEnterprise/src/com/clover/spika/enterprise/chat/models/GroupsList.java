package com.clover.spika.enterprise.chat.models;

import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class GroupsList extends BaseModel {

    @SerializedName("page")
    @Expose
    private int page;

    @SerializedName("total_count")
    @Expose
    private int totalCount;

    @SerializedName("groups")
    @Expose
    private List<Group> groupList;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public List<Group> getGroupList() {
        return groupList == null ? new ArrayList<Group>() : groupList;
    }

    public void setGroupList(List<Group> groupList) {
        this.groupList = groupList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        GroupsList groupModel = (GroupsList) o;

        if (page != groupModel.page) return false;
        if (totalCount != groupModel.totalCount) return false;
        if (groupList != null ? !groupList.equals(groupModel.groupList) : groupModel.groupList != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + page;
        result = 31 * result + totalCount;
        result = 31 * result + (groupList != null ? groupList.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "GroupModel{" +
                "page=" + page +
                ", totalCount=" + totalCount +
                ", groupList=" + groupList +
                "} " + super.toString();
    }
}
