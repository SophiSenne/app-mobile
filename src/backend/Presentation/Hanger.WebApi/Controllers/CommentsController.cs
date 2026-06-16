using Hanger.Application.Abstractions;
using Hanger.Application.DTOs;
using Microsoft.AspNetCore.Mvc;

namespace Hanger.WebApi.Controllers;

[ApiController]
[Route("posts/{postId:guid}/comments")]
public class CommentsController(ICommentsService service) : ApiControllerBase
{
    /// <summary>Listar comentários de um post.</summary>
    [HttpGet]
    [ProducesResponseType<IReadOnlyList<CommentDto>>(StatusCodes.Status200OK)]
    public async Task<IActionResult> GetByPost(
        Guid postId,
        [FromQuery] int limit = 20,
        [FromQuery] int offset = 0,
        CancellationToken cancellationToken = default)
    {
        var comments = await service.GetByPostIdAsync(postId, limit, offset, cancellationToken);
        return Ok(comments);
    }

    /// <summary>Buscar comentário por id.</summary>
    [HttpGet("{commentId:guid}")]
    [ProducesResponseType<CommentDto>(StatusCodes.Status200OK)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> GetById(Guid postId, Guid commentId, CancellationToken cancellationToken)
    {
        var comment = await service.GetByIdAsync(commentId, cancellationToken);
        return comment is null ? NotFoundProblem("Comentário não encontrado.") : Ok(comment);
    }

    /// <summary>Contagem de comentários de um post.</summary>
    [HttpGet("count")]
    [ProducesResponseType(StatusCodes.Status200OK)]
    public async Task<IActionResult> Count(Guid postId, CancellationToken cancellationToken)
    {
        var count = await service.CountByPostIdAsync(postId, cancellationToken);
        return Ok(new { postId, count });
    }

    /// <summary>Comentar em um post.</summary>
    [HttpPost]
    [ProducesResponseType<CommentDto>(StatusCodes.Status201Created)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status400BadRequest)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status401Unauthorized)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> Create(
        Guid postId,
        [FromHeader(Name = "X-User-Id")] Guid userId,
        [FromBody] CreateCommentRequest request,
        CancellationToken cancellationToken)
    {
        if (IsMissingUserId(userId))
            return UnauthorizedProblem("Informe o header X-User-Id com o id do usuario logado.");

        try
        {
            var comment = await service.CreateAsync(postId, userId, request, cancellationToken);
            return CreatedAtAction(nameof(GetById), new { postId, commentId = comment.Id }, comment);
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

    /// <summary>Editar comentário (somente o autor).</summary>
    [HttpPut("{commentId:guid}")]
    [ProducesResponseType<CommentDto>(StatusCodes.Status200OK)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status400BadRequest)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status401Unauthorized)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> Update(
        Guid postId,
        Guid commentId,
        [FromHeader(Name = "X-User-Id")] Guid userId,
        [FromBody] UpdateCommentRequest request,
        CancellationToken cancellationToken)
    {
        if (IsMissingUserId(userId))
            return UnauthorizedProblem("Informe o header X-User-Id com o id do usuario logado.");

        try
        {
            var updated = await service.UpdateAsync(commentId, userId, request, cancellationToken);
            return updated is null ? NotFoundProblem("Comentário não encontrado ou sem permissão para editar.") : Ok(updated);
        }
        catch (InvalidOperationException ex)
        {
            return BadRequestProblem(ex.Message);
        }
    }

    /// <summary>Deletar comentário (somente o autor).</summary>
    [HttpDelete("{commentId:guid}")]
    [ProducesResponseType(StatusCodes.Status204NoContent)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status401Unauthorized)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> Delete(
        Guid postId,
        Guid commentId,
        [FromHeader(Name = "X-User-Id")] Guid userId,
        CancellationToken cancellationToken)
    {
        if (IsMissingUserId(userId))
            return UnauthorizedProblem("Informe o header X-User-Id com o id do usuario logado.");

        var deleted = await service.DeleteAsync(commentId, userId, cancellationToken);
        return deleted ? NoContent() : NotFoundProblem("Comentário não encontrado ou sem permissão para deletar.");
    }
}
