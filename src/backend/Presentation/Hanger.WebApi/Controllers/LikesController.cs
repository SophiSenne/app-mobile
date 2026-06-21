using Hanger.Application.Abstractions;
using Hanger.Application.DTOs;
using Microsoft.AspNetCore.Mvc;

namespace Hanger.WebApi.Controllers;

[ApiController]
[Route("posts/{postId:guid}/likes")]
public class LikesController(ILikesService service) : ApiControllerBase
{
    /// <summary>Listar usuários que curtiram um post.</summary>
    [HttpGet]
    [ProducesResponseType<IReadOnlyList<LikeDto>>(StatusCodes.Status200OK)]
    public async Task<IActionResult> GetByPost(
        Guid postId,
        [FromQuery] int limit = 20,
        [FromQuery] int offset = 0,
        CancellationToken cancellationToken = default)
    {
        var likes = await service.GetByPostIdAsync(postId, limit, offset, cancellationToken);
        return Ok(likes);
    }

    /// <summary>Contagem de likes de um post.</summary>
    [HttpGet("count")]
    [ProducesResponseType(StatusCodes.Status200OK)]
    public async Task<IActionResult> Count(Guid postId, CancellationToken cancellationToken)
    {
        var count = await service.CountByPostIdAsync(postId, cancellationToken);
        return Ok(new { postId, count });
    }

    /// <summary>Verifica se um usuário específico curtiu o post.</summary>
    [HttpGet("check")]
    [ProducesResponseType(StatusCodes.Status200OK)]
    public async Task<IActionResult> Check(
        Guid postId,
        [FromQuery] Guid userId,
        CancellationToken cancellationToken)
    {
        var liked = await service.HasLikedAsync(userId, postId, cancellationToken);
        return Ok(new { postId, userId, liked });
    }

    /// <summary>Curtir um post.</summary>
    [HttpPost]
    [ProducesResponseType<LikeDto>(StatusCodes.Status201Created)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status400BadRequest)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status401Unauthorized)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> Like(
        Guid postId,
        [FromHeader(Name = "X-User-Id")] Guid userId,
        CancellationToken cancellationToken)
    {
        if (IsMissingUserId(userId))
            return UnauthorizedProblem("Informe o header X-User-Id com o id do usuario logado.");

        try
        {
            var like = await service.LikeAsync(userId, postId, cancellationToken);
            return StatusCode(StatusCodes.Status201Created, like);
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

    /// <summary>Remover like de um post.</summary>
    [HttpDelete]
    [ProducesResponseType(StatusCodes.Status204NoContent)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status401Unauthorized)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> Unlike(
        Guid postId,
        [FromHeader(Name = "X-User-Id")] Guid userId,
        CancellationToken cancellationToken)
    {
        if (IsMissingUserId(userId))
            return UnauthorizedProblem("Informe o header X-User-Id com o id do usuario logado.");

        var removed = await service.UnlikeAsync(userId, postId, cancellationToken);
        return removed ? NoContent() : NotFoundProblem("Like não encontrado.");
    }
}
