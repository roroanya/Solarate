import time
from typing import Any


class TTLCache:
    """Cache in-memory sederhana dengan masa berlaku (hilang saat server restart)."""

    def __init__(self, maxsize: int = 1024):
        self._data: dict[Any, tuple[float, Any]] = {}
        self.maxsize = maxsize

    def get(self, key: Any) -> Any | None:
        item = self._data.get(key)
        if item is None:
            return None
        expires, value = item
        if expires < time.monotonic():
            self._data.pop(key, None)
            return None
        return value

    def set(self, key: Any, value: Any, ttl: float) -> None:
        if len(self._data) >= self.maxsize:
            now = time.monotonic()
            for k in [k for k, (e, _) in self._data.items() if e < now]:
                self._data.pop(k, None)
            while len(self._data) >= self.maxsize:
                self._data.pop(next(iter(self._data)))
        self._data[key] = (time.monotonic() + ttl, value)
