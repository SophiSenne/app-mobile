using Hanger.Application.DTOs;
using Hanger.Application.Services;
using Microsoft.AspNetCore.Mvc;

namespace Hanger.WebApi.Controllers;

[ApiController]
[Route("users")]
public sealed class UsersController(IHangerService service) : ApiControllerBase
{
    /// <summary>Read meu usuario.</summary>
    [HttpGet("me")]
    [ProducesResponseType<UserDto>(StatusCodes.Status200OK)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status401Unauthorized)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> GetMe(
        [FromHeader(Name = "X-User-Id")] Guid userId,
        CancellationToken cancellationToken)
    {
        if (IsMissingUserId(userId))
        {
            return UnauthorizedProblem("Informe o header X-User-Id com o id do usuario logado.");
        }

        var user = await service.GetMeAsync(userId, cancellationToken);
        return user is null ? NotFoundProblem("Usuario nao encontrado.") : Ok(user);
    }

    /// <summary>Deletar conta.</summary>
    [HttpDelete("me")]
    [ProducesResponseType(StatusCodes.Status204NoContent)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status401Unauthorized)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> DeleteMe(
        [FromHeader(Name = "X-User-Id")] Guid userId,
        CancellationToken cancellationToken)
    {
        if (IsMissingUserId(userId))
        {
            return UnauthorizedProblem("Informe o header X-User-Id com o id do usuario logado.");
        }

        var deleted = await service.DeleteAccountAsync(userId, cancellationToken);
        return deleted ? NoContent() : NotFoundProblem("Usuario nao encontrado.");
    }

    /// <summary>Read posts por usuario.</summary>
    [HttpGet("{userId:guid}/posts")]
    [ProducesResponseType<IReadOnlyList<PostDto>>(StatusCodes.Status200OK)]
    public async Task<IActionResult> GetPostsByUser(Guid userId, CancellationToken cancellationToken)
    {
        var posts = await service.GetPostsByUserAsync(userId, cancellationToken);
        return Ok(posts);
    }
}
