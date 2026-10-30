FROM ubuntu:latest
LABEL authors="sudip"

ENTRYPOINT ["top", "-b"]