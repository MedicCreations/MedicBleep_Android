package jp.co.vector.chat.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Group {

    @SerializedName("group_id")
    @Expose
    private String group_id;

    @SerializedName("owner_id")
    @Expose
    private String owner_id;

    @SerializedName("group_name")
    @Expose
    private String group_name;

    @SerializedName("image_name")
    @Expose
    private String image_name;

    @SerializedName("created")
    @Expose
    private String created;

    @SerializedName("modified")
    @Expose
    private String modified;

    public Group() {
    }

    public String getGroupId() {
	return group_id;
    }

    public void setGroupId(String group_id) {
	this.group_id = group_id;
    }

    public String getOwner_id() {
	return owner_id;
    }

    public void setOwner_id(String owner_id) {
	this.owner_id = owner_id;
    }

    public String getGroup_name() {
	return group_name;
    }

    public void setGroup_name(String group_name) {
	this.group_name = group_name;
    }

    public String getCreated() {
	return created;
    }

    public void setCreated(String created) {
	this.created = created;
    }

    public String getModified() {
	return modified;
    }

    public void setModified(String modified) {
	this.modified = modified;
    }

    public String getImage_name() {
	return image_name;
    }

    public void setImage_name(String image_name) {
	this.image_name = image_name;
    }

}
