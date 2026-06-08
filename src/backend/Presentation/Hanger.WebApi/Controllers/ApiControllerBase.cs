using Microsoft.AspNetCore.Mvc;

namespace Hanger.WebApi.Controllers;

public abstract class ApiControllerBase : ControllerBase
{
    protected static bool IsMissingUserId(Guid userId) => userId == Guid.Empty;

    protected IActionResult BadRequestProblem(string detail) =>
        Problem(
            title: "Requisicao invalida.",
            detail: detail,
            statusCode: StatusCodes.Status400BadRequest);

    protected IActionResult NotFoundProblem(string detail) =>
        Problem(
            title: "Recurso nao encontrado.",
            detail: detail,
            statusCode: StatusCodes.Status404NotFound);

    protected IActionResult UnauthorizedProblem(string detail) =>
        Problem(
            title: "Nao autenticado.",
            detail: detail,
            statusCode: StatusCodes.Status401Unauthorized);
}
