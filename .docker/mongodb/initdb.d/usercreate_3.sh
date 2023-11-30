set -Eeuo pipefail
 
mongosh -u "$MONGO_INITDB_ROOT_USERNAME" -p "$MONGO_INITDB_ROOT_PASSWORD" --authenticationDatabase admin "$MONGO_DATABASE_3" <<EOF
    db.createUser({
        user: '$MONGO_DATABASE_3_USERNAME',
        pwd: '$MONGO_DATABASE_3_PASSWORD',
        roles: [ { role: 'readWrite', db: '$MONGO_DATABASE_3' } ]
    });
    db.createUser({
            user: '$MONGO_SAGA_USER',
            pwd: '$MONGO_SAGA_PASSWORD',
            roles: [ { role: 'readWrite', db: '$MONGO_DATABASE_3' } ]
        })
EOF
