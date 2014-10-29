package com.clover.spika.enterprise.chat.models;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class User implements Serializable{

	@SerializedName("id")
	@Expose
	private String id;
	
	@SerializedName("user_id")
	@Expose
	private String userId;

	@SerializedName("firstname")
	@Expose
	private String firstName;

	@SerializedName("lastname")
	@Expose
	private String lastName;

	@SerializedName("type")
	@Expose
	private String type;

	@SerializedName("image")
	@Expose
	private String image;

	@SerializedName("image_thumb")
	@Expose
	private String imageThumb;

	@SerializedName("is_member")
	@Expose
	private boolean isMember;

    @SerializedName("details")
    @Expose
    private List<Map<String, String>> details;

	private boolean isSelected = false;
	
	public User(String id, String userId, String firstName, String lastName, String type, String image,
			String imageThumb, boolean isMember, List<Map<String, String>> details, boolean isSelected) {
		super();
		this.id = id;
		this.userId = userId;
		this.firstName = firstName;
		this.lastName = lastName;
		this.type = type;
		this.image = image;
		this.imageThumb = imageThumb;
		this.isMember = isMember;
		this.details = details;
		this.isSelected = isSelected;
	}

	public String getId() {
		if(id == null || id.equals("")){
			return userId == null ? "" : userId;
		}
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName == null ? "" : firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName == null ? "" : lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getImage() {
		return image == null ? "" : image;
	}

	public void setImage(String image) {
		this.image = image;
	}

    public String getImageThumb() {
        return imageThumb;
    }

    public void setImageThumb(String imageThumb) {
        this.imageThumb = imageThumb;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public boolean isMember() {
        return isMember;
    }

    public void setIsMember(boolean isMember) {
        this.isMember = isMember;
    }

    public List<Map<String, String>> getDetails() {
        return details;
    }

    public Map<String, String> getPublicDetails() {
        Map<String, String> detailsMap = new HashMap<String, String>();

        // WHOA A CHRISTMAS TREE
        if (getDetails() != null) {
            for (Map<String, String> map : getDetails()) {
                if (map.containsKey("public") && (map.get("public").equals("true") || "1".equals(map.get("public")))) {
                    for (String key : map.keySet()) {
                        if (!"public".equals(key) && !map.get(key).equals("")) {
                            detailsMap.put(key, map.get(key));
                        }
                    }
                }
            }
        }

        return detailsMap;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        if (isMember != user.isMember) return false;
        if (isSelected != user.isSelected) return false;
        if (details != null ? !details.equals(user.details) : user.details != null) return false;
        if (firstName != null ? !firstName.equals(user.firstName) : user.firstName != null)
            return false;
        if (id != null ? !id.equals(user.id) : user.id != null) return false;
        if (image != null ? !image.equals(user.image) : user.image != null) return false;
        if (imageThumb != null ? !imageThumb.equals(user.imageThumb) : user.imageThumb != null)
            return false;
        if (lastName != null ? !lastName.equals(user.lastName) : user.lastName != null)
            return false;
        if (type != null ? !type.equals(user.type) : user.type != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (firstName != null ? firstName.hashCode() : 0);
        result = 31 * result + (lastName != null ? lastName.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (image != null ? image.hashCode() : 0);
        result = 31 * result + (imageThumb != null ? imageThumb.hashCode() : 0);
        result = 31 * result + (isMember ? 1 : 0);
        result = 31 * result + (details != null ? details.hashCode() : 0);
        result = 31 * result + (isSelected ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", type='" + type + '\'' +
                ", image='" + image + '\'' +
                ", imageThumb='" + imageThumb + '\'' +
                ", isMember=" + isMember +
                ", details=" + details +
                ", isSelected=" + isSelected +
                '}';
    }

}
