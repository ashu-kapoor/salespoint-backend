set -Eeuo pipefail
 
mongosh -u "$MONGO_INITDB_ROOT_USERNAME" -p "$MONGO_INITDB_ROOT_PASSWORD" --authenticationDatabase admin "$MONGO_DATABASE_1" <<EOF
    db.createUser({
        user: '$MONGO_DATABASE_1_USERNAME',
        pwd: '$MONGO_DATABASE_1_PASSWORD',
        roles: [ { role: 'readWrite', db: '$MONGO_DATABASE_1' } ]
    });
    db.createUser({
            user: '$MONGO_SAGA_USER',
            pwd: '$MONGO_SAGA_PASSWORD',
            roles: [ { role: 'readWrite', db: '$MONGO_DATABASE_1' } ]
        })
EOF
