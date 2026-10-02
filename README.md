# Stock Trading Platform

A starter full-stack stock trading platform prototype designed to provide a solid foundation for live market data integration, portfolio management, order execution, and watchlist tracking.

## Overview

This repository was previously empty apart from the standard project metadata. It now includes a minimal backend MVP so the project has a working starting point for future development.

## Included in this MVP

- FastAPI backend service
- Health check endpoint
- Sample market data
- Portfolio summary endpoint
- Watchlist endpoint
- Basic order placement endpoint
- Environment example configuration

## Project structure

```text
.
├── app/
│   ├── __init__.py
│   └── main.py
├── .env.example
├── .gitignore
├── README.md
├── requirements.txt
└── LICENSE
```

## Quick start

1. Create a virtual environment

```bash
python -m venv .venv
source .venv/bin/activate
```

2. Install dependencies

```bash
pip install -r requirements.txt
```

3. Run the app

```bash
uvicorn app.main:app --reload
```

The API will be available at:

- http://127.0.0.1:8000
- Swagger docs: http://127.0.0.1:8000/docs

## Core endpoints

- GET /health
- GET /stocks
- GET /portfolio
- GET /watchlist
- GET /dashboard
- POST /orders

## Example order payload

```json
{
  "symbol": "AAPL",
  "side": "buy",
  "quantity": 10,
  "order_type": "market"
}
```

## Next steps to complete the platform

This starter covers the minimum foundation. The next major missing pieces are:

- Authentication and user accounts
- Persistent database (PostgreSQL / SQLite)
- Real market data provider integration
- Trade execution and order validation
- Portfolio analytics and P&L calculations
- Frontend dashboard for charts and trading UI
- CI pipeline and test coverage
- Deployment configuration for cloud hosting

## Tech stack recommendation

For the next phase, a strong production setup would be:

- Backend: FastAPI + SQLAlchemy + PostgreSQL
- Frontend: Next.js or React
- Auth: JWT or OAuth
- Data: Alpha Vantage, Finnhub, Polygon, or Yahoo Finance
- Caching: Redis
- Deployment: Docker + Render / Railway / Azure / AWS

## License

This project is licensed under the GNU General Public License v3.0.
