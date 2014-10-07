package com.clover.spika.enterprise.chat.models;

import com.clover.spika.enterprise.chat.extendables.BaseModel;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class RoomsList extends BaseModel {

    @SerializedName("page")
    @Expose
    private int page;

    @SerializedName("total_count")
    @Expose
    private int totalCount;

    @SerializedName("rooms")
    @Expose
    private List<Chat> roomsList;

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

    public List<Chat> getRoomsList() {
        return roomsList == null ? new ArrayList<Chat>() : roomsList;
    }

    public void setGroupList(List<Chat> roomsList) {
        this.roomsList = roomsList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        RoomsList roomsModel = (RoomsList) o;

        if (page != roomsModel.page) return false;
        if (totalCount != roomsModel.totalCount) return false;
        if (roomsList != null ? !roomsList.equals(roomsModel.roomsList) : roomsModel.roomsList != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + page;
        result = 31 * result + totalCount;
        result = 31 * result + (roomsList != null ? roomsList.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "RoomsModel{" +
                "page=" + page +
                ", totalCount=" + totalCount +
                ", roomsList=" + roomsList +
                "} " + super.toString();
    }
}
