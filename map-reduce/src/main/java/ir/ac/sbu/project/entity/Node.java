package ir.ac.sbu.project.entity;

import java.util.ArrayList;
import java.util.List;

public class Node {
    private String name;
    private List<Node> adjList;

    public Node() {
        this.adjList = new ArrayList<>();
    }

    public Node(String name) {
        this.name = name;
        this.adjList = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public List<Node> getAdjList() {
        return adjList;
    }
}
