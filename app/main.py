from typing import Literal

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field

app = FastAPI(
    title="Stock Trading Platform",
    version="0.1.0",
    description="Starter backend for a multi-asset stock trading platform.",
)

MARKET_DATA = {
    "AAPL": {"symbol": "AAPL", "name": "Apple Inc.", "price": 214.42, "change": 1.74},
    "MSFT": {"symbol": "MSFT", "name": "Microsoft", "price": 436.18, "change": -0.63},
    "NVDA": {"symbol": "NVDA", "name": "NVIDIA", "price": 126.95, "change": 2.12},
    "AMZN": {"symbol": "AMZN", "name": "Amazon", "price": 184.27, "change": 0.71},
    "GOOGL": {"symbol": "GOOGL", "name": "Alphabet", "price": 176.54, "change": -1.02},
}

WATCHLIST = ["AAPL", "MSFT", "NVDA"]

PORTFOLIO = {
    "cash": 100000.0,
    "positions": [
        {"symbol": "AAPL", "shares": 25, "avg_price": 201.42},
        {"symbol": "MSFT", "shares": 18, "avg_price": 402.15},
    ],
}


class OrderRequest(BaseModel):
    symbol: str = Field(..., min_length=1, max_length=10)
    side: Literal["buy", "sell"]
    quantity: int = Field(..., gt=0)
    order_type: Literal["market", "limit"] = "market"
    limit_price: float | None = None


@app.get("/health")
def health_check():
    return {"status": "ok", "service": "stock-trading-platform"}


@app.get("/stocks")
def get_stocks():
    return {"items": list(MARKET_DATA.values())}


@app.get("/portfolio")
def get_portfolio():
    total_value = PORTFOLIO["cash"]
    positions = []

    for position in PORTFOLIO["positions"]:
        symbol = position["symbol"]
        quote = MARKET_DATA.get(symbol)
        shares = position["shares"]
        avg_price = position["avg_price"]
        current_price = quote["price"] if quote else 0.0
        market_value = shares * current_price
        total_value += market_value

        positions.append(
            {
                "symbol": symbol,
                "shares": shares,
                "avg_price": avg_price,
                "current_price": current_price,
                "market_value": round(market_value, 2),
            }
        )

    return {
        "cash": round(PORTFOLIO["cash"], 2),
        "positions": positions,
        "total_value": round(total_value, 2),
    }


@app.get("/watchlist")
def get_watchlist():
    return {"symbols": WATCHLIST, "items": [MARKET_DATA[s] for s in WATCHLIST if s in MARKET_DATA]}


@app.get("/dashboard")
def get_dashboard():
    return {
        "market_snapshot": list(MARKET_DATA.values()),
        "watchlist": get_watchlist()["items"],
        "portfolio": get_portfolio(),
    }


@app.post("/orders")
def create_order(order: OrderRequest):
    symbol = order.symbol.upper()
    if symbol not in MARKET_DATA:
        raise HTTPException(status_code=404, detail=f"Symbol '{symbol}' not found")

    if order.order_type == "limit" and order.limit_price is None:
        raise HTTPException(status_code=400, detail="limit_price is required when order_type is 'limit'")

    quote = MARKET_DATA[symbol]
    execution_price = order.limit_price if order.order_type == "limit" else quote["price"]

    return {
        "status": "accepted",
        "symbol": symbol,
        "side": order.side,
        "quantity": order.quantity,
        "order_type": order.order_type,
        "limit_price": execution_price,
        "estimated_total": round(order.quantity * execution_price, 2),
    }
