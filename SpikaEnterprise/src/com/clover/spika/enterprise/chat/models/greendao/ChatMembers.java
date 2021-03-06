package com.clover.spika.enterprise.chat.models.greendao;

// THIS CODE IS GENERATED BY greenDAO, EDIT ONLY INSIDE THE "KEEP"-SECTIONS

// KEEP INCLUDES - put your custom includes here
// KEEP INCLUDES END
/**
 * Entity mapped to table CHAT_MEMBERS.
 */
public class ChatMembers {

    private long id;
    private String chatMembers;

    // KEEP FIELDS - put your custom fields here
    // KEEP FIELDS END

    public ChatMembers() {
    }

    public ChatMembers(long id) {
        this.id = id;
    }

    public ChatMembers(long id, String chatMembers) {
        this.id = id;
        this.chatMembers = chatMembers;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getChatMembers() {
        return chatMembers;
    }

    public void setChatMembers(String chatMembers) {
        this.chatMembers = chatMembers;
    }

    // KEEP METHODS - put your custom methods here
    // KEEP METHODS END

}
