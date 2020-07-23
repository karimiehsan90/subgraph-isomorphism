FROM m.docker-registry.ir/python:3.6.9

COPY requirements.txt /requirements.txt

RUN pip install -r /requirements.txt

COPY main.py parser.py scanner.py /opt/

WORKDIR /opt

ENTRYPOINT ["python", "main.py"]
