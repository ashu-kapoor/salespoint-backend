db = db.getSiblingDB('salesdb');

db.createUser(
        {
            user: "api_user_sales",
            pwd: "api123",
            roles: [
                {
                    role: "readWrite",
                    db: "salesdb"
                }
            ]
        }
);