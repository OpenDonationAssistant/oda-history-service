FROM fedora:39
WORKDIR /app
COPY target/oda-history-service /app

CMD ["./oda-history-service"]