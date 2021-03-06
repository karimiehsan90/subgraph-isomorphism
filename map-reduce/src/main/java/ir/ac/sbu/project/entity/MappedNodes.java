package ir.ac.sbu.project.entity;

import org.apache.hadoop.io.Text;

import java.util.*;

public class MappedNodes {
    private Map<String, String> map;
    private boolean isBase;
    private boolean isValidated;
    private int checkedUntil = 0;
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

    public int getCheckedUntil() {
        return checkedUntil;
    }

    public void setCheckedUntil(int checkedUntil) {
        this.checkedUntil = checkedUntil;
    }

    public Map<String, String> getMap() {
        return map;
    }

    public boolean isValidated() {
        return isValidated;
    }

    public void setValidated(boolean validated) {
        isValidated = validated;
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
        mappedNodes.isValidated = Boolean.parseBoolean(objectFields[2]);
        mappedNodes.checkedUntil = Integer.parseInt(objectFields[3]);
        if (!mappedNodes.isBase) {
            Arrays.asList(objectFields)
                    .subList(4, objectFields.length)
                    .forEach(field -> {
                        String[] keyValue = field.split(":");
                        mappedNodes.map.put(keyValue[0], keyValue[1]);
                    });
        } else {
            mappedNodes.children.addAll(Arrays.asList(objectFields)
                    .subList(4, objectFields.length));
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

    private String toResultString() {
        String[] values = new String[map.size()];
        int i = 0;
        for (String key : map.keySet()) {
            values[i] = key + ":" + map.get(key);
            i++;
        }
        return String.join(" ", values);
    }

    public Text toResultText() {
        return new Text(toResultString());
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder("");
        stringBuilder.append(isBase)
                .append(" ")
                .append(latestQueryGraphMappedNode)
                .append(" ")
                .append(isValidated)
                .append(" ")
                .append(checkedUntil);
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
