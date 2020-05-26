FROM debian:stretch-slim

RUN apt-get update
RUN apt-get -y install git python3 python3-pip
RUN pip3 install gitchangelog
