FROM mongo
COPY keyfile /opt
RUN chown 999:999 /opt/keyfile
RUN chmod 400 /opt/keyfile
#RUN chown 999:999 /opt/keyfile
#RUN echo "rs.initiate({'_id':'rs0', members: [{'_id':0, 'host':'127.0.0.1:27017'}]});" > "/docker-entrypoint-initdb.d/init_replicaset.js"
#CMD ["--bind_ip_all", "--keyFile", "/opt/keyfile", "--replSet", "rs0"]