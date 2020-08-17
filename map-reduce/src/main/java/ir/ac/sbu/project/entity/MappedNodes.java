package ir.ac.sbu.project.entity;

import org.apache.hadoop.io.Text;

import java.util.*;
import java.util.stream.Collectors;

public class MappedNodes {
    private Map<String, String> map;
    private boolean isBase;
    private List<String> children;
    private String latestQueryGraphMappedNode = "$";

    public MappedNodes(boolean isBase) {
        this.map = new HashMap<>();
        this.children = new ArrayList<>();
        this.isBase = isBase;
    }

    public MappedNodes() {
        this.map = new HashMap<>();
        this.children = new ArrayList<>();
    }

    public MappedNodes(MappedNodes mappedNodes, String latestQueryGraphMappedNode) {
        this.map = new HashMap<>();
        this.children = new ArrayList<>();
        this.isBase = false;

        for (String key : mappedNodes.map.keySet()) {
            this.map.put(key, mappedNodes.map.get(key));
        }

        this.latestQueryGraphMappedNode = latestQueryGraphMappedNode;
    }

    public Map<String, String> getMap() {
        return map;
    }

    public void setLatestQueryGraphMappedNode(String latestQueryGraphMappedNode) {
        this.latestQueryGraphMappedNode = latestQueryGraphMappedNode;
    }

    public boolean isCompleteResult(QueryGraph queryGraph) {
        return map.size() == queryGraph.getNodes().size();
    }

    public String getLatestQueryGraphMappedNode() {
        return latestQueryGraphMappedNode;
    }

    public boolean isBase() {
        return isBase;
    }

    public List<String> getChildren() {
        return children;
    }

    public void addPair(String key, String value) {
        map.put(key, value);
    }

    public void addChild(String child) {
        this.children.add(child);
    }

    public Text toText() {
        return new Text(toString());
    }

    public static MappedNodes fromString(String objectAsString) {
        MappedNodes mappedNodes = new MappedNodes();
        String[] objectFields = objectAsString.split(" ");
        mappedNodes.isBase = Boolean.parseBoolean(objectFields[0]);
        mappedNodes.latestQueryGraphMappedNode = objectFields[1];
        if (!mappedNodes.isBase) {
            Arrays.asList(objectFields)
                    .subList(2, objectFields.length)
                    .forEach(field -> {
                        String[] keyValue = field.split(":");
                        mappedNodes.map.put(keyValue[0], keyValue[1]);
                    });
        } else {
            mappedNodes.children.addAll(Arrays.asList(objectFields)
                    .subList(2, objectFields.length));
        }
        return mappedNodes;
    }

    private String toKeyString() {
        List<String> values = new ArrayList<>(map.values());
        values.sort(String::compareTo);
        StringBuilder stringBuilder = new StringBuilder("");
        for (String value : values) {
            stringBuilder.append(value)
                    .append(" ");
        }
        return stringBuilder.toString();
    }

    public Text toKeyText() {
        return new Text(toKeyString());
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("");
        stringBuilder.append(isBase)
                .append(" ")
                .append(latestQueryGraphMappedNode);
        if (!isBase) {
            for (String key : map.keySet()) {
                stringBuilder.append(" ")
                        .append(key)
                        .append(":")
                        .append(map.get(key));
            }
        } else {
            for (String child : children) {
                stringBuilder.append(" ")
                        .append(child);
            }
        }
        return stringBuilder.toString();
    }
}
