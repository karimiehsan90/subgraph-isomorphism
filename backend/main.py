from flask import Flask, request, Response
from flask_cors import CORS
from scanner import Scanner
from parser import Parser, Graph
import json
import os
from hdfs import InsecureClient
import paramiko

BIND_IP = os.getenv('BACKEND_BIND_IP', '0.0.0.0')
PORT = int(os.getenv('BACKEND_PORT', '8000'))
NODE_USER = os.getenv('NODE_USER', 'vagrant')
HDFS_ADDRESS = os.getenv('HDFS_ADDRESS', 'http://master:50070')
MASTER_HOST = os.getenv('MASTER_HOST', '192.168.100.101')
MASTER_PASSWORD = os.getenv('MASTER_PASSWORD', 'vagrant')
HADOOP_PATH = os.getenv('HADOOP_PATH', '/opt/hadoop')
HADOOP_BINARY = HADOOP_PATH + '/bin/hadoop'
OUTPUT_TMP_DIR = '/subgraph-isomorphism/tmp'
OUTPUT_RESULT_DIR = '/subgraph-isomorphism/output'
QUERY_FILE = '/subgraph-isomorphism/query'
app = Flask(__name__)
CORS(app)
scanner = Scanner()
parser = Parser()
hdfs_client = InsecureClient(HDFS_ADDRESS, user=NODE_USER)
ssh_client = paramiko.SSHClient()
ssh_client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
IS_RUNNING_MAP_REDUCE = False
graph = None


def delete_if_exists(filename):
    file_status = hdfs_client.status(filename, strict=False)
    if file_status:
        hdfs_client.delete(filename, recursive=True)


def run_map_reduce():
    delete_if_exists(OUTPUT_TMP_DIR)
    delete_if_exists(OUTPUT_RESULT_DIR)
    ssh_client.connect(MASTER_HOST, username=NODE_USER, password=MASTER_PASSWORD)
    ssh_client.exec_command('{} jar subgraph-isomorphism.jar ir.ac.sbu.project.App /subgraph-isomorphism/input {} {} '
                            '{} 10 >/dev/null 2>&1 &'.format(HADOOP_BINARY, QUERY_FILE, OUTPUT_TMP_DIR,
                                                             OUTPUT_RESULT_DIR))


def write_query_to_hdfs(nodes):
    query_file_status = hdfs_client.status(QUERY_FILE, strict=False)
    if query_file_status:
        hdfs_client.delete(QUERY_FILE)
    with hdfs_client.write(QUERY_FILE, encoding='utf-8') as writer:
        for from_node, adj_list in nodes.items():
            for to_node in adj_list:
                writer.write('{} {}\n'.format(from_node, to_node))


def read_first_line_of_result():
    line = ''
    with hdfs_client.read('{}/part-r-00000'.format(OUTPUT_RESULT_DIR), encoding='utf-8', delimiter='\n') as reader:
        for file_line in reader:
            if not line:
                line = file_line
    return line


def create_response(graph):
    answer = {
        'nodes': [],
        'edges': [],
    }
    for from_node, adj_list in graph.nodes.items():
        answer['nodes'].append({
            'id': from_node,
            'label': from_node,
        })
        for to_node in adj_list:
            answer['edges'].append({
                'from': from_node,
                'to': to_node,
            })
    return answer


@app.route('/')
def run_query():
    global IS_RUNNING_MAP_REDUCE, graph
    if not IS_RUNNING_MAP_REDUCE:
        query = request.args.get('query')
        tokens = scanner.scan(query)
        graph = parser.parse(tokens)
        write_query_to_hdfs(graph.nodes)
        run_map_reduce()
        IS_RUNNING_MAP_REDUCE = True
        result = Graph()
    else:
        output_file_status = hdfs_client.status(OUTPUT_RESULT_DIR, strict=False)
        if output_file_status:
            IS_RUNNING_MAP_REDUCE = False
            line = read_first_line_of_result()
            result = Graph()
            mappings = line.split(' ')
            mapping_result = {}
            for mapping in mappings:
                key_value = mapping.split(':')
                mapping_result[key_value[0]] = key_value[1]
            for from_node, adj_list in graph.nodes.items():
                result.add_node(mapping_result[from_node])
                for to_node in adj_list:
                    result.add_single_edge(mapping_result[from_node], mapping_result[to_node])
        else:
            result = Graph()
    response = create_response(result)
    return Response(json.dumps(response), mimetype='application/json')


app.run(BIND_IP, PORT)
