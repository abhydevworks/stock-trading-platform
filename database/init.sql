-- Initialize database
CREATE DATABASE stock_trading OWNER admin;

\c stock_trading

-- Enable extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";

-- Execute schema
\i database/schema.sql
