FROM postgres:17-alpine

# init_db.sh ins Init-Verzeichnis kopieren
COPY init_db.sh /docker-entrypoint-initdb.d/init_db.sh

# CRLF -> LF und ausführbar machen (BusyBox sed ist vorhanden)
RUN sed -i 's/\r$//' /docker-entrypoint-initdb.d/init_db.sh \
 && chmod +x /docker-entrypoint-initdb.d/init_db.sh
