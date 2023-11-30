db = db.getSiblingDB('customerdb');

db.createUser(
        {
            user: "api_user",
            pwd: "api123",
            roles: [
                {
                    role: "readWrite",
                    db: "customerdb"
                }
            ]
        }
);