-- Product Service Database Schema
-- Version 1.0.0 - Initial schema creation

-- Create categories table
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    slug VARCHAR(100) UNIQUE NOT NULL,
    parent_id BIGINT REFERENCES categories(id) ON DELETE SET NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    sort_order INTEGER DEFAULT 0,
    image_url VARCHAR(500),
    meta_title VARCHAR(200),
    meta_description VARCHAR(500),
    meta_keywords VARCHAR(300),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create products table
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL,
    sku VARCHAR(100) UNIQUE,
    description TEXT,
    detailed_description TEXT,
    price DECIMAL(10,2) NOT NULL,
    discount_price DECIMAL(10,2),
    stock_quantity INTEGER NOT NULL DEFAULT 0,
    reserved_quantity INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT true,
    featured BOOLEAN NOT NULL DEFAULT false,
    image_url VARCHAR(500),
    additional_images TEXT[], -- Array of image URLs
    tags TEXT[], -- Array of tags
    weight DECIMAL(8,3), -- in kg
    length DECIMAL(8,2), -- in cm
    width DECIMAL(8,2), -- in cm
    height DECIMAL(8,2), -- in cm
    brand VARCHAR(100),
    manufacturer VARCHAR(100),
    model VARCHAR(100),
    meta_title VARCHAR(200),
    meta_description VARCHAR(500),
    meta_keywords VARCHAR(300),
    category_id BIGINT NOT NULL REFERENCES categories(id) ON DELETE RESTRICT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for better performance
CREATE INDEX idx_categories_parent_id ON categories(parent_id);
CREATE INDEX idx_categories_active ON categories(active);
CREATE INDEX idx_categories_slug ON categories(slug);

CREATE INDEX idx_products_category_id ON products(category_id);
CREATE INDEX idx_products_sku ON products(sku);
CREATE INDEX idx_products_active ON products(active);
CREATE INDEX idx_products_featured ON products(featured);
CREATE INDEX idx_products_brand ON products(brand);
CREATE INDEX idx_products_price ON products(price);
CREATE INDEX idx_products_stock_quantity ON products(stock_quantity);
CREATE INDEX idx_products_name_gin ON products USING gin(to_tsvector('english', name));
CREATE INDEX idx_products_description_gin ON products USING gin(to_tsvector('english', description));

-- Create function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create triggers for updated_at columns
CREATE TRIGGER update_categories_updated_at 
    BEFORE UPDATE ON categories 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_products_updated_at 
    BEFORE UPDATE ON products 
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- Insert initial sample categories
INSERT INTO categories (name, description, slug, parent_id, active, sort_order, image_url, meta_title, meta_description) VALUES
('Electronics', 'Electronic devices and accessories', 'electronics', NULL, true, 1, '/images/categories/electronics.jpg', 'Electronics - Latest Gadgets & Devices', 'Shop the latest electronic devices, gadgets, and accessories from top brands'),
('Computers', 'Desktop computers, laptops, and computer accessories', 'computers', 1, true, 1, '/images/categories/computers.jpg', 'Computers & Laptops', 'Find desktop computers, laptops, and computer accessories'),
('Laptops', 'Portable computers and laptop accessories', 'laptops', 2, true, 1, '/images/categories/laptops.jpg', 'Laptops & Notebooks', 'Discover the latest laptops and notebooks for work and gaming'),
('Desktop Computers', 'Desktop PCs and workstations', 'desktop-computers', 2, true, 2, '/images/categories/desktops.jpg', 'Desktop Computers & PCs', 'High-performance desktop computers and workstations'),
('Mobile Phones', 'Smartphones and mobile devices', 'mobile-phones', 1, true, 2, '/images/categories/phones.jpg', 'Smartphones & Mobile Phones', 'Latest smartphones and mobile devices from top manufacturers'),
('Tablets', 'Tablet computers and accessories', 'tablets', 1, true, 3, '/images/categories/tablets.jpg', 'Tablets & iPads', 'Explore tablet computers and accessories for work and entertainment'),
('Home & Garden', 'Home improvement and garden supplies', 'home-garden', NULL, true, 2, '/images/categories/home-garden.jpg', 'Home & Garden Supplies', 'Everything for your home and garden improvement projects'),
('Furniture', 'Indoor and outdoor furniture', 'furniture', 7, true, 1, '/images/categories/furniture.jpg', 'Home Furniture', 'Quality furniture for every room in your home'),
('Garden Tools', 'Tools and equipment for gardening', 'garden-tools', 7, true, 2, '/images/categories/garden-tools.jpg', 'Garden Tools & Equipment', 'Professional garden tools and equipment for all your gardening needs'),
('Clothing', 'Fashion and apparel for all ages', 'clothing', NULL, true, 3, '/images/categories/clothing.jpg', 'Fashion & Clothing', 'Trendy clothing and fashion accessories for men, women, and children');

