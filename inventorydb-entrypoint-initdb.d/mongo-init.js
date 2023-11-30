db = db.getSiblingDB('inventorydb');

db.createUser(
        {
            user: "api_user_inventory",
            pwd: "api123",
            roles: [
                {
                    role: "readWrite",
                    db: "inventorydb"
                }
            ]
        }
);