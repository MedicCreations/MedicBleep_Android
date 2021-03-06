package com.clover.spika.enterprise.chat.models.greendao;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
// KEEP INCLUDES END
/**
 * Entity mapped to table USER.
 */
public class User {

    private long id;
    private String firstname;
    private String lastname;
    private Integer type;
    private String image;
    private String image_thumb;
    private Boolean is_member;
    private Integer is_admin;
    private String name;
    private String groupname;
    private String chat_id;
    private Integer is_user;
    private Integer is_group;
    private Integer is_room;
    private Long organization_id;

    // KEEP FIELDS - put your custom fields here
    // KEEP FIELDS END

    public User() {
    }

    public User(long id) {
        this.id = id;
    }

    public User(long id, String firstname, String lastname, Integer type, String image, String image_thumb, Boolean is_member, Integer is_admin, String name, String groupname, String chat_id, Integer is_user, Integer is_group, Integer is_room, Long organization_id) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.type = type;
        this.image = image;
        this.image_thumb = image_thumb;
        this.is_member = is_member;
        this.is_admin = is_admin;
        this.name = name;
        this.groupname = groupname;
        this.chat_id = chat_id;
        this.is_user = is_user;
        this.is_group = is_group;
        this.is_room = is_room;
        this.organization_id = organization_id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImage_thumb() {
        return image_thumb;
    }

    public void setImage_thumb(String image_thumb) {
        this.image_thumb = image_thumb;
    }

    public Boolean getIs_member() {
        return is_member;
    }

    public void setIs_member(Boolean is_member) {
        this.is_member = is_member;
    }

    public Integer getIs_admin() {
        return is_admin;
    }

    public void setIs_admin(Integer is_admin) {
        this.is_admin = is_admin;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGroupname() {
        return groupname;
    }

    public void setGroupname(String groupname) {
        this.groupname = groupname;
    }

    public String getChat_id() {
        return chat_id;
    }

    public void setChat_id(String chat_id) {
        this.chat_id = chat_id;
    }

    public Integer getIs_user() {
        return is_user;
    }

    public void setIs_user(Integer is_user) {
        this.is_user = is_user;
    }

    public Integer getIs_group() {
        return is_group;
    }

    public void setIs_group(Integer is_group) {
        this.is_group = is_group;
    }

    public Integer getIs_room() {
        return is_room;
    }

    public void setIs_room(Integer is_room) {
        this.is_room = is_room;
    }

    public Long getOrganization_id() {
        return organization_id;
    }

    public void setOrganization_id(Long organization_id) {
        this.organization_id = organization_id;
    }

    // KEEP METHODS - put your custom methods here
    // KEEP METHODS END

}
