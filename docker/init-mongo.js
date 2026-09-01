// MongoDB initialization script — creates databases and default indexes
// This runs only on first container startup

// users_db
db = db.getSiblingDB('users_db');
db.createCollection('users');
db.createCollection('organizations');
db.createCollection('audit_logs');

db.users.createIndex({ email: 1 }, { unique: true });
db.users.createIndex({ organizationId: 1 });
db.organizations.createIndex({ registrationNumber: 1 }, { unique: true });
db.organizations.createIndex({ type: 1 });
db.audit_logs.createIndex({ userId: 1 });
db.audit_logs.createIndex({ action: 1 });
db.audit_logs.createIndex({ timestamp: 1 });

// products_db
db = db.getSiblingDB('products_db');
db.createCollection('products');
db.createCollection('inventory');

db.products.createIndex({ sku: 1 }, { unique: true });
db.products.createIndex({ category: 1, active: 1 });
db.products.createIndex({ distributorId: 1 });
db.inventory.createIndex({ productId: 1, warehouseId: 1, batchNumber: 1 }, { unique: true });
db.inventory.createIndex({ expiryDate: 1 });
db.inventory.createIndex({ distributorId: 1 });

// orders_db
db = db.getSiblingDB('orders_db');
db.createCollection('orders');

db.orders.createIndex({ orderNumber: 1 }, { unique: true });
db.orders.createIndex({ buyerOrgId: 1, status: 1 });
db.orders.createIndex({ distributorOrgId: 1, status: 1 });
db.orders.createIndex({ createdAt: 1 });

print('MongoDB initialization complete.');
