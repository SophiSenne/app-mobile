using Hanger.Application.Abstractions;
using Hanger.Application.DTOs;
using Microsoft.AspNetCore.Mvc;

namespace Hanger.WebApi.Controllers;

[ApiController]
[Route("users")]
public class FollowsController(IFollowsService service) : ApiControllerBase
{
    /// <summary>Listar quem o usuário segue.</summary>
    [HttpGet("{userId:guid}/following")]
    [ProducesResponseType<IReadOnlyList<FollowDto>>(StatusCodes.Status200OK)]
    public async Task<IActionResult> GetFollowing(
        Guid userId,
        [FromQuery] int limit = 20,
        [FromQuery] int offset = 0,
        CancellationToken cancellationToken = default)
    {
        var result = await service.GetFollowingAsync(userId, limit, offset, cancellationToken);
        return Ok(result);
    }

    /// <summary>Listar seguidores de um usuário.</summary>
    [HttpGet("{userId:guid}/followers")]
    [ProducesResponseType<IReadOnlyList<FollowDto>>(StatusCodes.Status200OK)]
    public async Task<IActionResult> GetFollowers(
        Guid userId,
        [FromQuery] int limit = 20,
        [FromQuery] int offset = 0,
        CancellationToken cancellationToken = default)
    {
        var result = await service.GetFollowersAsync(userId, limit, offset, cancellationToken);
        return Ok(result);
    }

    /// <summary>Contagem de seguidores e seguindo de um usuário.</summary>
    [HttpGet("{userId:guid}/follow-counts")]
    [ProducesResponseType(StatusCodes.Status200OK)]
    public async Task<IActionResult> GetFollowCounts(Guid userId, CancellationToken cancellationToken)
    {
        var (followers, following) = await service.GetCountsAsync(userId, cancellationToken);
        return Ok(new { followers, following });
    }

    /// <summary>Verificar se o usuário autenticado segue outro usuário.</summary>
    [HttpGet("{userId:guid}/following/{targetId:guid}")]
    [ProducesResponseType(StatusCodes.Status200OK)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status401Unauthorized)]
    public async Task<IActionResult> IsFollowing(
        [FromHeader(Name = "X-User-Id")] Guid currentUserId,
        Guid userId,
        Guid targetId,
        CancellationToken cancellationToken)
    {
        if (IsMissingUserId(currentUserId))
            return UnauthorizedProblem("Informe o header X-User-Id com o id do usuario logado.");

        var isFollowing = await service.IsFollowingAsync(userId, targetId, cancellationToken);
        return Ok(new { isFollowing });
    }

    /// <summary>Seguir um usuário.</summary>
    [HttpPost("{followingId:guid}/follow")]
    [ProducesResponseType<FollowDto>(StatusCodes.Status201Created)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status400BadRequest)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status401Unauthorized)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> Follow(
        [FromHeader(Name = "X-User-Id")] Guid currentUserId,
        Guid followingId,
        CancellationToken cancellationToken)
    {
        if (IsMissingUserId(currentUserId))
            return UnauthorizedProblem("Informe o header X-User-Id com o id do usuario logado.");

        try
        {
            var follow = await service.FollowAsync(currentUserId, followingId, cancellationToken);
            return StatusCode(StatusCodes.Status201Created, follow);
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

    /// <summary>Deixar de seguir um usuário.</summary>
    [HttpDelete("{followingId:guid}/follow")]
    [ProducesResponseType(StatusCodes.Status204NoContent)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status401Unauthorized)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> Unfollow(
        [FromHeader(Name = "X-User-Id")] Guid currentUserId,
        Guid followingId,
        CancellationToken cancellationToken)
    {
        if (IsMissingUserId(currentUserId))
            return UnauthorizedProblem("Informe o header X-User-Id com o id do usuario logado.");

        var removed = await service.UnfollowAsync(currentUserId, followingId, cancellationToken);
        return removed ? NoContent() : NotFoundProblem("Você não segue este usuário.");
    }
}
