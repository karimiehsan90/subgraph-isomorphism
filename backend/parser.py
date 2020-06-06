class Graph:
    def __init__(self):
        self.nodes = {}

    def add_edge(self, row):
        for i in range(len(row) - 1):
            if not row[i + 1] in self.nodes[row[i]]:
                self.nodes[row[i]].append(row[i + 1])
            if not row[i] in self.nodes[row[i + 1]]:
                self.nodes[row[i + 1]].append(row[i])

    def add_node(self, node):
        if node not in self.nodes.keys():
            self.nodes[node] = []


class Parser:
    def read_id(self, tokens):
        if len(tokens) < 3:
            raise Exception('Cannot parse tokens')
        if tokens[0].key == 'open-paren' and tokens[1].key == 'id' and tokens[2].key == 'close-paren':
            return tokens[1]
        else:
            raise Exception('Cannot parse tokens')

    def parse(self, tokens):
        graph = Graph()
        match_statements = []
        match_statement = []
        for token in tokens:
            if token.key == 'semicolon':
                match_statements.append(match_statement)
                match_statement = []
            else:
                match_statement.append(token)
        for match_statement in match_statements:
            row = []
            j = 0
            if match_statement[j].key != 'match-keyword':
                raise Exception('Cannot parse tokens')
            j += 1
            id_token = self.read_id(match_statement[j:])
            node_value = id_token.value
            graph.add_node(node_value)
            row.append(node_value)
            j += 3
            while j < len(match_statement):
                if match_statement[j].key != 'dash':
                    raise Exception('Cannot parse tokens')
                id_token = self.read_id(match_statement[j + 1:])
                node_value = id_token.value
                graph.add_node(node_value)
                row.append(node_value)
                j += 4
            graph.add_edge(row)
        return graph
