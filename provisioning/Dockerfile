FROM m.docker-registry.ir/python:3.6.9

COPY requirements.txt /requirements.txt

RUN pip install -r /requirements.txt

RUN apt update \
    && apt install -y --no-install-recommends sshpass

COPY ansible run.sh /opt/

RUN chmod +x /opt/run.sh

WORKDIR /opt

ENTRYPOINT ["./run.sh"]
