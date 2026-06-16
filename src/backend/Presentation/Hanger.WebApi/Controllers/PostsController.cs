using Hanger.Application.Abstractions;
using Hanger.Application.DTOs;
using Microsoft.AspNetCore.Mvc;

namespace Hanger.WebApi.Controllers;

[ApiController]
[Route("posts")]
public class PostsController(IPostsService service) : ApiControllerBase
{
    /// <summary>Listar todos os posts (feed geral).</summary>
    [HttpGet]
    [ProducesResponseType<IReadOnlyList<PostDto>>(StatusCodes.Status200OK)]
    public async Task<IActionResult> GetAll(
        [FromQuery] int limit = 20,
        [FromQuery] int offset = 0,
        CancellationToken cancellationToken = default)
    {
        var posts = await service.GetAllAsync(limit, offset, cancellationToken);
        return Ok(posts);
    }

    /// <summary>Buscar post por id.</summary>
    [HttpGet("{postId:guid}")]
    [ProducesResponseType<PostDto>(StatusCodes.Status200OK)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> GetById(Guid postId, CancellationToken cancellationToken)
    {
        var post = await service.GetByIdAsync(postId, cancellationToken);
        return post is null ? NotFoundProblem("Post não encontrado.") : Ok(post);
    }

    /// <summary>Criar novo post.</summary>
    [HttpPost]
    [ProducesResponseType<PostDto>(StatusCodes.Status201Created)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status400BadRequest)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status401Unauthorized)]
    public async Task<IActionResult> Create(
        [FromHeader(Name = "X-User-Id")] Guid userId,
        [FromBody] CreatePostRequest request,
        CancellationToken cancellationToken)
    {
        if (IsMissingUserId(userId))
            return UnauthorizedProblem("Informe o header X-User-Id com o id do usuario logado.");

        var post = await service.CreateAsync(userId, request, cancellationToken);
        return CreatedAtAction(nameof(GetById), new { postId = post.Id }, post);
    }

    /// <summary>Atualizar post (campos opcionais — somente o dono pode editar).</summary>
    [HttpPut("{postId:guid}")]
    [ProducesResponseType<PostDto>(StatusCodes.Status200OK)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status401Unauthorized)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> Update(
        Guid postId,
        [FromHeader(Name = "X-User-Id")] Guid userId,
        [FromBody] UpdatePostRequest request,
        CancellationToken cancellationToken)
    {
        if (IsMissingUserId(userId))
            return UnauthorizedProblem("Informe o header X-User-Id com o id do usuario logado.");

        var updated = await service.UpdateAsync(postId, userId, request, cancellationToken);
        return updated is null ? NotFoundProblem("Post não encontrado ou sem permissão para editar.") : Ok(updated);
    }

    /// <summary>Deletar post (somente o dono pode deletar).</summary>
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
            return UnauthorizedProblem("Informe o header X-User-Id com o id do usuario logado.");

        var deleted = await service.DeleteAsync(postId, userId, cancellationToken);
        return deleted ? NoContent() : NotFoundProblem("Post não encontrado ou sem permissão para deletar.");
    }
}