import logging
from datetime import UTC, datetime
from typing import Any

from fastapi import FastAPI, Request
from fastapi.exceptions import RequestValidationError
from fastapi.responses import JSONResponse
from sqlalchemy.exc import IntegrityError
from starlette.exceptions import HTTPException as StarletteHTTPException

from app.core.exceptions import AppError

logger = logging.getLogger("mecanica.errors")

_INTEGRITY_MESSAGE = (
    "Operação viola uma restrição de integridade dos dados "
    "(ex.: registro duplicado ou vínculo existente)"
)


def _body(
    status: int, message: str | None = None, errors: list[str] | None = None
) -> dict[str, Any]:
    body: dict[str, Any] = {"timestamp": datetime.now(UTC).isoformat(), "status": status}
    if message is not None:
        body["message"] = message
    if errors is not None:
        body["errors"] = errors
    return body


def _translate(error: dict[str, Any]) -> str:
    """Traduz os erros de validação mais comuns do Pydantic para português."""
    kind = error.get("type", "")
    ctx = error.get("ctx") or {}
    match kind:
        case "missing":
            return "campo obrigatório"
        case "string_too_short":
            return f"deve ter no mínimo {ctx.get('min_length')} caracteres"
        case "string_too_long":
            return f"deve ter no máximo {ctx.get('max_length')} caracteres"
        case "greater_than":
            return f"deve ser maior que {ctx.get('gt')}"
        case "greater_than_equal":
            return f"deve ser maior ou igual a {ctx.get('ge')}"
        case "less_than_equal":
            return f"deve ser menor ou igual a {ctx.get('le')}"
        case "enum":
            return f"valor inválido (esperado: {ctx.get('expected')})"
        case "value_error":
            reason = ctx.get("reason")
            return str(reason) if reason else "valor inválido"
        case "int_parsing" | "int_from_float" | "float_parsing" | "decimal_parsing":
            return "deve ser um número válido"
        case "bool_parsing":
            return "deve ser verdadeiro ou falso"
        case "uuid_parsing":
            return "identificador inválido"
        case "string_pattern_mismatch":
            return "formato inválido"
        case "decimal_max_places":
            return f"no máximo {ctx.get('decimal_places')} casas decimais"
        case _:
            return str(error.get("msg", "valor inválido"))


def register_exception_handlers(app: FastAPI) -> None:
    @app.exception_handler(AppError)
    async def handle_app_error(_: Request, exc: AppError) -> JSONResponse:
        return JSONResponse(
            status_code=exc.status_code, content=_body(exc.status_code, exc.message)
        )

    @app.exception_handler(RequestValidationError)
    async def handle_validation_error(_: Request, exc: RequestValidationError) -> JSONResponse:
        errors = []
        for err in exc.errors():
            loc = [str(p) for p in err["loc"] if p not in ("body", "query", "path")]
            field = ".".join(loc)
            text = _translate(err)
            errors.append(f"{field}: {text}" if field else text)
        return JSONResponse(status_code=422, content=_body(422, errors=errors))

    @app.exception_handler(StarletteHTTPException)
    async def handle_http_exception(_: Request, exc: StarletteHTTPException) -> JSONResponse:
        message = exc.detail if isinstance(exc.detail, str) else "Erro na requisição"
        return JSONResponse(
            status_code=exc.status_code,
            content=_body(exc.status_code, message),
            headers=getattr(exc, "headers", None),
        )

    @app.exception_handler(IntegrityError)
    async def handle_integrity_error(_: Request, exc: IntegrityError) -> JSONResponse:
        logger.warning("Violação de integridade: %s", exc.orig)
        return JSONResponse(status_code=409, content=_body(409, _INTEGRITY_MESSAGE))

    @app.exception_handler(Exception)
    async def handle_unexpected(_: Request, exc: Exception) -> JSONResponse:
        logger.exception("Erro não tratado ao processar requisição", exc_info=exc)
        return JSONResponse(status_code=500, content=_body(500, "Erro interno do servidor"))
