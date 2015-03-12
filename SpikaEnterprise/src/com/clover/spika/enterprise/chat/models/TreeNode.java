package com.clover.spika.enterprise.chat.models;

import com.clover.spika.enterprise.chat.utils.MessageIdComparator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

public class TreeNode {

	private int level;
	private Message message;
	private TreeSet<TreeNode> children = new TreeSet<TreeNode>(new TreeNodeComparator());

	public TreeNode() {

	}

	public TreeNode(Message data) {
		this.message = data;
		this.level = 0;
	}

	private TreeNode(Message data, int level) {
		this.message = data;
		this.level = level;
	}

	public TreeNode(List<Message> collection) {
		if (!collection.isEmpty()) {
			Collections.sort(collection, new MessageIdComparator());

			this.level = 0;
			this.message = collection.get(0);

			collection.remove(0);
			addAll(collection);
		} else {
			throw new IllegalArgumentException("List must not be empty!");
		}
	}

	public TreeSet<TreeNode> getChildren() {
		return children;
	}

	public Message getMessage() {
		return message;
	}

	public int getLevel() {
		return level;
	}

	public void add(Message message) {
		int parentId = message.getParentId();

		if (parentId == this.message.getIntegerId()) {
			children.add(new TreeNode(message, this.level + 1));
		} else {
			for (TreeNode node : children) {
				if (parentId == node.message.getIntegerId()) {
					node.children.add(new TreeNode(message, node.level + 1));
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

	public List<TreeNode> asList() {
		List<TreeNode> nodes = new ArrayList<TreeNode>();

		nodes.add(this);
		for (TreeNode node : this.children) {
			nodes.addAll(node.asList());
		}

		return nodes;
	}

	@Override
	public String toString() {
		return "TreeNode{" + "level=" + level + ", message=" + message + ", children=" + children + '}';
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
