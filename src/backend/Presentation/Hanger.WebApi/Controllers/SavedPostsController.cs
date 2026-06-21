using Hanger.Application.Abstractions;
using Hanger.Application.DTOs;
using Microsoft.AspNetCore.Mvc;

namespace Hanger.WebApi.Controllers;

[ApiController]
[Route("users/{userId:guid}/saved-posts")]
public class SavedPostsController(ISavedPostsService service) : ApiControllerBase
{
    /// <summary>Listar posts salvos de um usuário.</summary>
    [HttpGet]
    [ProducesResponseType<IReadOnlyList<PostDto>>(StatusCodes.Status200OK)]
    public async Task<IActionResult> GetByUser(
        Guid userId,
        [FromQuery] int limit = 20,
        [FromQuery] int offset = 0,
        CancellationToken cancellationToken = default)
    {
        var posts = await service.GetByUserIdAsync(userId, limit, offset, cancellationToken);
        return Ok(posts);
    }

    /// <summary>Contagem de posts salvos de um usuário.</summary>
    [HttpGet("count")]
    [ProducesResponseType(StatusCodes.Status200OK)]
    public async Task<IActionResult> Count(Guid userId, CancellationToken cancellationToken)
    {
        var count = await service.CountByUserIdAsync(userId, cancellationToken);
        return Ok(new { userId, count });
    }

    /// <summary>Verificar se o usuário salvou um post específico.</summary>
    [HttpGet("{postId:guid}")]
    [ProducesResponseType(StatusCodes.Status200OK)]
    public async Task<IActionResult> HasSaved(
        Guid userId,
        Guid postId,
        CancellationToken cancellationToken)
    {
        var saved = await service.HasSavedAsync(userId, postId, cancellationToken);
        return Ok(new { userId, postId, saved });
    }

    /// <summary>Salvar um post.</summary>
    [HttpPost("{postId:guid}")]
    [ProducesResponseType<SavedPostDto>(StatusCodes.Status201Created)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status400BadRequest)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status401Unauthorized)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> Save(
        Guid userId,
        Guid postId,
        [FromHeader(Name = "X-User-Id")] Guid requestingUserId,
        CancellationToken cancellationToken)
    {
        if (IsMissingUserId(requestingUserId))
            return UnauthorizedProblem("Informe o header X-User-Id com o id do usuario logado.");

        if (requestingUserId != userId)
            return UnauthorizedProblem("Você só pode salvar posts na sua própria coleção.");

        try
        {
            var saved = await service.SaveAsync(userId, postId, cancellationToken);
            return StatusCode(StatusCodes.Status201Created, saved);
        }
        catch (KeyNotFoundException ex)
        {
            return NotFoundProblem(ex.Message);
        }
        catch (InvalidOperationException ex)
        {
            return BadRequestProblem(ex.Message);
        }
    }

    /// <summary>Remover post salvo.</summary>
    [HttpDelete("{postId:guid}")]
    [ProducesResponseType(StatusCodes.Status204NoContent)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status401Unauthorized)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> Unsave(
        Guid userId,
        Guid postId,
        [FromHeader(Name = "X-User-Id")] Guid requestingUserId,
        CancellationToken cancellationToken)
    {
        if (IsMissingUserId(requestingUserId))
            return UnauthorizedProblem("Informe o header X-User-Id com o id do usuario logado.");

        if (requestingUserId != userId)
            return UnauthorizedProblem("Você só pode remover posts da sua própria coleção.");

        var removed = await service.UnsaveAsync(userId, postId, cancellationToken);
        return removed ? NoContent() : NotFoundProblem("Post salvo não encontrado.");
    }
}
