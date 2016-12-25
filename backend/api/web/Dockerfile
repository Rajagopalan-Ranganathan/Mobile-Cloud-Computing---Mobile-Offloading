FROM node:latest

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app
COPY . .

ARG HTTP_PORT=8080
ARG LOG_LEVEL=debug
ARG SECRET=abc123defjkl
ARG KUBERNETES=false
ARG DB_NAME=usersDB

ENV DB_NAME=${DB_NAME}
ENV KUBERNETES=${KUBERNETES}
ENV HTTP_PORT ${HTTP_PORT}
ENV LOG_LEVEL ${LOG_LEVEL}
ENV SECRET ${SECRET}

RUN npm install nodemon -g

RUN apt-get update

RUN apt-get -q -y install tesseract-ocr

RUN apt-get -q -y install tesseract-ocr-eng

RUN mkdir -p /usr/local/share/tessdata/
RUN cp -R /usr/share/tesseract-ocr/tessdata/* /usr/local/share/tessdata/

RUN apt-get -q -y install imagemagick

CMD npm install && npm start