-- Insert initial sample products
INSERT INTO products (name, sku, description, detailed_description, price, discount_price, stock_quantity, active, featured, image_url, tags, weight, brand, manufacturer, category_id, meta_title, meta_description) VALUES
('MacBook Pro 16"', 'MBP16-2023-001', 'Latest MacBook Pro with M2 Pro chip', 'The most powerful MacBook Pro ever, featuring the revolutionary M2 Pro chip for groundbreaking performance and all-day battery life. Perfect for professionals and creatives.', 2499.00, 2299.00, 25, true, true, '/images/products/macbook-pro-16.jpg', ARRAY['laptop', 'apple', 'professional', 'M2'], 2.15, 'Apple', 'Apple Inc.', 3, 'MacBook Pro 16" - Professional Laptop', 'Powerful MacBook Pro 16" with M2 Pro chip, perfect for professionals and creatives'),

('Dell XPS 13', 'DELL-XPS13-2023-001', 'Ultra-portable laptop with stunning display', 'Experience exceptional performance in an ultra-portable design. The Dell XPS 13 features a beautiful InfinityEdge display and powerful Intel processors.', 1299.00, NULL, 40, true, true, '/images/products/dell-xps-13.jpg', ARRAY['laptop', 'dell', 'ultrabook', 'portable'], 1.27, 'Dell', 'Dell Technologies', 3, 'Dell XPS 13 - Ultra-portable Laptop', 'Dell XPS 13 ultrabook with stunning display and powerful performance'),

('iPhone 15 Pro', 'IPHONE15PRO-001', 'Latest iPhone with titanium design', 'The most advanced iPhone yet, featuring a titanium design, A17 Pro chip, and revolutionary camera system with 5x telephoto zoom.', 999.00, NULL, 150, true, true, '/images/products/iphone-15-pro.jpg', ARRAY['smartphone', 'apple', 'titanium', '5G'], 0.19, 'Apple', 'Apple Inc.', 5, 'iPhone 15 Pro - Titanium Smartphone', 'Latest iPhone 15 Pro with titanium design and advanced camera system'),

('Samsung Galaxy S24', 'GALAXY-S24-001', 'Flagship Android smartphone', 'Experience the power of Galaxy AI with the Samsung Galaxy S24. Features advanced camera capabilities and all-day battery life.', 799.00, 749.00, 80, true, true, '/images/products/galaxy-s24.jpg', ARRAY['smartphone', 'samsung', 'android', 'AI'], 0.17, 'Samsung', 'Samsung Electronics', 5, 'Samsung Galaxy S24 - AI Smartphone', 'Samsung Galaxy S24 with Galaxy AI features and advanced camera'),

