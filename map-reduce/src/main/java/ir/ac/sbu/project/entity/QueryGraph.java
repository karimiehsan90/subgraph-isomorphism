package ir.ac.sbu.project.entity;

import java.util.ArrayList;
import java.util.List;

public class QueryGraph {
    private List<Node> nodes;

    public QueryGraph() {
        nodes = new ArrayList<>();
    }

    public void addEdge(String from, String to) {
        Node fromNode = getNodeByName(from);
        Node toNode = getNodeByName(to);
        if (fromNode == null) {
            fromNode = new Node(from);
            nodes.add(fromNode);
        }
        if (toNode == null) {
            toNode = new Node(to);
            nodes.add(toNode);
        }
        fromNode.getAdjList().add(toNode);
    }

    public List<Node> getNodes() {
        return nodes;
    }

    private Node getNodeByName(String nodeName) {
        for (Node node : nodes) {
            if (node.getName().equals(nodeName)) {
                return node;
            }
        }
        return null;
    }
}
