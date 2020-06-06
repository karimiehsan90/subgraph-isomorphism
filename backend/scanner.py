import re


def add_to_tokens_post_action(tokens, key, value):
    tokens.append(Token(key, value))


def do_nothing(token, key, value):
    pass


class Token:
    def __init__(self, key, value):
        self.key = key
        self.value = value

    def __str__(self):
        return self.key + ': ' + self.value


class Scanner:
    def __init__(self):
        self.token_regular_expressions = [
            {
                'regex': 'match',
                'key': 'match-keyword',
            },
            {
                'regex': '\\s+',
                'key': 'whitespace',
                'post-action': do_nothing,
            },
            {
                'regex': '#[^\\n]+',
                'key': 'comment',
                'post-action': do_nothing,
            },
            {
                'regex': '\\w+',
                'key': 'id',
            },
            {
                'regex': '\\(',
                'key': 'open-paren',
            },
            {
                'regex': '\\)',
                'key': 'close-paren',
            },
            {
                'regex': '\\-',
                'key': 'dash',
            },
            {
                'regex': ';',
                'key': 'semicolon',
            }
        ]

    def scan(self, text):
        text = text.lower()
        tokens = []
        while text:
            max_result = None
            for token_regular_expression in self.token_regular_expressions:
                regex_result = re.search(token_regular_expression['regex'], text)
                if regex_result:
                    start = regex_result.start()
                    end = regex_result.end()
                    if start == 0 and (not max_result or max_result['end'] < end):
                        max_result = {
                            'end': end,
                            'key': token_regular_expression['key'],
                            'post-action': token_regular_expression.get('post-action', add_to_tokens_post_action),
                        }
            if max_result:
                key = max_result['key']
                value = text[0: max_result['end']]
                max_result['post-action'](tokens, key, value)
                if max_result['end'] < len(text):
                    text = text[max_result['end']:]
                else:
                    text = ''
            else:
                raise Exception("Cannot scan text")
        return tokens
