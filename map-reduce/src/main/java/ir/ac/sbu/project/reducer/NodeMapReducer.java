package ir.ac.sbu.project.reducer;

import ir.ac.sbu.project.entity.MappedNodes;
import ir.ac.sbu.project.entity.Node;
import ir.ac.sbu.project.entity.QueryGraph;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class NodeMapReducer extends Reducer<Text, Text, Text, Text> {
    private QueryGraph queryGraph;

    @Override
    protected void setup(Context context) throws IOException {
        queryGraph = new QueryGraph();
        URI[] cacheFiles = context.getCacheFiles();
        if (cacheFiles != null) {
            FileSystem hdfs = FileSystem.get(context.getConfiguration());
            for (URI cacheFile : cacheFiles) {
                Path cacheFilePath = new Path(cacheFile.toString());
                BufferedReader reader = new BufferedReader(new InputStreamReader(hdfs.open(cacheFilePath)));
                String line;
                while ((line = reader.readLine()) != null) {
                    String[] nodes = line.split(" ");
                    queryGraph.addEdge(nodes[0], nodes[1]);
                }
            }
        }
    }

    @Override
    protected void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
        List<String> mappedNodesListAsString = new ArrayList<>();
        for (Text value : values) {
            mappedNodesListAsString.add(value.toString());
        }
        List<MappedNodes> mappedNodesList = mappedNodesListAsString
                .stream()
                .map(MappedNodes::fromString)
                .collect(Collectors.toList());
        List<MappedNodes> completeResults = mappedNodesList.stream()
                .filter(mappedNodes -> mappedNodes.isCompleteResult(queryGraph))
                .collect(Collectors.toList());
        String keyString = key.toString();
        MappedNodes baseMappedNodes = getBase(mappedNodesList);
        if (baseMappedNodes != null) {
            for (MappedNodes completeResult : completeResults) {
                Node latestMappedNodeInQueryGraph = getNodeByLabel(completeResult.getLatestQueryGraphMappedNode());
                if (latestMappedNodeInQueryGraph != null
                        && isValidMapped(baseMappedNodes, completeResult, latestMappedNodeInQueryGraph)) {
                    context.write(key, completeResult.toText());
                }
            }
            context.write(key, baseMappedNodes.toText());
            if (mappedNodesList.size() == 1) {
                String firstNodeLabel = queryGraph.getNodes().get(0).getName();
                MappedNodes copyMappedNodes = new MappedNodes(baseMappedNodes, firstNodeLabel);
                copyMappedNodes.addPair(firstNodeLabel, keyString);
                for (String child : baseMappedNodes.getChildren()) {
                    context.write(new Text(child), copyMappedNodes.toText());
                }
            } else {
                for (MappedNodes mappedNodes : mappedNodesList) {
                    if (!mappedNodes.isBase()) {
                        if (!mappedNodes.getMap().containsValue(keyString)) {
                            String latestQueryGraphMappedNodeLabel = mappedNodes.getLatestQueryGraphMappedNode();
                            Node latestQueryGraphMappedNode = getNodeByLabel(latestQueryGraphMappedNodeLabel);
                            if (latestQueryGraphMappedNode != null && hasUnmappedChild(latestQueryGraphMappedNode, mappedNodes)) {
                                for (Node node : latestQueryGraphMappedNode.getAdjList()) {
                                    if (!mappedNodes.getMap().containsKey(node.getName())) {
                                        MappedNodes copyMappedNodes = new MappedNodes(mappedNodes, node.getName());
                                        copyMappedNodes.addPair(node.getName(), keyString);
                                        Text copyMappedNodesText = copyMappedNodes.toText();
                                        if (isValidMapped(baseMappedNodes, copyMappedNodes, node)) {
                                            for (String child : baseMappedNodes.getChildren()) {
                                                context.write(new Text(child), copyMappedNodesText);
                                            }
                                        }
                                    }
                                }
                            } else if (latestQueryGraphMappedNode != null) {
                                backtrack(mappedNodes, context);
                            }
                        } else {
                            String nodeInQueryGraph = getMappedKey(mappedNodes, keyString);
                            mappedNodes.setLatestQueryGraphMappedNode(nodeInQueryGraph);
                            for (String child : baseMappedNodes.getChildren()) {
                                if (!mappedNodes.getMap().containsValue(child)) {
                                    context.write(new Text(child), mappedNodes.toText());
                                }
                            }
                        }
                    }
                }
            }
        } else {
            for (MappedNodes completeResult : completeResults) {
                Node latestMappedNodeInQueryGraph = getNodeByLabel(completeResult.getLatestQueryGraphMappedNode());
                if (latestMappedNodeInQueryGraph != null
                        && isValidMapped(new MappedNodes(true), completeResult, latestMappedNodeInQueryGraph)) {
                    context.write(key, completeResult.toText());
                }
            }
            for (MappedNodes mappedNodes : mappedNodesList) {
                if (!mappedNodes.getMap().containsValue(keyString)) {
                    String latestQueryGraphMappedNodeLabel = mappedNodes.getLatestQueryGraphMappedNode();
                    Node latestQueryGraphMappedNode = getNodeByLabel(latestQueryGraphMappedNodeLabel);
                    if (latestQueryGraphMappedNode != null) {
                        for (Node node : latestQueryGraphMappedNode.getAdjList()) {
                            MappedNodes copyMappedNodes = new MappedNodes(mappedNodes, node.getName());
                            copyMappedNodes.addPair(node.getName(), keyString);
                            if (isValidMapped(new MappedNodes(true), copyMappedNodes, node)) {
                                context.write(new Text(keyString), copyMappedNodes.toText());
                            }
                        }
                    }
                } else {
                    backtrack(mappedNodes, context);
                }
            }
        }
    }

    private String getMappedKey(MappedNodes mappedNodes, String nodeInDataGraph) {
        for (String nodeInQueryGraph : mappedNodes.getMap().keySet()) {
            if (mappedNodes.getMap().get(nodeInQueryGraph).equals(nodeInDataGraph)) {
                return nodeInQueryGraph;
            }
        }
        return "$";
    }

    private void backtrack(MappedNodes mappedNodes, Context context) throws IOException, InterruptedException {
        String latestQueryGraphMappedNodeLabel = mappedNodes.getLatestQueryGraphMappedNode();
        Node latestQueryGraphMappedNode = getNodeByLabel(latestQueryGraphMappedNodeLabel);
        if (latestQueryGraphMappedNode != null && latestQueryGraphMappedNode.getAdjList().size() == 0) {
            for (String nodeLabel : mappedNodes.getMap().keySet()) {
                Node nodeByLabel = getNodeByLabel(nodeLabel);
                if (nodeByLabel != null && hasUnmappedChild(nodeByLabel, mappedNodes)) {
                    context.write(new Text(mappedNodes.getMap().get(nodeLabel)), mappedNodes.toText());
                }
            }
        }
    }

    private boolean isValidMapped(MappedNodes baseMappedNodes, MappedNodes currentMappedNodes, Node currentNodeInQueryGraph) {
        for (Node childInQueryGraph : currentNodeInQueryGraph.getAdjList()) {
            if (currentMappedNodes.getMap().containsKey(childInQueryGraph.getName())) {
                String dataNodeInMappedNodes = currentMappedNodes.getMap().get(childInQueryGraph.getName());
                if (!baseMappedNodes.getChildren().contains(dataNodeInMappedNodes)) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean hasUnmappedChild(Node node, MappedNodes mappedNodes) {
        for (Node childNode : node.getAdjList()) {
            if (!mappedNodes.getMap().containsKey(childNode.getName())) {
                return true;
            }
        }
        return false;
    }

    private Node getNodeByLabel(String label) {
        for (Node node : queryGraph.getNodes()) {
            if (node.getName().equals(label)) {
                return node;
            }
        }
        return null;
    }

    private MappedNodes getBase(List<MappedNodes> mappedNodesList) {
        for (MappedNodes mappedNodes : mappedNodesList) {
            if (mappedNodes.isBase()) {
                return mappedNodes;
            }
        }
        return null;
    }
}
