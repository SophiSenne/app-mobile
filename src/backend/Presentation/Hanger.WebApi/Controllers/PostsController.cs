using Hanger.Application.DTOs;
using Hanger.Application.Services;
using Microsoft.AspNetCore.Mvc;
using Npgsql;

namespace Hanger.WebApi.Controllers;

[ApiController]
[Route("posts")]
public sealed class PostsController(IHangerService service) : ApiControllerBase
{
    /// <summary>Read posts todos.</summary>
    [HttpGet]
    [ProducesResponseType<IReadOnlyList<PostDto>>(StatusCodes.Status200OK)]
    public async Task<IActionResult> GetAll(CancellationToken cancellationToken)
    {
        var posts = await service.GetAllPostsAsync(cancellationToken);
        return Ok(posts);
    }

    /// <summary>Pesquisar por topico.</summary>
    [HttpGet("search")]
    [ProducesResponseType<IReadOnlyList<PostDto>>(StatusCodes.Status200OK)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status400BadRequest)]
    public async Task<IActionResult> Search([FromQuery] string topic, CancellationToken cancellationToken)
    {
        try
        {
            var posts = await service.SearchPostsByTopicAsync(topic, cancellationToken);
            return Ok(posts);
        }
        catch (ArgumentException exception)
        {
            return BadRequestProblem(exception.Message);
        }
    }

    /// <summary>Criar post.</summary>
    [HttpPost]
    [ProducesResponseType<PostDto>(StatusCodes.Status201Created)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status400BadRequest)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status401Unauthorized)]
    public async Task<IActionResult> Create(
        [FromHeader(Name = "X-User-Id")] Guid userId,
        CreatePostRequest request,
        CancellationToken cancellationToken)
    {
        if (IsMissingUserId(userId))
        {
            return UnauthorizedProblem("Informe o header X-User-Id com o id do usuario logado.");
        }

        try
        {
            var post = await service.CreatePostAsync(userId, request, cancellationToken);
            return CreatedAtAction(nameof(GetByUserAlias), new { userId = post.UserId }, post);
        }
        catch (PostgresException exception) when (exception.SqlState == PostgresErrorCodes.ForeignKeyViolation)
        {
            return BadRequestProblem("Usuario, categoria ou tipo informado nao existe.");
        }
        catch (ArgumentException exception)
        {
            return BadRequestProblem(exception.Message);
        }
    }

    /// <summary>Editar post.</summary>
    [HttpPut("{postId:guid}")]
    [ProducesResponseType<PostDto>(StatusCodes.Status200OK)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status401Unauthorized)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> Update(
        Guid postId,
        [FromHeader(Name = "X-User-Id")] Guid userId,
        UpdatePostRequest request,
        CancellationToken cancellationToken)
    {
        if (IsMissingUserId(userId))
        {
            return UnauthorizedProblem("Informe o header X-User-Id com o id do usuario logado.");
        }

        try
        {
            var post = await service.UpdatePostAsync(postId, userId, request, cancellationToken);
            return post is null ? NotFoundProblem("Post nao encontrado para este usuario.") : Ok(post);
        }
        catch (PostgresException exception) when (exception.SqlState == PostgresErrorCodes.ForeignKeyViolation)
        {
            return BadRequestProblem("Categoria ou tipo informado nao existe.");
        }
    }

    /// <summary>Deletar post.</summary>
    [HttpDelete("{postId:guid}")]
    [ProducesResponseType(StatusCodes.Status204NoContent)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status401Unauthorized)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> Delete(
        Guid postId,
        [FromHeader(Name = "X-User-Id")] Guid userId,
        CancellationToken cancellationToken)
    {
        if (IsMissingUserId(userId))
        {
            return UnauthorizedProblem("Informe o header X-User-Id com o id do usuario logado.");
        }

        var deleted = await service.DeletePostAsync(postId, userId, cancellationToken);
        return deleted ? NoContent() : NotFoundProblem("Post nao encontrado para este usuario.");
    }

    /// <summary>Read posts por usuario.</summary>
    [HttpGet("user/{userId:guid}")]
    [ProducesResponseType<IReadOnlyList<PostDto>>(StatusCodes.Status200OK)]
    public async Task<IActionResult> GetByUserAlias(Guid userId, CancellationToken cancellationToken)
    {
        var posts = await service.GetPostsByUserAsync(userId, cancellationToken);
        return Ok(posts);
    }
}
