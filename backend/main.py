from flask import Flask, request, Response
from flask_cors import CORS
from scanner import Scanner
from parser import Parser
import json


app = Flask(__name__)
CORS(app)
scanner = Scanner()
parser = Parser()


def run_map_reduce(query_graph):
    nodes = []
    edges = []
    for node, adj_list in query_graph.nodes.items():
        nodes.append({
            'id': node,
            'label': node,
        })
        for dest_node in adj_list:
            edges.append({
                'from': node,
                'to': dest_node,
            })
    return {
        'nodes': nodes,
        'edges': edges,
    }


@app.route('/')
def run_query():
    query = request.args.get('query')
    tokens = scanner.scan(query)
    graph = parser.parse(tokens)
    result = run_map_reduce(graph)
    return Response(json.dumps(result), mimetype='application/json')


app.run('0.0.0.0', 8000)
