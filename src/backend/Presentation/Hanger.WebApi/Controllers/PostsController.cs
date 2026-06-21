using Hanger.Application.Abstractions;
using Hanger.Application.DTOs;
using Microsoft.AspNetCore.Mvc;

namespace Hanger.WebApi.Controllers;

[ApiController]
[Route("posts")]
public class PostsController(IPostsService service, IStorageRepository storageRepository) : ApiControllerBase
{
    private static readonly string[] AllowedImageTypes = ["image/jpeg", "image/png", "image/webp", "image/gif"];
    private const long MaxFileSizeBytes = 10 * 1024 * 1024; // 10 MB

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

    /// <summary>
    /// Criar novo post com imagem.
    /// Enviar como multipart/form-data — o campo "image" é obrigatório.
    /// </summary>
    [HttpPost]
    [Consumes("multipart/form-data")]
    [ProducesResponseType<PostDto>(StatusCodes.Status201Created)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status400BadRequest)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status401Unauthorized)]
    public async Task<IActionResult> Create(
        [FromHeader(Name = "X-User-Id")] Guid userId,
        [FromForm] CreatePostFormRequest form,
        CancellationToken cancellationToken)
    {
        if (IsMissingUserId(userId))
            return UnauthorizedProblem("Informe o header X-User-Id com o id do usuario logado.");

        if (form.Image is null || form.Image.Length == 0)
            return BadRequestProblem("O campo 'image' é obrigatório.");

        if (!AllowedImageTypes.Contains(form.Image.ContentType))
            return BadRequestProblem($"Tipo de imagem não suportado. Use: {string.Join(", ", AllowedImageTypes)}.");

        if (form.Image.Length > MaxFileSizeBytes)
            return BadRequestProblem("A imagem não pode ultrapassar 10 MB.");

        string imageUrl;
        await using (var stream = form.Image.OpenReadStream())
        {
            imageUrl = await storageRepository.UploadImageAsync(
                stream,
                form.Image.FileName,
                form.Image.ContentType,
                "posts",
                cancellationToken);
        }

        var request = new CreatePostRequest(
            imageUrl,
            form.Title!,
            form.Caption,
            form.WeatherCondition,
            form.Temperature,
            form.City,
            form.CategoryId,
            form.TypeId);

        var post = await service.CreateAsync(userId, request, cancellationToken);
        return CreatedAtAction(nameof(GetById), new { postId = post.Id }, post);
    }

    /// <summary>Atualizar post (campos opcionais — somente o dono pode editar).</summary>
    [HttpPut("{postId:guid}")]
    [Consumes("multipart/form-data")]
    [ProducesResponseType<PostDto>(StatusCodes.Status200OK)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status401Unauthorized)]
    [ProducesResponseType<ProblemDetails>(StatusCodes.Status404NotFound)]
    public async Task<IActionResult> Update(
        Guid postId,
        [FromHeader(Name = "X-User-Id")] Guid userId,
        [FromForm] UpdatePostFormRequest form,
        CancellationToken cancellationToken)
    {
        if (IsMissingUserId(userId))
            return UnauthorizedProblem("Informe o header X-User-Id com o id do usuario logado.");

        string? imageUrl = null;

        if (form.Image is not null && form.Image.Length > 0)
        {
            if (!AllowedImageTypes.Contains(form.Image.ContentType))
                return BadRequestProblem($"Tipo de imagem não suportado. Use: {string.Join(", ", AllowedImageTypes)}.");

            if (form.Image.Length > MaxFileSizeBytes)
                return BadRequestProblem("A imagem não pode ultrapassar 10 MB.");

            await using var stream = form.Image.OpenReadStream();
            imageUrl = await storageRepository.UploadImageAsync(
                stream,
                form.Image.FileName,
                form.Image.ContentType,
                "posts",
                cancellationToken);
        }

        var request = new UpdatePostRequest(
            imageUrl,
            form.Title,
            form.Caption,
            form.WeatherCondition,
            form.Temperature,
            form.City,
            form.CategoryId,
            form.TypeId);

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