('iPad Pro 12.9"', 'IPADPRO129-2023-001', 'Professional tablet with M2 chip', 'The ultimate iPad experience with the power of M2 chip. Perfect for creative professionals and demanding workflows.', 1099.00, NULL, 60, true, false, '/images/products/ipad-pro-129.jpg', ARRAY['tablet', 'apple', 'professional', 'M2'], 0.68, 'Apple', 'Apple Inc.', 6, 'iPad Pro 12.9" - Professional Tablet', 'iPad Pro 12.9" with M2 chip for professional workflows'),

('Gaming Desktop PC', 'GAMING-PC-001', 'High-performance gaming computer', 'Ultimate gaming experience with RTX 4080, Intel i7 processor, and 32GB RAM. Built for serious gamers and content creators.', 2899.00, 2699.00, 15, true, true, '/images/products/gaming-pc.jpg', ARRAY['desktop', 'gaming', 'RTX', 'high-performance'], 12.5, 'Custom Build', 'TechBuilder', 4, 'Gaming Desktop PC - High Performance', 'High-performance gaming desktop with RTX 4080 and Intel i7'),

('Office Chair Pro', 'CHAIR-OFFICE-001', 'Ergonomic office chair', 'Premium ergonomic office chair designed for all-day comfort. Features adjustable lumbar support and breathable mesh back.', 399.00, 349.00, 30, true, false, '/images/products/office-chair.jpg', ARRAY['furniture', 'office', 'ergonomic', 'comfortable'], 18.2, 'ErgoDesk', 'ErgoDesk Inc.', 8, 'Office Chair Pro - Ergonomic Comfort', 'Premium ergonomic office chair with adjustable lumbar support'),

('Garden Tool Set', 'GARDEN-SET-001', 'Complete gardening tool set', 'Professional 10-piece garden tool set including spade, rake, pruners, and more. Made from durable stainless steel.', 129.00, NULL, 50, true, false, '/images/products/garden-tools.jpg', ARRAY['garden', 'tools', 'stainless-steel', 'complete-set'], 3.8, 'GreenThumb', 'GreenThumb Tools', 9, 'Garden Tool Set - Professional Quality', 'Complete 10-piece garden tool set made from durable stainless steel'),

('Wireless Headphones', 'HEADPHONES-WL-001', 'Premium noise-canceling headphones', 'Experience superior sound quality with active noise cancellation and 30-hour battery life. Perfect for travel and work.', 299.00, 279.00, 75, true, true, '/images/products/wireless-headphones.jpg', ARRAY['audio', 'wireless', 'noise-canceling', 'premium'], 0.25, 'SoundMax', 'SoundMax Audio', 1, 'Wireless Headphones - Premium Audio', 'Premium wireless headphones with noise cancellation and long battery life'),

('Smart Watch', 'SMARTWATCH-001', 'Fitness and health tracking watch', 'Advanced smartwatch with comprehensive health monitoring, GPS, and 7-day battery life. Compatible with iOS and Android.', 249.00, NULL, 100, true, false, '/images/products/smart-watch.jpg', ARRAY['wearable', 'fitness', 'health', 'GPS'], 0.045, 'FitTech', 'FitTech Wearables', 1, 'Smart Watch - Fitness & Health Tracker', 'Advanced smartwatch with health monitoring and GPS tracking');

-- Create view for product analytics
CREATE VIEW product_analytics AS
SELECT 
    p.id,
    p.name,
    p.sku,
    p.price,
    p.discount_price,
    p.stock_quantity,
    p.reserved_quantity,
    (p.stock_quantity - p.reserved_quantity) as available_quantity,
    CASE WHEN p.discount_price IS NOT NULL THEN p.discount_price ELSE p.price END as effective_price,
    CASE WHEN p.stock_quantity > p.reserved_quantity THEN true ELSE false END as in_stock,
    CASE WHEN p.discount_price IS NOT NULL AND p.discount_price < p.price THEN true ELSE false END as on_sale,
    c.name as category_name,
    c.slug as category_slug,
    p.created_at,
    p.updated_at
FROM products p
JOIN categories c ON p.category_id = c.id
WHERE p.active = true;