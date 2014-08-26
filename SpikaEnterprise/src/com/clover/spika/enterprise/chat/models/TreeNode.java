package com.clover.spika.enterprise.chat.models;

import com.clover.spika.enterprise.chat.utils.MessageIdComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

public class TreeNode {

    private Message message;
    private TreeSet<TreeNode> children = new TreeSet<TreeNode>(new TreeNodeComparator());

    public TreeNode(Message data) {
        this.message = data;
    }

    public TreeNode(List<Message> collection) {
        if (!collection.isEmpty()) {
            Collections.sort(collection, new MessageIdComparator());
            List<Integer> ids = new ArrayList<Integer>();
            for (Message m : collection) {
                ids.add(m.getIntegerId());
            }

            this.message = collection.get(0);
            collection.remove(0);
            addAll(collection);
        }
    }

    public TreeSet<TreeNode> getChildren() {
        return children;
    }

    public Message getMessage() {
        return message;
    }

    public void add(Message message) {
        int parentId = message.getParentId();

        if (parentId == this.message.getIntegerId()) {
            children.add(new TreeNode(message));
        } else {
            for (TreeNode node : children) {
                if (parentId == node.message.getIntegerId()) {
                    node.children.add(new TreeNode(message));
                    break;
                } else {
                    node.add(message);
                }
            }
        }
    }

    public void addAll(List<Message> collection) {
        Collections.sort(collection, new MessageIdComparator());

        for (Message message : collection) {
            add(message);
        }
    }

    public List<Message> toArrayList() {
        List<Message> messages = new ArrayList<Message>();

        for (TreeNode node : this.children) {
            messages.addAll(node.toArrayList());
        }
        messages.add(this.message);

        return messages;
    }

    @Override
    public String toString() {
        return "TreeNode{" +
                "message=" + message +
                ", children=" + children +
                '}';
    }

    private static class TreeNodeComparator implements Comparator<TreeNode> {
        @Override
        public int compare(TreeNode lhs, TreeNode rhs) {
            if (lhs.message.getIntegerId() > rhs.message.getIntegerId()) {
                return 1;
            } else if (lhs.message.getIntegerId() < rhs.message.getIntegerId()) {
                return -1;
            } else {
                return 0;
            }
        }
    }

}
